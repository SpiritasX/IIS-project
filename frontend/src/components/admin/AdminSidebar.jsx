import { useLocation, useNavigate } from 'react-router-dom'

const sidebarItems = [
  { label: 'Profile', path: '/admin' },
  { label: 'Requests', path: '/admin/requests' },
  { label: 'Reports', path: '/admin/order-analysis' },
  { label: 'Locations', path: '/admin/locations' },
  { label: 'Plants', path: '/admin/plants' },
  { label: 'Health logs', path: '/admin/health-logs' },
  { label: 'Relocation logs', path: '/admin/relocation-logs' },
]

function AdminSidebar({ sites, selectedSiteId, onSiteChange }) {
  const location = useLocation()
  const navigate = useNavigate()

  return (
    <aside className="user-sidebar botanist-sidebar" aria-label="Admin navigation">
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

export default AdminSidebar
