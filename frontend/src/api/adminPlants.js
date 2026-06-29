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
