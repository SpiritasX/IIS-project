import api from './client'

export function getSites() {
  return api.get('/botanist/sites')
}

export function getDashboardStats(siteId) {
  return api.get('/botanist/dashboard/stats', { params: siteId != null ? { siteId } : {} })
}

export function getDashboardStock(siteId) {
  return api.get('/botanist/dashboard/stock', { params: siteId != null ? { siteId } : {} })
}

export function getDashboardRelocations(siteId) {
  return api.get('/botanist/dashboard/relocations', { params: siteId != null ? { siteId } : {} })
}

export function getDashboardLogs(siteId) {
  return api.get('/botanist/dashboard/logs', { params: siteId != null ? { siteId } : {} })
}
