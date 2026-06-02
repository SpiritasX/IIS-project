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
    category: product.category || 'Other',
    id: String(product.id),
    imageAlt: `${product.name} placeholder`,
    price: Number(product.price),
    priceId: String(product.priceId),
  }
}

function errorMessageFor(error) {
  return (
    error.response?.data?.detail ||
    error.response?.data?.message ||
    error.response?.data?.error ||
    'Unable to place the order right now.'
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

function CartPage() {
  const navigate = useNavigate()
  const { updateUser, user } = useAuth()
  const { cartCount, cartItems, clearCart } = useCart()
  const [catalogProducts, setCatalogProducts] = useState([])
  const [loadingProducts, setLoadingProducts] = useState(false)
  const [productError, setProductError] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [filterOpen, setFilterOpen] = useState(false)
  const [category, setCategory] = useState('All')
  const [sort, setSort] = useState('featured')
  const [orderPlaced, setOrderPlaced] = useState(false)
  const [orderError, setOrderError] = useState('')
  const [placingOrder, setPlacingOrder] = useState(false)
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
        const response = await api.get('/api/plants')

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

  const checkoutProducts = useMemo(() => {
    const selectedProducts = Object.entries(cartItems)
      .map(([productId, quantity]) => {
        const product = catalogProducts.find((item) => item.priceId === productId)

        return product ? { product, quantity } : null
      })
      .filter(Boolean)

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
  }, [cartItems, catalogProducts, category, searchTerm, sort])

  const productsCanExpand = checkoutProducts.length > checkoutColumnCount

  function handleResetFilters() {
    setCategory('All')
    setSort('featured')
    setFilterOpen(false)
    setProductsExpanded(false)
  }

  async function handlePlaceOrder() {
    if (cartCount === 0) {
      return
    }

    setOrderError('')
    setPlacingOrder(true)

    try {
      await api.post('/api/orders', {
        deliveryAddress,
        items: Object.entries(cartItems).map(([plantPriceId, quantity]) => ({
          plantPriceId: Number(plantPriceId),
          quantity,
        })),
      })

      clearCart()
      setOrderPlaced(true)
    } catch (error) {
      setOrderError(errorMessageFor(error))
    } finally {
      setPlacingOrder(false)
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
      const response = await api.patch('/api/auth/profile/address', addressForm)
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

      <PageTitle label="Checkout" onBack={() => navigate('/home')} wide />

      <section className="checkout-shell">
        <UserSidebar />

        <div className="checkout-main">
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
                <CheckoutProductCard key={product.priceId} product={product} quantity={quantity} />
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
              disabled={cartCount === 0 || loadingProducts || placingOrder}
              onClick={handlePlaceOrder}
              type="button"
            >
              {placingOrder ? 'Ordering...' : 'Order'}
            </button>
            {orderError ? (
              <p className="checkout-order-error" role="alert">
                {orderError}
              </p>
            ) : null}
          </div>
        </div>
      </section>

      {orderPlaced ? (
        <div className="order-modal-backdrop" role="presentation">
          <section
            aria-labelledby="order-success-title"
            aria-modal="true"
            className="order-modal"
            role="dialog"
          >
            <h2 id="order-success-title">Order placed successfully!</h2>
            <p>returning home</p>
            <button
              className="order-modal-button"
              onClick={() => navigate('/home')}
              type="button"
            >
              Okay
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
