import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  getDashboardStats,
  getDeletionReasonStats,
  getPlantCountByUnit,
  getRelocationLogs,
  getSites,
  getStockByVariety,
} from '../api/admin'
import { getActiveRelocations } from '../api/adminPlants'
import DeletionReasonChart from '../components/worker/DeletionReasonChart'
import PlantCountChart from '../components/worker/PlantCountChart'
import RelocationLogsTable from '../components/worker/RelocationLogsTable'
import StockByVarietyChart from '../components/worker/StockByVarietyChart'
import AdminSidebar from '../components/admin/AdminSidebar'
import StatCard from '../components/botanist/StatCard'
import PageTitle from '../components/home/PageTitle'
import SearchBar from '../components/home/SearchBar'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/worker.css'

function AdminDashboardPage() {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [sites, setSites] = useState([])
  const [selectedSiteId, setSelectedSiteId] = useState(null)

  const [stats, setStats] = useState(null)
  const [stockByVariety, setStockByVariety] = useState([])
  const [plantCount, setPlantCount] = useState([])
  const [logs, setLogs] = useState([])
  const [deletionStats, setDeletionStats] = useState([])
  const [activeRelocations, setActiveRelocations] = useState([])

  const [loadingData, setLoadingData] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')

  useEffect(() => {
    let ignore = false

    async function loadSites() {
      try {
        const res = await getSites()
        if (!ignore) setSites(res.data)
      } catch {
        // dashboard still works in global mode
      }
    }

    loadSites()
    return () => { ignore = true }
  }, [])

  useEffect(() => {
    let ignore = false

    async function loadDashboard() {
      setLoadingData(true)
      try {
        const [statsRes, stockRes, plantRes, logsRes, delRes, activeRelRes] = await Promise.all([
          getDashboardStats(selectedSiteId),
          getStockByVariety(selectedSiteId),
          getPlantCountByUnit(selectedSiteId),
          getRelocationLogs(selectedSiteId),
          getDeletionReasonStats(),
          getActiveRelocations(),
        ])
        if (!ignore) {
          setStats(statsRes.data)
          setStockByVariety(stockRes.data)
          setPlantCount(plantRes.data)
          setLogs(logsRes.data)
          setDeletionStats(delRes.data)
          setActiveRelocations(activeRelRes.data)
        }
      } catch {
        // leave previous data visible on error
      } finally {
        if (!ignore) setLoadingData(false)
      }
    }

    loadDashboard()
    return () => { ignore = true }
  }, [selectedSiteId])

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <main className="home-page">
      <header className="home-header botanist-header">
        <button
          aria-label="Admin dashboard"
          className="home-logo-placeholder"
          onClick={() => navigate('/admin')}
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

      <PageTitle label="Admin" onBack={handleLogout} />

      <section className="home-body">
        <AdminSidebar
          onSiteChange={setSelectedSiteId}
          selectedSiteId={selectedSiteId}
          sites={sites}
        />

        <div className="worker-dashboard-grid">
          <StockByVarietyChart data={stockByVariety} loading={loadingData} />
          <PlantCountChart data={plantCount} loading={loadingData} />

          <StatCard label="Varieties" loading={loadingData} value={stats?.varietiesCount} />
          <StatCard label="Storage spaces" loading={loadingData} value={stats?.storageSpacesCount} />
          <StatCard label="Plants" loading={loadingData} onClick={() => navigate('/admin/plants')} value={stats?.plantsCount} />
          <StatCard label="Active relocations" loading={loadingData} value={stats?.activeRelocations} />

          <DeletionReasonChart data={deletionStats} loading={loadingData} />
          <RelocationLogsTable loading={loadingData} logs={logs} searchTerm={searchTerm} />

          <ActiveRelocationsWidget loading={loadingData} relocations={activeRelocations} />
        </div>
      </section>
    </main>
  )
}

function ActiveRelocationsWidget({ relocations, loading }) {
  const STATE_BADGE = {
    WAITING: { background: '#fef9c3', color: '#713f12' },
    TRANSPORTING: { background: '#dbeafe', color: '#1e3a5f' },
  }
  return (
    <div className="varieties-list-card" style={{ gridColumn: '1 / -1', marginTop: 8 }}>
      <h3 style={{ margin: '0 0 12px', fontSize: 15, fontWeight: 700 }}>
        Active relocations
        {!loading && relocations.length > 0 && (
          <span style={{ marginLeft: 8, fontSize: 13, fontWeight: 400, color: 'var(--color-text-secondary)' }}>
            ({relocations.length})
          </span>
        )}
      </h3>
      {loading ? (
        <p style={{ fontSize: 13, color: 'var(--color-text-secondary)' }}>Loading…</p>
      ) : relocations.length === 0 ? (
        <p style={{ fontSize: 13, color: 'var(--color-text-secondary)' }}>No active relocations.</p>
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
            {relocations.map((r) => (
              <tr key={r.id}>
                <td className="varieties-name">{r.plantName}</td>
                <td>{[r.fromSiteName, r.fromStorageSpaceName, r.fromSectorName].filter(Boolean).join(' / ') || '—'}</td>
                <td>{[r.toSiteName, r.toStorageSpaceName, r.toSectorName].filter(Boolean).join(' / ')}</td>
                <td>
                  <span style={{ ...STATE_BADGE[r.state], padding: '2px 8px', borderRadius: 4, fontWeight: 600 }}>
                    {r.state === 'WAITING' ? 'Waiting' : 'Transporting'}
                  </span>
                </td>
                <td>{new Date(r.startedAt).toLocaleString('sr-RS', { dateStyle: 'short', timeStyle: 'short' })}</td>
                <td>{r.initiatedBy}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}

export default AdminDashboardPage
