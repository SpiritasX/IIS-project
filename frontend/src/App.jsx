import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/routing/ProtectedRoute'
import AuthProvider from './contexts/AuthProvider'
import CartProvider from './contexts/CartProvider'
import AdminDashboardPage from './pages/AdminDashboardPage'
import AddAdminPlantsPage from './pages/AddAdminPlantsPage'
import AddNurserySitePage from './pages/AddNurserySitePage'
import BotanistDashboardPage from './pages/BotanistDashboardPage'
import VarietiesPage from './pages/VarietiesPage'
import WorkerDashboardPage from './pages/WorkerDashboardPage'
import AddPlantsPage from './pages/AddPlantsPage'
import StorageSpacesPage from './pages/StorageSpacesPage'
import AddStorageSpacePage from './pages/AddStorageSpacePage'
import EditStorageSpacePage from './pages/EditStorageSpacePage'
import CartPage from './pages/CartPage'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import ProfilePage from './pages/ProfilePage'
import RequestsPage from './pages/RequestsPage'
import SignUpPage from './pages/SignUpPage'
import * as locationsApi from './api/locations'
import * as adminLocationsApi from './api/adminLocations'

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
                <AdminDashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/plants"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AddAdminPlantsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/nursery-sites/new"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AddNurserySitePage api={adminLocationsApi} />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/locations"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <StorageSpacesPage api={adminLocationsApi} basePath="/admin" />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/locations/new"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AddStorageSpacePage api={adminLocationsApi} basePath="/admin" />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/locations/:id"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <EditStorageSpacePage api={adminLocationsApi} basePath="/admin" />
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
          <Route
            path="/worker/locations"
            element={
              <ProtectedRoute allowedRoles={['WORKER']}>
                <StorageSpacesPage api={locationsApi} basePath="/worker" />
              </ProtectedRoute>
            }
          />
          <Route
            path="/worker/locations/new"
            element={
              <ProtectedRoute allowedRoles={['WORKER']}>
                <AddStorageSpacePage api={locationsApi} basePath="/worker" />
              </ProtectedRoute>
            }
          />
          <Route
            path="/worker/locations/:id"
            element={
              <ProtectedRoute allowedRoles={['WORKER']}>
                <EditStorageSpacePage api={locationsApi} basePath="/worker" />
              </ProtectedRoute>
            }
          />
        </Routes>
      </CartProvider>
    </AuthProvider>
  )
}

export default App
