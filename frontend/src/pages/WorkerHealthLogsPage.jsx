import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getDeletionLog, getDeletionLogConditionHistory, getPlantConditionLogs, getPlants } from '../api/plants'
import WorkerSidebar from '../components/worker/WorkerSidebar'
import PageTitle from '../components/home/PageTitle'
import SearchBar from '../components/home/SearchBar'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/worker.css'
import '../styles/varieties.css'

function formatTs(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  return d.toLocaleString('sr-RS', { dateStyle: 'short', timeStyle: 'short' })
}

function WorkerHealthLogsPage() {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [plants, setPlants] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [searchTerm, setSearchTerm] = useState('')

  const [expandedId, setExpandedId] = useState(null)
  const [conditionLogs, setConditionLogs] = useState({})
  const [conditionLogsLoading, setConditionLogsLoading] = useState(false)

  const [deletionLog, setDeletionLog] = useState([])
  const [deletionLogLoading, setDeletionLogLoading] = useState(true)
  const [expandedDeletionId, setExpandedDeletionId] = useState(null)
  const [deletionConditionLogs, setDeletionConditionLogs] = useState({})
  const [deletionConditionLogsLoading, setDeletionConditionLogsLoading] = useState(false)

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
    if (expandedId == null) return
    if (conditionLogs[expandedId] !== undefined) return
    let ignore = false
    setConditionLogsLoading(true)
    getPlantConditionLogs(expandedId)
      .then((res) => { if (!ignore) setConditionLogs((prev) => ({ ...prev, [expandedId]: res.data })) })
      .catch(() => { if (!ignore) setConditionLogs((prev) => ({ ...prev, [expandedId]: [] })) })
      .finally(() => { if (!ignore) setConditionLogsLoading(false) })
    return () => { ignore = true }
  }, [expandedId])

  useEffect(() => {
    if (expandedDeletionId == null) return
    if (deletionConditionLogs[expandedDeletionId] !== undefined) return
    let ignore = false
    setDeletionConditionLogsLoading(true)
    getDeletionLogConditionHistory(expandedDeletionId)
      .then((res) => { if (!ignore) setDeletionConditionLogs((prev) => ({ ...prev, [expandedDeletionId]: res.data })) })
      .catch(() => { if (!ignore) setDeletionConditionLogs((prev) => ({ ...prev, [expandedDeletionId]: [] })) })
      .finally(() => { if (!ignore) setDeletionConditionLogsLoading(false) })
    return () => { ignore = true }
  }, [expandedDeletionId])

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  function toggleExpand(id) {
    setExpandedId((prev) => (prev === id ? null : id))
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

      <PageTitle label="Health logs" onBack={handleLogout} />

      <section className="home-body">
        <WorkerSidebar onSiteChange={() => {}} selectedSiteId={null} sites={[]} />

        <div className="varieties-content">
          <section className="varieties-list-card" aria-labelledby="plants-heading">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <h2 className="variety-form-heading" id="plants-heading" style={{ margin: 0 }}>
                All plants <span className="varieties-count">({filtered.length})</span>
              </h2>
            </div>

            {error && <p className="variety-form-error">{error}</p>}

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
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((p) => {
                    const isOpen = expandedId === p.id
                    const logs = conditionLogs[p.id]
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
                        </tr>

                        {isOpen && (
                          <tr key={`${p.id}-detail`} style={{ background: 'var(--color-bg-soft, #f9fafb)' }}>
                            <td />
                            <td colSpan={7} style={{ paddingBottom: 16, paddingTop: 4 }}>
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
                    <th style={{ width: 28 }}></th>
                    <th>Plant</th>
                    <th>Variety</th>
                    <th>Reason</th>
                    <th>Date &amp; time</th>
                    <th>Deleted by</th>
                  </tr>
                </thead>
                <tbody>
                  {deletionLog.map((entry) => {
                    const isDOpen = expandedDeletionId === entry.id
                    const dLogs = deletionConditionLogs[entry.id]
                    return (
                      <>
                        <tr
                          key={entry.id}
                          onClick={() => setExpandedDeletionId((prev) => (prev === entry.id ? null : entry.id))}
                          style={{ cursor: 'pointer', background: isDOpen ? 'var(--color-bg-soft, #f9fafb)' : undefined }}
                        >
                          <td style={{ color: 'var(--color-text-secondary)', fontSize: 11 }}>
                            {isDOpen ? '▼' : '▶'}
                          </td>
                          <td>{entry.plantName}</td>
                          <td>{entry.varietyName}</td>
                          <td>{entry.reason}</td>
                          <td>{formatTs(entry.deletedAt)}</td>
                          <td>{entry.deletedBy}</td>
                        </tr>
                        {isDOpen && (
                          <tr key={`${entry.id}-history`} style={{ background: 'var(--color-bg-soft, #f9fafb)' }}>
                            <td />
                            <td colSpan={5} style={{ paddingBottom: 16, paddingTop: 4 }}>
                              <p style={{ fontWeight: 600, fontSize: 13, marginBottom: 8, color: 'var(--color-text-secondary)' }}>
                                Care history
                              </p>
                              {deletionConditionLogsLoading && expandedDeletionId === entry.id && dLogs === undefined ? (
                                <p style={{ fontSize: 13, color: 'var(--color-text-secondary)' }}>Loading…</p>
                              ) : dLogs && dLogs.length > 0 ? (
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
                                    {dLogs.map((log) => (
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
                                <p style={{ fontSize: 13, color: 'var(--color-text-secondary)' }}>No care history recorded for this plant.</p>
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

function DetailField({ label, value }) {
  if (value == null || value === '') return null
  return (
    <div>
      <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>{label}: </span>
      <span>{value}</span>
    </div>
  )
}

export default WorkerHealthLogsPage
