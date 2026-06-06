import { Link } from 'react-router-dom'
import ImageOutlinedIcon from '@mui/icons-material/ImageOutlined'

function ProductCard({ onAddToCart, onRemoveFromCart, product, quantity }) {
  return (
    <article className="product-card">
      <div className="product-image" role="img" aria-label={product.imageAlt}>
        <ImageOutlinedIcon fontSize="inherit" />
      </div>

      <div className="product-copy">
        <h2>
          <Link className="product-link" to={`/plants/${product.id}`}>
            {product.name}
          </Link>
        </h2>
        <p>
          <Link className="product-link product-description-link" to={`/plants/${product.id}`}>
            {product.description}
          </Link>
        </p>
        <strong>Price: {product.price}</strong>
      </div>

      {quantity > 0 ? (
        <div className="product-quantity-control" aria-label={`${product.name} quantity`}>
          <button
            aria-label={`Remove one ${product.name}`}
            className="product-quantity-button"
            onClick={() => onRemoveFromCart(product.priceId)}
            type="button"
          >
            -
          </button>
          <span className="product-quantity-value" aria-live="polite">
            {quantity}
          </span>
          <button
            aria-label={`Add one more ${product.name}`}
            className="product-quantity-button"
            onClick={() => onAddToCart(product.priceId)}
            type="button"
          >
            +
          </button>
        </div>
      ) : (
        <button
          className="product-add-button"
          disabled={!product.available}
          onClick={() => onAddToCart(product.priceId)}
          type="button"
        >
          Add to cart
        </button>
      )}
    </article>
  )
}

export default ProductCard
