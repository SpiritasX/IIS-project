import { useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'

function RoleDashboardPage({ actions = [], title, subtitle }) {
  const navigate = useNavigate()
  const { logout, user } = useAuth()

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <main className="home-page role-page">
      <section className="role-shell" aria-labelledby="role-page-title">
        <p className="role-eyebrow">{user?.role}</p>
        <h1 id="role-page-title">{title}</h1>
        <p>{subtitle}</p>
        <div className="role-actions">
          {actions.map((action) => (
            <button
              className="role-primary-button"
              key={action.label}
              onClick={() => navigate(action.path)}
              type="button"
            >
              {action.label}
            </button>
          ))}
          <button className="role-logout-button" onClick={handleLogout} type="button">
            Log out
          </button>
        </div>
      </section>
    </main>
  )
}

export default RoleDashboardPage
