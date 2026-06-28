import api from './client'

export function getVarieties() {
  return api.get('/botanist/varieties')
}

export function addVariety(data) {
  return api.post('/botanist/varieties', data)
}

export function getCategories() {
  return api.get('/botanist/taxonomy/categories')
}

export function getTypesByCategory(categoryId) {
  return api.get('/botanist/taxonomy/types', { params: { categoryId } })
}

export function getSpeciesByType(typeId) {
  return api.get('/botanist/taxonomy/species', { params: { typeId } })
}

export function getStorageSpaces() {
  return api.get('/botanist/taxonomy/storage-spaces')
}
