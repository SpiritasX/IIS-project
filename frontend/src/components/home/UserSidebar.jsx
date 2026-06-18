import { useLocation, useNavigate } from 'react-router-dom'

const sidebarItems = [
  { activePath: '/home', label: 'Home', path: '/home' },
  { activePath: '/planner', label: 'Planner', path: '/planner' },
  { activePath: '/profile', label: 'Profile', path: '/profile' },
  { activePath: '/requests', label: 'Requests', path: '/requests' },
  { activePath: '/reports', label: 'Reports', path: '/home' },
]

function UserSidebar() {
  const location = useLocation()
  const navigate = useNavigate()

  return (
    <aside className="user-sidebar" aria-label="User navigation">
      {sidebarItems.map((item) => (
        <button
          className={`sidebar-button${location.pathname === item.activePath ? ' sidebar-button-active' : ''}`}
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

export default UserSidebar
