import PrintIcon from '@mui/icons-material/Print'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  getDashboardStats,
  getPlantCountByUnit,
  getRelocationLogs,
  getSites,
  getStockByVariety,
} from '../api/worker'
import { getActiveRelocations } from '../api/plants'
import WorkerSidebar from '../components/worker/WorkerSidebar'
import PageTitle from '../components/home/PageTitle'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/orderAnalysis.css'

const PALETTE = ['#15803d', '#22523a', '#4c6f93', '#d97706', '#b91c1c', '#6b21a8', '#0f766e']

const STATE_BADGE = {
  WAITING: { background: '#fef9c3', color: '#713f12' },
  TRANSPORTING: { background: '#dbeafe', color: '#1e3a5f' },
}

function pointOnCircle(center, radius, angle) {
  const radians = ((angle - 90) * Math.PI) / 180
  return { x: center + radius * Math.cos(radians), y: center + radius * Math.sin(radians) }
}

function describeSlice(startAngle, endAngle) {
  const center = 110
  const radius = 90
  const start = pointOnCircle(center, radius, endAngle)
  const end = pointOnCircle(center, radius, startAngle)
  const largeArc = endAngle - startAngle > 180 ? 1 : 0
  return [`M ${center} ${center}`, `L ${start.x} ${start.y}`, `A ${radius} ${radius} 0 ${largeArc} 0 ${end.x} ${end.y}`, 'Z'].join(' ')
}

function UnitDonut({ data }) {
  const slices = data.slice(0, PALETTE.length)
  const total = slices.reduce((s, d) => s + Number(d.plantCount), 0)
  let currentAngle = 0
  return (
    <div className="analysis-pie-wrap">
      <svg aria-label="Plants by storage unit" className="analysis-pie" viewBox="0 0 220 220">
        {total === 0 ? (
          <circle className="analysis-pie-empty" cx="110" cy="110" r="90" />
        ) : (
          slices.map((d, i) => {
            const count = Number(d.plantCount)
            if (count === 0) return null
            const color = PALETTE[i]
            const angle = (count / total) * 360
            const startAngle = currentAngle
            const endAngle = currentAngle + angle
            currentAngle = endAngle
            if (count === total) return <circle cx="110" cy="110" fill={color} key={d.unitName} r="90" />
            return <path d={describeSlice(startAngle, endAngle)} fill={color} key={d.unitName} />
          })
        )}
        <circle className="analysis-pie-center" cx="110" cy="110" r="54" />
        <text className="analysis-pie-total" textAnchor="middle" x="110" y="105">{total}</text>
        <text className="analysis-pie-label" textAnchor="middle" x="110" y="128">plants</text>
      </svg>
      <div className="analysis-legend">
        {slices.filter((d) => Number(d.plantCount) > 0).map((d, i) => (
          <p key={d.unitName}>
            <span style={{ backgroundColor: PALETTE[i] }} />
            <strong>{d.unitName}</strong>
            <em>{d.plantCount}</em>
          </p>
        ))}
        {total === 0 && <p style={{ color: 'var(--color-text-secondary)', fontSize: 13 }}>No plants in stock.</p>}
      </div>
    </div>
  )
}

function StockBars({ data }) {
  const maxVal = Math.max(...data.map((d) => Number(d.totalStock)), 1)
  return (
    <div style={{ maxHeight: 260, overflowY: 'auto' }}>
      <div className="analysis-transition-graph">
        {data.length === 0 ? (
          <p style={{ color: 'var(--color-text-secondary)', fontSize: 14 }}>No variety data available.</p>
        ) : data.map((d, i) => (
          <div className="analysis-transition-row" key={d.varietyName}>
            <div className="analysis-transition-label">
              <strong style={{ color: PALETTE[i % PALETTE.length] }}>{d.varietyName}</strong>
              <span>{d.totalStock} {Number(d.totalStock) === 1 ? 'plant' : 'plants'}</span>
            </div>
            <div className="analysis-transition-track">
              <span style={{ width: `${(Number(d.totalStock) / maxVal) * 100}%`, background: PALETTE[i % PALETTE.length] }} />
            </div>
            <b>{Number(d.totalStock) === 0 ? '—' : d.totalStock}</b>
          </div>
        ))}
      </div>
    </div>
  )
}

function formatDate(ts) {
  if (!ts) return '—'
  return new Date(ts).toLocaleString('sr-RS', { dateStyle: 'short', timeStyle: 'short' })
}

function WorkerDashboardPage() {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [sites, setSites] = useState([])
  const [selectedSiteId, setSelectedSiteId] = useState(null)
  const [stats, setStats] = useState(null)
  const [plantCountByUnit, setPlantCountByUnit] = useState([])
  const [stockByVariety, setStockByVariety] = useState([])
  const [activeRelocations, setActiveRelocations] = useState([])
  const [relocationLogs, setRelocationLogs] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let ignore = false
    getSites().then((r) => { if (!ignore) setSites(r.data) }).catch(() => {})
    return () => { ignore = true }
  }, [])

  useEffect(() => {
    let ignore = false
    async function load() {
      setLoading(true)
      const [statsRes, unitRes, stockRes, activeRelRes, logsRes] = await Promise.allSettled([
        getDashboardStats(selectedSiteId),
        getPlantCountByUnit(selectedSiteId),
        getStockByVariety(selectedSiteId),
        getActiveRelocations(),
        getRelocationLogs(selectedSiteId),
      ])
      if (!ignore) {
        if (statsRes.status === 'fulfilled') setStats(statsRes.value.data)
        if (unitRes.status === 'fulfilled') setPlantCountByUnit(unitRes.value.data)
        if (stockRes.status === 'fulfilled') setStockByVariety(stockRes.value.data)
        if (activeRelRes.status === 'fulfilled') setActiveRelocations(activeRelRes.value.data)
        if (logsRes.status === 'fulfilled') setRelocationLogs(logsRes.value.data)
        setLoading(false)
      }
    }
    load()
    return () => { ignore = true }
  }, [selectedSiteId])

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  const KPI = [
    { label: 'Plants in stock', value: stats?.plantsCount, color: '#22523a', onClick: () => navigate('/worker/plants') },
    { label: 'Active relocations', value: stats?.activeRelocations, color: '#d97706', onClick: () => navigate('/worker/relocation-logs') },
    { label: 'Storage spaces', value: stats?.storageSpacesCount, color: '#4c6f93', onClick: () => navigate('/worker/locations') },
    { label: 'Varieties', value: stats?.varietiesCount, color: '#15803d', onClick: null },
  ]

  return (
    <main className="home-page botanist-dash-page">
      <header className="home-header botanist-header">
        <button aria-label="Worker dashboard" className="home-logo-placeholder" onClick={() => navigate('/worker')} type="button">
          <span aria-hidden="true" />
        </button>
      </header>

      <PageTitle label="Worker" onBack={handleLogout} />

      <section className="home-body">
        <WorkerSidebar onSiteChange={setSelectedSiteId} selectedSiteId={selectedSiteId} sites={sites} />

        <div className="botanist-dash-shell">
          <header className="order-analysis-header">
            <div>
              <p className="order-analysis-eyebrow">Worker dashboard</p>
              <h2>Locations &amp; relocations</h2>
            </div>
            <button className="analysis-print-button" onClick={() => window.print()} type="button">
              <PrintIcon fontSize="inherit" />
              Export PDF
            </button>
          </header>

          <div className="analysis-phase-strip botanist-kpi-strip">
            {KPI.map((k) => (
              <article
                className={k.onClick ? 'botanist-kpi-clickable' : undefined}
                key={k.label}
                onClick={k.onClick ?? undefined}
                role={k.onClick ? 'button' : undefined}
                tabIndex={k.onClick ? 0 : undefined}
                onKeyDown={k.onClick ? (e) => { if (e.key === 'Enter') k.onClick() } : undefined}
              >
                <span style={{ backgroundColor: k.color }} />
                <p>{k.label}</p>
                <strong>{loading ? '…' : (k.value ?? 0)}</strong>
              </article>
            ))}
          </div>

          <div className="analysis-grid">
            <section className="analysis-section">
              <h3>Plants by storage unit</h3>
              {loading ? <p className="botanist-dash-empty">Loading…</p> : <UnitDonut data={plantCountByUnit} />}
            </section>

            <section className="analysis-section">
              <h3>Stock by variety</h3>
              {loading ? <p className="botanist-dash-empty">Loading…</p> : <StockBars data={stockByVariety} />}
            </section>
          </div>

          <section
            className="analysis-section botanist-section-clickable"
            onClick={() => navigate('/worker/relocation-logs')}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => { if (e.key === 'Enter') navigate('/worker/relocation-logs') }}
            title="Open relocation logs"
          >
            <h3>
              Active relocations
              {!loading && activeRelocations.length > 0 && (
                <span style={{ marginLeft: 10, fontSize: 16, fontWeight: 400, color: 'var(--color-text-secondary)' }}>
                  ({activeRelocations.length})
                </span>
              )}
            </h3>
            {loading ? (
              <p className="botanist-dash-empty">Loading…</p>
            ) : activeRelocations.length === 0 ? (
              <p className="botanist-dash-empty">No active relocations.</p>
            ) : (
              <table className="varieties-table" style={{ fontSize: 13 }}>
                <thead>
                  <tr>
                    <th>Plant</th>
                    <th>From</th>
                    <th>To</th>
                    <th>State</th>
                    <th>Started</th>
                    <th>By</th>
                  </tr>
                </thead>
                <tbody>
                  {activeRelocations.map((r) => (
                    <tr key={r.id}>
                      <td className="varieties-name">{r.plantName}</td>
                      <td>{[r.fromSiteName, r.fromStorageSpaceName, r.fromSectorName].filter(Boolean).join(' / ') || '—'}</td>
                      <td>{[r.toSiteName, r.toStorageSpaceName, r.toSectorName].filter(Boolean).join(' / ')}</td>
                      <td>
                        <span style={{ ...STATE_BADGE[r.state], padding: '2px 8px', borderRadius: 4, fontWeight: 600 }}>
                          {r.state === 'WAITING' ? 'Waiting' : 'Transporting'}
                        </span>
                      </td>
                      <td style={{ whiteSpace: 'nowrap', color: 'var(--color-text-secondary)' }}>{formatDate(r.startedAt)}</td>
                      <td style={{ color: 'var(--color-text-secondary)' }}>{r.initiatedBy}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </section>

          <section className="analysis-section">
            <h3>Recent relocation history</h3>
            {loading ? (
              <p className="botanist-dash-empty">Loading…</p>
            ) : relocationLogs.length === 0 ? (
              <p className="botanist-dash-empty">No relocation history recorded yet.</p>
            ) : (
              <table className="varieties-table" style={{ fontSize: 13 }}>
                <thead>
                  <tr>
                    <th>Plant</th>
                    <th>Location</th>
                    <th>Reason</th>
                    <th>In stock</th>
                    <th>Date</th>
                  </tr>
                </thead>
                <tbody>
                  {relocationLogs.slice(0, 20).map((log, i) => (
                    <tr key={i}>
                      <td className="varieties-name">{log.plantName}</td>
                      <td>{log.parcelName || '—'}</td>
                      <td>{log.reason || '—'}</td>
                      <td>{log.inStock ?? '—'}</td>
                      <td style={{ whiteSpace: 'nowrap', color: 'var(--color-text-secondary)' }}>
                        {log.startTime ? formatDate(log.startTime) : '—'}
                      </td>
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

export default WorkerDashboardPage
