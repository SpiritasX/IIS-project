import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  getDashboardStats,
  getPlantCountByUnit,
  getRelocationLogs,
  getSites,
  getStockByVariety,
} from '../api/worker'
import PlantCountChart from '../components/worker/PlantCountChart'
import RelocationLogsTable from '../components/worker/RelocationLogsTable'
import StockByVarietyChart from '../components/worker/StockByVarietyChart'
import WorkerSidebar from '../components/worker/WorkerSidebar'
import StatCard from '../components/botanist/StatCard'
import PageTitle from '../components/home/PageTitle'
import SearchBar from '../components/home/SearchBar'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/worker.css'

function WorkerDashboardPage() {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [sites, setSites] = useState([])
  const [selectedSiteId, setSelectedSiteId] = useState(null)

  const [stats, setStats] = useState(null)
  const [stockByVariety, setStockByVariety] = useState([])
  const [plantCount, setPlantCount] = useState([])
  const [logs, setLogs] = useState([])

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
        const [statsRes, stockRes, plantRes, logsRes] = await Promise.all([
          getDashboardStats(selectedSiteId),
          getStockByVariety(selectedSiteId),
          getPlantCountByUnit(selectedSiteId),
          getRelocationLogs(selectedSiteId),
        ])
        if (!ignore) {
          setStats(statsRes.data)
          setStockByVariety(stockRes.data)
          setPlantCount(plantRes.data)
          setLogs(logsRes.data)
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
          aria-label="Worker dashboard"
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

      <PageTitle label="Worker" onBack={handleLogout} />

      <section className="home-body">
        <WorkerSidebar
          onSiteChange={setSelectedSiteId}
          selectedSiteId={selectedSiteId}
          sites={sites}
        />

        <div className="worker-dashboard-grid">
          <StockByVarietyChart data={stockByVariety} loading={loadingData} />
          <PlantCountChart data={plantCount} loading={loadingData} />

          <StatCard label="Varieties" loading={loadingData} value={stats?.varietiesCount} />
          <StatCard label="Storage spaces" loading={loadingData} value={stats?.storageSpacesCount} />
          <StatCard label="Plants" loading={loadingData} onClick={() => navigate('/worker/plants')} value={stats?.plantsCount} />
          <StatCard label="Active relocations" loading={loadingData} value={stats?.activeRelocations} />

          <RelocationLogsTable loading={loadingData} logs={logs} searchTerm={searchTerm} />
        </div>
      </section>
    </main>
  )
}

export default WorkerDashboardPage
