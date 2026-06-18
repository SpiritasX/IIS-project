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

const homeSortOptions = [
  { label: 'Recommended', value: 'recommended' },
  { label: 'Price', value: 'price' },
  { label: 'Name', value: 'name' },
]

function normalizeProduct(product) {
  const available =
    typeof product.available === 'boolean'
      ? product.available
      : product.availability === undefined
        ? true
        : Number(product.availability) > 0
  const id = String(product.id)

  return {
    ...product,
    available,
    category: product.category || product.plantTypeName || 'Other',
    description: product.description || '',
    id,
    imageAlt: `${product.name} placeholder`,
    price: Number(product.price || 0),
    priceId: String(product.priceId || product.id),
    species: product.species || product.speciesName || '',
    variety: product.variety || product.varietyName || '',
  }
}

function includesText(value, searchValue) {
  return !searchValue || String(value || '').toLowerCase().includes(searchValue)
}

function optionalIncludesText(value, searchValue) {
  return !searchValue || !value || String(value).toLowerCase().includes(searchValue)
}

function sortProducts(products, sort, order) {
  if (sort === 'recommended') {
    return products
  }

  const sortedProducts = [...products].sort((first, second) => {
    if (sort === 'name') {
      return first.name.localeCompare(second.name)
    }

    return first.price - second.price
  })

  return order === 'desc' ? sortedProducts.reverse() : sortedProducts
}

function productsForRecommendations(recommendations, productsById, sort, order) {
  const products = recommendations
    .map((recommendation) => productsById.get(String(recommendation.id)))
    .filter(Boolean)

  return sortProducts(products, sort, order)
}

function RecommendationList({
  emptyText,
  loading,
  onAddToCart,
  onRemoveFromCart,
  products,
  quantities,
  title,
  titleId,
}) {
  return (
    <section className="recommendation-section" aria-labelledby={titleId}>
      <h2 id={titleId}>{title}</h2>

      <div className="recommendation-list">
        {loading ? (
          <div className="product-empty">Loading products...</div>
        ) : products.length > 0 ? (
          products.map((product) => (
            <ProductCard
              key={product.priceId}
              onAddToCart={onAddToCart}
              onRemoveFromCart={onRemoveFromCart}
              product={product}
              quantity={quantities[product.priceId] || 0}
            />
          ))
        ) : (
          <div className="product-empty">{emptyText}</div>
        )}
      </div>
    </section>
  )
}

function HomePage() {
  const navigate = useNavigate()
  const { logout, user } = useAuth()
  const [catalogProducts, setCatalogProducts] = useState([])
  const [trendingRecommendations, setTrendingRecommendations] = useState([])
  const [personalRecommendations, setPersonalRecommendations] = useState([])
  const [loadingProducts, setLoadingProducts] = useState(true)
  const [productError, setProductError] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [filterOpen, setFilterOpen] = useState(false)
  const [category, setCategory] = useState('All')
  const [sort, setSort] = useState('recommended')
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
        const personalRequest = user?.id
          ? api.get(`/recommendations/customer/${user.id}`)
          : Promise.resolve({ data: [] })
        const [catalogResponse, trendingResponse, personalResponse] = await Promise.all([
          api.get('/plants'),
          api.get('/recommendations/'),
          personalRequest,
        ])

        if (!ignore) {
          setCatalogProducts(catalogResponse.data.map(normalizeProduct))
          setTrendingRecommendations(trendingResponse.data)
          setPersonalRecommendations(personalResponse.data)
        }
      } catch {
        if (!ignore) {
          setProductError('Unable to load recommendations from the backend.')
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
  }, [user?.id])

  const categoryOptions = useMemo(() => {
    const categories = catalogProducts.map((product) => product.category).filter(Boolean)
    return ['All', ...new Set(categories)]
  }, [catalogProducts])

  const filteredProductsById = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase()
    const normalizedSpecies = species.trim().toLowerCase()
    const normalizedVariety = variety.trim().toLowerCase()
    const minimumPrice = minPrice === '' ? null : Number(minPrice)
    const maximumPrice = maxPrice === '' ? null : Number(maxPrice)
    const filteredProducts = catalogProducts.filter((product) => {
      const matchesSearch =
        includesText(product.name, normalizedSearch) ||
        includesText(product.description, normalizedSearch)
      const matchesCategory = category === 'All' || product.category === category
      const matchesSpecies = optionalIncludesText(product.species, normalizedSpecies)
      const matchesVariety = optionalIncludesText(product.variety, normalizedVariety)
      const matchesMinimumPrice = minimumPrice === null || product.price >= minimumPrice
      const matchesMaximumPrice = maximumPrice === null || product.price <= maximumPrice

      return (
        matchesSearch &&
        matchesCategory &&
        matchesSpecies &&
        matchesVariety &&
        matchesMinimumPrice &&
        matchesMaximumPrice
      )
    })

    return new Map(filteredProducts.map((product) => [product.id, product]))
  }, [catalogProducts, category, maxPrice, minPrice, searchTerm, species, variety])

  const trendingProducts = useMemo(
    () => productsForRecommendations(trendingRecommendations, filteredProductsById, sort, order),
    [filteredProductsById, order, sort, trendingRecommendations],
  )
  const personalProducts = useMemo(
    () => productsForRecommendations(personalRecommendations, filteredProductsById, sort, order),
    [filteredProductsById, order, personalRecommendations, sort],
  )

  async function handleBack() {
    await logout()
    navigate('/login')
  }

  function handleResetFilters() {
    setCategory('All')
    setSort('recommended')
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
        sortOptions={homeSortOptions}
        order={order}
        species={species}
        variety={variety}
        minPrice={minPrice}
        maxPrice={maxPrice}
      />

      <PageTitle label="Home" onBack={handleBack} />

      <section className="home-body">
        <UserSidebar />

        <div className="recommendation-columns" aria-live="polite">
          {productError ? (
            <div className="product-empty recommendation-grid-message">{productError}</div>
          ) : (
            <>
              <RecommendationList
                emptyText="No trending plants match your filters."
                loading={loadingProducts}
                onAddToCart={addToCart}
                onRemoveFromCart={removeFromCart}
                products={trendingProducts}
                quantities={cartItems}
                title="Trending and seasonal plants"
                titleId="trending-plants-title"
              />
              <RecommendationList
                emptyText="No personal recommendations match your filters."
                loading={loadingProducts}
                onAddToCart={addToCart}
                onRemoveFromCart={removeFromCart}
                products={personalProducts}
                quantities={cartItems}
                title="Personal recommendations"
                titleId="personal-recommendations-title"
              />
            </>
          )}
        </div>
      </section>
    </main>
  )
}

export default HomePage
