import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import AdminSidebar from '../components/admin/AdminSidebar'
import WorkerSidebar from '../components/worker/WorkerSidebar'
import PageTitle from '../components/home/PageTitle'
import SearchBar from '../components/home/SearchBar'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/worker.css'
import '../styles/varieties.css'

function StorageSpacesPage({ api, basePath }) {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [sites, setSites] = useState([])
  const [selectedSiteId, setSelectedSiteId] = useState(null)
  const [locations, setLocations] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [deleteError, setDeleteError] = useState('')
  const [deleteSiteError, setDeleteSiteError] = useState('')
  const [spaceTypes, setSpaceTypes] = useState([])
  const [newTypeName, setNewTypeName] = useState('')
  const [typeError, setTypeError] = useState('')
  const [typeSubmitting, setTypeSubmitting] = useState(false)

  const isAdmin = basePath === '/admin'

  useEffect(() => {
    api.getSites()
      .then((res) => setSites(res.data))
      .catch(() => {})
  }, [api])

  useEffect(() => {
    if (!isAdmin) return
    api.getSpaceTypes()
      .then((res) => setSpaceTypes(res.data))
      .catch(() => {})
  }, [api, isAdmin])

  useEffect(() => {
    if (isAdmin && selectedSiteId === null) {
      setLocations([])
      setLoading(false)
      return
    }
    let ignore = false
    setLoading(true)
    setDeleteError('')
    api.getLocations(selectedSiteId)
      .then((res) => { if (!ignore) setLocations(res.data) })
      .catch(() => {})
      .finally(() => { if (!ignore) setLoading(false) })
    return () => { ignore = true }
  }, [selectedSiteId, api, isAdmin])

  function handleSiteSelect(siteId) {
    setSelectedSiteId((prev) => (prev === siteId ? null : siteId))
    setDeleteSiteError('')
  }

  async function handleDelete(id) {
    setDeleteError('')
    try {
      await api.deleteLocation(id)
      setLocations((prev) => prev.filter((l) => l.id !== id))
    } catch (err) {
      setDeleteError(err.response?.data?.message ?? 'Failed to delete storage space.')
    }
  }

  async function handleDeleteSite(id) {
    setDeleteSiteError('')
    try {
      await api.deleteNurserySite(id)
      setSites((prev) => prev.filter((s) => s.id !== id))
      if (selectedSiteId === id) setSelectedSiteId(null)
    } catch (err) {
      setDeleteSiteError(err.response?.data?.message ?? 'Failed to delete nursery site.')
    }
  }

  async function handleAddType(e) {
    e.preventDefault()
    if (!newTypeName.trim()) { setTypeError('Type name is required.'); return }
    setTypeSubmitting(true)
    setTypeError('')
    try {
      const res = await api.createSpaceType(newTypeName.trim())
      setSpaceTypes((prev) => [...prev, res.data])
      setNewTypeName('')
    } catch (err) {
      setTypeError(err.response?.data?.message ?? 'Failed to add type.')
    } finally {
      setTypeSubmitting(false)
    }
  }

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  const filtered = searchTerm
    ? locations.filter(
        (l) =>
          l.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          l.type?.toLowerCase().includes(searchTerm.toLowerCase()),
      )
    : locations

  const Sidebar = isAdmin ? AdminSidebar : WorkerSidebar
  const selectedSite = sites.find((s) => s.id === selectedSiteId)

  return (
    <main className="home-page">
      <header className="home-header botanist-header">
        <button
          aria-label="Back to dashboard"
          className="home-logo-placeholder"
          onClick={() => navigate(basePath)}
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

      <PageTitle label="Locations" onBack={handleLogout} />

      <section className="home-body">
        <Sidebar
          onSiteChange={setSelectedSiteId}
          selectedSiteId={selectedSiteId}
          sites={sites}
        />

        <div className="varieties-content">
          {/* Nursery sites card — admin only */}
          {isAdmin && (
            <section className="varieties-list-card" aria-labelledby="sites-heading">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
                <h2 className="variety-form-heading" id="sites-heading" style={{ margin: 0 }}>
                  Nursery sites{' '}
                  <span className="varieties-count">({sites.length})</span>
                </h2>
                <button
                  className="variety-submit-button"
                  onClick={() => navigate('/admin/nursery-sites/new')}
                  type="button"
                >
                  + Add nursery site
                </button>
              </div>

              {deleteSiteError && (
                <p className="variety-form-error" role="alert" style={{ marginBottom: 12 }}>
                  {deleteSiteError}
                </p>
              )}

              {sites.length === 0 ? (
                <p className="varieties-empty">No nursery sites yet.</p>
              ) : (
                <table className="varieties-table">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Address</th>
                      <th>Storage spaces</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {sites.map((site) => (
                      <tr
                        key={site.id}
                        onClick={() => handleSiteSelect(site.id)}
                        style={{
                          cursor: 'pointer',
                          background: selectedSiteId === site.id
                            ? 'rgba(34, 82, 58, 0.08)'
                            : undefined,
                        }}
                      >
                        <td className="varieties-name">
                          {selectedSiteId === site.id && (
                            <span style={{ color: 'var(--color-primary)', marginRight: 6 }}>▶</span>
                          )}
                          {site.name}
                        </td>
                        <td>{site.address ?? '—'}</td>
                        <td>{site.storageSpacesCount ?? 0}</td>
                        <td onClick={(e) => e.stopPropagation()}>
                          <button
                            className="variety-submit-button btn-danger"
                            onClick={() => handleDeleteSite(site.id)}
                            type="button"
                            style={{ minWidth: 'unset', padding: '0 16px', height: 36, fontSize: 14 }}
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </section>
          )}

          {/* Storage space types card — admin only */}
          {isAdmin && (
            <section className="varieties-list-card" aria-labelledby="types-heading">
              <h2 className="variety-form-heading" id="types-heading" style={{ marginBottom: 16 }}>
                Storage space types{' '}
                <span className="varieties-count">({spaceTypes.length})</span>
              </h2>

              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 16 }}>
                {spaceTypes.map((t) => (
                  <span key={t.id} className="variety-category-badge">{t.name}</span>
                ))}
                {spaceTypes.length === 0 && (
                  <p className="varieties-empty" style={{ margin: 0 }}>No types defined yet.</p>
                )}
              </div>

              <form
                className="variety-form"
                noValidate
                onSubmit={handleAddType}
                style={{ display: 'flex', gap: 10, alignItems: 'flex-end', flexWrap: 'wrap' }}
              >
                <div className="variety-field" style={{ flex: 1, minWidth: 180 }}>
                  <label className="variety-label" htmlFor="newTypeName">New type name</label>
                  <input
                    className="variety-input"
                    id="newTypeName"
                    onChange={(e) => { setNewTypeName(e.target.value); setTypeError('') }}
                    placeholder="e.g. Lager"
                    type="text"
                    value={newTypeName}
                  />
                </div>
                <button
                  className="variety-submit-button"
                  disabled={typeSubmitting}
                  type="submit"
                  style={{ minWidth: 'unset', padding: '0 20px', height: 40, fontSize: 14 }}
                >
                  {typeSubmitting ? 'Adding…' : '+ Add type'}
                </button>
              </form>
              {typeError && (
                <p className="variety-form-error" role="alert" style={{ marginTop: 8 }}>
                  {typeError}
                </p>
              )}
            </section>
          )}

          {/* Storage spaces card — for worker always, for admin only when a site is selected */}
          {(!isAdmin || selectedSiteId !== null) && (
            <section className="varieties-list-card" aria-labelledby="locations-heading">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
                <h2 className="variety-form-heading" id="locations-heading" style={{ margin: 0 }}>
                  {isAdmin && selectedSite ? (
                    <>Storage spaces — <span style={{ color: 'var(--color-primary)' }}>{selectedSite.name}</span>{' '}</>
                  ) : 'Storage spaces '}
                  <span className="varieties-count">({filtered.length})</span>
                </h2>
                <button
                  className="variety-submit-button"
                  onClick={() => navigate(`${basePath}/locations/new`)}
                  type="button"
                >
                  + Add storage space
                </button>
              </div>

              {deleteError && (
                <p className="variety-form-error" role="alert" style={{ marginBottom: 12 }}>
                  {deleteError}
                </p>
              )}

              {loading ? (
                <p className="varieties-empty">Loading…</p>
              ) : filtered.length === 0 ? (
                <p className="varieties-empty">No storage spaces found.</p>
              ) : (
                <table className="varieties-table">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Type</th>
                      <th>Sectors</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filtered.map((loc) => (
                      <tr
                        key={loc.id}
                        onClick={() => navigate(`${basePath}/locations/${loc.id}`)}
                        style={{ cursor: 'pointer' }}
                      >
                        <td className="varieties-name">{loc.name}</td>
                        <td>
                          <span className="variety-category-badge">{loc.type}</span>
                        </td>
                        <td>{loc.sectors?.length ?? 0}</td>
                        <td onClick={(e) => e.stopPropagation()}>
                          <button
                            className="variety-submit-button btn-danger"
                            onClick={() => handleDelete(loc.id)}
                            type="button"
                            style={{ minWidth: 'unset', padding: '0 16px', height: 36, fontSize: 14 }}
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </section>
          )}
        </div>
      </section>
    </main>
  )
}

export default StorageSpacesPage
