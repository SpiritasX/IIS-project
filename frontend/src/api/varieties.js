import api from './client'

export function getAllPlants() {
  return api.get('/botanist/plants/all')
}

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

export function getTaxonomyTree() {
  return api.get('/botanist/taxonomy/tree')
}

export function createCategory(name) {
  return api.post('/botanist/taxonomy/categories', { name })
}

export function renameCategory(id, name) {
  return api.put(`/botanist/taxonomy/categories/${id}`, { name })
}

export function deleteCategory(id) {
  return api.delete(`/botanist/taxonomy/categories/${id}`)
}

export function createType(categoryId, name) {
  return api.post('/botanist/taxonomy/types', { categoryId, name })
}

export function renameType(id, name) {
  return api.put(`/botanist/taxonomy/types/${id}`, { name })
}

export function deleteType(id) {
  return api.delete(`/botanist/taxonomy/types/${id}`)
}

export function createSpecies(typeId, name) {
  return api.post('/botanist/taxonomy/species', { typeId, name })
}

export function renameSpecies(id, name) {
  return api.put(`/botanist/taxonomy/species/${id}`, { name })
}

export function deleteSpecies(id) {
  return api.delete(`/botanist/taxonomy/species/${id}`)
}
