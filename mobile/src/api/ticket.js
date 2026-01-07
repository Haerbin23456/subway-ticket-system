import request from './request'

export function quoteFare(from, to) {
  return request.get('/fares/quote', { params: { from, to } })
}

export function createOrder(data) {
  return request.post('/orders', data)
}

export function mockPay(orderId) {
  return request.post('/payments/mock', { orderId })
}

export function getQrCode(orderId) {
  return request.get(`/orders/${orderId}/qrcode`)
}
