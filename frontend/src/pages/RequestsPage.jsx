import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import HomeHeader from '../components/home/HomeHeader'
import UserSidebar from '../components/home/UserSidebar'
import RequestCard from '../components/requests/RequestCard'
import { mockOrders } from '../data/orders'
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

function RequestsPage() {
  const navigate = useNavigate()
  const { cartCount } = useCart()
  const [searchTerm, setSearchTerm] = useState('')
  const [filterOpen, setFilterOpen] = useState(false)
  const [status, setStatus] = useState('All')
  const [sort, setSort] = useState('newest')
  const [selectedOrder, setSelectedOrder] = useState(null)

  const orders = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase()

    const filtered = mockOrders.filter((order) => {
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
      return [...filtered].reverse()
    }

    if (sort === 'high-low') {
      return [...filtered].sort((first, second) => second.total - first.total)
    }

    if (sort === 'low-high') {
      return [...filtered].sort((first, second) => first.total - second.total)
    }

    return filtered
  }, [searchTerm, sort, status])

  function handleResetFilters() {
    setStatus('All')
    setSort('newest')
    setFilterOpen(false)
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
        onSearchChange={(event) => setSearchTerm(event.target.value)}
        onSortChange={(event) => setSort(event.target.value)}
        onToggleFilter={() => setFilterOpen((current) => !current)}
        searchTerm={searchTerm}
        sort={sort}
        sortLabel="Sort"
        sortOptions={sortOptions}
      />

      <section className="requests-title-row">
        <button className="home-back-button" onClick={() => navigate('/home')} type="button" aria-label="Back">
          <ArrowBackIcon fontSize="inherit" />
        </button>
        <h1>Requests</h1>
      </section>

      <section className="requests-shell">
        <UserSidebar />

        <div className="requests-list" aria-live="polite">
          {orders.length > 0 ? (
            orders.map((order) => (
              <RequestCard key={order.id} onViewDetails={setSelectedOrder} order={order} />
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
