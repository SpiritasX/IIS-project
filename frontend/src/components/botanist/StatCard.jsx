function StatCard({ label, value, loading, onClick }) {
  return (
    <article
      className={`dashboard-stat-card${onClick ? ' dashboard-stat-card-link' : ''}`}
      onClick={onClick}
      role={onClick ? 'button' : undefined}
      tabIndex={onClick ? 0 : undefined}
      onKeyDown={onClick ? (e) => e.key === 'Enter' && onClick() : undefined}
    >
      <span className="dashboard-stat-value">{loading ? '—' : (value ?? 0)}</span>
      <span className="dashboard-stat-label">{label}</span>
    </article>
  )
}

export default StatCard
