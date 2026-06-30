import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { deletePlant, getPlants, updatePlant } from '../api/plants'
import WorkerSidebar from '../components/worker/WorkerSidebar'
import PageTitle from '../components/home/PageTitle'
import SearchBar from '../components/home/SearchBar'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/worker.css'
import '../styles/varieties.css'

const PROPAGATION_OPTIONS = ['Seed', 'Slip', 'Sapling', 'Cuttings', 'Grafting', 'Division']

const btnSm = { minWidth: 'unset', padding: '0 12px', height: 30, fontSize: 13 }
const inputSm = { height: 30, fontSize: 13, padding: '0 8px' }

function emptyEditForm(p) {
  return {
    name: p.name ?? '',
    propagationMethod: p.propagationMethod ?? '',
    hatchingDate: p.hatchingDate ?? '',
    color: p.color ?? '',
    height: p.height ?? '',
    state: p.state ?? '',
    conditionDescription: p.conditionDescription ?? '',
  }
}

function WorkerPlantsPage() {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [plants, setPlants] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [searchTerm, setSearchTerm] = useState('')

  const [expandedId, setExpandedId] = useState(null)
  const [editingId, setEditingId] = useState(null)
  const [editForm, setEditForm] = useState({})
  const [editSubmitting, setEditSubmitting] = useState(false)
  const [editError, setEditError] = useState('')
  const [deleteError, setDeleteError] = useState('')

  useEffect(() => {
    let ignore = false
    getPlants()
      .then((res) => { if (!ignore) setPlants(res.data) })
      .catch(() => { if (!ignore) setError('Failed to load plants.') })
      .finally(() => { if (!ignore) setLoading(false) })
    return () => { ignore = true }
  }, [])

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  function toggleExpand(id) {
    if (editingId === id) return
    setExpandedId((prev) => (prev === id ? null : id))
  }

  function startEdit(p, e) {
    e.stopPropagation()
    setEditingId(p.id)
    setEditForm(emptyEditForm(p))
    setExpandedId(p.id)
    setEditError('')
    setDeleteError('')
  }

  function cancelEdit(e) {
    e?.stopPropagation()
    setEditingId(null)
    setEditForm({})
    setEditError('')
  }

  function handleEditField(e) {
    const { name, value } = e.target
    setEditForm((prev) => ({ ...prev, [name]: value }))
    setEditError('')
  }

  async function saveEdit(id, e) {
    e.stopPropagation()
    if (!editForm.name.trim()) { setEditError('Name is required.'); return }
    setEditSubmitting(true)
    setEditError('')
    try {
      const res = await updatePlant(id, {
        name: editForm.name.trim(),
        propagationMethod: editForm.propagationMethod || null,
        hatchingDate: editForm.hatchingDate || null,
        color: editForm.color.trim() || null,
        height: editForm.height !== '' ? Number(editForm.height) : null,
        state: editForm.state !== '' ? Number(editForm.state) : null,
        conditionDescription: editForm.conditionDescription.trim() || null,
      })
      setPlants((prev) => prev.map((p) => (p.id === id ? res.data : p)))
      setEditingId(null)
      setExpandedId(id)
    } catch (err) {
      setEditError(err.response?.data?.message ?? 'Failed to save changes.')
    } finally {
      setEditSubmitting(false)
    }
  }

  async function handleDelete(id, e) {
    e.stopPropagation()
    setDeleteError('')
    try {
      await deletePlant(id)
      setPlants((prev) => prev.filter((p) => p.id !== id))
      if (expandedId === id) setExpandedId(null)
      if (editingId === id) setEditingId(null)
    } catch (err) {
      setDeleteError(err.response?.data?.message ?? 'Failed to delete plant.')
    }
  }

  const filtered = searchTerm
    ? plants.filter((p) =>
        p.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.varietyName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.latinName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.speciesName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.categoryName?.toLowerCase().includes(searchTerm.toLowerCase())
      )
    : plants

  return (
    <main className="home-page">
      <header className="home-header botanist-header">
        <button
          aria-label="Back to dashboard"
          className="home-logo-placeholder"
          onClick={() => navigate('/worker')}
          type="button"
        >
          <span aria-hidden="true" />
        </button>
        <div className="home-header-controls">
          <SearchBar
            onChange={(e) => setSearchTerm(e.target.value)}
            onClear={() => setSearchTerm('')}
            value={searchTerm}
          />
        </div>
      </header>

      <PageTitle label="Plants" onBack={handleLogout} />

      <section className="home-body">
        <WorkerSidebar onSiteChange={() => {}} selectedSiteId={null} sites={[]} />

        <div className="varieties-content">
          <section className="varieties-list-card" aria-labelledby="plants-heading">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <h2 className="variety-form-heading" id="plants-heading" style={{ margin: 0 }}>
                All plants <span className="varieties-count">({filtered.length})</span>
              </h2>
              <button
                className="variety-submit-button"
                onClick={() => navigate('/worker/plants/add')}
                type="button"
                style={{ minWidth: 'unset', padding: '0 18px', height: 36, fontSize: 14 }}
              >
                + Add plant lot
              </button>
            </div>

            {error && <p className="variety-form-error">{error}</p>}
            {deleteError && <p className="variety-form-error" role="alert">{deleteError}</p>}

            {loading ? (
              <p className="varieties-empty">Loading plants…</p>
            ) : filtered.length === 0 ? (
              <p className="varieties-empty">
                {searchTerm ? 'No plants match your search.' : 'No plants registered yet.'}
              </p>
            ) : (
              <table className="varieties-table">
                <thead>
                  <tr>
                    <th style={{ width: 28 }}></th>
                    <th>Name</th>
                    <th>Variety</th>
                    <th>Species</th>
                    <th>Category</th>
                    <th>Condition</th>
                    <th>Qty</th>
                    <th>Location</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((p) => {
                    const isOpen = expandedId === p.id
                    const isEditing = editingId === p.id
                    return (
                      <>
                        <tr
                          key={p.id}
                          onClick={() => toggleExpand(p.id)}
                          style={{ cursor: 'pointer', background: isOpen ? 'var(--color-bg-soft, #f9fafb)' : undefined }}
                        >
                          <td style={{ color: 'var(--color-text-secondary)', fontSize: 11 }}>
                            {isOpen ? '▼' : '▶'}
                          </td>
                          <td className="varieties-name">{p.name}</td>
                          <td>
                            {p.varietyName}
                            {p.latinName && (
                              <span style={{ display: 'block', fontSize: 12, fontStyle: 'italic', color: 'var(--color-text-secondary)' }}>
                                {p.latinName}
                              </span>
                            )}
                          </td>
                          <td>{p.speciesName}</td>
                          <td><span className="variety-category-badge">{p.categoryName}</span></td>
                          <td>{p.state != null ? `${p.state}/5` : '—'}</td>
                          <td>{p.currentQuantity ?? '—'}</td>
                          <td style={{ fontSize: 13 }}>
                            {p.siteName ? (
                              <>
                                <span>{p.siteName}</span>
                                {p.storageSpaceName && (
                                  <span style={{ color: 'var(--color-text-secondary)' }}> / {p.storageSpaceName}</span>
                                )}
                              </>
                            ) : '—'}
                          </td>
                          <td onClick={(e) => e.stopPropagation()}>
                            <div style={{ display: 'flex', gap: 6 }}>
                              <button
                                className="variety-submit-button"
                                onClick={(e) => startEdit(p, e)}
                                type="button"
                                style={btnSm}
                              >
                                Edit
                              </button>
                              <button
                                className="variety-submit-button btn-danger"
                                onClick={(e) => handleDelete(p.id, e)}
                                type="button"
                                style={btnSm}
                              >
                                Delete
                              </button>
                            </div>
                          </td>
                        </tr>

                        {isOpen && !isEditing && (
                          <tr key={`${p.id}-detail`} style={{ background: 'var(--color-bg-soft, #f9fafb)' }}>
                            <td />
                            <td colSpan={8} style={{ paddingBottom: 16, paddingTop: 4 }}>
                              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '8px 24px', fontSize: 13 }}>
                                <DetailField label="Subcategory" value={p.typeName} />
                                <DetailField label="Sector" value={p.sectorName} />
                                <DetailField label="Storage type" value={p.storageSpaceTypeName} />
                                <DetailField label="Condition" value={p.state != null ? `${p.state}/5` : null} />
                                <DetailField label="Propagation" value={p.propagationMethod} />
                                <DetailField label="Hatching date" value={p.hatchingDate} />
                                <DetailField label="Color" value={p.color} />
                                <DetailField label="Height (cm)" value={p.height} />
                                <DetailField label="Humidity" value={p.humidity != null ? `${p.humidity}%` : null} />
                                <DetailField label="Soil type" value={p.soil} />
                              </div>
                              {(p.conditionDescription || p.careInstructions || p.description) && (
                                <div style={{ marginTop: 10, fontSize: 13 }}>
                                  {p.conditionDescription && (
                                    <div>
                                      <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Condition notes: </span>
                                      {p.conditionDescription}
                                    </div>
                                  )}
                                  {p.careInstructions && (
                                    <div style={{ marginTop: 4 }}>
                                      <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Care instructions: </span>
                                      {p.careInstructions}
                                    </div>
                                  )}
                                  {p.description && (
                                    <div style={{ marginTop: 4 }}>
                                      <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Description: </span>
                                      {p.description}
                                    </div>
                                  )}
                                </div>
                              )}
                            </td>
                          </tr>
                        )}

                        {isEditing && (
                          <tr key={`${p.id}-edit`} style={{ background: 'var(--color-bg-soft, #f9fafb)' }}>
                            <td />
                            <td colSpan={8} style={{ paddingBottom: 16, paddingTop: 8 }}>
                              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px 24px', fontSize: 13 }}>
                                <label style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                  <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Name *</span>
                                  <input
                                    className="variety-input"
                                    name="name"
                                    onChange={handleEditField}
                                    style={inputSm}
                                    type="text"
                                    value={editForm.name}
                                  />
                                </label>
                                <label style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                  <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Condition (1–5)</span>
                                  <select
                                    className="variety-select"
                                    name="state"
                                    onChange={handleEditField}
                                    style={inputSm}
                                    value={editForm.state}
                                  >
                                    <option value="">Not rated</option>
                                    <option value="1">1 — Poor</option>
                                    <option value="2">2 — Fair</option>
                                    <option value="3">3 — Good</option>
                                    <option value="4">4 — Very good</option>
                                    <option value="5">5 — Excellent</option>
                                  </select>
                                </label>
                                <label style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                  <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Propagation</span>
                                  <select
                                    className="variety-select"
                                    name="propagationMethod"
                                    onChange={handleEditField}
                                    style={inputSm}
                                    value={editForm.propagationMethod}
                                  >
                                    <option value="">Not specified</option>
                                    {PROPAGATION_OPTIONS.map((m) => <option key={m} value={m}>{m}</option>)}
                                  </select>
                                </label>
                                <label style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                  <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Hatching date</span>
                                  <input
                                    className="variety-input"
                                    name="hatchingDate"
                                    onChange={handleEditField}
                                    style={inputSm}
                                    type="date"
                                    value={editForm.hatchingDate}
                                  />
                                </label>
                                <label style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                  <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Color</span>
                                  <input
                                    className="variety-input"
                                    name="color"
                                    onChange={handleEditField}
                                    placeholder="e.g. Green"
                                    style={inputSm}
                                    type="text"
                                    value={editForm.color}
                                  />
                                </label>
                                <label style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                  <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Height (cm)</span>
                                  <input
                                    className="variety-input"
                                    min="0"
                                    name="height"
                                    onChange={handleEditField}
                                    placeholder="e.g. 30"
                                    step="0.1"
                                    style={inputSm}
                                    type="number"
                                    value={editForm.height}
                                  />
                                </label>
                              </div>
                              <label style={{ display: 'flex', flexDirection: 'column', gap: 4, marginTop: 10, fontSize: 13 }}>
                                <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Condition notes</span>
                                <textarea
                                  className="variety-textarea"
                                  name="conditionDescription"
                                  onChange={handleEditField}
                                  placeholder="Describe the plant's current condition…"
                                  rows={2}
                                  value={editForm.conditionDescription}
                                />
                              </label>
                              {editError && <p className="variety-form-error" role="alert" style={{ marginTop: 8 }}>{editError}</p>}
                              <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
                                <button
                                  className="variety-submit-button"
                                  disabled={editSubmitting}
                                  onClick={(e) => saveEdit(p.id, e)}
                                  type="button"
                                  style={btnSm}
                                >
                                  {editSubmitting ? 'Saving…' : 'Save'}
                                </button>
                                <button
                                  className="variety-submit-button"
                                  onClick={cancelEdit}
                                  type="button"
                                  style={{ ...btnSm, background: 'var(--color-text-secondary)' }}
                                >
                                  Cancel
                                </button>
                              </div>
                            </td>
                          </tr>
                        )}
                      </>
                    )
                  })}
                </tbody>
              </table>
            )}
          </section>
        </div>
      </section>
    </main>
  )
}

function DetailField({ label, value }) {
  if (value == null || value === '') return null
  return (
    <div>
      <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>{label}: </span>
      <span>{value}</span>
    </div>
  )
}

export default WorkerPlantsPage
