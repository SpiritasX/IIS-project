import { useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import api from '../api/client'
import HomeHeader from '../components/home/HomeHeader'
import PageTitle from '../components/home/PageTitle'
import ProductCard from '../components/home/ProductCard'
import UserSidebar from '../components/home/UserSidebar'
import { useAuth } from '../hooks/useAuth'
import { useCart } from '../hooks/useCart'
import '../styles/home.css'
import '../styles/search.css'
import { normalizeProduct } from '../utils/products'
import { trackPlantSearch } from '../utils/searchTracking'

const defaultFilters = {
  query: '',
  plantType: 'All',
  species: '',
  variety: '',
  minPrice: '',
  maxPrice: '',
  sort: 'price',
  order: 'asc',
}

const searchSortOptions = [
  { label: 'Price', value: 'price' },
  { label: 'Name', value: 'name' },
  { label: 'Newest', value: 'createdAt' },
]

function filtersFromSearch(search) {
  const params = new URLSearchParams(search)

  return {
    query: params.get('q') || '',
    plantType: params.get('plant_type') || 'All',
    species: params.get('species') || '',
    variety: params.get('variety') || '',
    minPrice: params.get('min_price') || '',
    maxPrice: params.get('max_price') || '',
    sort: params.get('sort_by') || 'price',
    order: params.get('order') || 'asc',
  }
}

function searchPathForFilters(filters) {
  const params = new URLSearchParams()
  const normalizedQuery = filters.query.trim()

  if (normalizedQuery) {
    params.set('q', normalizedQuery)
  }
  if (filters.plantType !== 'All') {
    params.set('plant_type', filters.plantType)
  }
  if (filters.species.trim()) {
    params.set('species', filters.species.trim())
  }
  if (filters.variety.trim()) {
    params.set('variety', filters.variety.trim())
  }
  if (filters.minPrice !== '') {
    params.set('min_price', filters.minPrice)
  }
  if (filters.maxPrice !== '') {
    params.set('max_price', filters.maxPrice)
  }
  if (filters.sort !== 'price') {
    params.set('sort_by', filters.sort)
  }
  if (filters.order !== 'asc') {
    params.set('order', filters.order)
  }

  const queryString = params.toString()
  return queryString ? `/search?${queryString}` : '/search'
}

function requestParamsForFilters(filters) {
  const params = {
    page: 1,
    page_size: 100,
    sort_by: filters.sort,
    order: filters.order,
  }

  if (filters.query.trim()) {
    params.query = filters.query.trim()
  }
  if (filters.plantType !== 'All') {
    params.plant_type = filters.plantType
  }
  if (filters.species.trim()) {
    params.species = filters.species.trim()
  }
  if (filters.variety.trim()) {
    params.variety = filters.variety.trim()
  }
  if (filters.minPrice !== '') {
    params.min_price = filters.minPrice
  }
  if (filters.maxPrice !== '') {
    params.max_price = filters.maxPrice
  }

  return params
}

function totalFromResponse(data, hits) {
  if (typeof data?.total === 'number') {
    return data.total
  }

  if (typeof data?.total?.value === 'number') {
    return data.total.value
  }

  return hits.length
}

function resultCountLabel(total, visibleCount) {
  const totalLabel = `${total} ${total === 1 ? 'plant' : 'plants'}`

  if (visibleCount < total) {
    return `Showing ${visibleCount} of ${totalLabel}`
  }

  return `${totalLabel} found`
}

function selectOptionsWithCurrent(options, currentValue) {
  return ['All', ...new Set([...options, currentValue].filter((item) => item && item !== 'All'))]
}

function SearchResultsPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { user } = useAuth()
  const { addToCart, cartCount, cartItems, removeFromCart } = useCart()
  const [products, setProducts] = useState([])
  const [totalResults, setTotalResults] = useState(0)
  const [loadingResults, setLoadingResults] = useState(true)
  const [searchError, setSearchError] = useState('')
  const [typeOptions, setTypeOptions] = useState([])
  const [filterOpen, setFilterOpen] = useState(false)
  const [query, setQuery] = useState(defaultFilters.query)
  const [plantType, setPlantType] = useState(defaultFilters.plantType)
  const [species, setSpecies] = useState(defaultFilters.species)
  const [variety, setVariety] = useState(defaultFilters.variety)
  const [minPrice, setMinPrice] = useState(defaultFilters.minPrice)
  const [maxPrice, setMaxPrice] = useState(defaultFilters.maxPrice)
  const [sort, setSort] = useState(defaultFilters.sort)
  const [order, setOrder] = useState(defaultFilters.order)

  useEffect(() => {
    const nextFilters = filtersFromSearch(location.search)

    setQuery(nextFilters.query)
    setPlantType(nextFilters.plantType)
    setSpecies(nextFilters.species)
    setVariety(nextFilters.variety)
    setMinPrice(nextFilters.minPrice)
    setMaxPrice(nextFilters.maxPrice)
    setSort(nextFilters.sort)
    setOrder(nextFilters.order)
  }, [location.search])

  useEffect(() => {
    let ignore = false

    async function loadOptions() {
      try {
        const response = await api.get('/plants/options')

        if (!ignore) {
          setTypeOptions(response.data.types || [])
        }
      } catch {
        if (!ignore) {
          setTypeOptions([])
        }
      }
    }

    loadOptions()

    return () => {
      ignore = true
    }
  }, [])

  useEffect(() => {
    let ignore = false
    const activeFilters = filtersFromSearch(location.search)

    async function loadResults() {
      setLoadingResults(true)
      setSearchError('')

      try {
        const response = await api.get('/search/search/plants', {
          params: requestParamsForFilters(activeFilters),
        })
        const hits = Array.isArray(response.data) ? response.data : response.data.hits || []

        if (!ignore) {
          setProducts(hits.map(normalizeProduct))
          setTotalResults(totalFromResponse(response.data, hits))
        }
      } catch {
        if (!ignore) {
          setProducts([])
          setTotalResults(0)
          setSearchError('Unable to load search results from Elasticsearch.')
        }
      } finally {
        if (!ignore) {
          setLoadingResults(false)
        }
      }
    }

    loadResults()

    return () => {
      ignore = true
    }
  }, [location.search])

  const typeSelectOptions = useMemo(
    () => selectOptionsWithCurrent(typeOptions, plantType),
    [plantType, typeOptions],
  )
  const resultSummary = resultCountLabel(totalResults, products.length)

  function currentFilters(nextQuery = query) {
    return {
      query: nextQuery,
      plantType,
      species,
      variety,
      minPrice,
      maxPrice,
      sort,
      order,
    }
  }

  function handleSearch(nextQuery) {
    const nextFilters = currentFilters(nextQuery)

    void trackPlantSearch(api, user?.id, nextFilters)
    navigate(searchPathForFilters(nextFilters))
  }

  function handleResetFilters() {
    setQuery(defaultFilters.query)
    setPlantType(defaultFilters.plantType)
    setSpecies(defaultFilters.species)
    setVariety(defaultFilters.variety)
    setMinPrice(defaultFilters.minPrice)
    setMaxPrice(defaultFilters.maxPrice)
    setSort(defaultFilters.sort)
    setOrder(defaultFilters.order)
    setFilterOpen(false)
    navigate('/search')
  }

  return (
    <main className="home-page search-page">
      <HomeHeader
        cartCount={cartCount}
        category={plantType}
        categoryLabel="Type"
        categoryOptions={typeSelectOptions}
        filterOpen={filterOpen}
        onCategoryChange={(event) => setPlantType(event.target.value)}
        onClearSearch={() => setQuery('')}
        onResetFilters={handleResetFilters}
        onSearch={handleSearch}
        onSortChange={(event) => setSort(event.target.value)}
        onOrderChange={(event) => setOrder(event.target.value)}
        onSpeciesChange={(event) => setSpecies(event.target.value)}
        onVarietyChange={(event) => setVariety(event.target.value)}
        onMinPriceChange={(event) => setMinPrice(event.target.value)}
        onMaxPriceChange={(event) => setMaxPrice(event.target.value)}
        onToggleFilter={() => setFilterOpen((current) => !current)}
        searchTerm={query}
        sort={sort}
        sortOptions={searchSortOptions}
        order={order}
        species={species}
        variety={variety}
        minPrice={minPrice}
        maxPrice={maxPrice}
      />

      <PageTitle label="Search" onBack={() => navigate('/home')} />

      <section className="home-body search-shell">
        <UserSidebar />

        <div className="product-list search-results" aria-live="polite">
          <div className="search-summary">
            {loadingResults ? 'Loading products...' : resultSummary}
          </div>

          {searchError ? (
            <div className="product-empty">{searchError}</div>
          ) : loadingResults ? (
            <div className="product-empty">Loading products...</div>
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
            <div className="product-empty">No plants match your filters.</div>
          )}
        </div>
      </section>
    </main>
  )
}

export default SearchResultsPage
