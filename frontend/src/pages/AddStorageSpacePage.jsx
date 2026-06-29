import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import AdminSidebar from '../components/admin/AdminSidebar'
import WorkerSidebar from '../components/worker/WorkerSidebar'
import PageTitle from '../components/home/PageTitle'
import SearchBar from '../components/home/SearchBar'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/worker.css'
import '../styles/varieties.css'

const EMPTY_FORM = { name: '', type: '', nurserySiteId: '' }

function AddStorageSpacePage({ api, basePath }) {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [sites, setSites] = useState([])
  const [spaceTypes, setSpaceTypes] = useState([])
  const [form, setForm] = useState(EMPTY_FORM)
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')

  useEffect(() => {
    let ignore = false
    Promise.all([api.getSites(), api.getSpaceTypes()])
      .then(([sitesRes, typesRes]) => {
        if (!ignore) {
          setSites(sitesRes.data)
          setSpaceTypes(typesRes.data)
        }
      })
      .catch(() => {})
    return () => { ignore = true }
  }, [api])

  function handleField(e) {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
    setFormError('')
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!form.name.trim()) { setFormError('Name is required.'); return }
    if (!form.type.trim()) { setFormError('Type is required.'); return }
    if (!form.nurserySiteId) { setFormError('Nursery site is required.'); return }

    setSubmitting(true)
    try {
      await api.createLocation({
        name: form.name.trim(),
        type: form.type.trim(),
        nurserySiteId: Number(form.nurserySiteId),
      })
      navigate(`${basePath}/locations`)
    } catch (err) {
      setFormError(err.response?.data?.message ?? 'Failed to create storage space.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  const Sidebar = basePath === '/admin' ? AdminSidebar : WorkerSidebar

  return (
    <main className="home-page">
      <header className="home-header botanist-header">
        <button
          aria-label="Back to locations"
          className="home-logo-placeholder"
          onClick={() => navigate(`${basePath}/locations`)}
          type="button"
        >
          <span aria-hidden="true" />
        </button>
        <div className="home-header-controls">
          <SearchBar onChange={() => {}} onClear={() => {}} value="" />
        </div>
      </header>

      <PageTitle label="Add Storage Space" onBack={handleLogout} />

      <section className="home-body">
        <Sidebar onSiteChange={() => {}} selectedSiteId={null} sites={[]} />

        <div className="varieties-content">
          <section className="variety-form-card" aria-labelledby="add-location-heading">
            <h2 className="variety-form-heading" id="add-location-heading">
              New storage space
            </h2>

            <form className="variety-form" noValidate onSubmit={handleSubmit}>
              <div className="variety-form-row variety-form-row-3">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="name">
                    Name *
                  </label>
                  <input
                    className="variety-input"
                    id="name"
                    name="name"
                    onChange={handleField}
                    placeholder="e.g. North Greenhouse"
                    required
                    type="text"
                    value={form.name}
                  />
                </div>

                <div className="variety-field">
                  <label className="variety-label" htmlFor="type">
                    Type *
                  </label>
                  <select
                    className="variety-select"
                    id="type"
                    name="type"
                    onChange={handleField}
                    required
                    value={form.type}
                  >
                    <option value="">Select type</option>
                    {spaceTypes.map((t) => (
                      <option key={t.id} value={t.name}>{t.name}</option>
                    ))}
                  </select>
                </div>

                <div className="variety-field">
                  <label className="variety-label" htmlFor="nurserySiteId">
                    Nursery site *
                  </label>
                  <select
                    className="variety-select"
                    id="nurserySiteId"
                    name="nurserySiteId"
                    onChange={handleField}
                    required
                    value={form.nurserySiteId}
                  >
                    <option value="">Select nursery site</option>
                    {sites.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {formError && (
                <p className="variety-form-error" role="alert">
                  {formError}
                </p>
              )}

              <div className="variety-form-actions">
                <button
                  className="variety-submit-button"
                  disabled={submitting}
                  type="submit"
                >
                  {submitting ? 'Saving…' : 'Create storage space'}
                </button>
              </div>
            </form>
          </section>
        </div>
      </section>
    </main>
  )
}

export default AddStorageSpacePage
