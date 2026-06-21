export function normalizeProduct(product) {
  const available =
    typeof product.available === 'boolean'
      ? product.available
      : product.availability === undefined
        ? true
        : Number(product.availability) > 0
  const id = String(product.id)
  const type = product.type || product.plantTypeName || ''

  return {
    ...product,
    available,
    category: product.category || type || 'Other',
    description: product.description || '',
    id,
    imageAlt: `${product.name} placeholder`,
    price: Number(product.price || 0),
    priceId: String(product.priceId || product.plantPriceId || product.id),
    species: product.species || product.speciesName || '',
    type,
    variety: product.variety || product.varietyName || '',
  }
}
