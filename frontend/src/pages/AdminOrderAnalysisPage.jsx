import PrintIcon from '@mui/icons-material/Print'
import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/client'
import PageTitle from '../components/home/PageTitle'
import '../styles/home.css'
import '../styles/orderAnalysis.css'

const phaseOrder = ['Ponuda', 'Rezervacija', 'Spremno', 'Isporuka']

const phaseColors = {
  Ponuda: '#22523a',
  Rezervacija: '#77b24e',
  Spremno: '#d09f2f',
  Isporuka: '#4c6f93',
}

function formatPercent(value) {
  return `${Number(value || 0).toFixed(1)}%`
}

function formatDuration(seconds) {
  const totalSeconds = Number(seconds || 0)

  if (totalSeconds < 60) {
    return `${totalSeconds}s`
  }

  const days = Math.floor(totalSeconds / 86400)
  const hours = Math.floor((totalSeconds % 86400) / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)

  if (days > 0) {
    return `${days}d ${hours}h`
  }

  if (hours > 0) {
    return `${hours}h ${minutes}m`
  }

  return `${minutes}m`
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

function pointOnCircle(center, radius, angle) {
  const radians = ((angle - 90) * Math.PI) / 180

  return {
    x: center + radius * Math.cos(radians),
    y: center + radius * Math.sin(radians),
  }
}

function describeSlice(startAngle, endAngle) {
  const center = 110
  const radius = 90
  const start = pointOnCircle(center, radius, endAngle)
  const end = pointOnCircle(center, radius, startAngle)
  const largeArc = endAngle - startAngle > 180 ? 1 : 0

  return [
    `M ${center} ${center}`,
    `L ${start.x} ${start.y}`,
    `A ${radius} ${radius} 0 ${largeArc} 0 ${end.x} ${end.y}`,
    'Z',
  ].join(' ')
}

function normalizeReport(report) {
  return {
    phaseCounts: phaseOrder.map((phaseName) => {
      const match = (report.phaseCounts || []).find((item) => item.phaseName === phaseName)

      return {
        phaseName,
        orderCount: Number(match?.orderCount || 0),
      }
    }),
    transitionRates: (report.transitionRates || []).map((item) => ({
      ...item,
      ratePercent: Number(item.ratePercent || 0),
      reachedCount: Number(item.reachedCount || 0),
      transitionedCount: Number(item.transitionedCount || 0),
    })),
    averageDurations: phaseOrder.map((phaseName) => {
      const match = (report.averageDurations || []).find((item) => item.phaseName === phaseName)

      return {
        phaseName,
        averageDurationSeconds: Number(match?.averageDurationSeconds || 0),
        sampleCount: Number(match?.sampleCount || 0),
      }
    }),
    longestActiveOrders: (report.longestActiveOrders || []).map((item) => ({
      ...item,
      durationSeconds: Number(item.durationSeconds || 0),
      rank: Number(item.rank || 0),
    })),
  }
}

function PhasePieChart({ phaseCounts }) {
  const total = phaseCounts.reduce((sum, phase) => sum + phase.orderCount, 0)
  let currentAngle = 0

  return (
    <div className="analysis-pie-wrap">
      <svg aria-label="Active orders by phase pie chart" className="analysis-pie" viewBox="0 0 220 220">
        {total === 0 ? (
          <circle className="analysis-pie-empty" cx="110" cy="110" r="90" />
        ) : (
          phaseCounts.map((phase) => {
            if (phase.orderCount === 0) {
              return null
            }

            const angle = (phase.orderCount / total) * 360
            const startAngle = currentAngle
            const endAngle = currentAngle + angle
            currentAngle = endAngle

            if (phase.orderCount === total) {
              return (
                <circle
                  cx="110"
                  cy="110"
                  fill={phaseColors[phase.phaseName]}
                  key={phase.phaseName}
                  r="90"
                />
              )
            }

            return (
              <path
                d={describeSlice(startAngle, endAngle)}
                fill={phaseColors[phase.phaseName]}
                key={phase.phaseName}
              />
            )
          })
        )}
        <circle className="analysis-pie-center" cx="110" cy="110" r="54" />
        <text className="analysis-pie-total" textAnchor="middle" x="110" y="105">
          {total}
        </text>
        <text className="analysis-pie-label" textAnchor="middle" x="110" y="128">
          active
        </text>
      </svg>

      <div className="analysis-legend">
        {phaseCounts.map((phase) => (
          <p key={phase.phaseName}>
            <span style={{ backgroundColor: phaseColors[phase.phaseName] }} />
            <strong>{phase.phaseName}</strong>
            <em>{phase.orderCount}</em>
          </p>
        ))}
      </div>
    </div>
  )
}

function TransitionGraph({ transitionRates }) {
  return (
    <div className="analysis-transition-graph">
      {transitionRates.map((rate) => (
        <div className="analysis-transition-row" key={`${rate.fromPhase}-${rate.toPhase}`}>
          <div className="analysis-transition-label">
            <strong>
              {rate.fromPhase} {'->'} {rate.toPhase}
            </strong>
            <span>
              {rate.transitionedCount}/{rate.reachedCount}
            </span>
          </div>
          <div className="analysis-transition-track">
            <span style={{ width: `${Math.min(rate.ratePercent, 100)}%` }} />
          </div>
          <b>{formatPercent(rate.ratePercent)}</b>
        </div>
      ))}
    </div>
  )
}

function AdminOrderAnalysisPage() {
  const navigate = useNavigate()
  const [report, setReport] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let ignore = false

    async function loadReport() {
      setLoading(true)
      setError('')

      try {
        const response = await api.get('/admin/order-analysis')

        if (!ignore) {
          setReport(normalizeReport(response.data))
        }
      } catch {
        if (!ignore) {
          setError('Unable to load order analysis.')
        }
      } finally {
        if (!ignore) {
          setLoading(false)
        }
      }
    }

    loadReport()

    return () => {
      ignore = true
    }
  }, [])

  const longestOrdersByPhase = useMemo(() => {
    if (!report) {
      return []
    }

    return phaseOrder.map((phaseName) => ({
      phaseName,
      orders: report.longestActiveOrders.filter((order) => order.phaseName === phaseName),
    }))
  }, [report])

  function handlePrint() {
    window.print()
  }

  return (
    <main className="home-page order-analysis-page">
      <PageTitle label="Order Analysis" onBack={() => navigate('/admin')} wide />

      <section className="order-analysis-shell" aria-live="polite">
        <header className="order-analysis-header">
          <div>
            <p className="order-analysis-eyebrow">Admin report</p>
            <h2>Orders by process phase</h2>
          </div>
          <button className="analysis-print-button" onClick={handlePrint} type="button">
            <PrintIcon fontSize="inherit" />
            Export PDF
          </button>
        </header>

        {loading ? (
          <div className="analysis-message">Loading order analysis...</div>
        ) : error ? (
          <div className="analysis-message" role="alert">
            {error}
          </div>
        ) : report ? (
          <>
            <div className="analysis-phase-strip">
              {report.phaseCounts.map((phase) => (
                <article key={phase.phaseName}>
                  <span style={{ backgroundColor: phaseColors[phase.phaseName] }} />
                  <p>{phase.phaseName}</p>
                  <strong>{phase.orderCount}</strong>
                </article>
              ))}
            </div>

            <div className="analysis-grid">
              <section className="analysis-section">
                <h3>Active orders by phase</h3>
                <PhasePieChart phaseCounts={report.phaseCounts} />
              </section>

              <section className="analysis-section">
                <h3>Transition rate</h3>
                <TransitionGraph transitionRates={report.transitionRates} />
              </section>
            </div>

            <section className="analysis-section">
              <h3>Average phase duration</h3>
              <div className="analysis-table">
                <div className="analysis-table-head">
                  <span>Phase</span>
                  <span>Average time</span>
                  <span>Completed phases</span>
                </div>
                {report.averageDurations.map((phase) => (
                  <div className="analysis-table-row" key={phase.phaseName}>
                    <span>{phase.phaseName}</span>
                    <strong>{formatDuration(phase.averageDurationSeconds)}</strong>
                    <span>{phase.sampleCount}</span>
                  </div>
                ))}
              </div>
            </section>

            <section className="analysis-section">
              <h3>Top 3 active orders by time in phase</h3>
              <div className="analysis-longest-grid">
                {longestOrdersByPhase.map((phaseGroup) => (
                  <article className="analysis-longest-group" key={phaseGroup.phaseName}>
                    <h4>{phaseGroup.phaseName}</h4>
                    {phaseGroup.orders.length > 0 ? (
                      phaseGroup.orders.map((order) => (
                        <div className="analysis-order-row" key={`${order.phaseName}-${order.processId}`}>
                          <span>#{order.rank}</span>
                          <div>
                            <strong>Process {order.processId}</strong>
                            <p>
                              Offer {order.offerId} / {order.customerName || order.customerEmail}
                            </p>
                            <em>Started {formatDateTime(order.phaseStartTime)}</em>
                          </div>
                          <b>{formatDuration(order.durationSeconds)}</b>
                        </div>
                      ))
                    ) : (
                      <p className="analysis-empty-small">No active orders.</p>
                    )}
                  </article>
                ))}
              </div>
            </section>
          </>
        ) : null}
      </section>
    </main>
  )
}

export default AdminOrderAnalysisPage
