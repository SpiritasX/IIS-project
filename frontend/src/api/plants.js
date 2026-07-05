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

export function getPlantHealthLogs(id) {
  return api.get(`/worker/plants/${id}/health-logs`)
}

export function getPlantRelocationHistory(id) {
  return api.get(`/worker/plants/${id}/relocation-history`)
}

export function getPlantConditionLogs(id) {
  return api.get(`/worker/plants/${id}/condition-logs`)
}

export function getDeletionLog() {
  return api.get('/worker/plants/deletion-log')
}

export function getDeletionLogConditionHistory(deletionLogId) {
  return api.get(`/worker/plants/deletion-log/${deletionLogId}/condition-history`)
}

export function startRelocation(plantId, sectorId) {
  return api.post(`/worker/plants/${plantId}/relocate`, { sectorId })
}

export function updateRelocationState(relocationId, state) {
  return api.put(`/worker/relocations/${relocationId}/state`, { state })
}

export function getPlantRelocationLogs(plantId) {
  return api.get(`/worker/plants/${plantId}/relocation-logs`)
}

export function getActiveRelocations() {
  return api.get('/worker/relocations/active')
}
