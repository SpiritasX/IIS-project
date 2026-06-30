import api from './client'

export function getCatalogPlants() {
  return api.get('/plants')
}

export function getPriceHistory(plantId) {
  return api.get(`/price/${plantId}/history`)
}

export function getDemandHistory(plantId) {
  return api.get(`/price/${plantId}/demand`)
}

export function updatePlantPrice(plantId, data) {
  return api.post(`/price/update/${plantId}`, data)
}

export function rollbackPlantPrice(plantId, data) {
  return api.post(`/price/update/${plantId}/rollback`, data)
}
