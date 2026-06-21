function emptyToNull(value) {
  const normalizedValue = String(value || '').trim()
  return normalizedValue ? normalizedValue : null
}

function numberOrNull(value) {
  if (value === '' || value === null || value === undefined) {
    return null
  }

  const numericValue = Number(value)
  return Number.isFinite(numericValue) ? numericValue : null
}

export function trackPlantSearch(api, userId, filters) {
  if (!userId) {
    return Promise.resolve()
  }

  return api
    .post('/recommendations/search', {
      customer_id: userId,
      query: String(filters.query || '').trim(),
      min_price: numberOrNull(filters.minPrice),
      max_price: numberOrNull(filters.maxPrice),
      variety: emptyToNull(filters.variety),
      species: emptyToNull(filters.species),
      type: filters.plantType && filters.plantType !== 'All' ? filters.plantType : null,
    })
    .catch(() => {
      // Recommendation tracking should not block product search results.
    })
}
