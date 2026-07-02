import { useLocation, useNavigate } from 'react-router-dom'

const sidebarItems = [
  { label: 'Profile', path: '/worker' },
  { label: 'Requests', path: '/worker/requests' },
  { label: 'Reports', path: '/worker' },
  { label: 'Locations', path: '/worker/locations' },
  { label: 'Plants', path: '/worker/plants' },
  { label: 'Health logs', path: '/worker/health-logs' },
  { label: 'Relocation logs', path: '/worker/relocation-logs' },
]

function WorkerSidebar({ sites, selectedSiteId, onSiteChange }) {
  const location = useLocation()
  const navigate = useNavigate()

  return (
    <aside className="user-sidebar botanist-sidebar" aria-label="Worker navigation">
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

export default WorkerSidebar
