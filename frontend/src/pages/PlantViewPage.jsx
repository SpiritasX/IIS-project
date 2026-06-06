import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api/client'
import PageTitle from '../components/home/PageTitle'
import { useAuth } from '../hooks/useAuth'
import { useCart } from '../hooks/useCart'
import '../styles/plant.css'

function PlantViewPage() {
  const navigate = useNavigate()
  const { plantId } = useParams()
  const { user } = useAuth()
  const [plant, setPlant] = useState(null)
  const [quantitySelected, setQuantitySelected] = useState(1)
  const [quantity, setQuantity] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [liked, setLiked] = useState(false)
  const [savingLike, setSavingLike] = useState(false)
  const [actionError, setActionError] = useState('')
  const { addNToCart, cartItems, removeFromCart } = useCart()

  useEffect(() => {
    let ignore = false

    async function loadPlant() {
      setLoading(true)
      setError('')

      try {
        const response = await api.get(`/plants/${plantId}`)

        let liked = false

        try {
          const userLikes = await api.get(
              `/recommendations/customer/${user?.id}/like/${plantId}`
          )

          liked = userLikes.status === 200
        } catch {
          liked = false
        }

        if (!ignore) {
          setPlant(response.data)
          setLiked(liked)
        }
      } catch {
        if (!ignore) {
          setError('Unable to load plant details.')
        }
      } finally {
        if (!ignore) {
          setLoading(false)
        }
      }
    }

    if (plantId) {
      loadPlant()
    }

    return () => {
      ignore = true
    }
  }, [plantId])

  useEffect(() => {
    if (!plant || !user?.id) {
      return
    }

    api.post(`/recommendations/customer/${user.id}/view/${plant.id}`).catch(() => {
      // optional analytics call, ignore failures
    })
  }, [plant, user])

  async function handleToggleLike() {
    if (!plant || !user?.id) {
      return
    }

    setActionError('')
    setSavingLike(true)

    try {
      if (liked) {
        await api.delete(`/recommendations/customer/${user.id}/like/${plant.id}`)
      } else {
        await api.post(`/recommendations/customer/${user.id}/like/${plant.id}`)
      }

      setLiked(!liked)
    } catch {
      setActionError('Unable to update like status. Please try again.')
    } finally {
      setSavingLike(false)
    }
  }

  useEffect(() => {
    if (!plant) {
      return
    }

    async function quantityChanged() {
      setQuantity(cartItems[plant.id])
    }

    quantityChanged()

  }, [plant, cartItems])

  const displayPrice = typeof plant?.price === 'number' ? `$${(plant.price / 100).toFixed(2)}` : plant?.price || 'N/A'

  return (
    <main className="home-page">
      <PageTitle label={plant?.name || 'Plant details'} onBack={() => navigate('/home')} />

      <section className="plant-view-shell">
        {loading ? (
          <div className="product-empty">Loading plant details...</div>
        ) : error ? (
          <div className="product-empty">{error}</div>
        ) : plant ? (
            <div className="plant-page">
              <div className="plant-top">
                <div className="plant-thumbnails">
                  {Array.from({ length: 8 }).map((_, index) => (
                      <div key={index} className="plant-thumbnail" />
                  ))}
                </div>

                <div className="plant-main-image">
                  <span>🌿</span>
                </div>

                <div className="plant-content">
                  <h1>{plant.name}</h1>

                  <div className="plant-description">
                    {plant.description || 'No description available.'}
                  </div>

                  <div className="plant-taxonomy">
                    <div className="plant-taxonomy-row">
                      <span className="plant-taxonomy-label">Category:</span>
                      <span>{plant.category || 'Unknown'}</span>
                    </div>
                  </div>
                </div>

                <aside className="plant-purchase">
                  <div className="plant-price">{displayPrice}</div>

                  <div className="plant-stock">
                    {plant.available ? 'In stock' : 'Out of stock'}
                  </div>

                  <label className="filter-field">
                    <span>Quantity: {quantity}</span>
                    <select defaultValue="1"
                            onChange={(event) => setQuantitySelected(Number(event.target.value))}>
                      <option value="1">Quantity: 1</option>
                      <option value="2">Quantity: 2</option>
                      <option value="3">Quantity: 3</option>
                      <option value="4">Quantity: 4</option>
                      <option value="5">Quantity: 5</option>
                    </select>
                  </label>

                  <button
                      disabled={!plant.available}
                      onClick={() => addNToCart(plant.id, quantitySelected)}
                      type="button"
                      className="plant-add-button"
                  >
                    Add to cart
                  </button>

                  <button
                      disabled={quantity <= 0}
                      onClick={() => removeFromCart(plant.id)}
                      type="button"
                      className="plant-add-button"
                  >
                    Remove from cart
                  </button>

                  <button
                      type="button"
                      className="plant-like-button"
                      onClick={handleToggleLike}
                      disabled={savingLike}
                  >
                    {liked ? 'Unlike plant' : 'Like plant'}
                  </button>

                  {actionError && (
                      <p className="checkout-order-error">
                        {actionError}
                      </p>
                  )}
                </aside>
              </div>

              <section className="similar-plants">
                <h2>Similar plants</h2>

                <div className="similar-plants-row">
                  {Array.from({ length: 6 }).map((_, index) => (
                      <div key={index} className="similar-placeholder">
                        <div className="similar-placeholder-image" />
                        <div className="similar-placeholder-name" />
                      </div>
                  ))}
                </div>
              </section>
            </div>
        ) : null}
      </section>
    </main>
  )
}

export default PlantViewPage
