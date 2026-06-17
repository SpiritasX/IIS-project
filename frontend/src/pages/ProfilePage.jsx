import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/client'
import HomeHeader from '../components/home/HomeHeader'
import PageTitle from '../components/home/PageTitle'
import UserSidebar from '../components/home/UserSidebar'
import ProfileField from '../components/profile/ProfileField'
import ProfilePanel from '../components/profile/ProfilePanel'
import { useAuth } from '../hooks/useAuth'
import { useCart } from '../hooks/useCart'
import '../styles/home.css'
import '../styles/profile.css'

const emptyStatus = { message: '', type: '' }
const noop = () => undefined

function messageFor(error, fallback) {
  return (
    error.response?.data?.detail ||
    error.response?.data?.message ||
    error.response?.data?.error ||
    fallback
  )
}

function personalFormFor(user) {
  return {
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    phoneNumber: user?.phoneNumber || '',
  }
}

function addressFormFor(user) {
  return {
    address: user?.addressLine || '',
    city: user?.city || '',
    country: user?.country || '',
    zipCode: user?.zipCode || '',
  }
}

function ProfilePage() {
  const navigate = useNavigate()
  const { updateUser, user } = useAuth()
  const { cartCount } = useCart()
  const [searchTerm, setSearchTerm] = useState('')
  const [filterOpen, setFilterOpen] = useState(false)
  const [personalForm, setPersonalForm] = useState(personalFormFor(user))
  const [addressForm, setAddressForm] = useState(addressFormFor(user))
  const [personalStatus, setPersonalStatus] = useState(emptyStatus)
  const [addressStatus, setAddressStatus] = useState(emptyStatus)

  function handleFormChange(setForm) {
    return (event) => {
      const { name, value } = event.target
      setForm((current) => ({
        ...current,
        [name]: value,
      }))
    }
  }

  async function submitProfileSection(event, endpoint, form, setStatus, resetForm) {
    event.preventDefault()
    setStatus(emptyStatus)

    try {
      const response = await api.patch(endpoint, form)
      updateUser(response.data)
      setStatus({ message: 'Saved', type: 'success' })

      if (resetForm) {
        resetForm()
      }
    } catch (error) {
      setStatus({ message: messageFor(error, 'Unable to save changes'), type: 'error' })
    }
  }

  return (
    <main className="home-page profile-page">
      <HomeHeader
        cartCount={cartCount}
        category="All"
        filterOpen={filterOpen}
        onCategoryChange={noop}
        onClearSearch={() => setSearchTerm('')}
        onResetFilters={() => setFilterOpen(false)}
        onSearchChange={(event) => setSearchTerm(event.target.value)}
        onSortChange={noop}
        onToggleFilter={() => setFilterOpen((current) => !current)}
        searchTerm={searchTerm}
        sort="featured"
      />

      <PageTitle label="Profile" onBack={() => navigate('/home')} />

      <section className="profile-shell">
        <UserSidebar />

        <section className="profile-grid" aria-label="Profile editor">
          <ProfilePanel
            message={personalStatus.message}
            onSubmit={(event) =>
              submitProfileSection(
                event,
                '/auth/profile/personal',
                personalForm,
                setPersonalStatus,
              )
            }
            status={personalStatus.type}
            title="Personal data"
          >
            <ProfileField
              label="First name"
              name="firstName"
              onChange={handleFormChange(setPersonalForm)}
              type="text"
              value={personalForm.firstName}
            />
            <ProfileField
              label="Last name"
              name="lastName"
              onChange={handleFormChange(setPersonalForm)}
              type="text"
              value={personalForm.lastName}
            />
            <ProfileField
              label="Phone number"
              name="phoneNumber"
              onChange={handleFormChange(setPersonalForm)}
              type="tel"
              value={personalForm.phoneNumber}
            />
          </ProfilePanel>

          <ProfilePanel
            message={addressStatus.message}
            onSubmit={(event) =>
              submitProfileSection(
                event,
                '/auth/profile/address',
                addressForm,
                setAddressStatus,
              )
            }
            status={addressStatus.type}
            title="Address"
          >
            <ProfileField
              label="Country"
              name="country"
              onChange={handleFormChange(setAddressForm)}
              type="text"
              value={addressForm.country}
            />
            <ProfileField
              label="City"
              name="city"
              onChange={handleFormChange(setAddressForm)}
              type="text"
              value={addressForm.city}
            />
            <ProfileField
              label="Address"
              name="address"
              onChange={handleFormChange(setAddressForm)}
              type="text"
              value={addressForm.address}
            />
            <ProfileField
              label="Zip code"
              name="zipCode"
              onChange={handleFormChange(setAddressForm)}
              type="text"
              value={addressForm.zipCode}
            />
          </ProfilePanel>
        </section>
      </section>
    </main>
  )
}

export default ProfilePage
