import api from './client'

export function getVarieties() {
  return api.get('/admin/varieties')
}

export function addVariety(data) {
  return api.post('/admin/varieties', data)
}

export function updateVariety(id, data) {
  return api.put(`/admin/varieties/${id}`, data)
}

export function getCategories() {
  return api.get('/admin/taxonomy/categories')
}

export function getTypesByCategory(categoryId) {
  return api.get('/admin/taxonomy/types', { params: { categoryId } })
}

export function getSpeciesByType(typeId) {
  return api.get('/admin/taxonomy/species', { params: { typeId } })
}

export function getStorageSpaces() {
  return api.get('/admin/taxonomy/storage-spaces')
}

export function getTaxonomyTree() {
  return api.get('/admin/taxonomy/tree')
}

export function createCategory(name) {
  return api.post('/admin/taxonomy/categories', { name })
}

export function renameCategory(id, name) {
  return api.put(`/admin/taxonomy/categories/${id}`, { name })
}

export function deleteCategory(id) {
  return api.delete(`/admin/taxonomy/categories/${id}`)
}

export function createType(categoryId, name) {
  return api.post('/admin/taxonomy/types', { categoryId, name })
}

export function renameType(id, name) {
  return api.put(`/admin/taxonomy/types/${id}`, { name })
}

export function deleteType(id) {
  return api.delete(`/admin/taxonomy/types/${id}`)
}

export function createSpecies(typeId, name) {
  return api.post('/admin/taxonomy/species', { typeId, name })
}

export function renameSpecies(id, name) {
  return api.put(`/admin/taxonomy/species/${id}`, { name })
}

export function deleteSpecies(id) {
  return api.delete(`/admin/taxonomy/species/${id}`)
}
