import { Navigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import '../../styles/home.css'

function ProtectedRoute({ children }) {
  const { authLoading, isAuthenticated } = useAuth()

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

  return children
}

export default ProtectedRoute
