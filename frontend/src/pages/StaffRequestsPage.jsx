import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/client'
import PageTitle from '../components/home/PageTitle'
import OrderHistory from '../components/requests/OrderHistory'
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

const actionLabels = {
  CANCEL: 'Cancel process',
  MARK_READY: 'Mark ready',
  START_DELIVERY: 'Start delivery',
}

function formatDate(value) {
  if (!value) {
    return ''
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
    'Unable to update the process right now.'
  )
}

function normalizeProcess(process) {
  return {
    ...process,
    allowedActions: process.allowedActions || [],
    endTime: formatDateTime(process.endTime),
    expiresAt: formatDateTime(process.expiresAt),
    id: String(process.processId),
    phaseHistory: (process.phaseHistory || []).map((phase) => ({
      ...phase,
      endTime: formatDateTime(phase.endTime),
      startTime: formatDateTime(phase.startTime),
    })),
    orderHistory: (process.orderHistory || []).map((snapshot) => ({
      ...snapshot,
      changedAt: formatDateTime(snapshot.changedAt),
      total: Number(snapshot.total || 0),
      items: (snapshot.items || []).map((item) => ({
        ...item,
        price: Number(item.price || 0),
        priceId: String(item.priceId),
        quantity: Number(item.quantity || 0),
      })),
    })),
    rawStartTime: process.startTime,
    startDate: formatDate(process.startTime),
    total: Number(process.total || 0),
    items: (process.items || []).map((item) => ({
      ...item,
      adjusted: Boolean(item.adjusted),
      offeredQuantity: Number(item.offeredQuantity ?? item.quantity ?? 0),
      priceId: String(item.priceId),
      quantity: Number(item.quantity ?? item.offeredQuantity ?? 0),
    })),
  }
}

function shouldKeepProcess(process, variant) {
  if (variant === 'admin') {
    return true
  }

  return ['Rezervacija', 'Spremno'].includes(process.currentPhase)
}

function StaffProcessCard({ onViewDetails, process }) {
  const nextAction = process.allowedActions.find((action) => action !== 'CANCEL')
  const visibleItems = process.items.slice(0, 2)
  const extraCount = process.items.length - visibleItems.length

  return (
    <article className="request-card staff-request-card">
      <div className="staff-request-meta">
        <span>#{process.processId}</span>
        <strong>{process.customerName}</strong>
        <em>{process.customerEmail}</em>
      </div>

      <div className="request-copy">
        <h2>{process.startDate || 'Process date'}</h2>
        <p className="request-status-line">
          {process.status}
          {process.currentPhase ? ` / ${process.currentPhase}` : ''}
        </p>
        <div className="request-items">
          {visibleItems.map((item) => (
            <p key={`${process.id}-${item.priceId}`}>
              {item.quantity} x {item.name}
            </p>
          ))}
          {extraCount > 0 ? <p>+{extraCount} more</p> : null}
        </div>
        <strong>Price: {process.total}</strong>
      </div>

      <div className="staff-request-actions">
        {nextAction ? <span>{actionLabels[nextAction] || nextAction}</span> : <span>No action</span>}
        <button className="request-details-button" onClick={() => onViewDetails(process)} type="button">
          View Details
        </button>
      </div>
    </article>
  )
}

function StaffRequestsPage({ backPath, emptyMessage, title, variant }) {
  const navigate = useNavigate()
  const [processes, setProcesses] = useState([])
  const [loadingProcesses, setLoadingProcesses] = useState(true)
  const [processError, setProcessError] = useState('')
  const [selectedProcess, setSelectedProcess] = useState(null)
  const [actionError, setActionError] = useState('')
  const [actionLoading, setActionLoading] = useState(false)
  const [cancelReason, setCancelReason] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [status, setStatus] = useState('All')
  const [sort, setSort] = useState('newest')

  useEffect(() => {
    let ignore = false

    async function loadProcesses() {
      setLoadingProcesses(true)
      setProcessError('')

      try {
        const response = await api.get('/staff/processes')

        if (!ignore) {
          setProcesses(response.data.map(normalizeProcess))
        }
      } catch {
        if (!ignore) {
          setProcessError('Unable to load requests from the backend.')
        }
      } finally {
        if (!ignore) {
          setLoadingProcesses(false)
        }
      }
    }

    loadProcesses()

    return () => {
      ignore = true
    }
  }, [])

  const visibleProcesses = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase()

    const filtered = processes.filter((process) => {
      const itemText = process.items.map((item) => item.name).join(' ')
      const matchesStatus = status === 'All' || process.status === status
      const matchesSearch =
        !normalizedSearch ||
        process.customerName.toLowerCase().includes(normalizedSearch) ||
        process.customerEmail.toLowerCase().includes(normalizedSearch) ||
        process.status.toLowerCase().includes(normalizedSearch) ||
        (process.currentPhase || '').toLowerCase().includes(normalizedSearch) ||
        itemText.toLowerCase().includes(normalizedSearch)

      return matchesStatus && matchesSearch
    })

    if (sort === 'oldest') {
      return [...filtered].sort((first, second) => new Date(first.rawStartTime) - new Date(second.rawStartTime))
    }

    if (sort === 'newest') {
      return [...filtered].sort((first, second) => new Date(second.rawStartTime) - new Date(first.rawStartTime))
    }

    if (sort === 'high-low') {
      return [...filtered].sort((first, second) => second.total - first.total)
    }

    if (sort === 'low-high') {
      return [...filtered].sort((first, second) => first.total - second.total)
    }

    return filtered
  }, [processes, searchTerm, sort, status])

  function upsertProcess(process) {
    setProcesses((current) => {
      if (!shouldKeepProcess(process, variant)) {
        return current.filter((item) => item.id !== process.id)
      }

      if (current.some((item) => item.id === process.id)) {
        return current.map((item) => (item.id === process.id ? process : item))
      }

      return [process, ...current]
    })
    setSelectedProcess(process)
  }

  function handleModalBackdropMouseDown(event) {
    if (event.target === event.currentTarget) {
      setSelectedProcess(null)
    }
  }

  async function handleViewDetails(process) {
    setActionError('')
    setCancelReason('')

    try {
      const response = await api.get(`/staff/processes/${process.processId}`)
      const normalized = normalizeProcess(response.data)
      upsertProcess(normalized)
    } catch {
      setSelectedProcess(process)
    }
  }

  async function handleTransition(action) {
    if (!selectedProcess) {
      return
    }

    setActionError('')
    setActionLoading(true)

    try {
      const response = await api.post(`/staff/processes/${selectedProcess.processId}/transition`, {
        action,
      })
      upsertProcess(normalizeProcess(response.data))
    } catch (error) {
      setActionError(errorMessageFor(error))
    } finally {
      setActionLoading(false)
    }
  }

  async function handleCancelSubmit(event) {
    event.preventDefault()

    if (!selectedProcess) {
      return
    }

    if (!cancelReason.trim()) {
      setActionError('Cancellation reason is required.')
      return
    }

    setActionError('')
    setActionLoading(true)

    try {
      const response = await api.post(`/staff/processes/${selectedProcess.processId}/cancel`, {
        reason: cancelReason.trim(),
      })
      upsertProcess(normalizeProcess(response.data))
      setCancelReason('')
    } catch (error) {
      setActionError(errorMessageFor(error))
    } finally {
      setActionLoading(false)
    }
  }

  return (
    <main className="home-page requests-page staff-requests-page">
      <PageTitle label={title} onBack={() => navigate(backPath)} wide />

      <section className="staff-requests-shell">
        <div className="staff-requests-toolbar" aria-label="Request filters">
          <input
            aria-label="Search requests"
            onChange={(event) => setSearchTerm(event.target.value)}
            placeholder="Search customer, phase, or plant"
            type="search"
            value={searchTerm}
          />
          <select aria-label="Status" onChange={(event) => setStatus(event.target.value)} value={status}>
            {statusOptions.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
          <select aria-label="Sort" onChange={(event) => setSort(event.target.value)} value={sort}>
            {sortOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className="requests-list staff-requests-list" aria-live="polite">
          {loadingProcesses ? (
            <div className="requests-empty">Loading requests...</div>
          ) : processError ? (
            <div className="requests-empty">{processError}</div>
          ) : visibleProcesses.length > 0 ? (
            visibleProcesses.map((process) => (
              <StaffProcessCard
                key={process.id}
                onViewDetails={handleViewDetails}
                process={process}
                variant={variant}
              />
            ))
          ) : (
            <div className="requests-empty">{emptyMessage}</div>
          )}
        </div>
      </section>

      {selectedProcess ? (
        <div className="request-modal-backdrop" onMouseDown={handleModalBackdropMouseDown} role="presentation">
          <section
            aria-labelledby="staff-request-details-title"
            aria-modal="true"
            className="request-modal request-modal-wide"
            role="dialog"
          >
            <h2 id="staff-request-details-title">{selectedProcess.customerName}</h2>
            <p className="request-modal-status">
              Status: {selectedProcess.status}
              {selectedProcess.currentPhase ? ` / ${selectedProcess.currentPhase}` : ''}
            </p>
            <p className="request-modal-muted">
              {selectedProcess.customerEmail} / Process #{selectedProcess.processId} / Offer #
              {selectedProcess.offerId}
            </p>
            {selectedProcess.currentPhase === 'Ponuda' && selectedProcess.expiresAt ? (
              <p className="request-modal-muted">Valid until {selectedProcess.expiresAt}</p>
            ) : null}
            {selectedProcess.endTime ? <p className="request-modal-muted">Ended {selectedProcess.endTime}</p> : null}

            <div className="request-modal-items">
              {selectedProcess.items.map((item) => (
                <div className="request-modal-item" key={`${selectedProcess.id}-modal-${item.priceId}`}>
                  <strong>{item.name}</strong>
                  <span>Amount: {item.offeredQuantity}</span>
                  {item.adjusted ? <em>Adjusted</em> : null}
                </div>
              ))}
            </div>
            <strong>Price: {selectedProcess.total}</strong>

            {selectedProcess.phaseHistory.length > 0 ? (
              <div className="request-phase-history">
                <h3>Phase history</h3>
                {selectedProcess.phaseHistory.map((phase) => (
                  <p
                    className={
                      phase.name === selectedProcess.currentPhase && !phase.endTime
                        ? 'request-phase-current'
                        : undefined
                    }
                    key={`${selectedProcess.id}-phase-${phase.id || phase.name}`}
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

            <OrderHistory snapshots={selectedProcess.orderHistory} />

            {actionError ? (
              <p className="request-action-error" role="alert">
                {actionError}
              </p>
            ) : null}

            <div className="request-modal-actions">
              {selectedProcess.allowedActions
                .filter((action) => action !== 'CANCEL')
                .map((action) => (
                  <button
                    className="request-modal-button"
                    disabled={actionLoading}
                    key={action}
                    onClick={() => handleTransition(action)}
                    type="button"
                  >
                    {actionLoading ? 'Working...' : actionLabels[action] || action}
                  </button>
                ))}
              <button
                className="request-modal-secondary"
                onClick={() => setSelectedProcess(null)}
                type="button"
              >
                Close
              </button>
            </div>

            {selectedProcess.allowedActions.includes('CANCEL') ? (
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
                  Cancel process
                </button>
              </form>
            ) : null}
          </section>
        </div>
      ) : null}
    </main>
  )
}

export default StaffRequestsPage
