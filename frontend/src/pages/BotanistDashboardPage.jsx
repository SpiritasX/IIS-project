import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  getDashboardLogs,
  getDashboardRelocations,
  getDashboardStats,
  getDashboardStock,
  getDeletionReasonStats,
  getSites,
} from '../api/botanist'
import BotanistSidebar from '../components/botanist/BotanistSidebar'
import LogsTable from '../components/botanist/LogsTable'
import RelocationChart from '../components/botanist/RelocationChart'
import StatCard from '../components/botanist/StatCard'
import StockBarChart from '../components/botanist/StockBarChart'
import DeletionReasonChart from '../components/worker/DeletionReasonChart'
import SearchBar from '../components/home/SearchBar'
import PageTitle from '../components/home/PageTitle'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'

function BotanistDashboardPage() {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [sites, setSites] = useState([])
  const [selectedSiteId, setSelectedSiteId] = useState(null)

  const [stats, setStats] = useState(null)
  const [stockData, setStockData] = useState([])
  const [relocationData, setRelocationData] = useState([])
  const [logs, setLogs] = useState([])
  const [deletionStats, setDeletionStats] = useState([])

  const [loadingData, setLoadingData] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')

  useEffect(() => {
    let ignore = false

    async function loadSites() {
      try {
        const res = await getSites()
        if (!ignore) setSites(res.data)
      } catch {
        // sites unavailable — dashboard still works in global mode
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
        const [statsRes, stockRes, relocRes, logsRes, delRes] = await Promise.all([
          getDashboardStats(selectedSiteId),
          getDashboardStock(selectedSiteId),
          getDashboardRelocations(selectedSiteId),
          getDashboardLogs(selectedSiteId),
          getDeletionReasonStats(),
        ])
        if (!ignore) {
          setStats(statsRes.data)
          setStockData(stockRes.data)
          setRelocationData(relocRes.data)
          setLogs(logsRes.data)
          setDeletionStats(delRes.data)
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
          aria-label="Botanist dashboard"
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

      <PageTitle label="Botanist" onBack={handleLogout} />

      <section className="home-body">
        <BotanistSidebar
          onSiteChange={setSelectedSiteId}
          selectedSiteId={selectedSiteId}
          sites={sites}
        />

        <div className="dashboard-grid">
          <StockBarChart data={stockData} loading={loadingData} />

          <StatCard label="Varieties" loading={loadingData} onClick={() => navigate('/botanist/varieties')} value={stats?.varietiesCount} />
          <StatCard label="Nursery sites" loading={loadingData} value={stats?.nurserySitesCount} />
          <StatCard label="Total plants" loading={loadingData} value={stats?.totalPlants} />

          <LogsTable loading={loadingData} logs={logs} searchTerm={searchTerm} />

          <StatCard label="Active relocations" loading={loadingData} value={stats?.activeRelocations} />
          <RelocationChart data={relocationData} loading={loadingData} />
          <DeletionReasonChart data={deletionStats} loading={loadingData} />
        </div>
      </section>
    </main>
  )
}

export default BotanistDashboardPage
