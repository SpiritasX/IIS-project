function changesForSnapshot(snapshots, index) {
  const snapshot = snapshots[index]

  if (index === 0) {
    return snapshot.items.map((item) => `${item.name}: ${item.quantity}`)
  }

  const previousItems = new Map(snapshots[index - 1].items.map((item) => [item.priceId, item]))
  const currentItems = new Map(snapshot.items.map((item) => [item.priceId, item]))
  const priceIds = new Set([...previousItems.keys(), ...currentItems.keys()])

  return [...priceIds].flatMap((priceId) => {
    const previous = previousItems.get(priceId)
    const current = currentItems.get(priceId)
    const previousQuantity = previous?.quantity || 0
    const currentQuantity = current?.quantity || 0

    if (previousQuantity === currentQuantity) {
      return []
    }

    const name = current?.name || previous?.name || 'Product'
    if (previousQuantity === 0) {
      return [`${name}: added ${currentQuantity}`]
    }
    if (currentQuantity === 0) {
      return [`${name}: removed ${previousQuantity}`]
    }
    return [`${name}: ${previousQuantity} → ${currentQuantity}`]
  })
}

function OrderHistory({ snapshots }) {
  if (!snapshots?.length) {
    return null
  }

  return (
    <div className="request-order-history">
      <h3>Order history</h3>
      {snapshots.map((snapshot, index) => {
        const changes = changesForSnapshot(snapshots, index)

        return (
          <section className="request-order-snapshot" key={snapshot.id || `${snapshot.changedAt}-${index}`}>
            <div className="request-order-snapshot-heading">
              <strong>{index === 0 ? 'Initial reservation' : 'Request edited'}</strong>
              <span>{snapshot.changedAt}</span>
            </div>
            {changes.map((change) => (
              <span key={change}>{change}</span>
            ))}
            <em>Total: {snapshot.total}</em>
          </section>
        )
      })}
    </div>
  )
}

export default OrderHistory
