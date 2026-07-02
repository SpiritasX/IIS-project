import api from './client'

export function getPlants() {
  return api.get('/botanist/plants/all')
}

export function updatePlantCondition(id, data) {
  return api.put(`/botanist/plants/${id}/condition`, data)
}

export function getPlantConditionLogs(id) {
  return api.get(`/botanist/plants/${id}/condition-logs`)
}

export function getDeletionLog() {
  return api.get('/botanist/plants/deletion-log')
}

export function getDeletionLogConditionHistory(deletionLogId) {
  return api.get(`/botanist/plants/deletion-log/${deletionLogId}/condition-history`)
}

export function deletePlant(id, reason) {
  return api.delete(`/botanist/plants/${id}`, { data: { reason } })
}
