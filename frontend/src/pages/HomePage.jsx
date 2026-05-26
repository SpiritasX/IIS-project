import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import HomeHeader from '../components/home/HomeHeader'
import ProductCard from '../components/home/ProductCard'
import UserSidebar from '../components/home/UserSidebar'
import { mockProducts } from '../data/products'
import { useAuth } from '../hooks/useAuth'
import { useCart } from '../hooks/useCart'
import '../styles/home.css'

function HomePage() {
  const navigate = useNavigate()
  const { logout } = useAuth()
  const [searchTerm, setSearchTerm] = useState('')
  const [filterOpen, setFilterOpen] = useState(false)
  const [category, setCategory] = useState('All')
  const [sort, setSort] = useState('featured')
  const { addToCart, cartCount, cartItems, removeFromCart } = useCart()

  const products = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase()

    const filtered = mockProducts.filter((product) => {
      const matchesCategory = category === 'All' || product.category === category
      const matchesSearch =
        !normalizedSearch ||
        product.name.toLowerCase().includes(normalizedSearch) ||
        product.description.toLowerCase().includes(normalizedSearch)

      return matchesCategory && matchesSearch
    })

    if (sort === 'low-high') {
      return [...filtered].sort((first, second) => first.price - second.price)
    }

    if (sort === 'high-low') {
      return [...filtered].sort((first, second) => second.price - first.price)
    }

    return filtered
  }, [category, searchTerm, sort])

  function handleBack() {
    logout()
    navigate('/login')
  }

  function handleResetFilters() {
    setCategory('All')
    setSort('featured')
    setFilterOpen(false)
  }

  return (
    <main className="home-page">
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

      <section className="home-title-row">
        <button className="home-back-button" onClick={handleBack} type="button" aria-label="Back">
          <ArrowBackIcon fontSize="inherit" />
        </button>
        <h1>Home</h1>
      </section>

      <section className="home-body">
        <UserSidebar />

        <div className="product-list" aria-live="polite">
          {products.length > 0 ? (
            products.map((product) => (
              <ProductCard
                key={product.id}
                onAddToCart={addToCart}
                onRemoveFromCart={removeFromCart}
                product={product}
                quantity={cartItems[product.id] || 0}
              />
            ))
          ) : (
            <div className="product-empty">No products match your filters.</div>
          )}
        </div>
      </section>
    </main>
  )
}

export default HomePage
