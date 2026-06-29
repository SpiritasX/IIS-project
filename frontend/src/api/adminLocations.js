import api from './client'

export const getSites = () =>
  api.get('/admin/sites')

export const getSpaceTypes = () =>
  api.get('/admin/storage-space-types')

export const createSpaceType = (name) =>
  api.post('/admin/storage-space-types', { name })

export const createNurserySite = (body) =>
  api.post('/admin/nursery-sites', body)

export const deleteNurserySite = (id) =>
  api.delete(`/admin/nursery-sites/${id}`)

export const getLocations = (siteId) =>
  api.get('/admin/locations', { params: siteId ? { siteId } : {} })

export const getLocation = (id) =>
  api.get(`/admin/locations/${id}`)

export const createLocation = (body) =>
  api.post('/admin/locations', body)

export const updateLocation = (id, body) =>
  api.put(`/admin/locations/${id}`, body)

export const deleteLocation = (id) =>
  api.delete(`/admin/locations/${id}`)

export const addSector = (id, body) =>
  api.post(`/admin/locations/${id}/sectors`, body)

export const updateSector = (id, body) =>
  api.put(`/admin/sectors/${id}`, body)

export const deleteSector = (id) =>
  api.delete(`/admin/sectors/${id}`)
