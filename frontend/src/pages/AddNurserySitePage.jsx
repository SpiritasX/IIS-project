import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import AdminSidebar from '../components/admin/AdminSidebar'
import PageTitle from '../components/home/PageTitle'
import SearchBar from '../components/home/SearchBar'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/worker.css'
import '../styles/varieties.css'

const EMPTY_FORM = {
  name: '',
  address: '',
  spaceName: '',
  spaceType: '',
  sectorName: '',
  sectorCapacity: '',
}

function AddNurserySitePage({ api }) {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [form, setForm] = useState(EMPTY_FORM)
  const [spaceTypes, setSpaceTypes] = useState([])
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')

  useEffect(() => {
    api.getSpaceTypes()
      .then((res) => setSpaceTypes(res.data))
      .catch(() => {})
  }, [api])

  function handleField(e) {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
    setFormError('')
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!form.name.trim()) { setFormError('Nursery site name is required.'); return }
    if (!form.address.trim()) { setFormError('Address is required.'); return }
    if (!form.spaceName.trim()) { setFormError('Storage space name is required.'); return }
    if (!form.spaceType.trim()) { setFormError('Storage space type is required.'); return }

    setSubmitting(true)
    try {
      await api.createNurserySite({
        name: form.name.trim(),
        address: form.address.trim(),
        spaceName: form.spaceName.trim(),
        spaceType: form.spaceType.trim(),
        sectorName: form.sectorName.trim() || null,
        sectorCapacity: form.sectorCapacity ? Number(form.sectorCapacity) : null,
      })
      navigate('/admin/locations')
    } catch (err) {
      setFormError(err.response?.data?.message ?? 'Failed to create nursery site.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <main className="home-page">
      <header className="home-header botanist-header">
        <button
          aria-label="Back to locations"
          className="home-logo-placeholder"
          onClick={() => navigate('/admin/locations')}
          type="button"
        >
          <span aria-hidden="true" />
        </button>
        <div className="home-header-controls">
          <SearchBar onChange={() => {}} onClear={() => {}} value="" />
        </div>
      </header>

      <PageTitle label="Add Nursery Site" onBack={handleLogout} />

      <section className="home-body">
        <AdminSidebar onSiteChange={() => {}} selectedSiteId={null} sites={[]} />

        <div className="varieties-content">
          {/* Nursery site fields */}
          <section className="variety-form-card" aria-labelledby="add-site-heading">
            <h2 className="variety-form-heading" id="add-site-heading">
              New nursery site
            </h2>

            <form className="variety-form" noValidate onSubmit={handleSubmit}>
              <div className="variety-form-row variety-form-row-2">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="name">Name *</label>
                  <input
                    className="variety-input"
                    id="name"
                    name="name"
                    onChange={handleField}
                    placeholder="e.g. Novi Sad Nursery"
                    required
                    type="text"
                    value={form.name}
                  />
                </div>
                <div className="variety-field">
                  <label className="variety-label" htmlFor="address">Address *</label>
                  <input
                    className="variety-input"
                    id="address"
                    name="address"
                    onChange={handleField}
                    placeholder="e.g. Bulevar Oslobođenja 12, Novi Sad"
                    required
                    type="text"
                    value={form.address}
                  />
                </div>
              </div>

              {/* Storage space fields */}
              <h3 className="variety-label" style={{ marginTop: 8, marginBottom: 12, fontSize: 15 }}>
                Initial storage space
              </h3>
              <div className="variety-form-row variety-form-row-2">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="spaceName">Storage space name *</label>
                  <input
                    className="variety-input"
                    id="spaceName"
                    name="spaceName"
                    onChange={handleField}
                    placeholder="e.g. Main Greenhouse"
                    required
                    type="text"
                    value={form.spaceName}
                  />
                </div>
                <div className="variety-field">
                  <label className="variety-label" htmlFor="spaceType">Storage space type *</label>
                  <select
                    className="variety-select"
                    id="spaceType"
                    name="spaceType"
                    onChange={handleField}
                    required
                    value={form.spaceType}
                  >
                    <option value="">Select type</option>
                    {spaceTypes.map((t) => (
                      <option key={t.id} value={t.name}>{t.name}</option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Sector fields (optional) */}
              <h3 className="variety-label" style={{ marginTop: 8, marginBottom: 12, fontSize: 15 }}>
                Initial sector <span style={{ fontWeight: 400, color: 'var(--color-text-secondary)' }}>(optional)</span>
              </h3>
              <div className="variety-form-row variety-form-row-2">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="sectorName">Sector name</label>
                  <input
                    className="variety-input"
                    id="sectorName"
                    name="sectorName"
                    onChange={handleField}
                    placeholder="e.g. Zone A"
                    type="text"
                    value={form.sectorName}
                  />
                </div>
                <div className="variety-field">
                  <label className="variety-label" htmlFor="sectorCapacity">Capacity</label>
                  <input
                    className="variety-input"
                    id="sectorCapacity"
                    min="1"
                    name="sectorCapacity"
                    onChange={handleField}
                    placeholder="e.g. 500"
                    type="number"
                    value={form.sectorCapacity}
                  />
                </div>
              </div>

              {formError && <p className="variety-form-error" role="alert">{formError}</p>}

              <div className="variety-form-actions">
                <button className="variety-submit-button" disabled={submitting} type="submit">
                  {submitting ? 'Saving…' : 'Create nursery site'}
                </button>
              </div>
            </form>
          </section>
        </div>
      </section>
    </main>
  )
}

export default AddNurserySitePage
