import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/routing/ProtectedRoute'
import AuthProvider from './contexts/AuthProvider'
import CartProvider from './contexts/CartProvider'
import BotanistDashboardPage from './pages/BotanistDashboardPage'
import VarietiesPage from './pages/VarietiesPage'
import WorkerDashboardPage from './pages/WorkerDashboardPage'
import AddPlantsPage from './pages/AddPlantsPage'
import CartPage from './pages/CartPage'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import ProfilePage from './pages/ProfilePage'
import RequestsPage from './pages/RequestsPage'
import RoleDashboardPage from './pages/RoleDashboardPage'
import SignUpPage from './pages/SignUpPage'

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
                  subtitle="Customer management and process dashboards will be added in the next parts."
                  title="Admin dashboard"
                />
              </ProtectedRoute>
            }
          />
          <Route
            path="/botanist"
            element={
              <ProtectedRoute allowedRoles={['BOTANIST']}>
                <BotanistDashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/botanist/varieties"
            element={
              <ProtectedRoute allowedRoles={['BOTANIST']}>
                <VarietiesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/worker"
            element={
              <ProtectedRoute allowedRoles={['WORKER']}>
                <WorkerDashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/worker/plants"
            element={
              <ProtectedRoute allowedRoles={['WORKER']}>
                <AddPlantsPage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </CartProvider>
    </AuthProvider>
  )
}

export default App
