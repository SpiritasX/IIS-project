import PrintIcon from '@mui/icons-material/Print'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  getConditionDistribution,
  getDashboardStats,
  getDeletionReasonStats,
  getPlantCountByUnit,
  getRelocationLogs,
  getPlantsNeedingAttention,
  getSites,
  getStockByVariety,
} from '../api/admin'
import { getActiveRelocations } from '../api/adminPlants'
import AdminSidebar from '../components/admin/AdminSidebar'
import PageTitle from '../components/home/PageTitle'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/orderAnalysis.css'

const CONDITION_COLORS = { 1: '#b91c1c', 2: '#c2410c', 3: '#d97706', 4: '#16a34a', 5: '#15803d' }
const DELETION_PALETTE = ['#b91c1c', '#c2410c', '#d97706', '#4c6f93', '#6b21a8', '#0f766e', '#15803d']
const UNIT_PALETTE = ['#15803d', '#22523a', '#4c6f93', '#d97706', '#9333ea', '#0f766e', '#c2410c']

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

function PaletteDonut({ data, getKey, getCount, getLabel, palette, centerLabel }) {
  const items = data.slice(0, palette.length)
  const total = items.reduce((s, d) => s + Number(getCount(d)), 0)
  let currentAngle = 0
  return (
    <div className="analysis-pie-wrap">
      <svg aria-label="Donut chart" className="analysis-pie" viewBox="0 0 220 220">
        {total === 0 ? (
          <circle className="analysis-pie-empty" cx="110" cy="110" r="90" />
        ) : (
          items.map((d, i) => {
            const count = Number(getCount(d))
            if (count === 0) return null
            const color = palette[i]
            const angle = (count / total) * 360
            const startAngle = currentAngle
            const endAngle = currentAngle + angle
            currentAngle = endAngle
            if (count === total) return <circle cx="110" cy="110" fill={color} key={getKey(d)} r="90" />
            return <path d={describeSlice(startAngle, endAngle)} fill={color} key={getKey(d)} />
          })
        )}
        <circle className="analysis-pie-center" cx="110" cy="110" r="54" />
        <text className="analysis-pie-total" textAnchor="middle" x="110" y="105">{total}</text>
        <text className="analysis-pie-label" textAnchor="middle" x="110" y="128">{centerLabel}</text>
      </svg>
      <div className="analysis-legend">
        {items.filter((d) => Number(getCount(d)) > 0).map((d, i) => (
          <p key={getKey(d)}>
            <span style={{ backgroundColor: palette[i] }} />
            <strong>{getLabel(d)}</strong>
            <em>{getCount(d)}</em>
          </p>
        ))}
        {total === 0 && <p style={{ color: 'var(--color-text-secondary)', fontSize: 13 }}>No data.</p>}
      </div>
    </div>
  )
}

function ConditionBars({ data }) {
  const total = data.reduce((s, d) => s + d.count, 0) || 1
  return (
    <div className="analysis-transition-graph">
      {data.map((d) => (
        <div className="analysis-transition-row" key={d.state}>
          <div className="analysis-transition-label">
            <strong style={{ color: CONDITION_COLORS[d.state] }}>{d.label}</strong>
            <span>{d.count} {d.count === 1 ? 'plant' : 'plants'}</span>
          </div>
          <div className="analysis-transition-track">
            <span style={{ width: `${(d.count / total) * 100}%`, background: CONDITION_COLORS[d.state] }} />
          </div>
          <b>{d.count === 0 ? '—' : `${Math.round((d.count / total) * 100)}%`}</b>
        </div>
      ))}
    </div>
  )
}

function StockBars({ data }) {
  const maxVal = Math.max(...data.map((d) => Number(d.totalStock)), 1)
  return (
    <div style={{ maxHeight: 260, overflowY: 'auto' }}>
      <div className="analysis-transition-graph">
        {data.length === 0 ? (
          <p style={{ color: 'var(--color-text-secondary)', fontSize: 14 }}>No variety data.</p>
        ) : data.map((d, i) => (
          <div className="analysis-transition-row" key={d.varietyName}>
            <div className="analysis-transition-label">
              <strong style={{ color: UNIT_PALETTE[i % UNIT_PALETTE.length] }}>{d.varietyName}</strong>
              <span>{d.totalStock} {Number(d.totalStock) === 1 ? 'plant' : 'plants'}</span>
            </div>
            <div className="analysis-transition-track">
              <span style={{ width: `${(Number(d.totalStock) / maxVal) * 100}%`, background: UNIT_PALETTE[i % UNIT_PALETTE.length] }} />
            </div>
            <b>{Number(d.totalStock) === 0 ? '—' : d.totalStock}</b>
          </div>
        ))}
      </div>
    </div>
  )
}

function conditionBadgeStyle(state) {
  const bg = { 1: '#fef2f2', 2: '#fff7ed', 3: '#fffbeb', 4: '#f0fdf4', 5: '#dcfce7' }
  return { background: bg[state] ?? '#f3f4f6', color: CONDITION_COLORS[state] ?? '#666', padding: '2px 10px', borderRadius: 4, fontWeight: 700, fontSize: 13, whiteSpace: 'nowrap' }
}

function formatDate(ts) {
  if (!ts) return '—'
  return new Date(ts).toLocaleString('sr-RS', { dateStyle: 'short', timeStyle: 'short' })
}

function SectionHeading({ children }) {
  return (
    <p style={{ fontSize: 11, fontWeight: 900, letterSpacing: '0.08em', textTransform: 'uppercase', color: 'var(--color-primary)', margin: '24px 0 10px', opacity: 0.7 }}>
      {children}
    </p>
  )
}

function AdminDashboardPage() {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [sites, setSites] = useState([])
  const [selectedSiteId, setSelectedSiteId] = useState(null)
  const [stats, setStats] = useState(null)
  const [conditionDist, setConditionDist] = useState([])
  const [deletionStats, setDeletionStats] = useState([])
  const [needingAttention, setNeedingAttention] = useState([])
  const [plantCountByUnit, setPlantCountByUnit] = useState([])
  const [stockByVariety, setStockByVariety] = useState([])
  const [activeRelocations, setActiveRelocations] = useState([])
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
      const [statsRes, condRes, delRes, attRes, unitRes, stockRes, activeRelRes] = await Promise.allSettled([
        getDashboardStats(selectedSiteId),
        getConditionDistribution(selectedSiteId),
        getDeletionReasonStats(),
        getPlantsNeedingAttention(selectedSiteId),
        getPlantCountByUnit(selectedSiteId),
        getStockByVariety(selectedSiteId),
        getActiveRelocations(),
      ])
      if (!ignore) {
        if (statsRes.status === 'fulfilled') setStats(statsRes.value.data)
        if (condRes.status === 'fulfilled') setConditionDist(condRes.value.data)
        if (delRes.status === 'fulfilled') setDeletionStats(delRes.value.data)
        if (attRes.status === 'fulfilled') setNeedingAttention(attRes.value.data)
        if (unitRes.status === 'fulfilled') setPlantCountByUnit(unitRes.value.data)
        if (stockRes.status === 'fulfilled') setStockByVariety(stockRes.value.data)
        if (activeRelRes.status === 'fulfilled') setActiveRelocations(activeRelRes.value.data)
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
    { label: 'Plants in stock', value: stats?.plantsCount, color: '#22523a', onClick: () => navigate('/admin/plants') },
    { label: 'Active relocations', value: stats?.activeRelocations, color: '#d97706', onClick: () => navigate('/admin/relocation-logs') },
    { label: 'Storage spaces', value: stats?.storageSpacesCount, color: '#4c6f93', onClick: () => navigate('/admin/locations') },
    { label: 'Varieties', value: stats?.varietiesCount, color: '#15803d', onClick: () => navigate('/admin/varieties') },
  ]

  return (
    <main className="home-page botanist-dash-page">
      <header className="home-header botanist-header">
        <button aria-label="Admin dashboard" className="home-logo-placeholder" onClick={() => navigate('/admin')} type="button">
          <span aria-hidden="true" />
        </button>
      </header>

      <PageTitle label="Admin" onBack={handleLogout} />

      <section className="home-body">
        <AdminSidebar onSiteChange={setSelectedSiteId} selectedSiteId={selectedSiteId} sites={sites} />

        <div className="botanist-dash-shell">
          <header className="order-analysis-header">
            <div>
              <p className="order-analysis-eyebrow">Admin dashboard</p>
              <h2>Full overview</h2>
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

          {/* — Health section (botanist graphs) — */}
          <SectionHeading>Plant health</SectionHeading>

          <div className="analysis-grid">
            <section
              className="analysis-section botanist-section-clickable"
              onClick={() => navigate('/admin/health-logs')}
              role="button"
              tabIndex={0}
              onKeyDown={(e) => { if (e.key === 'Enter') navigate('/admin/health-logs') }}
              title="Open health logs"
            >
              <h3>Deletions by reason</h3>
              {loading ? (
                <p className="botanist-dash-empty">Loading…</p>
              ) : (
                <PaletteDonut
                  centerLabel="deleted"
                  data={deletionStats}
                  getCount={(d) => d.count}
                  getKey={(d) => d.reason}
                  getLabel={(d) => d.reason}
                  palette={DELETION_PALETTE}
                />
              )}
            </section>

            <section
              className="analysis-section botanist-section-clickable"
              onClick={() => navigate('/admin/health-logs')}
              role="button"
              tabIndex={0}
              onKeyDown={(e) => { if (e.key === 'Enter') navigate('/admin/health-logs') }}
              title="Open health logs"
            >
              <h3>Condition breakdown</h3>
              {loading ? <p className="botanist-dash-empty">Loading…</p> : <ConditionBars data={conditionDist} />}
            </section>
          </div>

          <section
            className="analysis-section botanist-section-clickable"
            onClick={() => navigate('/admin/health-logs')}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => { if (e.key === 'Enter') navigate('/admin/health-logs') }}
            title="Open health logs"
          >
            <h3>
              Plants needing attention
              {!loading && needingAttention.length > 0 && (
                <span style={{ marginLeft: 10, fontSize: 16, fontWeight: 400, color: '#b91c1c' }}>
                  ({needingAttention.length})
                </span>
              )}
            </h3>
            {loading ? (
              <p className="botanist-dash-empty">Loading…</p>
            ) : needingAttention.length === 0 ? (
              <p className="botanist-dash-empty" style={{ color: '#16a34a' }}>All plants are in good condition.</p>
            ) : (
              <div className="analysis-table">
                <div className="botanist-condition-head">
                  <span>Plant</span>
                  <span>Variety</span>
                  <span>Condition</span>
                  <span>Description</span>
                  <span>Updated</span>
                </div>
                {needingAttention.map((p) => (
                  <div className="botanist-condition-row" key={p.plantId}>
                    <span style={{ fontWeight: 700 }}>{p.plantName}</span>
                    <span style={{ color: 'var(--color-text-secondary)' }}>{p.varietyName}</span>
                    <span><span style={conditionBadgeStyle(p.conditionState)}>{p.conditionLabel}</span></span>
                    <span style={{ color: 'var(--color-text-secondary)', fontSize: 14 }}>{p.description ?? '—'}</span>
                    <span style={{ color: 'var(--color-text-secondary)', fontSize: 13 }}>{formatDate(p.changedAt)}</span>
                  </div>
                ))}
              </div>
            )}
          </section>

          {/* — Inventory & relocations section (worker graphs) — */}
          <SectionHeading>Inventory &amp; relocations</SectionHeading>

          <div className="analysis-grid">
            <section className="analysis-section">
              <h3>Plants by storage unit</h3>
              {loading ? (
                <p className="botanist-dash-empty">Loading…</p>
              ) : (
                <PaletteDonut
                  centerLabel="plants"
                  data={plantCountByUnit}
                  getCount={(d) => d.plantCount}
                  getKey={(d) => d.unitName}
                  getLabel={(d) => d.unitName}
                  palette={UNIT_PALETTE}
                />
              )}
            </section>

            <section className="analysis-section">
              <h3>Stock by variety</h3>
              {loading ? <p className="botanist-dash-empty">Loading…</p> : <StockBars data={stockByVariety} />}
            </section>
          </div>

          <section
            className="analysis-section botanist-section-clickable"
            onClick={() => navigate('/admin/relocation-logs')}
            role="button"
            tabIndex={0}
            onKeyDown={(e) => { if (e.key === 'Enter') navigate('/admin/relocation-logs') }}
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
        </div>
      </section>
    </main>
  )
}

export default AdminDashboardPage
