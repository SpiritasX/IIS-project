import { Link } from 'react-router-dom'
import ImageOutlinedIcon from '@mui/icons-material/ImageOutlined'

function CheckoutProductCard({ product, quantity }) {
  const displayPrice = Math.round(product.price / 100)

  return (
    <article className="checkout-product-card">
      <div className="checkout-product-image" role="img" aria-label={product.imageAlt}>
        <ImageOutlinedIcon fontSize="inherit" />
      </div>
      <h3>
        <Link className="checkout-product-link" to={`/plants/${product.id}`}>
          {product.name}
        </Link>
      </h3>
      <p className="checkout-price-line">
        <span>-20%</span>
        <strong>${displayPrice}</strong>
        {quantity > 1 ? <em>x{quantity}</em> : null}
      </p>
    </article>
  )
}

export default CheckoutProductCard
