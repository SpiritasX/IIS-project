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

const statusOptions = [
  'All',
  'Ponuda',
  'Rezervacija',
  'Spremno',
  'Isporuka',
  'Isporuceno',
  'Odbijeno',
  'Isteklo',
  'Otkazano',
]
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

function formatDateTime(value) {
  if (!value) {
    return ''
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('en', {
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(date)
}

function errorMessageFor(error) {
  return (
    error.response?.data?.detail ||
    error.response?.data?.message ||
    error.response?.data?.error ||
    'Unable to update the request right now.'
  )
}

function normalizeOrder(order) {
  return {
    ...order,
    date: formatDate(order.date),
    expiresAt: formatDateTime(order.expiresAt),
    id: String(order.id),
    imageAlt: 'Order preview placeholder',
    phaseHistory: (order.phaseHistory || []).map((phase) => ({
      ...phase,
      endTime: formatDateTime(phase.endTime),
      startTime: formatDateTime(phase.startTime),
    })),
    rawDate: order.date,
    total: Number(order.total || 0),
    items: (order.items || []).map((item) => ({
      ...item,
      adjusted: Boolean(item.adjusted),
      offeredQuantity: Number(item.offeredQuantity ?? item.quantity ?? 0),
      price: Number(item.price || 0),
      priceId: String(item.priceId),
      quantity: Number(item.quantity ?? item.offeredQuantity ?? 0),
      requestedQuantity: Number(item.requestedQuantity ?? item.quantity ?? 0),
      reservedQuantity: Number(item.reservedQuantity || 0),
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
  const [actionError, setActionError] = useState('')
  const [actionLoading, setActionLoading] = useState(false)
  const [cancelReason, setCancelReason] = useState('')

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
        (order.currentPhase || '').toLowerCase().includes(normalizedSearch) ||
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

  function updateOrder(order) {
    setRequestOrders((current) => current.map((item) => (item.id === order.id ? order : item)))
    setSelectedOrder(order)
  }

  function handleResetFilters() {
    setStatus('All')
    setSort('newest')
    setFilterOpen(false)
  }

  function handleModalBackdropMouseDown(event) {
    if (event.target === event.currentTarget) {
      setSelectedOrder(null)
    }
  }

  async function handleViewDetails(order) {
    setActionError('')
    setCancelReason('')

    try {
      const response = await api.get(`/orders/${order.id}`)
      const normalized = normalizeOrder(response.data)
      updateOrder(normalized)
    } catch {
      setSelectedOrder(order)
    }
  }

  async function handleOrderAction(action) {
    if (!selectedOrder) {
      return
    }

    setActionError('')
    setActionLoading(true)

    try {
      const response = await api.post(`/orders/${selectedOrder.id}/${action}`)
      updateOrder(normalizeOrder(response.data))
      setCancelReason('')
    } catch (error) {
      setActionError(errorMessageFor(error))
      try {
        const response = await api.get(`/orders/${selectedOrder.id}`)
        updateOrder(normalizeOrder(response.data))
      } catch {
        // Keep the current modal state if refresh fails.
      }
    } finally {
      setActionLoading(false)
    }
  }

  async function handleCancelSubmit(event) {
    event.preventDefault()

    if (!selectedOrder) {
      return
    }

    if (!cancelReason.trim()) {
      setActionError('Cancellation reason is required.')
      return
    }

    setActionError('')
    setActionLoading(true)

    try {
      const response = await api.post(`/orders/${selectedOrder.id}/cancel`, {
        reason: cancelReason.trim(),
      })
      updateOrder(normalizeOrder(response.data))
      setCancelReason('')
    } catch (error) {
      setActionError(errorMessageFor(error))
      try {
        const response = await api.get(`/orders/${selectedOrder.id}`)
        updateOrder(normalizeOrder(response.data))
      } catch {
        // Keep the current modal state if refresh fails.
      }
    } finally {
      setActionLoading(false)
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
        onSearchChange={(event) => setSearchTerm(event.target.value)}
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
        <div className="request-modal-backdrop" onMouseDown={handleModalBackdropMouseDown} role="presentation">
          <section
            aria-labelledby="request-details-title"
            aria-modal="true"
            className="request-modal request-modal-wide"
            role="dialog"
          >
            <h2 id="request-details-title">{selectedOrder.date}</h2>
            <p className="request-modal-status">
              Status: {selectedOrder.status}
              {selectedOrder.currentPhase ? ` / ${selectedOrder.currentPhase}` : ''}
            </p>
            {selectedOrder.currentPhase === 'Ponuda' && selectedOrder.expiresAt ? (
              <p className="request-modal-muted">Valid until {selectedOrder.expiresAt}</p>
            ) : null}
            <div className="request-modal-items">
              {selectedOrder.items.map((item) => (
                <div className="request-modal-item" key={`${selectedOrder.id}-modal-${item.priceId}`}>
                  <strong>{item.name}</strong>
                  <span>Amount: {item.offeredQuantity}</span>
                  {item.adjusted ? <em>Adjusted</em> : null}
                </div>
              ))}
            </div>
            <strong>Price: {selectedOrder.total}</strong>

            {selectedOrder.phaseHistory.length > 0 ? (
              <div className="request-phase-history">
                <h3>Phase history</h3>
                {selectedOrder.phaseHistory.map((phase) => (
                  <p
                    className={
                      phase.name === selectedOrder.currentPhase && !phase.endTime
                        ? 'request-phase-current'
                        : undefined
                    }
                    key={`${selectedOrder.id}-phase-${phase.id || phase.name}`}
                  >
                    <strong>{phase.name}</strong>
                    <span>
                      {phase.startTime}
                      {phase.endTime ? ` - ${phase.endTime}` : ''}
                    </span>
                    {phase.cancellationReason ? <em>{phase.cancellationReason}</em> : null}
                    {phase.cancellationDetail ? <span>{phase.cancellationDetail}</span> : null}
                  </p>
                ))}
              </div>
            ) : null}

            {actionError ? (
              <p className="request-action-error" role="alert">
                {actionError}
              </p>
            ) : null}

            <div className="request-modal-actions">
              {selectedOrder.canAccept ? (
                <button
                  className="request-modal-button"
                  disabled={actionLoading}
                  onClick={() => handleOrderAction('accept')}
                  type="button"
                >
                  {actionLoading ? 'Working...' : 'Accept offer'}
                </button>
              ) : null}
              {selectedOrder.canReject ? (
                <button
                  className="request-modal-secondary"
                  disabled={actionLoading}
                  onClick={() => handleOrderAction('reject')}
                  type="button"
                >
                  Reject offer
                </button>
              ) : null}
              <button
                className="request-modal-secondary"
                onClick={() => setSelectedOrder(null)}
                type="button"
              >
                Close
              </button>
            </div>

            {selectedOrder.canCancel ? (
              <form className="request-cancel-form" onSubmit={handleCancelSubmit}>
                <label>
                  <span>Cancellation reason</span>
                  <textarea
                    onChange={(event) => setCancelReason(event.target.value)}
                    rows="3"
                    value={cancelReason}
                  />
                </label>
                <button className="request-modal-secondary" disabled={actionLoading} type="submit">
                  Cancel request
                </button>
              </form>
            ) : null}
          </section>
        </div>
      ) : null}
    </main>
  )
}

export default RequestsPage
