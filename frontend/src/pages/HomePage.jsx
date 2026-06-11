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
  const { logout, user } = useAuth()
  const [catalogProducts, setCatalogProducts] = useState([])
  const [loadingProducts, setLoadingProducts] = useState(true)
  const [productError, setProductError] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [filterOpen, setFilterOpen] = useState(false)
  const [category, setCategory] = useState('All')
  const [sort, setSort] = useState('price')
  const [order, setOrder] = useState('asc')
  const [species, setSpecies] = useState('')
  const [variety, setVariety] = useState('')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const { addToCart, cartCount, cartItems, removeFromCart } = useCart()

  useEffect(() => {
    let ignore = false

    async function loadProducts() {
      setLoadingProducts(true)
      setProductError('')

      try {
        const response = await api.get('/search/search/plants', {
          params: {
            query: searchTerm || undefined,
            plant_type: category !== 'All' ? category : undefined,
            species: species || undefined,
            variety: variety || undefined,
            min_price: minPrice || undefined,
            max_price: maxPrice || undefined,
            sort_by: sort,
            order,
          },
        })

        if (!ignore) {
          setCatalogProducts(response.data['hits'].map(normalizeProduct))
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
  }, [searchTerm, category, species, variety, minPrice, maxPrice, sort, order])

  const categoryOptions = useMemo(() => {
    const categories = catalogProducts.map((product) => product.category).filter(Boolean)
    return ['All', ...new Set(categories)]
  }, [catalogProducts])

  const products = catalogProducts

  async function handleBack() {
    await logout()
    navigate('/login')
  }

  function handleResetFilters() {
    setCategory('All')
    setSort('price')
    setOrder('asc')
    setSpecies('')
    setVariety('')
    setMinPrice('')
    setMaxPrice('')
    setFilterOpen(false)
  }

  function handleSearch(value) {
    setSearchTerm(value)

    if (!user?.id) {
      return
    }

    api
      .post('/recommendations/search', {
        customer_id: user.id,
        query: value,
        min_price: minPrice ? Number(minPrice) : null,
        max_price: maxPrice ? Number(maxPrice) : null,
        variety: variety || null,
        species: species || null,
        type: category !== 'All' ? category : null,
      })
      .catch(() => {
        // Recommendation tracking should not block product search results.
      })
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
        onSearch={handleSearch}
        onSortChange={(event) => setSort(event.target.value)}
        onOrderChange={(event) => setOrder(event.target.value)}
        onSpeciesChange={(event) => setSpecies(event.target.value)}
        onVarietyChange={(event) => setVariety(event.target.value)}
        onMinPriceChange={(event) => setMinPrice(event.target.value)}
        onMaxPriceChange={(event) => setMaxPrice(event.target.value)}
        onToggleFilter={() => setFilterOpen((current) => !current)}
        searchTerm={searchTerm}
        sort={sort}
        order={order}
        species={species}
        variety={variety}
        minPrice={minPrice}
        maxPrice={maxPrice}
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
