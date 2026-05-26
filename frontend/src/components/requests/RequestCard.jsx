import ImageOutlinedIcon from '@mui/icons-material/ImageOutlined'

function RequestCard({ onViewDetails, order }) {
  return (
    <article className="request-card">
      <div className="request-image" role="img" aria-label={order.imageAlt}>
        <ImageOutlinedIcon fontSize="inherit" />
      </div>

      <div className="request-copy">
        <h2>{order.date}</h2>
        <div className="request-items">
          {order.items.map((item) => (
            <p key={`${order.id}-${item.name}`}>
              {item.quantity} x {item.name}
            </p>
          ))}
        </div>
        <strong>Price: {order.total}</strong>
      </div>

      <button className="request-details-button" onClick={() => onViewDetails(order)} type="button">
        View Details
      </button>
    </article>
  )
}

export default RequestCard
