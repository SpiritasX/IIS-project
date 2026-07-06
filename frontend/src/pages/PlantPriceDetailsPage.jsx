import AttachMoneyIcon from '@mui/icons-material/AttachMoney'
import RestartAltIcon from '@mui/icons-material/RestartAlt'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  getCatalogPlants,
  getDemandHistory,
  getPriceHistory,
  rollbackPlantPrice,
  updatePlantPrice,
} from '../api/plantPrices'
import PageTitle from '../components/home/PageTitle'
import SearchBar from '../components/home/SearchBar'
import WorkerSidebar from '../components/worker/WorkerSidebar'
import { useAuth } from '../hooks/useAuth'
import '../styles/home.css'
import '../styles/botanist.css'
import '../styles/worker.css'
import '../styles/plant-prices.css'

const CHART_PADDING = { top: 18, right: 20, bottom: 42, left: 52 }
const CHART_W = 520
const CHART_H = 240

function toNumber(value) {
  const next = Number(value)
  return Number.isFinite(next) ? next : 0
}

function normalizeProduct(product) {
  return {
    ...product,
    availableQuantity: Number(product.availableQuantity || 0),
    id: Number(product.id),
    price: Number(product.price || 0),
    priceId: Number(product.priceId || 0),
  }
}

function formatNumber(value) {
  return new Intl.NumberFormat(undefined, {
    maximumFractionDigits: 2,
  }).format(toNumber(value))
}

function formatDate(value) {
  if (!value) return 'Not available'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)

  return new Intl.DateTimeFormat(undefined, {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(date)
}

function formatEndDate(value) {
  return value ? formatDate(value) : 'Active'
}

function formatChartDate(value) {
  if (!value) return ''

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)

  return new Intl.DateTimeFormat(undefined, {
    day: '2-digit',
    month: 'short',
  }).format(date)
}

function statusMessage(error, fallback) {
  return (
    error.response?.data?.detail ||
    error.response?.data?.message ||
    error.response?.data?.error ||
    fallback
  )
}

function LineTrendChart({ color, data, emptyLabel, legend, title, valueFormatter }) {
  if (!data.length) {
    return (
      <article className="price-chart-card">
        <h2>{title}</h2>
        <div className="price-chart-empty">{emptyLabel}</div>
        <span className="price-chart-legend" style={{ '--legend-color': color }}>
          {legend}
        </span>
      </article>
    )
  }

  const chartW = CHART_W - CHART_PADDING.left - CHART_PADDING.right
  const chartH = CHART_H - CHART_PADDING.top - CHART_PADDING.bottom
  const values = data.map((item) => item.value)
  const minValue = Math.min(...values, 0)
  const maxValue = Math.max(...values, 1)
  const valueRange = Math.max(maxValue - minValue, 1)
  const xFor = (index) => (
    data.length === 1
      ? CHART_PADDING.left + chartW / 2
      : CHART_PADDING.left + (chartW * index) / (data.length - 1)
  )
  const yFor = (value) => CHART_PADDING.top + chartH - ((value - minValue) / valueRange) * chartH
  const points = data.map((item, index) => ({
    ...item,
    x: xFor(index),
    y: yFor(item.value),
  }))
  const path = points.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`).join(' ')
  const labelStep = Math.max(1, Math.ceil(data.length / 5))

  return (
    <article className="price-chart-card">
      <h2>{title}</h2>
      <svg
        aria-label={title}
        className="price-line-chart"
        role="img"
        viewBox={`0 0 ${CHART_W} ${CHART_H}`}
      >
        {[0, 0.5, 1].map((ratio) => {
          const y = CHART_PADDING.top + chartH * (1 - ratio)
          const value = minValue + valueRange * ratio

          return (
            <g key={ratio}>
              <line
                stroke="rgba(34, 82, 58, 0.16)"
                strokeWidth="1"
                x1={CHART_PADDING.left}
                x2={CHART_W - CHART_PADDING.right}
                y1={y}
                y2={y}
              />
              <text dominantBaseline="middle" fill="#555" fontSize="10" textAnchor="end" x={CHART_PADDING.left - 8} y={y}>
                {valueFormatter(value)}
              </text>
            </g>
          )
        })}

        <line
          stroke="rgba(34, 82, 58, 0.28)"
          strokeWidth="1.2"
          x1={CHART_PADDING.left}
          x2={CHART_PADDING.left}
          y1={CHART_PADDING.top}
          y2={CHART_PADDING.top + chartH}
        />
        <line
          stroke="rgba(34, 82, 58, 0.28)"
          strokeWidth="1.2"
          x1={CHART_PADDING.left}
          x2={CHART_W - CHART_PADDING.right}
          y1={CHART_PADDING.top + chartH}
          y2={CHART_PADDING.top + chartH}
        />
        <path d={path} fill="none" stroke={color} strokeLinecap="round" strokeLinejoin="round" strokeWidth="4" />

        {points.map((point, index) => (
          <g key={`${point.label}-${index}`}>
            <circle cx={point.x} cy={point.y} fill="#f9fff9" r="5" stroke={color} strokeWidth="3" />
            {(index % labelStep === 0 || index === points.length - 1) && (
              <text fill="#444" fontSize="10" textAnchor="middle" x={point.x} y={CHART_H - 18}>
                {point.label}
              </text>
            )}
          </g>
        ))}
      </svg>
      <span className="price-chart-legend" style={{ '--legend-color': color }}>
        {legend}
      </span>
    </article>
  )
}

function PriceHistoryTable({ loading, plantName, prices }) {
  const rows = [...prices].reverse()

  return (
    <section className="price-history-panel">
      <div className="price-history-header">
        <span className="price-select-label">Selected plant</span>
        <h2 className="price-selected-plant-name">{plantName}</h2>
      </div>

      <div className="price-history-table-wrap">
        <table className="price-history-table">
          <thead>
            <tr>
              <th>Changed to</th>
              <th>Start date</th>
              <th>End date</th>
              <th>Changed by</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan="4">Loading price history...</td>
              </tr>
            ) : rows.length > 0 ? (
              rows.map((price) => (
                <tr key={price.id}>
                  <td>{formatNumber(price.price)}</td>
                  <td>{formatDate(price.startTime)}</td>
                  <td>{formatEndDate(price.endTime)}</td>
                  <td>{price.changedByUsername ?? 'Dynamic'}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="4">No price history for this plant.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  )
}

function PlantPriceDetailsPage() {
  const navigate = useNavigate()
  const { logout, user } = useAuth()

  const [plants, setPlants] = useState([])
  const [selectedPlantId, setSelectedPlantId] = useState('')
  const [priceHistory, setPriceHistory] = useState([])
  const [demandHistory, setDemandHistory] = useState([])
  const [searchTerm, setSearchTerm] = useState('')
  const [loadingPlants, setLoadingPlants] = useState(true)
  const [loadingDetails, setLoadingDetails] = useState(false)
  const [pageError, setPageError] = useState('')
  const [actionError, setActionError] = useState('')
  const [actionSuccess, setActionSuccess] = useState('')
  const [pricePromptOpen, setPricePromptOpen] = useState(false)
  const [newPrice, setNewPrice] = useState('')
  const [savingPrice, setSavingPrice] = useState(false)
  const [rollingBack, setRollingBack] = useState(false)

  useEffect(() => {
    let ignore = false

    async function loadPlants() {
      setLoadingPlants(true)
      setPageError('')

      try {
        const response = await getCatalogPlants()
        const nextPlants = response.data.map(normalizeProduct)

        if (!ignore) {
          setPlants(nextPlants)
          setSelectedPlantId((current) => current || (nextPlants[0]?.id ? String(nextPlants[0].id) : ''))
        }
      } catch (error) {
        if (!ignore) {
          setPageError(statusMessage(error, 'Unable to load plants.'))
        }
      } finally {
        if (!ignore) {
          setLoadingPlants(false)
        }
      }
    }

    loadPlants()

    return () => {
      ignore = true
    }
  }, [])

  const loadDetails = useCallback(async (plantId, { silent = false } = {}) => {
    if (!plantId) return

    if (!silent) {
      setLoadingDetails(true)
    }
    setPageError('')

    try {
      const [priceResponse, demandResponse] = await Promise.all([
        getPriceHistory(plantId),
        getDemandHistory(plantId),
      ])

      setPriceHistory(priceResponse.data)
      setDemandHistory(demandResponse.data)
    } catch (error) {
      setPageError(statusMessage(error, 'Unable to load plant price details.'))
    } finally {
      if (!silent) {
        setLoadingDetails(false)
      }
    }
  }, [])

  useEffect(() => {
    loadDetails(selectedPlantId)
  }, [loadDetails, selectedPlantId])

  const selectedPlant = useMemo(
    () => plants.find((plant) => String(plant.id) === String(selectedPlantId)) ?? null,
    [plants, selectedPlantId],
  )

  const currentPrice = useMemo(() => {
    const active = priceHistory.find((price) => !price.endTime)
    return active ?? priceHistory[priceHistory.length - 1] ?? null
  }, [priceHistory])

  const priceChartData = useMemo(
    () => priceHistory.map((item) => ({
      label: formatChartDate(item.startTime),
      value: toNumber(item.price),
    })),
    [priceHistory],
  )

  const demandChartData = useMemo(
    () => demandHistory.map((item) => ({
      label: item.period,
      value: toNumber(item.quantity),
    })),
    [demandHistory],
  )

  const filteredPlants = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase()
    if (!normalizedSearch) return plants

    return plants.filter((plant) => (
      plant.name?.toLowerCase().includes(normalizedSearch) ||
      plant.category?.toLowerCase().includes(normalizedSearch) ||
      plant.status?.toLowerCase().includes(normalizedSearch)
    ))
  }, [plants, searchTerm])

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  function handleSearchChange(event) {
    const value = event.target.value
    setSearchTerm(value)

    const normalizedValue = value.trim().toLowerCase()
    if (!normalizedValue) return

    const exactMatch = plants.find((plant) => plant.name?.toLowerCase() === normalizedValue)
    if (exactMatch) {
      setSelectedPlantId(String(exactMatch.id))
    }
  }

  function handleSelectPlant(plant) {
    setSelectedPlantId(String(plant.id))
    setSearchTerm('')
    setActionError('')
    setActionSuccess('')
  }

  function openPricePrompt() {
    const price = currentPrice?.price ?? selectedPlant?.price ?? ''
    setNewPrice(price ? String(price) : '')
    setActionError('')
    setActionSuccess('')
    setPricePromptOpen(true)
  }

  async function handleChangePrice(event) {
    event.preventDefault()

    const parsedPrice = Number(newPrice)
    if (!selectedPlantId || !Number.isFinite(parsedPrice) || parsedPrice <= 0) {
      setActionError('Enter a price greater than 0.')
      return
    }

    setSavingPrice(true)
    setActionError('')
    setActionSuccess('')

    try {
      await updatePlantPrice(selectedPlantId, {
        changedById: user?.id ?? null,
        price: parsedPrice,
      })
      await loadDetails(selectedPlantId, { silent: true })
      setActionSuccess('Price changed successfully.')
      setPricePromptOpen(false)
    } catch (error) {
      setActionError(statusMessage(error, 'Unable to change price.'))
    } finally {
      setSavingPrice(false)
    }
  }

  async function handleRollback() {
    if (!selectedPlantId) return

    setRollingBack(true)
    setActionError('')
    setActionSuccess('')

    try {
      await rollbackPlantPrice(selectedPlantId, {
        changedById: user?.id ?? null,
      })
      await loadDetails(selectedPlantId, { silent: true })
      setActionSuccess('Price rolled back successfully.')
    } catch (error) {
      setActionError(statusMessage(error, 'Unable to rollback price.'))
    } finally {
      setRollingBack(false)
    }
  }

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
          <div className="price-search-wrap">
            <SearchBar
              onChange={handleSearchChange}
              onClear={() => setSearchTerm('')}
              value={searchTerm}
            />
            {searchTerm && (
              <div className="price-search-results" aria-label="Plant search results">
                {filteredPlants.length > 0 ? (
                  filteredPlants.slice(0, 6).map((plant) => (
                    <button
                      className={`price-search-result${String(plant.id) === String(selectedPlantId) ? ' price-search-result-active' : ''}`}
                      key={plant.id}
                      onClick={() => handleSelectPlant(plant)}
                      type="button"
                    >
                      <span>{plant.name}</span>
                      <small>{plant.category ?? 'Plant'}</small>
                    </button>
                  ))
                ) : (
                  <p className="price-search-empty">No plants match your search.</p>
                )}
              </div>
            )}
          </div>
        </div>
      </header>

      <PageTitle label="Plant Details" onBack={handleLogout} wide />

      <section className="home-body">
        <WorkerSidebar onSiteChange={() => {}} selectedSiteId={null} sites={[]} />

        <div className="price-details-content">
          <PriceHistoryTable
            loading={loadingPlants || loadingDetails}
            plantName={selectedPlant?.name ?? (loadingPlants ? 'Loading plants...' : 'No plant selected')}
            prices={priceHistory}
          />

          {pageError && <p className="price-message price-message-error" role="alert">{pageError}</p>}
          {actionError && <p className="price-message price-message-error" role="alert">{actionError}</p>}
          {actionSuccess && <p className="price-message price-message-success" role="status">{actionSuccess}</p>}

          <section className="price-chart-grid" aria-busy={loadingDetails}>
            {loadingDetails ? (
              <>
                <div className="price-chart-skeleton" />
                <div className="price-chart-skeleton" />
              </>
            ) : (
              <>
                <LineTrendChart
                  color="var(--color-primary)"
                  data={priceChartData}
                  emptyLabel="No price history for this plant."
                  legend="Price through time"
                  title="Plant price through time"
                  valueFormatter={formatNumber}
                />
                <LineTrendChart
                  color="#6a7f2a"
                  data={demandChartData}
                  emptyLabel="No demand history for this plant."
                  legend="Requested quantity by month"
                  title="Plant demand through time"
                  valueFormatter={(value) => String(Math.round(value))}
                />
              </>
            )}
          </section>

          <div className="price-actions">
            <button
              className="price-action-button"
              disabled={!selectedPlantId || loadingDetails}
              onClick={openPricePrompt}
              type="button"
            >
              <AttachMoneyIcon fontSize="small" />
              <span>Change price</span>
            </button>
            <button
              className="price-action-button price-action-secondary"
              disabled={!selectedPlantId || loadingDetails || rollingBack}
              onClick={handleRollback}
              type="button"
            >
              <RestartAltIcon fontSize="small" />
              <span>{rollingBack ? 'Rolling back...' : 'Rollback price'}</span>
            </button>
          </div>
        </div>
      </section>

      {pricePromptOpen && (
        <div className="price-modal-backdrop" role="presentation">
          <form
            aria-modal="true"
            aria-labelledby="price-modal-title"
            className="price-modal"
            onSubmit={handleChangePrice}
            role="dialog"
          >
            <h2 id="price-modal-title">New price</h2>
            <label className="price-modal-label" htmlFor="new-price">
              Price
            </label>
            <input
              autoFocus
              className="price-modal-input"
              id="new-price"
              min="0.01"
              onChange={(event) => setNewPrice(event.target.value)}
              step="0.01"
              type="number"
              value={newPrice}
            />
            {actionError && <p className="price-modal-error" role="alert">{actionError}</p>}
            <div className="price-modal-actions">
              <button
                className="price-modal-cancel"
                disabled={savingPrice}
                onClick={() => setPricePromptOpen(false)}
                type="button"
              >
                Cancel
              </button>
              <button className="price-modal-submit" disabled={savingPrice} type="submit">
                {savingPrice ? 'Saving...' : 'Confirm'}
              </button>
            </div>
          </form>
        </div>
      )}
    </main>
  )
}

export default PlantPriceDetailsPage
