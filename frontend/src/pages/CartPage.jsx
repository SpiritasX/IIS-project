import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown'
import { useEffect, useMemo, useState, useSyncExternalStore } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/client'
import AddressCard from '../components/cart/AddressCard'
import AddressModal from '../components/cart/AddressModal'
import CheckoutProductCard from '../components/cart/CheckoutProductCard'
import HomeHeader from '../components/home/HomeHeader'
import PageTitle from '../components/home/PageTitle'
import UserSidebar from '../components/home/UserSidebar'
import { useAuth } from '../hooks/useAuth'
import { useCart } from '../hooks/useCart'
import '../styles/home.css'
import '../styles/cart.css'

const defaultAddress = 'Address Example, City Example, Country example'

function addressFormFor(user) {
  return {
    address: user?.addressLine || '',
    city: user?.city || '',
    country: user?.country || '',
    zipCode: user?.zipCode || '',
  }
}

function normalizeProduct(product) {
  return {
    ...product,
    available: Boolean(product.available),
    availableQuantity: Number(product.availableQuantity || 0),
    category: product.category || 'Other',
    id: String(product.id),
    imageAlt: `${product.name} placeholder`,
    price: Number(product.price),
    priceId: String(product.priceId),
  }
}

function normalizeOrder(order) {
  return {
    ...order,
    date: formatDate(order.date),
    expiresAt: formatDateTime(order.expiresAt),
    id: String(order.id),
    rawDate: order.date,
    total: Number(order.total || 0),
    items: (order.items || []).map((item) => ({
      ...item,
      adjusted: Boolean(item.adjusted),
      offeredQuantity: Number(item.offeredQuantity ?? item.quantity ?? 0),
      price: Number(item.price || 0),
      priceId: String(item.priceId),
      quantity: Number(item.quantity ?? item.offeredQuantity ?? 0),
      requestedQuantity: Number(item.requestedQuantity ?? item.quantity ?? 0),
      reservedQuantity: Number(item.reservedQuantity || 0),
    })),
  }
}

function formatDate(value) {
  if (!value) {
    return 'Request Date'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('en', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  }).format(date)
}

function formatDateTime(value) {
  if (!value) {
    return ''
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('en', {
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(date)
}

function errorMessageFor(error) {
  return (
    error.response?.data?.detail ||
    error.response?.data?.message ||
    error.response?.data?.error ||
    'Unable to create the request right now.'
  )
}

function subscribeToViewportResize(callback) {
  window.addEventListener('resize', callback)
  return () => window.removeEventListener('resize', callback)
}

function getCheckoutColumnCount() {
  if (window.matchMedia('(max-width: 520px)').matches) {
    return 1
  }

  if (window.matchMedia('(max-width: 1280px)').matches) {
    return 2
  }

  return 4
}

function getServerCheckoutColumnCount() {
  return 4
}

function useCheckoutColumnCount() {
  return useSyncExternalStore(
    subscribeToViewportResize,
    getCheckoutColumnCount,
    getServerCheckoutColumnCount,
  )
}

function adjustmentRowsFor(snapshot, offer) {
  const offerItemsByPriceId = new Map(offer.items.map((item) => [item.priceId, item]))

  return snapshot
    .map((item) => {
      const offeredItem = offerItemsByPriceId.get(item.priceId)

      if (!offeredItem) {
        return {
          ...item,
          offeredQuantity: 0,
        }
      }

      if (offeredItem.offeredQuantity < item.requestedQuantity) {
        return {
          ...item,
          offeredQuantity: offeredItem.offeredQuantity,
        }
      }

      return null
    })
    .filter(Boolean)
}

function CartPage() {
  const navigate = useNavigate()
  const { updateUser, user } = useAuth()
  const { addToCart, cartCount, cartItems, clearCart, removeFromCart } = useCart()
  const [catalogProducts, setCatalogProducts] = useState([])
  const [loadingProducts, setLoadingProducts] = useState(false)
  const [productError, setProductError] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [filterOpen, setFilterOpen] = useState(false)
  const [category, setCategory] = useState('All')
  const [sort, setSort] = useState('featured')
  const [reservationCreated, setReservationCreated] = useState(null)
  const [adjustedOffer, setAdjustedOffer] = useState(null)
  const [adjustmentRows, setAdjustmentRows] = useState([])
  const [adjustmentModalOpen, setAdjustmentModalOpen] = useState(false)
  const [requestError, setRequestError] = useState('')
  const [creatingRequest, setCreatingRequest] = useState(false)
  const [offerActionError, setOfferActionError] = useState('')
  const [offerActionLoading, setOfferActionLoading] = useState(false)
  const [cancelReason, setCancelReason] = useState('')
  const [productsExpanded, setProductsExpanded] = useState(false)
  const [addressModalOpen, setAddressModalOpen] = useState(false)
  const [addressForm, setAddressForm] = useState(addressFormFor(user))
  const [addressError, setAddressError] = useState('')
  const [savingAddress, setSavingAddress] = useState(false)
  const checkoutColumnCount = useCheckoutColumnCount()
  const deliveryAddress = user?.address || defaultAddress

  useEffect(() => {
    let ignore = false

    async function loadProducts() {
      if (cartCount === 0) {
        setCatalogProducts([])
        setProductError('')
        setLoadingProducts(false)
        return
      }

      setLoadingProducts(true)
      setProductError('')

      try {
        const response = await api.get('/plants')

        if (!ignore) {
          setCatalogProducts(response.data.map(normalizeProduct))
        }
      } catch {
        if (!ignore) {
          setProductError('Unable to load cart products from the backend.')
        }
      } finally {
        if (!ignore) {
          setLoadingProducts(false)
        }
      }
    }

    loadProducts()

    return () => {
      ignore = true
    }
  }, [cartCount])

  const categoryOptions = useMemo(() => {
    const categories = catalogProducts.map((product) => product.category).filter(Boolean)
    return ['All', ...new Set(categories)]
  }, [catalogProducts])

  const selectedProducts = useMemo(
    () =>
      Object.entries(cartItems)
        .map(([productId, quantity]) => {
          const product = catalogProducts.find((item) => item.priceId === productId)

          return product ? { product, quantity } : null
        })
        .filter(Boolean),
    [cartItems, catalogProducts],
  )

  const checkoutProducts = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase()

    const filtered = selectedProducts.filter(({ product }) => {
      const matchesCategory = category === 'All' || product.category === category
      const matchesSearch =
        !normalizedSearch ||
        product.name.toLowerCase().includes(normalizedSearch) ||
        product.description.toLowerCase().includes(normalizedSearch)

      return matchesCategory && matchesSearch
    })

    if (sort === 'low-high') {
      return [...filtered].sort((first, second) => first.product.price - second.product.price)
    }

    if (sort === 'high-low') {
      return [...filtered].sort((first, second) => second.product.price - first.product.price)
    }

    return filtered
  }, [selectedProducts, category, searchTerm, sort])

  const productsCanExpand = checkoutProducts.length > checkoutColumnCount
  const canCreateRequest =
    cartCount > 0 && !loadingProducts && !productError && !creatingRequest && selectedProducts.length > 0

  function handleResetFilters() {
    setCategory('All')
    setSort('featured')
    setFilterOpen(false)
    setProductsExpanded(false)
  }

  function cartSnapshot() {
    return Object.entries(cartItems).map(([plantPriceId, quantity]) => {
      const product = catalogProducts.find((item) => item.priceId === plantPriceId)

      return {
        name: product?.name || `Product ${plantPriceId}`,
        priceId: String(plantPriceId),
        requestedQuantity: Number(quantity),
      }
    })
  }

  async function handleCreateRequest() {
    if (!canCreateRequest) {
      return
    }

    const snapshot = cartSnapshot()
    setRequestError('')
    setOfferActionError('')
    setCreatingRequest(true)

    try {
      const response = await api.post('/orders', {
        autoAcceptIfUnchanged: true,
        deliveryAddress,
        items: Object.entries(cartItems).map(([plantPriceId, quantity]) => ({
          plantPriceId: Number(plantPriceId),
          quantity,
        })),
      })
      const order = normalizeOrder(response.data)

      clearCart()
      setAdjustedOffer(null)
      setAdjustmentRows([])
      setAdjustmentModalOpen(false)

      if (order.status === 'Rezervacija') {
        setReservationCreated(order)
      } else {
        const rows = adjustmentRowsFor(snapshot, order)
        setAdjustedOffer(order)
        setAdjustmentRows(rows)
        setAdjustmentModalOpen(rows.length > 0)
      }
    } catch (error) {
      setRequestError(errorMessageFor(error))
    } finally {
      setCreatingRequest(false)
    }
  }

  async function refreshAdjustedOffer(offerId) {
    try {
      const response = await api.get(`/orders/${offerId}`)
      setAdjustedOffer(normalizeOrder(response.data))
    } catch {
      // Keep the current offer visible if refresh fails.
    }
  }

  async function handleAcceptOffer() {
    if (!adjustedOffer) {
      return
    }

    setOfferActionError('')
    setOfferActionLoading(true)

    try {
      await api.post(`/orders/${adjustedOffer.id}/accept`)
      navigate('/requests')
    } catch (error) {
      setOfferActionError(errorMessageFor(error))
      await refreshAdjustedOffer(adjustedOffer.id)
    } finally {
      setOfferActionLoading(false)
    }
  }

  async function handleCancelOffer(event) {
    event.preventDefault()

    if (!adjustedOffer) {
      return
    }

    if (!cancelReason.trim()) {
      setOfferActionError('Cancellation reason is required.')
      return
    }

    setOfferActionError('')
    setOfferActionLoading(true)

    try {
      await api.post(`/orders/${adjustedOffer.id}/cancel`, {
        reason: cancelReason.trim(),
      })
      navigate('/requests')
    } catch (error) {
      setOfferActionError(errorMessageFor(error))
      await refreshAdjustedOffer(adjustedOffer.id)
    } finally {
      setOfferActionLoading(false)
    }
  }

  function handleReservationBackdropMouseDown(event) {
    if (event.target === event.currentTarget) {
      setReservationCreated(null)
    }
  }

  function handleAdjustmentBackdropMouseDown(event) {
    if (event.target === event.currentTarget) {
      setAdjustmentModalOpen(false)
    }
  }

  function handleAddressChange(event) {
    const { name, value } = event.target
    setAddressForm((current) => ({
      ...current,
      [name]: value,
    }))
  }

  async function handleAddressSubmit(event) {
    event.preventDefault()
    setAddressError('')
    setSavingAddress(true)

    try {
      const response = await api.patch('/auth/profile/address', addressForm)
      updateUser(response.data)
      setAddressModalOpen(false)
    } catch (error) {
      setAddressError(errorMessageFor(error))
    } finally {
      setSavingAddress(false)
    }
  }

  return (
    <main className="home-page checkout-page">
      <HomeHeader
        cartCount={cartCount}
        category={category}
        categoryOptions={categoryOptions}
        filterOpen={filterOpen}
        onCategoryChange={(event) => {
          setCategory(event.target.value)
          setProductsExpanded(false)
        }}
        onClearSearch={() => {
          setSearchTerm('')
          setProductsExpanded(false)
        }}
        onResetFilters={handleResetFilters}
        onSearchChange={(event) => {
          setSearchTerm(event.target.value)
          setProductsExpanded(false)
        }}
        onSortChange={(event) => {
          setSort(event.target.value)
          setProductsExpanded(false)
        }}
        onToggleFilter={() => setFilterOpen((current) => !current)}
        searchTerm={searchTerm}
        sort={sort}
      />

      <PageTitle label="Request" onBack={() => navigate('/home')} wide />

      <section className="checkout-shell">
        <UserSidebar />

        <div className="checkout-main">
          {adjustedOffer ? (
            <section className="offer-review" aria-labelledby="offer-review-title">
              <p className="offer-review-eyebrow">{adjustedOffer.status}</p>
              <h2 id="offer-review-title">Adjusted offer</h2>
              {adjustedOffer.expiresAt ? <p>Valid until {adjustedOffer.expiresAt}</p> : null}
              <div className="offer-review-items">
                {adjustedOffer.items.map((item) => (
                  <div className="offer-review-item" key={`${adjustedOffer.id}-${item.priceId}`}>
                    <strong>{item.name}</strong>
                    <span>
                      Requested {item.requestedQuantity}, offered {item.offeredQuantity}
                    </span>
                    {item.adjusted ? <em>Adjusted</em> : null}
                  </div>
                ))}
              </div>
              <strong className="offer-review-total">Price: {adjustedOffer.total}</strong>
              {offerActionError ? (
                <p className="checkout-order-error" role="alert">
                  {offerActionError}
                </p>
              ) : null}
              <div className="offer-review-actions">
                <button
                  className="checkout-order-button"
                  disabled={!adjustedOffer.canAccept || offerActionLoading}
                  onClick={handleAcceptOffer}
                  type="button"
                >
                  {offerActionLoading ? 'Working...' : 'Accept offer'}
                </button>
              </div>
              {adjustedOffer.canCancel ? (
                <form className="offer-cancel-form" onSubmit={handleCancelOffer}>
                  <label>
                    <span>Cancellation reason</span>
                    <textarea
                      onChange={(event) => setCancelReason(event.target.value)}
                      rows="3"
                      value={cancelReason}
                    />
                  </label>
                  <button className="offer-cancel-button" disabled={offerActionLoading} type="submit">
                    Cancel request
                  </button>
                </form>
              ) : null}
            </section>
          ) : (
            <>
              <h2>Products</h2>

              {cartCount === 0 ? (
                <div className="checkout-empty">Your cart is empty.</div>
              ) : loadingProducts ? (
                <div className="checkout-empty">Loading cart...</div>
              ) : productError ? (
                <div className="checkout-empty">{productError}</div>
              ) : checkoutProducts.length > 0 ? (
                <div
                  className={
                    productsExpanded && productsCanExpand
                      ? 'checkout-product-grid checkout-product-grid-expanded'
                      : 'checkout-product-grid checkout-product-grid-collapsed'
                  }
                >
                  {checkoutProducts.map(({ product, quantity }) => (
                    <CheckoutProductCard
                      disabled={creatingRequest}
                      key={product.priceId}
                      onAdd={addToCart}
                      onRemove={removeFromCart}
                      product={product}
                      quantity={quantity}
                    />
                  ))}
                </div>
              ) : (
                <div className="checkout-empty">No cart products match your filters.</div>
              )}

              {productsCanExpand ? (
                <button
                  aria-expanded={productsExpanded}
                  aria-label={productsExpanded ? 'Collapse products' : 'Show all products'}
                  className={productsExpanded ? 'checkout-down-cue checkout-down-cue-open' : 'checkout-down-cue'}
                  onClick={() => setProductsExpanded((current) => !current)}
                  type="button"
                >
                  <KeyboardArrowDownIcon fontSize="inherit" />
                </button>
              ) : null}

              <h2>Delivery Address</h2>
              <AddressCard
                address={deliveryAddress}
                onEdit={() => {
                  setAddressForm(addressFormFor(user))
                  setAddressModalOpen(true)
                }}
              />

              <div className="checkout-order-actions">
                <button
                  className="checkout-order-button"
                  disabled={!canCreateRequest}
                  onClick={handleCreateRequest}
                  type="button"
                >
                  {creatingRequest ? 'Creating request...' : 'Create request'}
                </button>
                {requestError ? (
                  <p className="checkout-order-error" role="alert">
                    {requestError}
                  </p>
                ) : null}
              </div>
            </>
          )}
        </div>
      </section>

      {reservationCreated ? (
        <div className="order-modal-backdrop" onMouseDown={handleReservationBackdropMouseDown} role="presentation">
          <section
            aria-labelledby="order-success-title"
            aria-modal="true"
            className="order-modal"
            role="dialog"
          >
            <h2 id="order-success-title">Request accepted!</h2>
            <p>Your reservation is ready to track.</p>
            <button
              className="order-modal-button"
              onClick={() => navigate('/requests')}
              type="button"
            >
              View requests
            </button>
          </section>
        </div>
      ) : null}
      {adjustmentModalOpen ? (
        <div className="order-modal-backdrop" onMouseDown={handleAdjustmentBackdropMouseDown} role="presentation">
          <section
            aria-labelledby="adjusted-offer-title"
            aria-modal="true"
            className="order-modal adjusted-offer-modal"
            role="dialog"
          >
            <h2 id="adjusted-offer-title">Adjusted offer</h2>
            <p>Some products from your request may not be available. We've adjusted the offer.</p>
            <div className="adjusted-offer-list">
              {adjustmentRows.map((item) => (
                <span key={`adjusted-${item.priceId}`}>
                  {item.name}: requested {item.requestedQuantity}, offered {item.offeredQuantity}
                </span>
              ))}
            </div>
            <button
              className="order-modal-button"
              onClick={() => setAdjustmentModalOpen(false)}
              type="button"
            >
              Review offer
            </button>
          </section>
        </div>
      ) : null}
      {addressModalOpen ? (
        <AddressModal
          form={addressForm}
          message={addressError}
          onChange={handleAddressChange}
          onClose={() => setAddressModalOpen(false)}
          onSubmit={handleAddressSubmit}
          saving={savingAddress}
        />
      ) : null}
    </main>
  )
}

export default CartPage
