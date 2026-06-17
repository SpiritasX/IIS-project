import { Navigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { homePathForRole } from '../../utils/roleRoutes'
import '../../styles/home.css'

function ProtectedRoute({ allowedRoles, children }) {
  const { authLoading, isAuthenticated, user } = useAuth()

  if (authLoading) {
    return (
      <main className="home-page">
        <div className="product-empty">Checking session...</div>
      </main>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  if (allowedRoles && !allowedRoles.includes(user?.role)) {
    return <Navigate to={homePathForRole(user?.role)} replace />
  }

  return children
}

export default ProtectedRoute
