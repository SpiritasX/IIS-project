import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/routing/ProtectedRoute'
import AuthProvider from './contexts/AuthProvider'
import CartProvider from './contexts/CartProvider'
import AdminOrderAnalysisPage from './pages/AdminOrderAnalysisPage'
import AdminDashboardPage from './pages/AdminDashboardPage'
import AdminPlantsPage from './pages/AdminPlantsPage'
import AddAdminPlantsPage from './pages/AddAdminPlantsPage'
import AddNurserySitePage from './pages/AddNurserySitePage'
import BotanistDashboardPage from './pages/BotanistDashboardPage'
import BotanistPlantsPage from './pages/BotanistPlantsPage'
import VarietiesPage from './pages/VarietiesPage'
import WorkerDashboardPage from './pages/WorkerDashboardPage'
import WorkerPlantsPage from './pages/WorkerPlantsPage'
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
import StaffRequestsPage from './pages/StaffRequestsPage'
import * as adminLocationsApi from './api/adminLocations'
import * as locationsApi from './api/locations'

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
                <AdminPlantsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/plants/add"
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
            path="/botanist/plants"
            element={
              <ProtectedRoute allowedRoles={['BOTANIST']}>
                <BotanistPlantsPage />
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
                <WorkerPlantsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/worker/plants/add"
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
            path="/admin/order-analysis"
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AdminOrderAnalysisPage />
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
