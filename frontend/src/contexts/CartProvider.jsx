import { useMemo, useState } from 'react'
import { CartContext } from './CartContext'

function CartProvider({ children }) {
  const [cartItems, setCartItems] = useState({})

  function addToCart(productId) {
    setCartItems((current) => ({
      ...current,
      [productId]: (current[productId] || 0) + 1,
    }))
  }

  function addNToCart(productId, quantity) {
    setCartItems((current) => ({
      ...current,
      [productId]: (current[productId] || 0) + quantity,
    }))
  }

  function removeFromCart(productId) {
    setCartItems((current) => {
      const nextQuantity = (current[productId] || 0) - 1
      const nextItems = { ...current }

      if (nextQuantity > 0) {
        nextItems[productId] = nextQuantity
      } else {
        delete nextItems[productId]
      }

      return nextItems
    })
  }

  function clearCart() {
    setCartItems({})
  }

  const cartCount = Object.values(cartItems).reduce((total, count) => total + count, 0)

  const value = useMemo(
    () => ({
      addToCart,
      addNToCart,
      cartCount,
      cartItems,
      clearCart,
      removeFromCart,
    }),
    [cartCount, cartItems],
  )

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

export default CartProvider
