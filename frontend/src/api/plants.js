import api from './client'

export function getVarieties() {
  return api.get('/worker/plants/varieties')
}

export function getCompatibleUnits(varietyId) {
  return api.get('/worker/plants/compatible-units', { params: { varietyId } })
}

export function getLocationParcels(unitId) {
  return api.get('/worker/plants/location-parcels', { params: { unitId } })
}

export function getNurserySites(parcelId) {
  return api.get('/worker/plants/nursery-sites', { params: { parcelId } })
}

export function addPlantLot(data) {
  return api.post('/worker/plants', data)
}
