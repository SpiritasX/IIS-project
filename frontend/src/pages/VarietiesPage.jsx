import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  addVariety,
  getCategories,
  getLocationTypes,
  getSpeciesByType,
  getTypesByCategory,
  getVarieties,
} from '../api/varieties'
import BotanistSidebar from '../components/botanist/BotanistSidebar'
import PageTitle from '../components/home/PageTitle'
import SearchBar from '../components/home/SearchBar'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/varieties.css'

const EMPTY_FORM = {
  name: '',
  humidity: '',
  soil: '',
  instructions: '',
  categoryId: '',
  typeId: '',
  speciesId: '',
  locationTypeId: '',
}

function VarietiesPage() {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [varieties, setVarieties] = useState([])
  const [categories, setCategories] = useState([])
  const [locationTypes, setLocationTypes] = useState([])
  const [types, setTypes] = useState([])
  const [species, setSpecies] = useState([])

  const [form, setForm] = useState(EMPTY_FORM)
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')
  const [formSuccess, setFormSuccess] = useState(false)

  const [searchTerm, setSearchTerm] = useState('')
  const [loadingList, setLoadingList] = useState(true)

  useEffect(() => {
    let ignore = false

    async function load() {
      try {
        const [varRes, catRes, locRes] = await Promise.all([getVarieties(), getCategories(), getLocationTypes()])
        if (!ignore) {
          setVarieties(varRes.data)
          setCategories(catRes.data)
          setLocationTypes(locRes.data)
        }
      } catch {
        // leave empty
      } finally {
        if (!ignore) setLoadingList(false)
      }
    }

    load()
    return () => { ignore = true }
  }, [])

  useEffect(() => {
    if (!form.categoryId) {
      setTypes([])
      setSpecies([])
      setForm((prev) => ({ ...prev, typeId: '', speciesId: '' }))
      return
    }

    let ignore = false
    getTypesByCategory(form.categoryId).then((res) => {
      if (!ignore) {
        setTypes(res.data)
        setSpecies([])
        setForm((prev) => ({ ...prev, typeId: '', speciesId: '' }))
      }
    })
    return () => { ignore = true }
  }, [form.categoryId])

  useEffect(() => {
    if (!form.typeId) {
      setSpecies([])
      setForm((prev) => ({ ...prev, speciesId: '' }))
      return
    }

    let ignore = false
    getSpeciesByType(form.typeId).then((res) => {
      if (!ignore) {
        setSpecies(res.data)
        setForm((prev) => ({ ...prev, speciesId: '' }))
      }
    })
    return () => { ignore = true }
  }, [form.typeId])

  function handleField(e) {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
    setFormError('')
    setFormSuccess(false)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!form.speciesId) {
      setFormError('Please select a category, type and species.')
      return
    }
    if (!form.locationTypeId) {
      setFormError('Please select a location type.')
      return
    }
    if (!form.name.trim()) {
      setFormError('Variety name is required.')
      return
    }

    setSubmitting(true)
    setFormError('')

    try {
      const res = await addVariety({
        name: form.name.trim(),
        humidity: form.humidity ? Number(form.humidity) : null,
        soil: form.soil.trim() || null,
        instructions: form.instructions.trim() || null,
        speciesId: Number(form.speciesId),
        locationTypeId: Number(form.locationTypeId),
      })
      setVarieties((prev) => [...prev, res.data])
      setForm(EMPTY_FORM)
      setTypes([])
      setSpecies([])
      setFormSuccess(true)
      setTimeout(() => setFormSuccess(false), 4000)
    } catch (err) {
      setFormError(err.response?.data?.message ?? 'Failed to add variety. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  const filtered = searchTerm
    ? varieties.filter(
        (v) =>
          v.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
          v.speciesName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          v.categoryName?.toLowerCase().includes(searchTerm.toLowerCase()),
      )
    : varieties

  return (
    <main className="home-page">
      <header className="home-header botanist-header">
        <button
          aria-label="Back to dashboard"
          className="home-logo-placeholder"
          onClick={() => navigate('/botanist')}
          type="button"
        >
          <span aria-hidden="true" />
        </button>

        <div className="home-header-controls">
          <SearchBar
            onChange={(e) => setSearchTerm(e.target.value)}
            onClear={() => setSearchTerm('')}
            value={searchTerm}
          />
        </div>
      </header>

      <PageTitle label="Varieties" onBack={handleLogout} />

      <section className="home-body">
        <BotanistSidebar onSiteChange={() => {}} selectedSiteId={null} sites={[]} />

        <div className="varieties-content">
          {/* ── Add variety form ── */}
          <section className="variety-form-card" aria-labelledby="add-variety-heading">
            <h2 className="variety-form-heading" id="add-variety-heading">
              Add new variety
            </h2>

            <form className="variety-form" noValidate onSubmit={handleSubmit}>
              {/* Taxonomy dropdowns */}
              <div className="variety-form-row variety-form-row-4">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="categoryId">
                    Category
                  </label>
                  <select
                    className="variety-select"
                    id="categoryId"
                    name="categoryId"
                    onChange={handleField}
                    required
                    value={form.categoryId}
                  >
                    <option value="">Select category</option>
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="variety-field">
                  <label className="variety-label" htmlFor="typeId">
                    Type (potkategorija)
                  </label>
                  <select
                    className="variety-select"
                    disabled={!form.categoryId}
                    id="typeId"
                    name="typeId"
                    onChange={handleField}
                    required
                    value={form.typeId}
                  >
                    <option value="">Select type</option>
                    {types.map((t) => (
                      <option key={t.id} value={t.id}>
                        {t.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="variety-field">
                  <label className="variety-label" htmlFor="speciesId">
                    Species (vrsta)
                  </label>
                  <select
                    className="variety-select"
                    disabled={!form.typeId}
                    id="speciesId"
                    name="speciesId"
                    onChange={handleField}
                    required
                    value={form.speciesId}
                  >
                    <option value="">Select species</option>
                    {species.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="variety-field">
                  <label className="variety-label" htmlFor="locationTypeId">
                    Location type (tip lokacije) *
                  </label>
                  <select
                    className="variety-select"
                    id="locationTypeId"
                    name="locationTypeId"
                    onChange={handleField}
                    required
                    value={form.locationTypeId}
                  >
                    <option value="">Select location type</option>
                    {locationTypes.map((l) => (
                      <option key={l.id} value={l.id}>
                        {l.name} — {l.type}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Variety details */}
              <div className="variety-form-row variety-form-row-2">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="name">
                    Variety name (sorta) *
                  </label>
                  <input
                    className="variety-input"
                    id="name"
                    name="name"
                    onChange={handleField}
                    placeholder="e.g. English lavender"
                    required
                    type="text"
                    value={form.name}
                  />
                </div>

                <div className="variety-field">
                  <label className="variety-label" htmlFor="humidity">
                    Humidity (%)
                  </label>
                  <input
                    className="variety-input"
                    id="humidity"
                    max="100"
                    min="0"
                    name="humidity"
                    onChange={handleField}
                    placeholder="e.g. 60"
                    step="1"
                    type="number"
                    value={form.humidity}
                  />
                </div>
              </div>

              <div className="variety-form-row">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="soil">
                    Soil type
                  </label>
                  <input
                    className="variety-input"
                    id="soil"
                    name="soil"
                    onChange={handleField}
                    placeholder="e.g. Well-drained alkaline soil"
                    type="text"
                    value={form.soil}
                  />
                </div>
              </div>

              <div className="variety-form-row">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="instructions">
                    Care instructions
                  </label>
                  <textarea
                    className="variety-textarea"
                    id="instructions"
                    name="instructions"
                    onChange={handleField}
                    placeholder="Describe care guidelines for this variety…"
                    rows={3}
                    value={form.instructions}
                  />
                </div>
              </div>

              {formError && <p className="variety-form-error" role="alert">{formError}</p>}
              {formSuccess && (
                <p className="variety-form-success" role="status">
                  Variety added successfully.
                </p>
              )}

              <div className="variety-form-actions">
                <button
                  className="variety-submit-button"
                  disabled={submitting}
                  type="submit"
                >
                  {submitting ? 'Saving…' : 'Add variety'}
                </button>
              </div>
            </form>
          </section>

          {/* ── Varieties list ── */}
          <section aria-labelledby="varieties-list-heading" className="varieties-list-card">
            <h2 className="variety-form-heading" id="varieties-list-heading">
              Registered varieties{' '}
              <span className="varieties-count">({filtered.length})</span>
            </h2>

            {loadingList ? (
              <p className="varieties-empty">Loading varieties…</p>
            ) : filtered.length === 0 ? (
              <p className="varieties-empty">
                {searchTerm ? 'No varieties match your search.' : 'No varieties registered yet.'}
              </p>
            ) : (
              <table className="varieties-table">
                <thead>
                  <tr>
                    <th>Variety (sorta)</th>
                    <th>Species (vrsta)</th>
                    <th>Type (potkategorija)</th>
                    <th>Category</th>
                    <th>Humidity</th>
                    <th>Soil</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((v) => (
                    <tr key={v.id}>
                      <td className="varieties-name">{v.name}</td>
                      <td>{v.speciesName}</td>
                      <td>{v.typeName}</td>
                      <td>
                        <span className="variety-category-badge">{v.categoryName}</span>
                      </td>
                      <td>{v.humidity != null ? `${v.humidity}%` : '—'}</td>
                      <td>{v.soil ?? '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </section>
        </div>
      </section>
    </main>
  )
}

export default VarietiesPage
