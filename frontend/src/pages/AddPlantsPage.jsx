import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  addPlantLot,
  getCompatibleUnits,
  getLocationParcels,
  getNurserySites,
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
  locationUnitId: '',
  locationParcelId: '',
  nurserySiteId: '',
  quantity: '',
  name: '',
  propagationMethod: '',
  hatchingDate: '',
  color: '',
  height: '',
  state: '',
}

function AddPlantsPage() {
  const navigate = useNavigate()
  const { logout } = useAuth()

  const [varieties, setVarieties] = useState([])
  const [units, setUnits] = useState([])
  const [parcels, setParcels] = useState([])
  const [sites, setSites] = useState([])
  const [recommendedUnitType, setRecommendedUnitType] = useState('')

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
      setUnits([])
      setParcels([])
      setSites([])
      setRecommendedUnitType('')
      setForm((prev) => ({ ...prev, locationUnitId: '', locationParcelId: '', nurserySiteId: '' }))
      return
    }
    let ignore = false
    getCompatibleUnits(form.varietyId).then((res) => {
      if (!ignore) {
        setUnits(res.data)
        const variety = varieties.find((v) => String(v.id) === String(form.varietyId))
        setRecommendedUnitType(variety?.locationTypeName ?? '')
        setForm((prev) => ({ ...prev, locationUnitId: res.data[0]?.id ?? '', locationParcelId: '', nurserySiteId: '' }))
        setParcels([])
        setSites([])
      }
    })
    return () => { ignore = true }
  }, [form.varietyId])

  useEffect(() => {
    if (!form.locationUnitId) {
      setParcels([])
      setSites([])
      setForm((prev) => ({ ...prev, locationParcelId: '', nurserySiteId: '' }))
      return
    }
    let ignore = false
    getLocationParcels(form.locationUnitId).then((res) => {
      if (!ignore) {
        setParcels(res.data)
        setForm((prev) => ({ ...prev, locationParcelId: '', nurserySiteId: '' }))
        setSites([])
      }
    })
    return () => { ignore = true }
  }, [form.locationUnitId])

  useEffect(() => {
    if (!form.locationParcelId) {
      setSites([])
      setForm((prev) => ({ ...prev, nurserySiteId: '' }))
      return
    }
    let ignore = false
    getNurserySites(form.locationParcelId).then((res) => {
      if (!ignore) {
        setSites(res.data)
        setForm((prev) => ({ ...prev, nurserySiteId: '' }))
      }
    })
    return () => { ignore = true }
  }, [form.locationParcelId])

  function handleField(e) {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
    setFormError('')
    setFormSuccess(false)
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!form.varietyId) { setFormError('Please select a variety.'); return }
    if (!form.locationUnitId) { setFormError('Please select a location unit.'); return }
    if (!form.locationParcelId) { setFormError('Please select a location parcel.'); return }
    if (!form.quantity || Number(form.quantity) <= 0) { setFormError('Quantity must be greater than 0.'); return }

    setSubmitting(true)
    setFormError('')

    try {
      const res = await addPlantLot({
        varietyId: Number(form.varietyId),
        locationUnitId: Number(form.locationUnitId),
        locationParcelId: Number(form.locationParcelId),
        nurserySiteId: form.nurserySiteId ? Number(form.nurserySiteId) : null,
        quantity: Number(form.quantity),
        name: form.name.trim() || null,
        propagationMethod: form.propagationMethod || null,
        hatchingDate: form.hatchingDate || null,
        color: form.color.trim() || null,
        height: form.height ? Number(form.height) : null,
        state: form.state.trim() || null,
      })
      setLots((prev) => [res.data, ...prev])
      setForm(EMPTY_FORM)
      setUnits([])
      setParcels([])
      setSites([])
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
                  {recommendedUnitType && (
                    <span className="variety-label" style={{ color: 'var(--color-primary)', fontSize: 13 }}>
                      Recommended unit type: {recommendedUnitType}
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

              <div className="variety-form-row variety-form-row-3">
                <div className="variety-field">
                  <label className="variety-label" htmlFor="locationUnitId">
                    Location unit *
                  </label>
                  <select
                    className="variety-select"
                    disabled={!form.varietyId}
                    id="locationUnitId"
                    name="locationUnitId"
                    onChange={handleField}
                    required
                    value={form.locationUnitId}
                  >
                    <option value="">Select unit</option>
                    {units.map((u) => (
                      <option key={u.id} value={u.id}>
                        {u.name} ({u.type})
                      </option>
                    ))}
                  </select>
                </div>

                <div className="variety-field">
                  <label className="variety-label" htmlFor="locationParcelId">
                    Location parcel *
                  </label>
                  <select
                    className="variety-select"
                    disabled={!form.locationUnitId}
                    id="locationParcelId"
                    name="locationParcelId"
                    onChange={handleField}
                    required
                    value={form.locationParcelId}
                  >
                    <option value="">Select parcel</option>
                    {parcels.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.name}{p.capacity ? ` (cap. ${p.capacity})` : ''}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="variety-field">
                  <label className="variety-label" htmlFor="nurserySiteId">
                    Nursery site (optional)
                  </label>
                  <select
                    className="variety-select"
                    disabled={!form.locationParcelId}
                    id="nurserySiteId"
                    name="nurserySiteId"
                    onChange={handleField}
                    value={form.nurserySiteId}
                  >
                    <option value="">No specific site</option>
                    {sites.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.name}
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
                    State / condition
                  </label>
                  <input
                    className="variety-input"
                    id="state"
                    name="state"
                    onChange={handleField}
                    placeholder="e.g. Seedling, Healthy"
                    type="text"
                    value={form.state}
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
                    <th>Unit</th>
                    <th>Parcel</th>
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
                      <td>{lot.state ?? '—'}</td>
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
