import ImageOutlinedIcon from '@mui/icons-material/ImageOutlined'

function CheckoutProductCard({ disabled, onAdd, onRemove, product, quantity }) {
  const canAdd = product.available && quantity < product.availableQuantity && !disabled
  const displayPrice = Math.round(product.price / 100)

  return (
    <article className="checkout-product-card">
      <div className="checkout-product-image" role="img" aria-label={product.imageAlt}>
        <ImageOutlinedIcon fontSize="inherit" />
      </div>
      <h3>{product.name}</h3>
      <p className="checkout-price-line">
        <span>-20%</span>
        <strong>${displayPrice}</strong>
        {quantity > 1 ? <em>x{quantity}</em> : null}
      </p>
      <div className="checkout-quantity-control" aria-label={`${product.name} quantity`}>
        <button
          aria-label={`Remove one ${product.name}`}
          className="checkout-quantity-button"
          disabled={disabled}
          onClick={() => onRemove(product.priceId)}
          type="button"
        >
          -
        </button>
        <span className="checkout-quantity-value" aria-live="polite">
          {quantity}
        </span>
        <button
          aria-label={`Add one more ${product.name}`}
          className="checkout-quantity-button"
          disabled={!canAdd}
          onClick={() => onAdd(product.priceId)}
          type="button"
        >
          +
        </button>
      </div>
    </article>
  )
}

export default CheckoutProductCard
