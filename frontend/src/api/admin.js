import api from './client'

export function getSites() {
  return api.get('/admin/sites')
}

export function getDashboardStats(siteId) {
  return api.get('/admin/dashboard/stats', { params: siteId != null ? { siteId } : {} })
}

export function getStockByVariety(siteId) {
  return api.get('/admin/dashboard/stock-by-variety', { params: siteId != null ? { siteId } : {} })
}

export function getPlantCountByUnit(siteId) {
  return api.get('/admin/dashboard/plant-count', { params: siteId != null ? { siteId } : {} })
}

export function getRelocationLogs(siteId) {
  return api.get('/admin/dashboard/relocation-logs', { params: siteId != null ? { siteId } : {} })
}

export function getDeletionReasonStats() {
  return api.get('/admin/dashboard/deletion-reasons')
}
