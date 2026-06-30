import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  addPlantLot,
  getCompatibleStorageSpaces,
  getSectors,
  getVarieties,
} from '../api/plants'
import WorkerSidebar from '../components/worker/WorkerSidebar'
import PageTitle from '../components/home/PageTitle'
import SearchBar from '../components/home/SearchBar'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/varieties.css'

const PROPAGATION_OPTIONS = ['Seed', 'Slip', 'Sapling', 'Cuttings', 'Grafting', 'Division']

const EMPTY_FORM = {
  varietyId: '',
  storageSpaceId: '',
  sectorId: '',
  quantity: '',
  name: '',
  propagationMethod: '',
  hatchingDate: '',
  color: '',
  height: '',
  state: '',
  conditionDescription: '',
}

function AddPlantsPage() {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [varieties, setVarieties] = useState([])
  const [storageSpaces, setStorageSpaces] = useState([])
  const [sectors, setSectors] = useState([])
  const [recommendedStorageSpaceType, setRecommendedStorageSpaceType] = useState('')

  const [lots, setLots] = useState([])
  const [form, setForm] = useState(EMPTY_FORM)
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')
  const [formSuccess, setFormSuccess] = useState(false)

  const [searchTerm, setSearchTerm] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let ignore = false
    getVarieties()
      .then((res) => { if (!ignore) setVarieties(res.data) })
      .catch(() => {})
      .finally(() => { if (!ignore) setLoading(false) })
    return () => { ignore = true }
  }, [])

  useEffect(() => {
    if (!form.varietyId) {
      setStorageSpaces([])
      setSectors([])
      setRecommendedStorageSpaceType('')
      setForm((prev) => ({ ...prev, storageSpaceId: '', sectorId: '' }))
      return
    }
    let ignore = false
    getCompatibleStorageSpaces(form.varietyId).then((res) => {
      if (!ignore) {
        setStorageSpaces(res.data)
        const variety = varieties.find((v) => String(v.id) === String(form.varietyId))
        setRecommendedStorageSpaceType(variety?.storageSpaceTypeName ?? '')
        setForm((prev) => ({ ...prev, storageSpaceId: res.data[0]?.id ?? '', sectorId: '' }))
        setSectors([])
      }
    })
    return () => { ignore = true }
  }, [form.varietyId])

  useEffect(() => {
    if (!form.storageSpaceId) {
      setSectors([])
      setForm((prev) => ({ ...prev, sectorId: '' }))
      return
    }
    let ignore = false
    getSectors(form.storageSpaceId).then((res) => {
      if (!ignore) {
        setSectors(res.data)
        setForm((prev) => ({ ...prev, sectorId: '' }))
      }
    })
    return () => { ignore = true }
  }, [form.storageSpaceId])

  function handleField(e) {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
    setFormError('')
    setFormSuccess(false)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!form.varietyId) { setFormError('Please select a variety.'); return }
    if (!form.storageSpaceId) { setFormError('Please select a storage space.'); return }
    if (!form.sectorId) { setFormError('Please select a sector.'); return }
    if (!form.quantity || Number(form.quantity) <= 0) { setFormError('Quantity must be greater than 0.'); return }

    setSubmitting(true)
    setFormError('')

    try {
      const res = await addPlantLot({
        varietyId: Number(form.varietyId),
        storageSpaceId: Number(form.storageSpaceId),
        sectorId: Number(form.sectorId),
        quantity: Number(form.quantity),
        name: form.name.trim() || null,
        propagationMethod: form.propagationMethod || null,
        hatchingDate: form.hatchingDate || null,
        color: form.color.trim() || null,
        height: form.height ? Number(form.height) : null,
        state: form.state ? Number(form.state) : null,
        conditionDescription: form.conditionDescription.trim() || null,
      })
      setLots((prev) => [res.data, ...prev])
      setForm(EMPTY_FORM)
      setStorageSpaces([])
      setSectors([])
      setFormSuccess(true)
      setTimeout(() => setFormSuccess(false), 4000)
    } catch (err) {
      setFormError(err.response?.data?.message ?? 'Failed to add plant lot. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  const filteredLots = searchTerm
    ? lots.filter(
        (l) =>
          l.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          l.varietyName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          l.parcelName?.toLowerCase().includes(searchTerm.toLowerCase()),
      )
    : lots

  return (
    <main className="home-page">
      <header className="home-header botanist-header">
        <button
          aria-label="Back to worker dashboard"
          className="home-logo-placeholder"
          onClick={() => navigate('/worker')}
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

      <PageTitle label="Add Plants" onBack={handleLogout} />

      <section className="home-body">
        <WorkerSidebar onSiteChange={() => {}} selectedSiteId={null} sites={[]} />

        <div className="varieties-content">
          <section className="variety-form-card" aria-labelledby="add-plants-heading">
            <h2 className="variety-form-heading" id="add-plants-heading">
              Add plant lot to nursery
            </h2>

            <form className="variety-form" noValidate onSubmit={handleSubmit}>
              {/* Variety + location cascade */}
              <div className="variety-form-row variety-form-row-2">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="varietyId">
                    Variety *
                  </label>
                  <select
                    className="variety-select"
                    id="varietyId"
                    name="varietyId"
                    onChange={handleField}
                    required
                    value={form.varietyId}
                  >
                    <option value="">Select variety</option>
                    {varieties.map((v) => (
                      <option key={v.id} value={v.id}>
                        {v.name} — {v.speciesName}
                      </option>
                    ))}
                  </select>
                  {recommendedStorageSpaceType && (
                    <span className="variety-label" style={{ color: 'var(--color-primary)', fontSize: 13 }}>
                      Recommended storage space type: {recommendedStorageSpaceType}
                    </span>
                  )}
                </div>

                <div className="variety-field">
                  <label className="variety-label" htmlFor="quantity">
                    Quantity *
                  </label>
                  <input
                    className="variety-input"
                    id="quantity"
                    min="1"
                    name="quantity"
                    onChange={handleField}
                    placeholder="e.g. 20"
                    required
                    type="number"
                    value={form.quantity}
                  />
                </div>
              </div>

              <div className="variety-form-row variety-form-row-2">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="storageSpaceId">
                    Storage space *
                  </label>
                  <select
                    className="variety-select"
                    disabled={!form.varietyId}
                    id="storageSpaceId"
                    name="storageSpaceId"
                    onChange={handleField}
                    required
                    value={form.storageSpaceId}
                  >
                    <option value="">Select storage space</option>
                    {storageSpaces.map((u) => (
                      <option key={u.id} value={u.id}>
                        {u.name} ({u.type})
                      </option>
                    ))}
                  </select>
                </div>

                <div className="variety-field">
                  <label className="variety-label" htmlFor="sectorId">
                    Sector *
                  </label>
                  <select
                    className="variety-select"
                    disabled={!form.storageSpaceId}
                    id="sectorId"
                    name="sectorId"
                    onChange={handleField}
                    required
                    value={form.sectorId}
                  >
                    <option value="">Select sector</option>
                    {sectors.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.name}{s.capacity ? ` (cap. ${s.capacity})` : ''}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Optional plant details */}
              <div className="variety-form-row variety-form-row-2">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="name">
                    Lot name (optional)
                  </label>
                  <input
                    className="variety-input"
                    id="name"
                    name="name"
                    onChange={handleField}
                    placeholder="Defaults to variety name + 'lot'"
                    type="text"
                    value={form.name}
                  />
                </div>

                <div className="variety-field">
                  <label className="variety-label" htmlFor="propagationMethod">
                    Propagation method
                  </label>
                  <select
                    className="variety-select"
                    id="propagationMethod"
                    name="propagationMethod"
                    onChange={handleField}
                    value={form.propagationMethod}
                  >
                    <option value="">Not specified</option>
                    {PROPAGATION_OPTIONS.map((m) => (
                      <option key={m} value={m}>{m}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="variety-form-row variety-form-row-4">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="hatchingDate">
                    Hatching date
                  </label>
                  <input
                    className="variety-input"
                    id="hatchingDate"
                    name="hatchingDate"
                    onChange={handleField}
                    type="date"
                    value={form.hatchingDate}
                  />
                </div>

                <div className="variety-field">
                  <label className="variety-label" htmlFor="color">
                    Color
                  </label>
                  <input
                    className="variety-input"
                    id="color"
                    name="color"
                    onChange={handleField}
                    placeholder="e.g. Green"
                    type="text"
                    value={form.color}
                  />
                </div>

                <div className="variety-field">
                  <label className="variety-label" htmlFor="height">
                    Height (cm)
                  </label>
                  <input
                    className="variety-input"
                    id="height"
                    min="0"
                    name="height"
                    onChange={handleField}
                    placeholder="e.g. 30"
                    step="0.1"
                    type="number"
                    value={form.height}
                  />
                </div>

                <div className="variety-field">
                  <label className="variety-label" htmlFor="state">
                    Condition (1–5)
                  </label>
                  <select
                    className="variety-select"
                    id="state"
                    name="state"
                    onChange={handleField}
                    value={form.state}
                  >
                    <option value="">Not rated</option>
                    <option value="1">1 — Poor</option>
                    <option value="2">2 — Fair</option>
                    <option value="3">3 — Good</option>
                    <option value="4">4 — Very good</option>
                    <option value="5">5 — Excellent</option>
                  </select>
                </div>
              </div>

              <div className="variety-form-row">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="conditionDescription">
                    Condition description (optional)
                  </label>
                  <textarea
                    className="variety-textarea"
                    id="conditionDescription"
                    name="conditionDescription"
                    onChange={handleField}
                    placeholder="Describe the plant's current condition…"
                    rows={2}
                    value={form.conditionDescription}
                  />
                </div>
              </div>

              {formError && <p className="variety-form-error" role="alert">{formError}</p>}
              {formSuccess && (
                <p className="variety-form-success" role="status">
                  Plant lot added successfully.
                </p>
              )}

              <div className="variety-form-actions">
                <button
                  className="variety-submit-button"
                  disabled={submitting}
                  type="submit"
                >
                  {submitting ? 'Saving…' : 'Add to nursery'}
                </button>
              </div>
            </form>
          </section>

          {lots.length > 0 && (
            <section aria-labelledby="lots-list-heading" className="varieties-list-card">
              <h2 className="variety-form-heading" id="lots-list-heading">
                Added this session{' '}
                <span className="varieties-count">({filteredLots.length})</span>
              </h2>

              <table className="varieties-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Variety</th>
                    <th>Storage space</th>
                    <th>Sector</th>
                    <th>Qty</th>
                    <th>Propagation</th>
                    <th>State</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredLots.map((lot) => (
                    <tr key={lot.id}>
                      <td className="varieties-name">{lot.name}</td>
                      <td>{lot.varietyName}</td>
                      <td>{lot.unitName}</td>
                      <td>
                        <span className="variety-category-badge">{lot.parcelName}</span>
                      </td>
                      <td>{lot.quantity}</td>
                      <td>{lot.propagationMethod ?? '—'}</td>
                      <td>{lot.state != null ? `${lot.state}/5` : '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>
          )}
        </div>
      </section>
    </main>
  )
}

export default AddPlantsPage
