function formatDate(timestamp) {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleDateString('en-GB', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
}

function LogsTable({ logs, loading, searchTerm }) {
  const filtered = searchTerm
    ? logs.filter(
        (log) =>
          log.plantName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          log.detail?.toLowerCase().includes(searchTerm.toLowerCase()),
      )
    : logs

  return (
    <section className="dashboard-logs-card" aria-label="Recent activity logs">
      <h3 className="dashboard-logs-title">Logs</h3>

      {loading ? (
        <p className="dashboard-logs-empty">Loading logs…</p>
      ) : filtered.length === 0 ? (
        <p className="dashboard-logs-empty">{searchTerm ? 'No matching logs.' : 'No logs recorded yet.'}</p>
      ) : (
        <table className="dashboard-logs-table">
          <thead>
            <tr>
              <th>Plant</th>
              <th>Detail</th>
              <th>Type</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((log, i) => (
              <tr key={i}>
                <td>{log.plantName}</td>
                <td>{log.detail}</td>
                <td>
                  <span className={`dashboard-log-badge dashboard-log-badge-${log.type?.toLowerCase()}`}>
                    {log.type}
                  </span>
                </td>
                <td className="dashboard-log-date">{formatDate(log.timestamp)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}

export default LogsTable
