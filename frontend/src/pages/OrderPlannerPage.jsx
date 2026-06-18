import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome'
import ImageOutlinedIcon from '@mui/icons-material/ImageOutlined'
import RestartAltIcon from '@mui/icons-material/RestartAlt'
import ShoppingCartCheckoutIcon from '@mui/icons-material/ShoppingCartCheckout'
import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/client'
import CartButton from '../components/home/CartButton'
import PageTitle from '../components/home/PageTitle'
import UserSidebar from '../components/home/UserSidebar'
import { useCart } from '../hooks/useCart'
import '../styles/home.css'
import '../styles/planner.css'

const emptyOptions = {
  seasons: [],
  categories: [],
  types: [],
  species: [],
  varieties: [],
}

const initialFilters = {
  category: '',
  maxQuantityPerPlant: '5',
  season: '',
  species: '',
  type: '',
  variety: '',
}

function nullableValue(value) {
  return value ? value : null
}

function numberValue(value) {
  const parsedValue = Number(value)
  return Number.isFinite(parsedValue) ? parsedValue : 0
}

function errorMessageFor(error) {
  return (
    error.response?.data?.detail ||
    error.response?.data?.message ||
    error.response?.data?.error ||
    'Unable to generate an order plan right now.'
  )
}

function selectOptions(items, emptyLabel) {
  return (
    <>
      <option value="">{emptyLabel}</option>
      {items.map((item) => (
        <option key={item} value={item}>
          {item}
        </option>
      ))}
    </>
  )
}

function OrderPlannerPage() {
  const navigate = useNavigate()
  const { addNToCart, cartCount } = useCart()
  const [budget, setBudget] = useState('5000')
  const [filters, setFilters] = useState(initialFilters)
  const [options, setOptions] = useState(emptyOptions)
  const [loadingOptions, setLoadingOptions] = useState(true)
  const [optionsError, setOptionsError] = useState('')
  const [generating, setGenerating] = useState(false)
  const [plan, setPlan] = useState(null)
  const [planError, setPlanError] = useState('')
  const budgetNumber = numberValue(budget)

  useEffect(() => {
    let ignore = false

    async function loadOptions() {
      setLoadingOptions(true)
      setOptionsError('')

      try {
        const response = await api.get('/plants/options')

        if (!ignore) {
          setOptions({
            ...emptyOptions,
            ...response.data,
          })
        }
      } catch {
        if (!ignore) {
          setOptionsError('Unable to load planner options.')
        }
      } finally {
        if (!ignore) {
          setLoadingOptions(false)
        }
      }
    }

    loadOptions()

    return () => {
      ignore = true
    }
  }, [])

  const hasPlanItems = Boolean(plan?.items?.length)
  const summary = useMemo(() => {
    if (!plan) {
      return null
    }

    return {
      remainingBudget: Number(plan.remainingBudget || 0),
      total: Number(plan.total || 0),
      units: Number(plan.itemCount || 0),
    }
  }, [plan])

  function handleFilterChange(event) {
    const { name, value } = event.target
    setFilters((current) => ({
      ...current,
      [name]: value,
    }))
  }

  function handleReset() {
    setBudget('5000')
    setFilters(initialFilters)
    setPlan(null)
    setPlanError('')
  }

  async function handleGeneratePlan(event) {
    event.preventDefault()

    if (budgetNumber <= 0) {
      setPlanError('Budget must be greater than zero.')
      setPlan(null)
      return
    }

    setGenerating(true)
    setPlanError('')

    try {
      const response = await api.post('/plants/recommend-order', {
        budget: budgetNumber,
        category: nullableValue(filters.category),
        maxQuantityPerPlant: numberValue(filters.maxQuantityPerPlant) || 5,
        season: nullableValue(filters.season),
        species: nullableValue(filters.species),
        type: nullableValue(filters.type),
        variety: nullableValue(filters.variety),
      })

      setPlan(response.data)
    } catch (error) {
      setPlan(null)
      setPlanError(errorMessageFor(error))
    } finally {
      setGenerating(false)
    }
  }

  function handleAddPlanToCart() {
    if (!hasPlanItems) {
      return
    }

    plan.items.forEach((item) => {
      addNToCart(String(item.priceId), item.quantity)
    })
    navigate('/cart')
  }

  return (
    <main className="home-page planner-page">
      <header className="home-header planner-header">
        <button
          aria-label="Go to home"
          className="home-logo-placeholder"
          onClick={() => navigate('/home')}
          type="button"
        >
          <span aria-hidden="true" />
        </button>

        <div className="planner-header-title">Smart order</div>

        <CartButton count={cartCount} />
      </header>

      <PageTitle label="Order planner" onBack={() => navigate('/home')} wide />

      <section className="planner-shell">
        <UserSidebar />

        <div className="planner-main">
          <form className="planner-form" onSubmit={handleGeneratePlan}>
            <label className="planner-field planner-budget-field">
              <span>Budget</span>
              <input
                min="1"
                name="budget"
                onChange={(event) => setBudget(event.target.value)}
                step="1"
                type="number"
                value={budget}
              />
            </label>

            <label className="planner-field">
              <span>Season</span>
              <select
                disabled={loadingOptions}
                name="season"
                onChange={handleFilterChange}
                value={filters.season}
              >
                {selectOptions(options.seasons, 'Any season')}
              </select>
            </label>

            <label className="planner-field">
              <span>Category</span>
              <select
                disabled={loadingOptions}
                name="category"
                onChange={handleFilterChange}
                value={filters.category}
              >
                {selectOptions(options.categories, 'Any category')}
              </select>
            </label>

            <label className="planner-field">
              <span>Type</span>
              <select
                disabled={loadingOptions}
                name="type"
                onChange={handleFilterChange}
                value={filters.type}
              >
                {selectOptions(options.types, 'Any type')}
              </select>
            </label>

            <label className="planner-field">
              <span>Species</span>
              <select
                disabled={loadingOptions}
                name="species"
                onChange={handleFilterChange}
                value={filters.species}
              >
                {selectOptions(options.species, 'Any species')}
              </select>
            </label>

            <label className="planner-field">
              <span>Variety</span>
              <select
                disabled={loadingOptions}
                name="variety"
                onChange={handleFilterChange}
                value={filters.variety}
              >
                {selectOptions(options.varieties, 'Any variety')}
              </select>
            </label>

            <label className="planner-field">
              <span>Max quantity</span>
              <input
                min="1"
                name="maxQuantityPerPlant"
                onChange={handleFilterChange}
                step="1"
                type="number"
                value={filters.maxQuantityPerPlant}
              />
            </label>

            <div className="planner-form-actions">
              <button
                className="planner-primary-button"
                disabled={generating || loadingOptions || budgetNumber <= 0}
                type="submit"
              >
                <AutoAwesomeIcon fontSize="small" />
                {generating ? 'Generating...' : 'Generate plan'}
              </button>
              <button className="planner-secondary-button" onClick={handleReset} type="button">
                <RestartAltIcon fontSize="small" />
                Reset
              </button>
            </div>
          </form>

          <section className="planner-results" aria-live="polite">
            {optionsError ? (
              <div className="planner-message" role="alert">
                {optionsError}
              </div>
            ) : null}

            {planError ? (
              <div className="planner-message planner-message-error" role="alert">
                {planError}
              </div>
            ) : null}

            {summary ? (
              <div className="planner-summary">
                <div>
                  <span>Total</span>
                  <strong>{summary.total}</strong>
                </div>
                <div>
                  <span>Remaining</span>
                  <strong>{summary.remainingBudget}</strong>
                </div>
                <div>
                  <span>Plants</span>
                  <strong>{summary.units}</strong>
                </div>
                <button
                  className="planner-cart-button"
                  disabled={!hasPlanItems}
                  onClick={handleAddPlanToCart}
                  type="button"
                >
                  <ShoppingCartCheckoutIcon fontSize="small" />
                  Add plan to cart
                </button>
              </div>
            ) : null}

            {hasPlanItems ? (
              <div className="planner-items">
                {plan.items.map((item) => (
                  <article className="planner-item" key={item.priceId}>
                    <div className="planner-item-image" role="img" aria-label={item.name}>
                      <ImageOutlinedIcon fontSize="inherit" />
                    </div>
                    <div className="planner-item-copy">
                      <h2>{item.name}</h2>
                      <p>{item.description}</p>
                      <div className="planner-item-tags">
                        <span>{item.season}</span>
                        <span>{item.category}</span>
                        <span>{item.variety}</span>
                      </div>
                    </div>
                    <div className="planner-item-total">
                      <span>Qty {item.quantity}</span>
                      <strong>{item.lineTotal}</strong>
                    </div>
                  </article>
                ))}
              </div>
            ) : plan ? (
              <div className="planner-message">No plants fit this budget and goal set.</div>
            ) : (
              <div className="planner-message">No plan generated.</div>
            )}
          </section>
        </div>
      </section>
    </main>
  )
}

export default OrderPlannerPage
