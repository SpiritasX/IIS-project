import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAllPlants } from '../api/varieties'
import BotanistSidebar from '../components/botanist/BotanistSidebar'
import PageTitle from '../components/home/PageTitle'
import SearchBar from '../components/home/SearchBar'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/varieties.css'

function BotanistPlantsPage() {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [plants, setPlants] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [expandedId, setExpandedId] = useState(null)

  useEffect(() => {
    let ignore = false
    getAllPlants()
      .then((res) => { if (!ignore) setPlants(res.data) })
      .catch(() => { if (!ignore) setError('Failed to load plants.') })
      .finally(() => { if (!ignore) setLoading(false) })
    return () => { ignore = true }
  }, [])

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

      <PageTitle label="Plants" onBack={handleLogout} />

      <section className="home-body">
        <BotanistSidebar onSiteChange={() => {}} selectedSiteId={null} sites={[]} />

        <div className="varieties-content">
          <section className="varieties-list-card" aria-labelledby="plants-heading">
            <h2 className="variety-form-heading" id="plants-heading">
              All plants <span className="varieties-count">({filtered.length})</span>
            </h2>

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
                                <DetailField label="Subcategory" value={p.typeName} />
                                <DetailField label="Sector" value={p.sectorName} />
                                <DetailField label="Storage type" value={p.storageSpaceTypeName} />
                                <DetailField label="Condition" value={p.state != null ? `${p.state}/5` : null} />
                                <div>
                                  <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Part of life cycle: </span>
                                  <span>{p.lifecycleStage ?? '—'}</span>
                                </div>
                                <DetailField label="Hatching date" value={p.hatchingDate} />
                                <DetailField label="Color" value={p.color} />
                                <DetailField label="Height (cm)" value={p.height} />
                                <DetailField label="Humidity" value={p.humidity != null ? `${p.humidity}%` : null} />
                                <DetailField label="Soil type" value={p.soil} />
                              </div>
                              {(p.conditionDescription || p.careInstructions || p.description) && (
                                <div style={{ marginTop: 10, fontSize: 13 }}>
                                  {p.conditionDescription && (
                                    <div>
                                      <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Condition notes: </span>
                                      {p.conditionDescription}
                                    </div>
                                  )}
                                  {p.careInstructions && (
                                    <div style={{ marginTop: 4 }}>
                                      <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Care instructions: </span>
                                      {p.careInstructions}
                                    </div>
                                  )}
                                  {p.description && (
                                    <div style={{ marginTop: 4 }}>
                                      <span style={{ fontWeight: 600, color: 'var(--color-text-secondary)' }}>Description: </span>
                                      {p.description}
                                    </div>
                                  )}
                                </div>
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

export default BotanistPlantsPage
