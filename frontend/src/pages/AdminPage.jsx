import { useEffect, useState } from "react";
import api from "../api/client.js";

export default function UserSearchAdmin() {
  const [filters, setFilters] = useState({
    query: "",
    city: "",
    country: "",
    min_purchases: "",
    min_reports: "",
    sort_by: "totalPurchases",
    order: "desc",
  });

  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    page: 1,
    page_size: 10,
  });

  const search = async () => {
    setLoading(true);

    const params = new URLSearchParams();

    Object.entries({ ...filters, ...pagination }).forEach(([key, value]) => {
      if (value !== "") {
        params.append(key, value);
      }
    });

    try {
      const res = await api.get(`/search/search/users?${params.toString()}`);

      setUsers(res.data['hits']);
      setPagination(res.data['pagination']);
    } finally {
      setLoading(false);
    }
  };

  const update = (key, value) => {
    setFilters((prev) => ({
      ...prev,
      [key]: value,
    }));

    setPagination((p) => ({ ...p, page: 1}))
  };

  useEffect(() => {
    search();
  }, [pagination.page])

  return (
    <div style={{ padding: 20 }}>
      <h2>User Search</h2>

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(4, 1fr)",
          gap: 12,
          marginBottom: 16,
        }}
      >
        <input
          placeholder="Search"
          value={filters.query}
          onChange={(e) => update("query", e.target.value)}
        />

        <input
          placeholder="City"
          value={filters.city}
          onChange={(e) => update("city", e.target.value)}
        />

        <input
          placeholder="Country"
          value={filters.country}
          onChange={(e) => update("country", e.target.value)}
        />

        <input
          type="number"
          placeholder="Min Purchases"
          value={filters.min_purchases}
          onChange={(e) => update("min_purchases", e.target.value)}
        />

        <input
          type="number"
          placeholder="Min Reports"
          value={filters.min_reports}
          onChange={(e) => update("min_reports", e.target.value)}
        />

        <select
          value={filters.sort_by}
          onChange={(e) => update("sort_by", e.target.value)}
        >
          <option value="totalPurchases">Purchases</option>
          <option value="totalReports">Reports</option>
          <option value="username">Username</option>
        </select>

        <select
          value={filters.order}
          onChange={(e) => update("order", e.target.value)}
        >
          <option value="desc">Desc</option>
          <option value="asc">Asc</option>
        </select>

        <button onClick={search}>Search</button>
      </div>

      <div style={{visibility: loading ? "visible" : "hidden"}}>Loading...</div>

      <div
        style={{
          height: "45vh",
          border: "1px solid #ddd",
          position: "relative",
        }}
      >
        <table width="100%">
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
          {users.map((user) => (
            <tr key={user.id}>
              <td>{user.id}</td>
              <td>{user.username}</td>
              <td>{user.city}</td>
              <td>{user.country}</td>
              <td>{user.totalPurchases}</td>
              <td>{user.totalReports}</td>
            </tr>
          ))}
          </tbody>
        </table>
        <div style={{ marginTop: 10, display: "flex", gap: 10, position: "absolute", bottom: 0 }}>
          <button
            disabled={pagination.page <= 1}
            onClick={() =>
              setPagination((p) => ({ ...p, page: p.page - 1 }))
            }
          >
            Prev
          </button>

          <span>Page {pagination.page}</span>

          <button
            onClick={() =>
              setPagination((p) => ({ ...p, page: p.page + 1 }))
            }
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
}