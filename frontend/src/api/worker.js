import api from './client'

export function getSites() {
  return api.get('/worker/sites')
}

export function getDashboardStats(siteId) {
  return api.get('/worker/dashboard/stats', { params: siteId != null ? { siteId } : {} })
}

export function getStockByVariety(siteId) {
  return api.get('/worker/dashboard/stock-by-variety', { params: siteId != null ? { siteId } : {} })
}

export function getPlantCountByUnit(siteId) {
  return api.get('/worker/dashboard/plant-count', { params: siteId != null ? { siteId } : {} })
}

export function getRelocationLogs(siteId) {
  return api.get('/worker/dashboard/relocation-logs', { params: siteId != null ? { siteId } : {} })
}
