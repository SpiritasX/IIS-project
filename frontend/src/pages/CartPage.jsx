import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown'
import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import AddressCard from '../components/cart/AddressCard'
import CheckoutProductCard from '../components/cart/CheckoutProductCard'
import HomeHeader from '../components/home/HomeHeader'
import UserSidebar from '../components/home/UserSidebar'
import { mockProducts } from '../data/products'
import { useCart } from '../hooks/useCart'
import '../styles/home.css'
import '../styles/cart.css'

function CartPage() {
  const navigate = useNavigate()
  const { cartCount, cartItems } = useCart()
  const [searchTerm, setSearchTerm] = useState('')
  const [filterOpen, setFilterOpen] = useState(false)
  const [category, setCategory] = useState('All')
  const [sort, setSort] = useState('featured')
  const [orderPlaced, setOrderPlaced] = useState(false)

  const checkoutProducts = useMemo(() => {
    const selectedProducts = Object.entries(cartItems)
      .map(([productId, quantity]) => {
        const product = mockProducts.find((item) => item.id === productId)

        return product ? { product, quantity } : null
      })
      .filter(Boolean)

    const baseProducts =
      selectedProducts.length > 0
        ? selectedProducts
        : mockProducts.slice(0, 4).map((product) => ({ product, quantity: 1 }))

    const normalizedSearch = searchTerm.trim().toLowerCase()

    const filtered = baseProducts.filter(({ product }) => {
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
  }, [cartItems, category, searchTerm, sort])

  function handleResetFilters() {
    setCategory('All')
    setSort('featured')
    setFilterOpen(false)
  }

  return (
    <main className="home-page checkout-page">
      <HomeHeader
        cartCount={cartCount}
        category={category}
        filterOpen={filterOpen}
        onCategoryChange={(event) => setCategory(event.target.value)}
        onClearSearch={() => setSearchTerm('')}
        onResetFilters={handleResetFilters}
        onSearchChange={(event) => setSearchTerm(event.target.value)}
        onSortChange={(event) => setSort(event.target.value)}
        onToggleFilter={() => setFilterOpen((current) => !current)}
        searchTerm={searchTerm}
        sort={sort}
      />

      <section className="checkout-title-row">
        <button className="home-back-button" onClick={() => navigate('/home')} type="button" aria-label="Back">
          <ArrowBackIcon fontSize="inherit" />
        </button>
        <h1>Checkout</h1>
      </section>

      <section className="checkout-shell">
        <UserSidebar />

        <div className="checkout-main">
          <h2>Products</h2>

          <div className="checkout-product-grid">
            {checkoutProducts.map(({ product, quantity }) => (
              <CheckoutProductCard key={product.id} product={product} quantity={quantity} />
            ))}
          </div>

          <div className="checkout-down-cue" aria-hidden="true">
            <KeyboardArrowDownIcon fontSize="inherit" />
          </div>

          <h2>Delivery Address</h2>
          <AddressCard />

          <div className="checkout-order-actions">
            <button
              className="checkout-order-button"
              onClick={() => setOrderPlaced(true)}
              type="button"
            >
              Order
            </button>
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
    </main>
  )
}

export default CartPage
