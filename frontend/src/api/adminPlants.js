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

export function getPlantHealthLogs(id) {
  return api.get(`/admin/plants/${id}/health-logs`)
}

export function getPlantRelocationHistory(id) {
  return api.get(`/admin/plants/${id}/relocation-history`)
}

export function updatePlantCondition(id, data) {
  return api.put(`/admin/plants/${id}/condition`, data)
}

export function getPlantConditionLogs(id) {
  return api.get(`/admin/plants/${id}/condition-logs`)
}

export function getDeletionLog() {
  return api.get('/admin/plants/deletion-log')
}

export function getDeletionLogConditionHistory(deletionLogId) {
  return api.get(`/admin/plants/deletion-log/${deletionLogId}/condition-history`)
}

export function startRelocation(plantId, sectorId) {
  return api.post(`/admin/plants/${plantId}/relocate`, { sectorId })
}

export function updateRelocationState(relocationId, state) {
  return api.put(`/admin/relocations/${relocationId}/state`, { state })
}

export function getPlantRelocationLogs(plantId) {
  return api.get(`/admin/plants/${plantId}/relocation-logs`)
}

export function getActiveRelocations() {
  return api.get('/admin/relocations/active')
}
