import AssessmentOutlinedIcon from '@mui/icons-material/AssessmentOutlined'
import PrintOutlinedIcon from '@mui/icons-material/PrintOutlined'
import RefreshOutlinedIcon from '@mui/icons-material/RefreshOutlined'
import SearchIcon from '@mui/icons-material/Search'
import { useEffect, useMemo, useState } from 'react'
import api from '../api/client.js'
import '../styles/admin.css'

const recommendationStatuses = [
  { label: 'All', value: 'all' },
  { label: 'Successful', value: 'successful' },
  { label: 'Unsuccessful', value: 'unsuccessful' },
  { label: 'Viewed', value: 'viewed' },
  { label: 'Liked', value: 'liked' },
  { label: 'Purchased', value: 'purchased' },
]

const reportStatusOptions = ['', 'OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED']

const initialReport = {
  effectiveness: {
    summary: {},
    top_plants: [],
    success_paths: [],
    weekday_histogram: [],
  },
  plants: {
    hits: [],
    aggregations: {},
  },
  recentRecommendations: [],
  serviceReports: {
    hits: [],
    aggregations: {},
  },
}

function compactParams(params) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== '' && value !== null && value !== undefined),
  )
}

function formatDate(value) {
  if (!value) {
    return '-'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('sr-RS', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

function formatNumber(value) {
  return new Intl.NumberFormat('sr-RS').format(Number(value || 0))
}

function formatPercent(value) {
  return `${Math.round(Number(value || 0) * 100)}%`
}

function formatPrice(value) {
  return new Intl.NumberFormat('sr-RS', {
    currency: 'RSD',
    maximumFractionDigits: 0,
    style: 'currency',
  }).format(Number(value || 0))
}

function bucketItems(aggregation) {
  return aggregation?.buckets || []
}

function reportFlagClass(active) {
  return active ? 'report-flag report-flag-active' : 'report-flag'
}

function EmptyRow({ colSpan, text }) {
  return (
    <tr>
      <td className="admin-empty-cell" colSpan={colSpan}>
        {text}
      </td>
    </tr>
  )
}

function MetricCard({ label, value, tone }) {
  return (
    <article className={`metric-card metric-card-${tone || 'default'}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  )
}

function BucketList({ buckets, label }) {
  if (!buckets.length) {
    return (
      <div className="bucket-list">
        <span className="bucket-empty">{label}: no data</span>
      </div>
    )
  }

  return (
    <div className="bucket-list" aria-label={label}>
      {buckets.slice(0, 6).map((bucket) => (
        <span className="bucket-pill" key={bucket.key}>
          {bucket.key} <strong>{formatNumber(bucket.doc_count)}</strong>
        </span>
      ))}
    </div>
  )
}

function SuccessPathChart({ data }) {
  const max = Math.max(...data.map((item) => item.count), 1)

  return (
    <div className="report-chart" role="img" aria-label="Recommendation success path chart">
      {data.length ? (
        data.map((item) => {
          const width = `${Math.max(4, (Number(item.count || 0) / max) * 100)}%`

          return (
            <div className="chart-row" key={item.path}>
              <span className="chart-label">{item.path}</span>
              <div className="chart-track">
                <span className="chart-bar" style={{ width }} />
              </div>
              <strong>{formatNumber(item.count)}</strong>
              <span>{formatPercent(item.share)}</span>
            </div>
          )
        })
      ) : (
        <div className="admin-empty-state">No recommendation interactions recorded.</div>
      )}
    </div>
  )
}

function WeekdayHistogram({ data }) {
  const bestDay = data
    .filter((item) => Number(item.recommendation_count || 0) > 0)
    .reduce(
      (best, item) => (Number(item.success_rate || 0) > Number(best?.success_rate || 0) ? item : best),
      null,
    )

  return (
    <div className="chart-group">
      <div className="chart-subheading">
        <strong>Success by weekday</strong>
        {bestDay ? (
          <span>
            Best: {bestDay.day} ({formatPercent(bestDay.success_rate)})
          </span>
        ) : null}
      </div>

      <div className="report-chart" role="img" aria-label="Recommendation success by weekday histogram">
        {data.length ? (
          data.map((item) => {
            const hasRecommendations = Number(item.recommendation_count || 0) > 0
            const width = hasRecommendations
              ? `${Math.max(4, Number(item.success_rate || 0) * 100)}%`
              : '0%'

            return (
              <div className="chart-row" key={item.day}>
                <span className="chart-label">{item.day}</span>
                <div className="chart-track">
                  <span className="chart-bar chart-bar-success" style={{ width }} />
                </div>
                <strong>
                  {formatNumber(item.successful_count)}/{formatNumber(item.recommendation_count)}
                </strong>
                <span>{formatPercent(item.success_rate)}</span>
              </div>
            )
          })
        ) : (
          <div className="admin-empty-state">No weekday recommendation data recorded.</div>
        )}
      </div>
    </div>
  )
}

function RecommendationFlags({ recommendation }) {
  return (
    <div className="report-flags">
      <span className={reportFlagClass(recommendation.viewed)}>View</span>
      <span className={reportFlagClass(recommendation.liked)}>Like</span>
      <span className={reportFlagClass(recommendation.purchased)}>Buy</span>
    </div>
  )
}

function ReportControls({ filters, loading, onChange, onGenerate, onPrint }) {
  return (
    <section className="admin-controls no-print" aria-label="Report controls">
      <div className="admin-field">
        <label htmlFor="recommendation-status">Recommendation status</label>
        <select
          id="recommendation-status"
          value={filters.recommendationStatus}
          onChange={(event) => onChange('recommendationStatus', event.target.value)}
        >
          {recommendationStatuses.map((status) => (
            <option key={status.value} value={status.value}>
              {status.label}
            </option>
          ))}
        </select>
      </div>

      <div className="admin-field">
        <label htmlFor="recommendation-limit">Recommendation rows</label>
        <input
          id="recommendation-limit"
          min="1"
          max="100"
          type="number"
          value={filters.recommendationLimit}
          onChange={(event) => onChange('recommendationLimit', event.target.value)}
        />
      </div>

      <div className="admin-field">
        <label htmlFor="plant-query">Plant query</label>
        <input
          id="plant-query"
          value={filters.plantQuery}
          onChange={(event) => onChange('plantQuery', event.target.value)}
        />
      </div>

      <div className="admin-field">
        <label htmlFor="plant-type">Plant type</label>
        <input
          id="plant-type"
          value={filters.plantType}
          onChange={(event) => onChange('plantType', event.target.value)}
        />
      </div>

      <div className="admin-field">
        <label htmlFor="species">Species</label>
        <input
          id="species"
          value={filters.species}
          onChange={(event) => onChange('species', event.target.value)}
        />
      </div>

      <div className="admin-field">
        <label htmlFor="variety">Variety</label>
        <input
          id="variety"
          value={filters.variety}
          onChange={(event) => onChange('variety', event.target.value)}
        />
      </div>

      <div className="admin-field">
        <label htmlFor="min-price">Min price</label>
        <input
          id="min-price"
          min="0"
          type="number"
          value={filters.minPrice}
          onChange={(event) => onChange('minPrice', event.target.value)}
        />
      </div>

      <div className="admin-field">
        <label htmlFor="max-price">Max price</label>
        <input
          id="max-price"
          min="0"
          type="number"
          value={filters.maxPrice}
          onChange={(event) => onChange('maxPrice', event.target.value)}
        />
      </div>

      <div className="admin-field">
        <label htmlFor="report-status">Service report status</label>
        <select
          id="report-status"
          value={filters.reportStatus}
          onChange={(event) => onChange('reportStatus', event.target.value)}
        >
          {reportStatusOptions.map((status) => (
            <option key={status || 'all'} value={status}>
              {status || 'All'}
            </option>
          ))}
        </select>
      </div>

      <div className="admin-field">
        <label htmlFor="min-rating">Min rating</label>
        <input
          id="min-rating"
          max="5"
          min="1"
          type="number"
          value={filters.minRating}
          onChange={(event) => onChange('minRating', event.target.value)}
        />
      </div>

      <div className="admin-actions">
        <button className="admin-primary-button" disabled={loading} onClick={onGenerate} type="button">
          <RefreshOutlinedIcon fontSize="small" />
          {loading ? 'Generating' : 'Generate'}
        </button>
        <button className="admin-secondary-button" onClick={onPrint} type="button">
          <PrintOutlinedIcon fontSize="small" />
          PDF
        </button>
      </div>
    </section>
  )
}

function ReportGenerator() {
  const [filters, setFilters] = useState({
    maxPrice: '',
    minPrice: '',
    minRating: '',
    plantQuery: '',
    plantType: '',
    recommendationLimit: '25',
    recommendationStatus: 'all',
    reportStatus: '',
    species: '',
    variety: '',
  })
  const [generatedAt, setGeneratedAt] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [report, setReport] = useState(initialReport)

  const plantTypeBuckets = useMemo(
    () => bucketItems(report.plants.aggregations?.plants_by_plant_type),
    [report.plants.aggregations],
  )
  const speciesBuckets = useMemo(
    () => bucketItems(report.plants.aggregations?.plants_by_species),
    [report.plants.aggregations],
  )
  const reportStatusBuckets = useMemo(
    () => bucketItems(report.serviceReports.aggregations?.reports_by_status),
    [report.serviceReports.aggregations],
  )

  async function loadReport() {
    setLoading(true)
    setError('')

    try {
      const plantParams = compactParams({
        max_price: filters.maxPrice,
        min_price: filters.minPrice,
        order: 'asc',
        plant_type: filters.plantType,
        query: filters.plantQuery,
        sort_by: 'price',
        species: filters.species,
        variety: filters.variety,
      })
      const serviceReportParams = compactParams({
        min_rating: filters.minRating,
        plant_type: filters.plantType,
        query: filters.plantQuery,
        species: filters.species,
        status: filters.reportStatus,
      })

      const [effectivenessResponse, recentResponse, plantResponse, serviceReportResponse] =
        await Promise.all([
          api.get('/recommendations/reports/effectiveness', { params: { limit: 10 } }),
          api.get('/recommendations/reports/recent', {
            params: {
              limit: filters.recommendationLimit,
              status: filters.recommendationStatus,
            },
          }),
          api.get('/search/search/plants', { params: plantParams }),
          api.get('/search/search/reports', { params: serviceReportParams }),
        ])

      setReport({
        effectiveness: effectivenessResponse.data,
        plants: plantResponse.data,
        recentRecommendations: recentResponse.data,
        serviceReports: serviceReportResponse.data,
      })
      setGeneratedAt(new Date().toISOString())
    } catch {
      setError('Report data could not be loaded.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadReport()
  }, [])

  function updateFilter(key, value) {
    setFilters((current) => ({
      ...current,
      [key]: value,
    }))
  }

  const summary = report.effectiveness.summary || {}
  const averagePrice = report.plants.aggregations?.avg_price?.value
  const averageRating = report.serviceReports.aggregations?.avg_feedback_rating?.value

  return (
    <>
      <ReportControls
        filters={filters}
        loading={loading}
        onChange={updateFilter}
        onGenerate={loadReport}
        onPrint={() => window.print()}
      />

      {error ? <div className="admin-error no-print">{error}</div> : null}

      <section className="report-paper" aria-label="Generated report">
        <div className="report-title-row">
          <div>
            <span className="report-eyebrow">Neo4j + Elasticsearch</span>
            <h2>Recommendation effectiveness report</h2>
          </div>
          <div className="report-meta">
            <span>Generated</span>
            <strong>{formatDate(generatedAt)}</strong>
          </div>
        </div>

        <div className="metric-grid">
          <MetricCard label="Generated recommendations" value={formatNumber(summary.total)} tone="default" />
          <MetricCard label="Success rate" value={formatPercent(summary.success_rate)} tone="success" />
          <MetricCard label="View rate" value={formatPercent(summary.view_rate)} tone="view" />
          <MetricCard label="Like rate" value={formatPercent(summary.like_rate)} tone="like" />
          <MetricCard label="Purchase rate" value={formatPercent(summary.purchase_rate)} tone="purchase" />
        </div>

        <section className="report-section">
          <div className="section-heading">
            <h3>Recommendation success</h3>
            <span>Neo4j graph query</span>
          </div>

          <SuccessPathChart data={report.effectiveness.success_paths || []} />
          <WeekdayHistogram data={report.effectiveness.weekday_histogram || []} />

          <div className="table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Plant</th>
                  <th>Season</th>
                  <th>Recommended</th>
                  <th>Successful</th>
                  <th>Views</th>
                  <th>Likes</th>
                  <th>Purchases</th>
                  <th>Rate</th>
                </tr>
              </thead>
              <tbody>
                {(report.effectiveness.top_plants || []).length ? (
                  report.effectiveness.top_plants.map((plant) => (
                    <tr key={plant.plant_id}>
                      <td>{plant.plant_name}</td>
                      <td>{plant.season || '-'}</td>
                      <td>{formatNumber(plant.recommendation_count)}</td>
                      <td>{formatNumber(plant.successful_count)}</td>
                      <td>{formatNumber(plant.viewed_count)}</td>
                      <td>{formatNumber(plant.liked_count)}</td>
                      <td>{formatNumber(plant.purchased_count)}</td>
                      <td>{formatPercent(plant.success_rate)}</td>
                    </tr>
                  ))
                ) : (
                  <EmptyRow colSpan={8} text="No generated recommendations found." />
                )}
              </tbody>
            </table>
          </div>
        </section>

        <section className="report-section">
          <div className="section-heading">
            <h3>Generated recommendations</h3>
            <span>Neo4j records</span>
          </div>

          <div className="table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Created</th>
                  <th>Customer</th>
                  <th>Plant</th>
                  <th>Variety</th>
                  <th>Status</th>
                  <th>Signals</th>
                </tr>
              </thead>
              <tbody>
                {report.recentRecommendations.length ? (
                  report.recentRecommendations.map((recommendation) => (
                    <tr key={recommendation.recommendation_id}>
                      <td>{formatDate(recommendation.created_at)}</td>
                      <td>{recommendation.customer_name || `Customer ${recommendation.customer_id}`}</td>
                      <td>{recommendation.plant_name}</td>
                      <td>{recommendation.variety || '-'}</td>
                      <td>
                        <span
                          className={
                            recommendation.successful
                              ? 'status-pill status-pill-success'
                              : 'status-pill status-pill-muted'
                          }
                        >
                          {recommendation.successful ? 'Successful' : 'No success'}
                        </span>
                      </td>
                      <td>
                        <RecommendationFlags recommendation={recommendation} />
                      </td>
                    </tr>
                  ))
                ) : (
                  <EmptyRow colSpan={6} text="No recommendation rows match the selected filter." />
                )}
              </tbody>
            </table>
          </div>
        </section>

        <section className="report-section">
          <div className="section-heading">
            <h3>Plant catalog search</h3>
            <span>Elasticsearch records</span>
          </div>

          <div className="aggregation-row">
            <BucketList buckets={plantTypeBuckets} label="Plant types" />
            <BucketList buckets={speciesBuckets} label="Species" />
            <span className="bucket-pill">
              Avg price <strong>{averagePrice ? formatPrice(averagePrice) : '-'}</strong>
            </span>
          </div>

          <div className="table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Plant</th>
                  <th>Type</th>
                  <th>Species</th>
                  <th>Variety</th>
                  <th>Price</th>
                </tr>
              </thead>
              <tbody>
                {report.plants.hits.length ? (
                  report.plants.hits.slice(0, 12).map((plant, index) => (
                    <tr key={plant.id || index}>
                      <td>{plant.name}</td>
                      <td>{plant.plantTypeName || '-'}</td>
                      <td>{plant.speciesName || '-'}</td>
                      <td>{plant.varietyName || '-'}</td>
                      <td>{formatPrice(plant.price)}</td>
                    </tr>
                  ))
                ) : (
                  <EmptyRow colSpan={5} text="No plants match the selected search filter." />
                )}
              </tbody>
            </table>
          </div>
        </section>

        <section className="report-section">
          <div className="section-heading">
            <h3>Search service report quality</h3>
            <span>Elasticsearch nested reports</span>
          </div>

          <div className="aggregation-row">
            <BucketList buckets={reportStatusBuckets} label="Report statuses" />
            <span className="bucket-pill">
              Avg feedback <strong>{averageRating ? Number(averageRating).toFixed(2) : '-'}</strong>
            </span>
          </div>

          <div className="table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Created</th>
                  <th>Plant</th>
                  <th>Type</th>
                  <th>Status</th>
                  <th>Rating</th>
                  <th>Description</th>
                </tr>
              </thead>
              <tbody>
                {report.serviceReports.hits.length ? (
                  report.serviceReports.hits.slice(0, 10).map((serviceReport) => (
                    <tr key={serviceReport.id}>
                      <td>{formatDate(serviceReport.createdAt)}</td>
                      <td>{serviceReport.plantName || '-'}</td>
                      <td>{serviceReport.plantTypeName || '-'}</td>
                      <td>{serviceReport.reportStatus || '-'}</td>
                      <td>{serviceReport.feedbackRating || '-'}</td>
                      <td>{serviceReport.description}</td>
                    </tr>
                  ))
                ) : (
                  <EmptyRow colSpan={6} text="No service reports match the selected filter." />
                )}
              </tbody>
            </table>
          </div>
        </section>
      </section>
    </>
  )
}

function UserSearchPanel() {
  const [filters, setFilters] = useState({
    city: '',
    country: '',
    min_purchases: '',
    min_reports: '',
    order: 'desc',
    query: '',
    sort_by: 'totalPurchases',
  })
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    page: 1,
    page_size: 10,
  })
  const [users, setUsers] = useState([])

  const search = async () => {
    setLoading(true)

    try {
      const res = await api.get('/search/search/users', {
        params: compactParams({ ...filters, ...pagination }),
      })

      setUsers(res.data.hits)
      setPagination(res.data.pagination)
    } finally {
      setLoading(false)
    }
  }

  const update = (key, value) => {
    setFilters((prev) => ({
      ...prev,
      [key]: value,
    }))

    setPagination((p) => ({ ...p, page: 1 }))
  }

  useEffect(() => {
    search()
  }, [pagination.page])

  return (
    <section className="admin-panel" aria-label="User search">
      <div className="section-heading">
        <h2>User search</h2>
        <span>Elasticsearch users index</span>
      </div>

      <div className="admin-controls">
        <div className="admin-field">
          <label htmlFor="user-query">Search</label>
          <input id="user-query" value={filters.query} onChange={(event) => update('query', event.target.value)} />
        </div>

        <div className="admin-field">
          <label htmlFor="user-city">City</label>
          <input id="user-city" value={filters.city} onChange={(event) => update('city', event.target.value)} />
        </div>

        <div className="admin-field">
          <label htmlFor="user-country">Country</label>
          <input
            id="user-country"
            value={filters.country}
            onChange={(event) => update('country', event.target.value)}
          />
        </div>

        <div className="admin-field">
          <label htmlFor="user-min-purchases">Min purchases</label>
          <input
            id="user-min-purchases"
            type="number"
            value={filters.min_purchases}
            onChange={(event) => update('min_purchases', event.target.value)}
          />
        </div>

        <div className="admin-field">
          <label htmlFor="user-min-reports">Min reports</label>
          <input
            id="user-min-reports"
            type="number"
            value={filters.min_reports}
            onChange={(event) => update('min_reports', event.target.value)}
          />
        </div>

        <div className="admin-field">
          <label htmlFor="user-sort">Sort by</label>
          <select id="user-sort" value={filters.sort_by} onChange={(event) => update('sort_by', event.target.value)}>
            <option value="totalPurchases">Purchases</option>
            <option value="totalReports">Reports</option>
            <option value="username">Username</option>
          </select>
        </div>

        <div className="admin-field">
          <label htmlFor="user-order">Order</label>
          <select id="user-order" value={filters.order} onChange={(event) => update('order', event.target.value)}>
            <option value="desc">Desc</option>
            <option value="asc">Asc</option>
          </select>
        </div>

        <div className="admin-actions">
          <button className="admin-primary-button" disabled={loading} onClick={search} type="button">
            <SearchIcon fontSize="small" />
            Search
          </button>
        </div>
      </div>

      <div className="table-wrap">
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Username</th>
              <th>City</th>
              <th>Country</th>
              <th>Purchases</th>
              <th>Reports</th>
            </tr>
          </thead>

          <tbody>
            {users.length ? (
              users.map((user) => (
                <tr key={user.id}>
                  <td>{user.id}</td>
                  <td>{user.username}</td>
                  <td>{user.city}</td>
                  <td>{user.country}</td>
                  <td>{user.totalPurchases}</td>
                  <td>{user.totalReports}</td>
                </tr>
              ))
            ) : (
              <EmptyRow colSpan={6} text={loading ? 'Loading users...' : 'No users match the selected filter.'} />
            )}
          </tbody>
        </table>
      </div>

      <div className="pagination-row">
        <button
          disabled={pagination.page <= 1}
          onClick={() => setPagination((p) => ({ ...p, page: p.page - 1 }))}
          type="button"
        >
          Prev
        </button>

        <span>Page {pagination.page}</span>

        <button onClick={() => setPagination((p) => ({ ...p, page: p.page + 1 }))} type="button">
          Next
        </button>
      </div>
    </section>
  )
}

export default function AdminPage() {
  const [activeTab, setActiveTab] = useState('report')

  return (
    <main className="admin-page">
      <header className="admin-header no-print">
        <div>
          <span className="report-eyebrow">Admin</span>
          <h1>Reports</h1>
        </div>

        <nav className="admin-tabs" aria-label="Admin sections">
          <button
            className={activeTab === 'report' ? 'admin-tab admin-tab-active' : 'admin-tab'}
            onClick={() => setActiveTab('report')}
            type="button"
          >
            <AssessmentOutlinedIcon fontSize="small" />
            Report
          </button>
          <button
            className={activeTab === 'users' ? 'admin-tab admin-tab-active' : 'admin-tab'}
            onClick={() => setActiveTab('users')}
            type="button"
          >
            <SearchIcon fontSize="small" />
            User search
          </button>
        </nav>
      </header>

      {activeTab === 'report' ? <ReportGenerator /> : <UserSearchPanel />}
    </main>
  )
}
