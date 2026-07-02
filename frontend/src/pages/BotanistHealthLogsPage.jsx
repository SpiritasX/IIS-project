import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  deletePlant,
  getDeletionLog,
  getPlantConditionLogs,
  getPlants,
  updatePlantCondition,
} from '../api/botanistPlants'
import BotanistSidebar from '../components/botanist/BotanistSidebar'
import PageTitle from '../components/home/PageTitle'
import SearchBar from '../components/home/SearchBar'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/worker.css'
import '../styles/varieties.css'

const DELETION_REASONS = [
  { value: 'PLAMENJACA', label: 'Plamenjača' },
  { value: 'PARAZITI', label: 'Paraziti' },
  { value: 'DEHIDRATACIJA', label: 'Dehidratacija' },
  { value: 'BOLEST', label: 'Bolest' },
  { value: 'DRUGO', label: 'Drugo' },
]

const btnSm = { minWidth: 'unset', padding: '0 12px', height: 30, fontSize: 13 }
const inputSm = { height: 30, fontSize: 13, padding: '0 8px' }

function formatTs(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  return d.toLocaleString('sr-RS', { dateStyle: 'short', timeStyle: 'short' })
}

function emptyEditForm(p) {
  return {
    state: p.state ?? '',
    conditionDescription: p.conditionDescription ?? '',
    color: p.color ?? '',
    height: p.height ?? '',
  }
}

function BotanistHealthLogsPage() {
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
  const [deletingId, setDeletingId] = useState(null)
  const [deleteReason, setDeleteReason] = useState('')
  const [deleteSubmitting, setDeleteSubmitting] = useState(false)

  const [conditionLogs, setConditionLogs] = useState({})
  const [conditionLogsLoading, setConditionLogsLoading] = useState(false)

  const [deletionLog, setDeletionLog] = useState([])
  const [deletionLogLoading, setDeletionLogLoading] = useState(true)

  useEffect(() => {
    let ignore = false
    getPlants()
      .then((res) => { if (!ignore) setPlants(res.data) })
      .catch(() => { if (!ignore) setError('Failed to load plants.') })
      .finally(() => { if (!ignore) setLoading(false) })
    getDeletionLog()
      .then((res) => { if (!ignore) setDeletionLog(res.data) })
      .catch(() => {})
      .finally(() => { if (!ignore) setDeletionLogLoading(false) })
    return () => { ignore = true }
  }, [])

  useEffect(() => {
    if (expandedId == null || editingId === expandedId) return
    if (conditionLogs[expandedId] !== undefined) return
    let ignore = false
    setConditionLogsLoading(true)
    getPlantConditionLogs(expandedId)
      .then((res) => { if (!ignore) setConditionLogs((prev) => ({ ...prev, [expandedId]: res.data })) })
      .catch(() => { if (!ignore) setConditionLogs((prev) => ({ ...prev, [expandedId]: [] })) })
      .finally(() => { if (!ignore) setConditionLogsLoading(false) })
    return () => { ignore = true }
  }, [expandedId, editingId])

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
    setDeletingId(null)
    setDeleteReason('')
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
    if (editForm.state !== '' && (Number(editForm.state) < 1 || Number(editForm.state) > 5)) {
      setEditError('Condition must be between 1 and 5.'); return
    }
    setEditSubmitting(true)
    setEditError('')
    try {
      const res = await updatePlantCondition(id, {
        state: editForm.state !== '' ? Number(editForm.state) : null,
        conditionDescription: editForm.conditionDescription.trim() || null,
        color: editForm.color.trim() || null,
        height: editForm.height !== '' ? Number(editForm.height) : null,
      })
      setPlants((prev) => prev.map((p) => (p.id === id ? res.data : p)))
      setConditionLogs((prev) => ({ ...prev, [id]: undefined }))
      setEditingId(null)
      setExpandedId(null)
    } catch (err) {
      setEditError(err.response?.data?.message ?? 'Failed to save changes.')
    } finally {
      setEditSubmitting(false)
    }
  }

  function requestDelete(p, e) {
    e.stopPropagation()
    setDeletingId(p.id)
    setDeleteReason('')
    setDeleteError('')
    setExpandedId(null)
    setEditingId(null)
    setEditForm({})
  }

  function cancelDelete(e) {
    e?.stopPropagation()
    setDeletingId(null)
    setDeleteReason('')
    setDeleteError('')
  }

  async function confirmDelete(id, e) {
    e.stopPropagation()
    if (!deleteReason) { setDeleteError('Please select a reason for deletion.'); return }
    setDeleteSubmitting(true)
    setDeleteError('')
    try {
      await deletePlant(id, deleteReason)
      setPlants((prev) => prev.filter((p) => p.id !== id))
      setDeletingId(null)
      setDeleteReason('')
      getDeletionLog().then((res) => setDeletionLog(res.data)).catch(() => {})
    } catch (err) {
      setDeleteError(err.response?.data?.message ?? 'Failed to delete plant.')
    } finally {
      setDeleteSubmitting(false)
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
          onClick={() => navigate('/botanist')}
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

      <PageTitle label="Health logs" onBack={handleLogout} />

      <section className="home-body">
        <BotanistSidebar onSiteChange={() => {}} selectedSiteId={null} sites={[]} />

        <div className="varieties-content">
          <section className="varieties-list-card" aria-labelledby="plants-heading">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <h2 className="variety-form-heading" id="plants-heading" style={{ margin: 0 }}>
                All plants <span className="varieties-count">({filtered.length})</span>
              </h2>
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
                    const isDeleting = deletingId === p.id
                    const logs = conditionLogs[p.id]
                    return (
                      <>
                        <tr
                          key={p.id}
                          onClick={() => toggleExpand(p.id)}
                          style={{ cursor: 'pointer', background: (isOpen || isDeleting) ? 'var(--color-bg-soft, #f9fafb)' : undefined }}
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
                              <button className="variety-submit-button" onClick={(e) => startEdit(p, e)} type="button" style={btnSm}>Edit</button>
                              <button className="variety-submit-button btn-danger" onClick={(e) => requestDelete(p, e)} type="button" style={btnSm}>Delete</button>
                            </div>
                          </td>
                        </tr>

                        {isOpen && !isEditing && (
                          <tr key={`${p.id}-detail`} style={{ background: 'var(--color-bg-soft, #f9fafb)' }}>
                            <td />
                            <td colSpan={8} style={{ paddingBottom: 16, paddingTop: 4 }}>
                              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '8px 24px', fontSize: 13 }}>
                                <DetailField label="Condition" value={p.state != null ? `${p.state}/5` : null} />
                                <DetailField label="Color" value={p.color} />
                                <DetailField label="Height (cm)" value={p.height} />
                                <DetailField label="Sector" value={p.sectorName} />
                                <DetailField label="Storage space" value={p.storageSpaceName} />
                                <DetailField label="Site" value={p.siteName} />
                              </div>
                              {p.conditionDescription && (
                                <div style={{ marginTop: 10, fontSize: 13 }}>
                                  <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Condition notes: </span>
                                  {p.conditionDescription}
                                </div>
                              )}

                              <div style={{ marginTop: 16 }}>
                                <p style={{ fontWeight: 600, fontSize: 13, marginBottom: 8, color: 'var(--color-text-secondary)' }}>
                                  Condition change history
                                </p>
                                {conditionLogsLoading && expandedId === p.id ? (
                                  <p style={{ fontSize: 13, color: 'var(--color-text-secondary)' }}>Loading…</p>
                                ) : logs && logs.length > 0 ? (
                                  <table className="varieties-table" style={{ fontSize: 12 }}>
                                    <thead>
                                      <tr>
                                        <th>Date &amp; time</th>
                                        <th>Condition</th>
                                        <th>Notes</th>
                                        <th>Color</th>
                                        <th>Height (cm)</th>
                                        <th>Changed by</th>
                                      </tr>
                                    </thead>
                                    <tbody>
                                      {logs.map((log) => (
                                        <tr key={log.id}>
                                          <td>{formatTs(log.changedAt)}</td>
                                          <td>{log.conditionState != null ? `${log.conditionState}/5` : '—'}</td>
                                          <td>{log.conditionDescription ?? '—'}</td>
                                          <td>{log.color ?? '—'}</td>
                                          <td>{log.height ?? '—'}</td>
                                          <td>{log.changedBy}</td>
                                        </tr>
                                      ))}
                                    </tbody>
                                  </table>
                                ) : (
                                  <p style={{ fontSize: 13, color: 'var(--color-text-secondary)' }}>No condition changes recorded.</p>
                                )}
                              </div>
                            </td>
                          </tr>
                        )}

                        {isDeleting && (
                          <tr key={`${p.id}-delete`} style={{ background: 'var(--color-bg-soft, #f9fafb)' }}>
                            <td />
                            <td colSpan={8} style={{ paddingBottom: 16, paddingTop: 12 }} onClick={(e) => e.stopPropagation()}>
                              <p style={{ margin: '0 0 10px', fontWeight: 600, fontSize: 13 }}>
                                Select reason for deleting <em>{p.name}</em>:
                              </p>
                              <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 12 }}>
                                {DELETION_REASONS.map((r) => (
                                  <label key={r.value} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 13, cursor: 'pointer' }}>
                                    <input checked={deleteReason === r.value} name="deleteReason" onChange={() => setDeleteReason(r.value)} type="radio" value={r.value} />
                                    {r.label}
                                  </label>
                                ))}
                              </div>
                              {deleteError && <p className="variety-form-error" role="alert" style={{ marginBottom: 8 }}>{deleteError}</p>}
                              <div style={{ display: 'flex', gap: 8 }}>
                                <button className="variety-submit-button btn-danger" disabled={!deleteReason || deleteSubmitting} onClick={(e) => confirmDelete(p.id, e)} type="button" style={btnSm}>
                                  {deleteSubmitting ? 'Deleting…' : 'Confirm deletion'}
                                </button>
                                <button className="variety-submit-button" onClick={cancelDelete} type="button" style={{ ...btnSm, background: 'var(--color-text-secondary)' }}>Cancel</button>
                              </div>
                            </td>
                          </tr>
                        )}

                        {isEditing && (
                          <tr key={`${p.id}-edit`} style={{ background: 'var(--color-bg-soft, #f9fafb)' }}>
                            <td />
                            <td colSpan={8} style={{ paddingBottom: 16, paddingTop: 8 }}>
                              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px 24px', fontSize: 13 }}>
                                <label style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                  <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Condition (1–5)</span>
                                  <select className="variety-select" name="state" onChange={handleEditField} style={inputSm} value={editForm.state}>
                                    <option value="">Not rated</option>
                                    <option value="1">1 — Poor</option>
                                    <option value="2">2 — Fair</option>
                                    <option value="3">3 — Good</option>
                                    <option value="4">4 — Very good</option>
                                    <option value="5">5 — Excellent</option>
                                  </select>
                                </label>
                                <label style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                  <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Color</span>
                                  <input className="variety-input" name="color" onChange={handleEditField} placeholder="e.g. Green" style={inputSm} type="text" value={editForm.color} />
                                </label>
                                <label style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                  <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Height (cm)</span>
                                  <input className="variety-input" min="0" name="height" onChange={handleEditField} placeholder="e.g. 30" step="0.1" style={inputSm} type="number" value={editForm.height} />
                                </label>
                              </div>
                              <label style={{ display: 'flex', flexDirection: 'column', gap: 4, marginTop: 10, fontSize: 13 }}>
                                <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Condition notes</span>
                                <textarea className="variety-textarea" name="conditionDescription" onChange={handleEditField} placeholder="Describe the plant's current condition…" rows={2} value={editForm.conditionDescription} />
                              </label>
                              {editError && <p className="variety-form-error" role="alert" style={{ marginTop: 8 }}>{editError}</p>}
                              <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
                                <button className="variety-submit-button" disabled={editSubmitting} onClick={(e) => saveEdit(p.id, e)} type="button" style={btnSm}>
                                  {editSubmitting ? 'Saving…' : 'Save'}
                                </button>
                                <button className="variety-submit-button" onClick={cancelEdit} type="button" style={{ ...btnSm, background: 'var(--color-text-secondary)' }}>Cancel</button>
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

          <section className="varieties-list-card" aria-labelledby="deletion-log-heading" style={{ marginTop: 24 }}>
            <h2 className="variety-form-heading" id="deletion-log-heading" style={{ marginBottom: 16 }}>
              Plant deletion log
            </h2>
            {deletionLogLoading ? (
              <p className="varieties-empty">Loading…</p>
            ) : deletionLog.length === 0 ? (
              <p className="varieties-empty">No plants have been deleted yet.</p>
            ) : (
              <table className="varieties-table">
                <thead>
                  <tr>
                    <th>Plant</th>
                    <th>Variety</th>
                    <th>Reason</th>
                    <th>Date &amp; time</th>
                    <th>Deleted by</th>
                  </tr>
                </thead>
                <tbody>
                  {deletionLog.map((entry) => (
                    <tr key={entry.id}>
                      <td>{entry.plantName}</td>
                      <td>{entry.varietyName}</td>
                      <td>{entry.reason}</td>
                      <td>{formatTs(entry.deletedAt)}</td>
                      <td>{entry.deletedBy}</td>
                    </tr>
                  ))}
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

export default BotanistHealthLogsPage
