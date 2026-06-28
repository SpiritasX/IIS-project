import api from './client'

export function getVarieties() {
  return api.get('/worker/plants/varieties')
}

export function getCompatibleStorageSpaces(varietyId) {
  return api.get('/worker/plants/compatible-storage-spaces', { params: { varietyId } })
}

export function getSectors(storageSpaceId) {
  return api.get('/worker/plants/sectors', { params: { storageSpaceId } })
}

export function getNurserySites(sectorId) {
  return api.get('/worker/plants/nursery-sites', { params: { sectorId } })
}

export function addPlantLot(data) {
  return api.post('/worker/plants', data)
}
