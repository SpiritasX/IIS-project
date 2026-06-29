import api from './client'

export const getSites = () =>
  api.get('/worker/sites')

export const getSpaceTypes = () =>
  api.get('/worker/storage-space-types')

export const getLocations = (siteId) =>
  api.get('/worker/locations', { params: siteId ? { siteId } : {} })

export const getLocation = (id) =>
  api.get(`/worker/locations/${id}`)

export const createLocation = (body) =>
  api.post('/worker/locations', body)

export const updateLocation = (id, body) =>
  api.put(`/worker/locations/${id}`, body)

export const deleteLocation = (id) =>
  api.delete(`/worker/locations/${id}`)

export const addSector = (id, body) =>
  api.post(`/worker/locations/${id}/sectors`, body)

export const updateSector = (id, body) =>
  api.put(`/worker/sectors/${id}`, body)

export const deleteSector = (id) =>
  api.delete(`/worker/sectors/${id}`)
