import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/client'
import HomeHeader from '../components/home/HomeHeader'
import PageTitle from '../components/home/PageTitle'
import ProductCard from '../components/home/ProductCard'
import UserSidebar from '../components/home/UserSidebar'
import { useAuth } from '../hooks/useAuth'
import { useCart } from '../hooks/useCart'
import '../styles/home.css'

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

function HomePage() {
  const navigate = useNavigate()
  const { logout } = useAuth()
  const [catalogProducts, setCatalogProducts] = useState([])
  const [loadingProducts, setLoadingProducts] = useState(true)
  const [productError, setProductError] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [filterOpen, setFilterOpen] = useState(false)
  const [category, setCategory] = useState('All')
  const [sort, setSort] = useState('featured')
  const { addToCart, cartCount, cartItems, removeFromCart } = useCart()

  useEffect(() => {
    let ignore = false

    async function loadProducts() {
      setLoadingProducts(true)
      setProductError('')

      try {
        const response = await api.get('/plants')

        if (!ignore) {
          setCatalogProducts(response.data.map(normalizeProduct))
        }
      } catch {
        if (!ignore) {
          setProductError('Unable to load products from the backend.')
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
  }, [])

  const categoryOptions = useMemo(() => {
    const categories = catalogProducts.map((product) => product.category).filter(Boolean)
    return ['All', ...new Set(categories)]
  }, [catalogProducts])

  const products = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase()

    const filtered = catalogProducts.filter((product) => {
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
  }, [catalogProducts, category, searchTerm, sort])

  async function handleBack() {
    await logout()
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
        categoryOptions={categoryOptions}
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

      <PageTitle label="Home" onBack={handleBack} />

      <section className="home-body">
        <UserSidebar />

        <div className="product-list" aria-live="polite">
          {loadingProducts ? (
            <div className="product-empty">Loading products...</div>
          ) : productError ? (
            <div className="product-empty">{productError}</div>
          ) : products.length > 0 ? (
            products.map((product) => (
              <ProductCard
                key={product.priceId}
                onAddToCart={addToCart}
                onRemoveFromCart={removeFromCart}
                product={product}
                quantity={cartItems[product.priceId] || 0}
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
