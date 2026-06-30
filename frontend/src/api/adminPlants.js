import api from './client'

export function getVarieties() {
  return api.get('/admin/plants/varieties')
}

export function getCompatibleStorageSpaces(varietyId) {
  return api.get('/admin/plants/compatible-storage-spaces', { params: { varietyId } })
}

export function getSectors(storageSpaceId) {
  return api.get('/admin/plants/sectors', { params: { storageSpaceId } })
}

export function addPlantLot(data) {
  return api.post('/admin/plants', data)
}

export function getPlants() {
  return api.get('/admin/plants/all')
}

export function updatePlant(id, data) {
  return api.put(`/admin/plants/${id}`, data)
}

export function deletePlant(id, reason) {
  return api.delete(`/admin/plants/${id}`, { data: { reason } })
}

export function getDeletionReasonStats() {
  return api.get('/admin/dashboard/deletion-reasons')
}
