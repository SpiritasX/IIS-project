import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import AdminSidebar from '../components/admin/AdminSidebar'
import WorkerSidebar from '../components/worker/WorkerSidebar'
import PageTitle from '../components/home/PageTitle'
import SearchBar from '../components/home/SearchBar'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/worker.css'
import '../styles/varieties.css'

const EMPTY_SECTOR_FORM = { name: '', capacity: '' }

function EditStorageSpacePage({ api, basePath }) {
  const { id } = useParams()
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [location, setLocation] = useState(null)
  const [spaceTypes, setSpaceTypes] = useState([])
  const [loadError, setLoadError] = useState('')

  const [editForm, setEditForm] = useState({ name: '', type: '' })
  const [editSubmitting, setEditSubmitting] = useState(false)
  const [editError, setEditError] = useState('')
  const [editSuccess, setEditSuccess] = useState(false)

  const [sectorForm, setSectorForm] = useState(EMPTY_SECTOR_FORM)
  const [sectorSubmitting, setSectorSubmitting] = useState(false)
  const [sectorError, setSectorError] = useState('')
  const [deleteSectorError, setDeleteSectorError] = useState('')
  const [editingSectorId, setEditingSectorId] = useState(null)
  const [sectorEditForm, setSectorEditForm] = useState({ name: '', capacity: '' })
  const [sectorEditError, setSectorEditError] = useState('')
  const [sectorEditSubmitting, setSectorEditSubmitting] = useState(false)

  useEffect(() => {
    let ignore = false
    Promise.all([api.getLocation(id), api.getSpaceTypes()])
      .then(([locRes, typesRes]) => {
        if (!ignore) {
          setLocation(locRes.data)
          setEditForm({ name: locRes.data.name, type: locRes.data.type })
          setSpaceTypes(typesRes.data)
        }
      })
      .catch(() => {
        if (!ignore) setLoadError('Storage space not found.')
      })
    return () => { ignore = true }
  }, [id, api])

  async function handleEditSubmit(e) {
    e.preventDefault()
    if (!editForm.name.trim()) { setEditError('Name is required.'); return }
    if (!editForm.type.trim()) { setEditError('Type is required.'); return }

    setEditSubmitting(true)
    setEditError('')
    setEditSuccess(false)
    try {
      const res = await api.updateLocation(id, { name: editForm.name.trim(), type: editForm.type.trim() })
      setLocation(res.data)
      setEditSuccess(true)
      setTimeout(() => setEditSuccess(false), 3000)
    } catch (err) {
      setEditError(err.response?.data?.message ?? 'Failed to save changes.')
    } finally {
      setEditSubmitting(false)
    }
  }

  async function handleAddSector(e) {
    e.preventDefault()
    if (!sectorForm.name.trim()) { setSectorError('Sector name is required.'); return }

    setSectorSubmitting(true)
    setSectorError('')
    try {
      const res = await api.addSector(id, {
        name: sectorForm.name.trim(),
        capacity: sectorForm.capacity ? Number(sectorForm.capacity) : null,
      })
      setLocation(res.data)
      setSectorForm(EMPTY_SECTOR_FORM)
    } catch (err) {
      setSectorError(err.response?.data?.message ?? 'Failed to add sector.')
    } finally {
      setSectorSubmitting(false)
    }
  }

  function handleStartEditSector(sector) {
    setEditingSectorId(sector.id)
    setSectorEditForm({ name: sector.name, capacity: sector.capacity ?? '' })
    setSectorEditError('')
    setDeleteSectorError('')
  }

  function handleCancelEditSector() {
    setEditingSectorId(null)
    setSectorEditForm({ name: '', capacity: '' })
    setSectorEditError('')
  }

  async function handleSaveEditSector(sectorId) {
    if (!sectorEditForm.name.trim()) { setSectorEditError('Name is required.'); return }
    setSectorEditSubmitting(true)
    setSectorEditError('')
    try {
      const res = await api.updateSector(sectorId, {
        name: sectorEditForm.name.trim(),
        capacity: sectorEditForm.capacity !== '' ? Number(sectorEditForm.capacity) : null,
      })
      setLocation(res.data)
      setEditingSectorId(null)
    } catch (err) {
      setSectorEditError(err.response?.data?.message ?? 'Failed to save sector.')
    } finally {
      setSectorEditSubmitting(false)
    }
  }

  async function handleDeleteSector(sectorId) {
    setDeleteSectorError('')
    try {
      await api.deleteSector(sectorId)
      setLocation((prev) => ({
        ...prev,
        sectors: prev.sectors.filter((s) => s.id !== sectorId),
      }))
    } catch (err) {
      setDeleteSectorError(err.response?.data?.message ?? 'Failed to delete sector.')
    }
  }

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  const Sidebar = basePath === '/admin' ? AdminSidebar : WorkerSidebar

  if (loadError) {
    return (
      <main className="home-page">
        <PageTitle label="Edit Storage Space" onBack={handleLogout} />
        <section className="home-body">
          <div className="varieties-content">
            <p className="variety-form-error">{loadError}</p>
          </div>
        </section>
      </main>
    )
  }

  return (
    <main className="home-page">
      <header className="home-header botanist-header">
        <button
          aria-label="Back to locations"
          className="home-logo-placeholder"
          onClick={() => navigate(`${basePath}/locations`)}
          type="button"
        >
          <span aria-hidden="true" />
        </button>
        <div className="home-header-controls">
          <SearchBar onChange={() => {}} onClear={() => {}} value="" />
        </div>
      </header>

      <PageTitle label="Edit Storage Space" onBack={handleLogout} />

      <section className="home-body">
        <Sidebar onSiteChange={() => {}} selectedSiteId={null} sites={[]} />

        <div className="varieties-content">
          {/* Edit name / type */}
          <section className="variety-form-card" aria-labelledby="edit-location-heading">
            <h2 className="variety-form-heading" id="edit-location-heading">
              {location?.name ?? 'Loading…'}
            </h2>

            <form className="variety-form" noValidate onSubmit={handleEditSubmit}>
              <div className="variety-form-row variety-form-row-2">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="name">Name *</label>
                  <input
                    className="variety-input"
                    id="name"
                    name="name"
                    onChange={(e) => { setEditForm((p) => ({ ...p, name: e.target.value })); setEditError('') }}
                    required
                    type="text"
                    value={editForm.name}
                  />
                </div>
                <div className="variety-field">
                  <label className="variety-label" htmlFor="type">Type *</label>
                  <select
                    className="variety-select"
                    id="type"
                    name="type"
                    onChange={(e) => { setEditForm((p) => ({ ...p, type: e.target.value })); setEditError('') }}
                    required
                    value={editForm.type}
                  >
                    <option value="">Select type</option>
                    {spaceTypes.map((t) => (
                      <option key={t.id} value={t.name}>{t.name}</option>
                    ))}
                  </select>
                </div>
              </div>

              {editError && <p className="variety-form-error" role="alert">{editError}</p>}
              {editSuccess && <p className="variety-form-success" role="status">Changes saved.</p>}

              <div className="variety-form-actions">
                <button className="variety-submit-button" disabled={editSubmitting} type="submit">
                  {editSubmitting ? 'Saving…' : 'Save changes'}
                </button>
              </div>
            </form>
          </section>

          {/* Sectors */}
          <section className="varieties-list-card" aria-labelledby="sectors-heading">
            <h2 className="variety-form-heading" id="sectors-heading">
              Sectors{' '}
              <span className="varieties-count">({location?.sectors?.length ?? 0})</span>
            </h2>

            {deleteSectorError && (
              <p className="variety-form-error" role="alert" style={{ marginBottom: 12 }}>
                {deleteSectorError}
              </p>
            )}

            {sectorEditError && (
              <p className="variety-form-error" role="alert" style={{ marginBottom: 12 }}>
                {sectorEditError}
              </p>
            )}

            {location?.sectors?.length === 0 ? (
              <p className="varieties-empty" style={{ marginBottom: 20 }}>No sectors yet.</p>
            ) : (
              <table className="varieties-table" style={{ marginBottom: 24 }}>
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Capacity</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {location?.sectors?.map((sector) => {
                    const isEditing = editingSectorId === sector.id
                    return (
                      <tr key={sector.id}>
                        <td className="varieties-name">
                          {isEditing ? (
                            <input
                              className="variety-input"
                              onChange={(e) => setSectorEditForm((p) => ({ ...p, name: e.target.value }))}
                              style={{ width: '100%', minWidth: 120 }}
                              type="text"
                              value={sectorEditForm.name}
                            />
                          ) : sector.name}
                        </td>
                        <td>
                          {isEditing ? (
                            <input
                              className="variety-input"
                              min="1"
                              onChange={(e) => setSectorEditForm((p) => ({ ...p, capacity: e.target.value }))}
                              style={{ width: '100%', minWidth: 80 }}
                              type="number"
                              value={sectorEditForm.capacity}
                            />
                          ) : (sector.capacity ?? '—')}
                        </td>
                        <td>
                          {isEditing ? (
                            <div style={{ display: 'flex', gap: 6 }}>
                              <button
                                className="variety-submit-button"
                                disabled={sectorEditSubmitting}
                                onClick={() => handleSaveEditSector(sector.id)}
                                type="button"
                                style={{ minWidth: 'unset', padding: '0 14px', height: 36, fontSize: 14 }}
                              >
                                {sectorEditSubmitting ? '…' : 'Save'}
                              </button>
                              <button
                                className="variety-submit-button"
                                onClick={handleCancelEditSector}
                                type="button"
                                style={{ minWidth: 'unset', padding: '0 14px', height: 36, fontSize: 14, background: 'var(--color-text-secondary)' }}
                              >
                                Cancel
                              </button>
                            </div>
                          ) : (
                            <div style={{ display: 'flex', gap: 6 }}>
                              <button
                                className="variety-submit-button"
                                onClick={() => handleStartEditSector(sector)}
                                type="button"
                                style={{ minWidth: 'unset', padding: '0 14px', height: 36, fontSize: 14 }}
                              >
                                Edit
                              </button>
                              <button
                                className="variety-submit-button btn-danger"
                                onClick={() => handleDeleteSector(sector.id)}
                                type="button"
                                style={{ minWidth: 'unset', padding: '0 14px', height: 36, fontSize: 14 }}
                              >
                                Delete
                              </button>
                            </div>
                          )}
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            )}

            {/* Add sector form */}
            <h3 className="variety-label" style={{ marginBottom: 12, fontSize: 15 }}>Add sector</h3>
            <form className="variety-form" noValidate onSubmit={handleAddSector}>
              <div className="variety-form-row variety-form-row-2">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="sectorName">Name *</label>
                  <input
                    className="variety-input"
                    id="sectorName"
                    name="name"
                    onChange={(e) => { setSectorForm((p) => ({ ...p, name: e.target.value })); setSectorError('') }}
                    placeholder="e.g. Zone A"
                    required
                    type="text"
                    value={sectorForm.name}
                  />
                </div>
                <div className="variety-field">
                  <label className="variety-label" htmlFor="sectorCapacity">Capacity</label>
                  <input
                    className="variety-input"
                    id="sectorCapacity"
                    min="1"
                    name="capacity"
                    onChange={(e) => setSectorForm((p) => ({ ...p, capacity: e.target.value }))}
                    placeholder="e.g. 500"
                    type="number"
                    value={sectorForm.capacity}
                  />
                </div>
              </div>

              {sectorError && <p className="variety-form-error" role="alert">{sectorError}</p>}

              <div className="variety-form-actions">
                <button className="variety-submit-button" disabled={sectorSubmitting} type="submit">
                  {sectorSubmitting ? 'Adding…' : 'Add sector'}
                </button>
              </div>
            </form>
          </section>
        </div>
      </section>
    </main>
  )
}

export default EditStorageSpacePage
