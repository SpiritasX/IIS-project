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
import { normalizeProduct } from '../utils/products'
import { trackPlantSearch } from '../utils/searchTracking'

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

function plantCountLabel(count) {
  return `${count} ${count === 1 ? 'plant' : 'plants'}`
}

const homeSortOptions = [
  { label: 'Recommended', value: 'recommended' },
  { label: 'Price', value: 'price' },
  { label: 'Name', value: 'name' },
]

function searchPathForFilters({ maxPrice, minPrice, order, plantType, query, sort, species, variety }) {
  const params = new URLSearchParams()
  const normalizedQuery = query.trim()

  if (normalizedQuery) {
    params.set('q', normalizedQuery)
  }
  if (plantType !== 'All') {
    params.set('plant_type', plantType)
  }
  if (species.trim()) {
    params.set('species', species.trim())
  }
  if (variety.trim()) {
    params.set('variety', variety.trim())
  }
  if (minPrice !== '') {
    params.set('min_price', minPrice)
  }
  if (maxPrice !== '') {
    params.set('max_price', maxPrice)
  }
  if (sort !== 'recommended') {
    params.set('sort_by', sort)
  }
  if (order !== 'asc') {
    params.set('order', order)
  }

  const queryString = params.toString()
  return queryString ? `/search?${queryString}` : '/search'
}

function RecommendationList({
  countLabel,
  emptyText,
  loading,
  onAddToCart,
  onRemoveFromCart,
  products,
  quantities,
  title,
  titleId,
  tone,
}) {
  return (
    <section
      className={`recommendation-section recommendation-section-${tone}`}
      aria-labelledby={titleId}
    >
      <div className="recommendation-section-header">
        <h2 id={titleId}>{title}</h2>
        {!loading && products.length > 0 ? (
          <span className="recommendation-count">{countLabel}</span>
        ) : null}
      </div>

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
  const [seasonalRecommendations, setSeasonalRecommendations] = useState([])
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
        const [catalogResponse, trendingResponse, seasonalResponse, personalResponse] = await Promise.all([
          api.get('/plants'),
          api.get('/recommendations/trending'),
          api.get('/recommendations/seasonal'),
          personalRequest,
        ])

        if (!ignore) {
          setCatalogProducts(catalogResponse.data.map(normalizeProduct))
          setTrendingRecommendations(trendingResponse.data)
          setSeasonalRecommendations(seasonalResponse.data)
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
    const types = catalogProducts.map((product) => product.type || product.category).filter(Boolean)
    return ['All', ...new Set(types)]
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
      const matchesCategory = category === 'All' || product.type === category || product.category === category
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
  const seasonalProducts = useMemo(
    () => productsForRecommendations(seasonalRecommendations, filteredProductsById, sort, order),
    [filteredProductsById, order, seasonalRecommendations, sort],
  )
  const personalProducts = useMemo(
    () => productsForRecommendations(personalRecommendations, filteredProductsById, sort, order),
    [filteredProductsById, order, personalRecommendations, sort],
  )
  const showPersonalRecommendations = loadingProducts ? Boolean(user?.id) : personalRecommendations.length > 0

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

    void trackPlantSearch(api, user?.id, {
      query: value,
      minPrice,
      maxPrice,
      variety,
      species,
      plantType: category,
    })

    navigate(
      searchPathForFilters({
        query: value,
        plantType: category,
        species,
        variety,
        minPrice,
        maxPrice,
        sort,
        order,
      }),
    )
  }

  return (
    <main className="home-page">
      <HomeHeader
        cartCount={cartCount}
        category={category}
        categoryLabel="Type"
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

        <div className="recommendation-sections" aria-live="polite">
          {productError ? (
            <div className="product-empty recommendation-grid-message">{productError}</div>
          ) : (
            <>
              {showPersonalRecommendations ? (
                <RecommendationList
                  countLabel={plantCountLabel(personalProducts.length)}
                  emptyText="No personal recommendations match your filters."
                  loading={loadingProducts}
                  onAddToCart={addToCart}
                  onRemoveFromCart={removeFromCart}
                  products={personalProducts}
                  quantities={cartItems}
                  title="Personalized for you"
                  titleId="personal-recommendations-title"
                  tone="personal"
                />
              ) : null}
              <RecommendationList
                countLabel={plantCountLabel(trendingProducts.length)}
                emptyText="No trending plants match your filters."
                loading={loadingProducts}
                onAddToCart={addToCart}
                onRemoveFromCart={removeFromCart}
                products={trendingProducts}
                quantities={cartItems}
                title="Trending plants"
                titleId="trending-plants-title"
                tone="trending"
              />
              <RecommendationList
                countLabel={plantCountLabel(seasonalProducts.length)}
                emptyText="No seasonal plants match your filters."
                loading={loadingProducts}
                onAddToCart={addToCart}
                onRemoveFromCart={removeFromCart}
                products={seasonalProducts}
                quantities={cartItems}
                title="Seasonal plants"
                titleId="seasonal-plants-title"
                tone="seasonal"
              />
            </>
          )}
        </div>
      </section>
    </main>
  )
}

export default HomePage
