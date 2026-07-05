import { useLocation, useNavigate } from 'react-router-dom'

const sidebarItems = [
  { label: 'Profile', path: '/botanist' },
  { label: 'Requests', path: '/botanist' },
  { label: 'Reports', path: '/botanist' },
  { label: 'Varieties', path: '/botanist/varieties' },
  { label: 'Plants', path: '/botanist/plants' },
  { label: 'Health logs', path: '/botanist/health-logs' },
  { label: 'Relocation logs', path: '/botanist' },
]

function BotanistSidebar({ sites, selectedSiteId, onSiteChange }) {
  const location = useLocation()
  const navigate = useNavigate()

  return (
    <aside className="user-sidebar botanist-sidebar" aria-label="Botanist navigation">
      <div className="botanist-location-wrap">
        <select
          aria-label="Select nursery site"
          className="botanist-location-select"
          onChange={(e) => onSiteChange(e.target.value ? Number(e.target.value) : null)}
          value={selectedSiteId ?? ''}
        >
          <option value="">All sites</option>
          {sites.map((site) => (
            <option key={site.id} value={site.id}>
              {site.name}
            </option>
          ))}
        </select>
      </div>

      {sidebarItems.map((item) => (
        <button
          className={`sidebar-button${location.pathname === item.path ? ' sidebar-button-active' : ''}`}
          key={item.label}
          onClick={() => navigate(item.path)}
          type="button"
        >
          {item.label}
        </button>
      ))}
    </aside>
  )
}

export default BotanistSidebar
