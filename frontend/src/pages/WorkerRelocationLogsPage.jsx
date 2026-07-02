import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  getCompatibleStorageSpaces,
  getPlantRelocationLogs,
  getPlants,
  getSectors,
  startRelocation,
  updateRelocationState,
} from '../api/plants'
import WorkerSidebar from '../components/worker/WorkerSidebar'
import PageTitle from '../components/home/PageTitle'
import SearchBar from '../components/home/SearchBar'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/worker.css'
import '../styles/varieties.css'

const btnSm = { minWidth: 'unset', padding: '0 12px', height: 30, fontSize: 13 }
const inputSm = { height: 30, fontSize: 13, padding: '0 8px' }

const STATE_LABELS = { WAITING: 'Waiting', TRANSPORTING: 'Transporting', FINISHED: 'Finished' }
const STATE_BADGE = {
  WAITING: { background: '#fef9c3', color: '#713f12' },
  TRANSPORTING: { background: '#dbeafe', color: '#1e3a5f' },
  FINISHED: { background: '#dcfce7', color: '#14532d' },
}

function formatTs(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('sr-RS', { dateStyle: 'short', timeStyle: 'short' })
}

function WorkerRelocationLogsPage() {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [plants, setPlants] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [searchTerm, setSearchTerm] = useState('')

  const [expandedId, setExpandedId] = useState(null)
  const [editingId, setEditingId] = useState(null)

  const [relocationLogs, setRelocationLogs] = useState({})
  const [relocationLogsLoading, setRelocationLogsLoading] = useState(false)

  const [storageSpaces, setStorageSpaces] = useState([])
  const [sectors, setSectors] = useState([])
  const [editForm, setEditForm] = useState({ siteId: '', storageSpaceId: '', sectorId: '' })
  const [editSubmitting, setEditSubmitting] = useState(false)
  const [editError, setEditError] = useState('')

  useEffect(() => {
    let ignore = false
    getPlants()
      .then((res) => { if (!ignore) setPlants(res.data) })
      .catch(() => { if (!ignore) setError('Failed to load plants.') })
      .finally(() => { if (!ignore) setLoading(false) })
    return () => { ignore = true }
  }, [])

  useEffect(() => {
    if (expandedId == null || editingId === expandedId) return
    if (relocationLogs[expandedId] !== undefined) return
    let ignore = false
    setRelocationLogsLoading(true)
    getPlantRelocationLogs(expandedId)
      .then((res) => { if (!ignore) setRelocationLogs((prev) => ({ ...prev, [expandedId]: res.data })) })
      .catch(() => { if (!ignore) setRelocationLogs((prev) => ({ ...prev, [expandedId]: [] })) })
      .finally(() => { if (!ignore) setRelocationLogsLoading(false) })
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

  async function startEdit(p, e) {
    e.stopPropagation()
    setEditingId(p.id)
    setExpandedId(p.id)
    setEditForm({ siteId: '', storageSpaceId: '', sectorId: '' })
    setSectors([])
    setEditError('')
    try {
      const res = await getCompatibleStorageSpaces(p.varietyId)
      setStorageSpaces(res.data)
    } catch {
      setEditError('Failed to load storage spaces.')
    }
  }

  function cancelEdit(e) {
    e?.stopPropagation()
    setEditingId(null)
    setEditForm({ siteId: '', storageSpaceId: '', sectorId: '' })
    setStorageSpaces([])
    setSectors([])
    setEditError('')
  }

  async function handleSiteChange(e) {
    const siteId = e.target.value
    setEditForm((prev) => ({ ...prev, siteId, storageSpaceId: '', sectorId: '' }))
    setSectors([])
  }

  async function handleStorageSpaceChange(e) {
    const storageSpaceId = e.target.value
    setEditForm((prev) => ({ ...prev, storageSpaceId, sectorId: '' }))
    setSectors([])
    if (!storageSpaceId) return
    try {
      const res = await getSectors(storageSpaceId)
      setSectors(res.data)
    } catch {
      setEditError('Failed to load sectors.')
    }
  }

  async function saveEdit(plantId, e) {
    e.stopPropagation()
    if (!editForm.sectorId) { setEditError('Please select a sector.'); return }
    setEditSubmitting(true)
    setEditError('')
    try {
      const res = await startRelocation(plantId, Number(editForm.sectorId))
      setRelocationLogs((prev) => ({
        ...prev,
        [plantId]: [res.data, ...(prev[plantId] ?? [])],
      }))
      cancelEdit()
    } catch (err) {
      setEditError(err.response?.data?.message ?? 'Failed to start relocation.')
    } finally {
      setEditSubmitting(false)
    }
  }

  async function handleStateChange(plantId, logId, newState) {
    try {
      const res = await updateRelocationState(logId, newState)
      setRelocationLogs((prev) => ({
        ...prev,
        [plantId]: (prev[plantId] ?? []).map((l) => (l.id === logId ? res.data : l)),
      }))
      if (newState === 'FINISHED') {
        getPlants()
          .then((r) => setPlants(r.data))
          .catch(() => {})
      }
    } catch (err) {
      alert(err.response?.data?.message ?? 'Failed to update state.')
    }
  }

  const uniqueSites = [...new Map(
    storageSpaces.filter((ss) => ss.siteId).map((ss) => [ss.siteId, { id: ss.siteId, name: ss.siteName }])
  ).values()]

  const filteredSpaces = editForm.siteId
    ? storageSpaces.filter((ss) => String(ss.siteId) === String(editForm.siteId))
    : storageSpaces

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
          <SearchBar onChange={(e) => setSearchTerm(e.target.value)} onClear={() => setSearchTerm('')} value={searchTerm} />
        </div>
      </header>

      <PageTitle label="Relocation logs" onBack={handleLogout} />

      <section className="home-body">
        <WorkerSidebar onSiteChange={() => {}} selectedSiteId={null} sites={[]} />

        <div className="varieties-content">
          <section className="varieties-list-card" aria-labelledby="plants-heading">
            <h2 className="variety-form-heading" id="plants-heading" style={{ margin: '0 0 16px' }}>
              All plants <span className="varieties-count">({filtered.length})</span>
            </h2>

            {error && <p className="variety-form-error">{error}</p>}

            {loading ? (
              <p className="varieties-empty">Loading plants…</p>
            ) : filtered.length === 0 ? (
              <p className="varieties-empty">{searchTerm ? 'No plants match your search.' : 'No plants registered yet.'}</p>
            ) : (
              <table className="varieties-table">
                <thead>
                  <tr>
                    <th style={{ width: 28 }} />
                    <th>Name</th>
                    <th>Variety</th>
                    <th>Species</th>
                    <th>Category</th>
                    <th>Qty</th>
                    <th>Current location</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((p) => {
                    const isOpen = expandedId === p.id
                    const isEditing = editingId === p.id
                    const logs = relocationLogs[p.id]
                    return (
                      <>
                        <tr
                          key={p.id}
                          onClick={() => toggleExpand(p.id)}
                          style={{ cursor: 'pointer', background: isOpen ? 'var(--color-bg-soft,#f9fafb)' : undefined }}
                        >
                          <td style={{ color: 'var(--color-text-secondary)', fontSize: 11 }}>{isOpen ? '▼' : '▶'}</td>
                          <td className="varieties-name">{p.name}</td>
                          <td>
                            {p.varietyName}
                            {p.latinName && <span style={{ display: 'block', fontSize: 12, fontStyle: 'italic', color: 'var(--color-text-secondary)' }}>{p.latinName}</span>}
                          </td>
                          <td>{p.speciesName}</td>
                          <td><span className="variety-category-badge">{p.categoryName}</span></td>
                          <td>{p.currentQuantity ?? '—'}</td>
                          <td style={{ fontSize: 13 }}>
                            {p.siteName ? (
                              <>{p.siteName}{p.storageSpaceName && <span style={{ color: 'var(--color-text-secondary)' }}> / {p.storageSpaceName}</span>}{p.sectorName && <span style={{ color: 'var(--color-text-secondary)' }}> / {p.sectorName}</span>}</>
                            ) : '—'}
                          </td>
                          <td onClick={(e) => e.stopPropagation()}>
                            <button className="variety-submit-button" onClick={(e) => startEdit(p, e)} type="button" style={btnSm}>Relocate</button>
                          </td>
                        </tr>

                        {isEditing && (
                          <tr key={`${p.id}-edit`} style={{ background: 'var(--color-bg-soft,#f9fafb)' }}>
                            <td />
                            <td colSpan={7} style={{ paddingBottom: 16, paddingTop: 10 }}>
                              <p style={{ fontWeight: 600, fontSize: 13, margin: '0 0 10px', color: 'var(--color-text-secondary)' }}>
                                New location for <em>{p.name}</em>
                              </p>
                              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px 24px', fontSize: 13 }}>
                                <label style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                  <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Site</span>
                                  <select className="variety-select" onChange={handleSiteChange} style={inputSm} value={editForm.siteId}>
                                    <option value="">All sites</option>
                                    {uniqueSites.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
                                  </select>
                                </label>
                                <label style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                  <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Storage space *</span>
                                  <select className="variety-select" disabled={filteredSpaces.length === 0} onChange={handleStorageSpaceChange} style={inputSm} value={editForm.storageSpaceId}>
                                    <option value="">Select storage space</option>
                                    {filteredSpaces.map((ss) => <option key={ss.id} value={ss.id}>{ss.name}{ss.siteName && !editForm.siteId ? ` (${ss.siteName})` : ''}</option>)}
                                  </select>
                                </label>
                                <label style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                                  <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Sector *</span>
                                  <select className="variety-select" disabled={sectors.length === 0} onChange={(e) => setEditForm((f) => ({ ...f, sectorId: e.target.value }))} style={inputSm} value={editForm.sectorId}>
                                    <option value="">Select sector</option>
                                    {sectors.map((s) => <option key={s.id} value={s.id}>{s.name}{s.capacity ? ` (cap: ${s.capacity})` : ''}</option>)}
                                  </select>
                                </label>
                              </div>
                              {editError && <p className="variety-form-error" role="alert" style={{ marginTop: 8 }}>{editError}</p>}
                              <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
                                <button className="variety-submit-button" disabled={editSubmitting || !editForm.sectorId} onClick={(e) => saveEdit(p.id, e)} type="button" style={btnSm}>
                                  {editSubmitting ? 'Saving…' : 'Start relocation'}
                                </button>
                                <button className="variety-submit-button" onClick={cancelEdit} type="button" style={{ ...btnSm, background: 'var(--color-text-secondary)' }}>Cancel</button>
                              </div>
                            </td>
                          </tr>
                        )}

                        {isOpen && !isEditing && (
                          <tr key={`${p.id}-detail`} style={{ background: 'var(--color-bg-soft,#f9fafb)' }}>
                            <td />
                            <td colSpan={7} style={{ paddingBottom: 16, paddingTop: 8 }}>
                              <p style={{ fontWeight: 600, fontSize: 13, marginBottom: 8, color: 'var(--color-text-secondary)' }}>Relocation log</p>
                              {relocationLogsLoading && expandedId === p.id ? (
                                <p style={{ fontSize: 13, color: 'var(--color-text-secondary)' }}>Loading…</p>
                              ) : logs && logs.length > 0 ? (
                                <table className="varieties-table" style={{ fontSize: 12 }}>
                                  <thead>
                                    <tr>
                                      <th>From</th>
                                      <th>To</th>
                                      <th>State</th>
                                      <th>Started</th>
                                      <th>Finished</th>
                                      <th>By</th>
                                    </tr>
                                  </thead>
                                  <tbody>
                                    {logs.map((log) => (
                                      <tr key={log.id}>
                                        <td>
                                          {log.fromSiteName || log.fromStorageSpaceName || log.fromSectorName
                                            ? [log.fromSiteName, log.fromStorageSpaceName, log.fromSectorName].filter(Boolean).join(' / ')
                                            : '—'}
                                        </td>
                                        <td>{[log.toSiteName, log.toStorageSpaceName, log.toSectorName].filter(Boolean).join(' / ')}</td>
                                        <td>
                                          {log.state === 'FINISHED' ? (
                                            <span style={{ ...STATE_BADGE.FINISHED, padding: '2px 8px', borderRadius: 4, fontWeight: 600 }}>
                                              {STATE_LABELS.FINISHED}
                                            </span>
                                          ) : (
                                            <select
                                              className="variety-select"
                                              onChange={(e) => handleStateChange(p.id, log.id, e.target.value)}
                                              style={{ ...inputSm, ...STATE_BADGE[log.state], border: 'none', fontWeight: 600 }}
                                              value={log.state}
                                            >
                                              <option value="WAITING">Waiting</option>
                                              <option value="TRANSPORTING">Transporting</option>
                                              <option value="FINISHED">Finished</option>
                                            </select>
                                          )}
                                        </td>
                                        <td>{formatTs(log.startedAt)}</td>
                                        <td>{formatTs(log.finishedAt)}</td>
                                        <td>{log.initiatedBy}</td>
                                      </tr>
                                    ))}
                                  </tbody>
                                </table>
                              ) : (
                                <p style={{ fontSize: 13, color: 'var(--color-text-secondary)' }}>No relocations recorded.</p>
                              )}
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

export default WorkerRelocationLogsPage
