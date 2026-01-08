import request from './request'

export function searchStations(keyword) {
  return request.get('/stations/search', {
    params: { keyword }
  })
}

export function getAllStations() {
  return request.get('/stations/all')
}
