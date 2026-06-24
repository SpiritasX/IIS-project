import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/routing/ProtectedRoute'
import AuthProvider from './contexts/AuthProvider'
import CartProvider from './contexts/CartProvider'
import CartPage from './pages/CartPage'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import ProfilePage from './pages/ProfilePage'
import RequestsPage from './pages/RequestsPage'
import RoleDashboardPage from './pages/RoleDashboardPage'
import SignUpPage from './pages/SignUpPage'
import StaffRequestsPage from './pages/StaffRequestsPage'

function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignUpPage />} />
          <Route
            path="/home"
            element={
              <ProtectedRoute allowedRoles={['CUSTOMER']}>
                <HomePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/cart"
            element={
              <ProtectedRoute allowedRoles={['CUSTOMER']}>
                <CartPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute allowedRoles={['CUSTOMER']}>
                <ProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/requests"
            element={
              <ProtectedRoute allowedRoles={['CUSTOMER']}>
                <RequestsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <RoleDashboardPage
                  actions={[{ label: 'Requests', path: '/admin/requests' }]}
                  subtitle="Review all customer requests and cancel active sales processes when needed."
                  title="Admin dashboard"
                />
              </ProtectedRoute>
            }
          />
          <Route
            path="/botanist"
            element={
              <ProtectedRoute allowedRoles={['BOTANIST']}>
                <RoleDashboardPage
                  subtitle="Botanist workspace foundations are ready for future plant-care features."
                  title="Botanist dashboard"
                />
              </ProtectedRoute>
            }
          />
          <Route
            path="/worker"
            element={
              <ProtectedRoute allowedRoles={['WORKER']}>
                <RoleDashboardPage
                  actions={[{ label: 'Requests', path: '/worker/requests' }]}
                  subtitle="Prepare reserved requests, mark them ready, and start delivery handoff."
                  title="Worker dashboard"
                />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/requests"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <StaffRequestsPage
                  backPath="/admin"
                  emptyMessage="No requests found."
                  title="Admin Requests"
                  variant="admin"
                />
              </ProtectedRoute>
            }
          />
          <Route
            path="/worker/requests"
            element={
              <ProtectedRoute allowedRoles={['WORKER']}>
                <StaffRequestsPage
                  backPath="/worker"
                  emptyMessage="No reservations are ready for worker action."
                  title="Worker Requests"
                  variant="worker"
                />
              </ProtectedRoute>
            }
          />
        </Routes>
      </CartProvider>
    </AuthProvider>
  )
}

export default App
