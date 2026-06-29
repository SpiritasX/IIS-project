function formatDate(timestamp) {
  if (!timestamp) return '—'
  return new Date(timestamp).toLocaleDateString('en-GB', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
}

function RelocationLogsTable({ logs, loading, searchTerm }) {
  const filtered = searchTerm
    ? logs.filter(
        (log) =>
          log.plantName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          log.parcelName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          log.reason?.toLowerCase().includes(searchTerm.toLowerCase()),
      )
    : logs

  return (
    <section className="dashboard-logs-card" aria-label="Relocation logs">
      <h3 className="dashboard-logs-title">Relocation Logs</h3>

      {loading ? (
        <p className="dashboard-logs-empty">Loading logs…</p>
      ) : filtered.length === 0 ? (
        <p className="dashboard-logs-empty">
          {searchTerm ? 'No matching logs.' : 'No relocation logs recorded yet.'}
        </p>
      ) : (
        <table className="dashboard-logs-table">
          <thead>
            <tr>
              <th>Plant</th>
              <th>Location</th>
              <th>Reason</th>
              <th>In stock</th>
              <th>Status</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((log, i) => (
              <tr key={i}>
                <td>{log.plantName}</td>
                <td>{log.parcelName}</td>
                <td>{log.reason}</td>
                <td>{log.inStock ?? '—'}</td>
                <td>
                  <span
                    className={`dashboard-log-badge ${log.endTime ? 'dashboard-log-badge-removal' : 'dashboard-log-badge-health'}`}
                  >
                    {log.endTime ? 'Completed' : 'Active'}
                  </span>
                </td>
                <td className="dashboard-log-date">{formatDate(log.startTime)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}

export default RelocationLogsTable
