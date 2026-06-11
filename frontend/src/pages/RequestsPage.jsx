import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/client'
import HomeHeader from '../components/home/HomeHeader'
import PageTitle from '../components/home/PageTitle'
import UserSidebar from '../components/home/UserSidebar'
import RequestCard from '../components/requests/RequestCard'
import { useCart } from '../hooks/useCart'
import '../styles/home.css'
import '../styles/requests.css'

const statusOptions = ['All', 'Pending', 'Delivered', 'Cancelled']
const sortOptions = [
  { label: 'Newest', value: 'newest' },
  { label: 'Oldest', value: 'oldest' },
  { label: 'High to low', value: 'high-low' },
  { label: 'Low to high', value: 'low-high' },
]

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

function normalizeOrder(order) {
  return {
    ...order,
    date: formatDate(order.date),
    id: String(order.id),
    imageAlt: 'Order preview placeholder',
    rawDate: order.date,
    total: Number(order.total),
    items: (order.items || []).map((item) => ({
      ...item,
      price: Number(item.price),
      quantity: Number(item.quantity),
    })),
  }
}

function RequestsPage() {
  const navigate = useNavigate()
  const { cartCount } = useCart()
  const [requestOrders, setRequestOrders] = useState([])
  const [loadingOrders, setLoadingOrders] = useState(true)
  const [ordersError, setOrdersError] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [filterOpen, setFilterOpen] = useState(false)
  const [status, setStatus] = useState('All')
  const [sort, setSort] = useState('newest')
  const [selectedOrder, setSelectedOrder] = useState(null)

  useEffect(() => {
    let ignore = false

    async function loadOrders() {
      setLoadingOrders(true)
      setOrdersError('')

      try {
        const response = await api.get('/orders')

        if (!ignore) {
          setRequestOrders(response.data.map(normalizeOrder))
        }
      } catch {
        if (!ignore) {
          setOrdersError('Unable to load requests from the backend.')
        }
      } finally {
        if (!ignore) {
          setLoadingOrders(false)
        }
      }
    }

    loadOrders()

    return () => {
      ignore = true
    }
  }, [])

  const orders = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase()

    const filtered = requestOrders.filter((order) => {
      const itemText = order.items.map((item) => item.name).join(' ')
      const matchesStatus = status === 'All' || order.status === status
      const matchesSearch =
        !normalizedSearch ||
        order.date.toLowerCase().includes(normalizedSearch) ||
        order.status.toLowerCase().includes(normalizedSearch) ||
        itemText.toLowerCase().includes(normalizedSearch)

      return matchesStatus && matchesSearch
    })

    if (sort === 'oldest') {
      return [...filtered].sort((first, second) => new Date(first.rawDate) - new Date(second.rawDate))
    }

    if (sort === 'newest') {
      return [...filtered].sort((first, second) => new Date(second.rawDate) - new Date(first.rawDate))
    }

    if (sort === 'high-low') {
      return [...filtered].sort((first, second) => second.total - first.total)
    }

    if (sort === 'low-high') {
      return [...filtered].sort((first, second) => first.total - second.total)
    }

    return filtered
  }, [requestOrders, searchTerm, sort, status])

  function handleResetFilters() {
    setStatus('All')
    setSort('newest')
    setFilterOpen(false)
  }

  async function handleViewDetails(order) {
    try {
      const response = await api.get(`/api/orders/${order.id}`)
      setSelectedOrder(normalizeOrder(response.data))
    } catch {
      setSelectedOrder(order)
    }
  }

  return (
    <main className="home-page requests-page">
      <HomeHeader
        cartCount={cartCount}
        category={status}
        categoryLabel="Status"
        categoryOptions={statusOptions}
        filterOpen={filterOpen}
        onCategoryChange={(event) => setStatus(event.target.value)}
        onClearSearch={() => setSearchTerm('')}
        onResetFilters={handleResetFilters}
        onSearch={setSearchTerm}
        onSortChange={(event) => setSort(event.target.value)}
        onToggleFilter={() => setFilterOpen((current) => !current)}
        searchTerm={searchTerm}
        sort={sort}
        sortLabel="Sort"
        sortOptions={sortOptions}
      />

      <PageTitle label="Requests" onBack={() => navigate('/home')} />

      <section className="requests-shell">
        <UserSidebar />

        <div className="requests-list" aria-live="polite">
          {loadingOrders ? (
            <div className="requests-empty">Loading requests...</div>
          ) : ordersError ? (
            <div className="requests-empty">{ordersError}</div>
          ) : orders.length > 0 ? (
            orders.map((order) => (
              <RequestCard key={order.id} onViewDetails={handleViewDetails} order={order} />
            ))
          ) : (
            <div className="requests-empty">No requests match your filters.</div>
          )}
        </div>
      </section>

      {selectedOrder ? (
        <div className="request-modal-backdrop" role="presentation">
          <section
            aria-labelledby="request-details-title"
            aria-modal="true"
            className="request-modal"
            role="dialog"
          >
            <h2 id="request-details-title">{selectedOrder.date}</h2>
            <p className="request-modal-status">Status: {selectedOrder.status}</p>
            <div className="request-modal-items">
              {selectedOrder.items.map((item) => (
                <p key={`${selectedOrder.id}-modal-${item.name}`}>
                  {item.quantity} x {item.name}
                </p>
              ))}
            </div>
            <strong>Price: {selectedOrder.total}</strong>
            <button
              className="request-modal-button"
              onClick={() => setSelectedOrder(null)}
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

export default RequestsPage
