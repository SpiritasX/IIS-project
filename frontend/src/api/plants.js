import api from './client'

export function getPlants() {
  return api.get('/worker/plants/all')
}

export function getVarieties() {
  return api.get('/worker/plants/varieties')
}

export function getCompatibleStorageSpaces(varietyId) {
  return api.get('/worker/plants/compatible-storage-spaces', { params: { varietyId } })
}

export function getSectors(storageSpaceId) {
  return api.get('/worker/plants/sectors', { params: { storageSpaceId } })
}

export function addPlantLot(data) {
  return api.post('/worker/plants', data)
}

export function updatePlant(id, data) {
  return api.put(`/worker/plants/${id}`, data)
}

export function deletePlant(id, reason) {
  return api.delete(`/worker/plants/${id}`, { data: { reason } })
}

export function getDeletionReasonStats() {
  return api.get('/worker/dashboard/deletion-reasons')
}
