import ShoppingCartOutlinedIcon from '@mui/icons-material/ShoppingCartOutlined'
import { useNavigate } from 'react-router-dom'

function CartButton({ count }) {
  const navigate = useNavigate()

  return (
    <button
      className="cart-button"
      onClick={() => navigate('/cart')}
      type="button"
      aria-label={`Cart with ${count} items`}
    >
      <ShoppingCartOutlinedIcon fontSize="inherit" />
      {count > 0 ? <span className="cart-count">{count}</span> : null}
    </button>
  )
}

export default CartButton
