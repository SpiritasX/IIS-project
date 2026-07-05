const PADDING = { top: 16, right: 16, bottom: 44, left: 44 }
const VIEW_W = 400
const VIEW_H = 180

function DeletionReasonChart({ data, loading }) {
  if (loading) {
    return (
      <div className="dashboard-chart-card">
        <h3 className="dashboard-chart-title">Deletions by reason</h3>
        <div className="dashboard-chart-placeholder" aria-busy="true" />
        <span className="dashboard-chart-legend">Loading…</span>
      </div>
    )
  }

  const chartW = VIEW_W - PADDING.left - PADDING.right
  const chartH = VIEW_H - PADDING.top - PADDING.bottom
  const maxVal = Math.max(...(data ?? []).map((d) => d.count ?? 0), 1)
  const slotW = chartW / 5
  const barW = Math.max(slotW * 0.55, 6)

  const rows = data ?? []

  return (
    <div className="dashboard-chart-card">
      <h3 className="dashboard-chart-title">Deletions by reason</h3>
      <svg
        aria-label="Plant deletions by reason bar chart"
        className="dashboard-chart-svg"
        role="img"
        viewBox={`0 0 ${VIEW_W} ${VIEW_H}`}
      >
        {[0, 0.25, 0.5, 0.75, 1].map((ratio) => {
          const y = PADDING.top + chartH * (1 - ratio)
          return (
            <g key={ratio}>
              <line stroke="#cde8c2" strokeWidth={1} x1={PADDING.left} x2={VIEW_W - PADDING.right} y1={y} y2={y} />
              <text dominantBaseline="middle" fill="#666" fontSize={9} textAnchor="end" x={PADDING.left - 4} y={y}>
                {Math.round(maxVal * ratio)}
              </text>
            </g>
          )
        })}

        {rows.map((d, i) => {
          const barH = Math.max(((d.count ?? 0) / maxVal) * chartH, 1)
          const x = PADDING.left + i * slotW + (slotW - barW) / 2
          const y = PADDING.top + chartH - barH
          const label = d.reason ?? ''

          return (
            <g key={d.reason + i}>
              <rect fill="var(--color-primary)" height={barH} rx={3} width={barW} x={x} y={y} />
              <text
                dominantBaseline="hanging"
                fill="#444"
                fontSize={8}
                textAnchor="middle"
                x={x + barW / 2}
                y={VIEW_H - PADDING.bottom + 6}
              >
                {label}
              </text>
              {d.count > 0 && (
                <text
                  dominantBaseline="auto"
                  fill="#333"
                  fontSize={8}
                  textAnchor="middle"
                  x={x + barW / 2}
                  y={y - 2}
                >
                  {d.count}
                </text>
              )}
            </g>
          )
        })}
      </svg>
      <span className="dashboard-chart-legend">Total plant deletions per reason</span>
    </div>
  )
}

export default DeletionReasonChart
