import {ref} from 'vue'
import {createOrder, getQrCode, mockPay, quoteFare} from '../api/ticket'

export function useTicket() {
  const fromCode = ref('')
  const toCode = ref('')
  const fromName = ref('')
  const toName = ref('')
  const selectionMode = ref('from') // 'from' | 'to'

  const quote = ref(null)
  const error = ref('')
  const loading = ref(false)
  const order = ref(null)
  const qr = ref(null)
  const qrImg = ref('')

  const history = ref([])

  // Initialize history from localStorage
  try {
    const saved = localStorage.getItem('subway_orders')
    if (saved) {
      history.value = JSON.parse(saved)
    }
  } catch (e) {
    console.error('Failed to load history', e)
  }

  function setSelectionMode(mode) {
    selectionMode.value = mode
  }

  function resetQuote() {
    quote.value = null
  }
  
  function saveToHistory(orderData, fromNameStr, toNameStr) {
    const item = {
      id: orderData.id,
      price: orderData.price,
      fromName: fromNameStr,
      toName: toNameStr,
      createdAt: new Date().toISOString() // Use client time for display simplicity
    }
    history.value.unshift(item) // Add to top
    // Limit history size to 20
    if (history.value.length > 20) {
      history.value = history.value.slice(0, 20)
    }
    localStorage.setItem('subway_orders', JSON.stringify(history.value))
  }
  
  function updateHistoryStatus(orderId, status) {
    const idx = history.value.findIndex(i => i.id === orderId)
    if (idx !== -1) {
      history.value[idx].status = status
      localStorage.setItem('subway_orders', JSON.stringify(history.value))
    }
  }

  /**
   * 处理站点选择逻辑
   * @param {string} id 
   * @param {string} name 
   * @returns {Object} result { shouldClearRoute: boolean }
   */
  function handleStationSelect(id, name) {
    let shouldClearRoute = false

    // If we already have quote, any selection should reset quote to allow re-selection
    if (quote.value) {
      resetQuote()
      shouldClearRoute = true
    }

    if (selectionMode.value === 'from') {
      // Set Start
      fromCode.value = id
      fromName.value = name
      
      // If end is already set, clear it if it's the same as start
      if (toCode.value === id) {
          toCode.value = ''
          toName.value = ''
          resetQuote()
          shouldClearRoute = true
      }
      
      // Auto switch to end selection only if end is not set
      if (!toCode.value) {
          selectionMode.value = 'to'
      }
    } else {
      // Set End
      toCode.value = id
      toName.value = name
    }

    return { shouldClearRoute }
  }

  async function fetchQuote() {
    if (!fromCode.value || !toCode.value) return false
    loading.value = true
    try {
      const res = await quoteFare(fromCode.value, toCode.value)
      quote.value = res.data
      error.value = ''
      return true
    } catch (e) {
      const resData = e.response?.data
      // Try to get message, or fallback to mode (e.g. STATION_NOT_FOUND)
      const msg = resData?.message || resData?.mode || e.message || '计算失败'
      error.value = msg
      quote.value = null
      return false
    } finally {
      loading.value = false
    }
  }

  async function createOrderAction() {
    if (!fromCode.value || !toCode.value) return false
    loading.value = true
    try {
      const createRes = await createOrder({ from: fromCode.value, to: toCode.value })
      order.value = createRes.data
      error.value = ''
      return true
    } catch (e) {
      error.value = e.response?.data?.message || '下单失败'
      return false
    } finally {
      loading.value = false
    }
  }

  async function payOrderAction() {
    if (!order.value) return false
    loading.value = true
    try {
      await mockPay(order.value.id)
      
      const qrRes = await getQrCode(order.value.id)
      qr.value = qrRes.data
      
      const QRCode = (await import('qrcode')).default
      qrImg.value = await QRCode.toDataURL(JSON.stringify(qr.value))
      
      // Save to history
      saveToHistory(order.value, fromName.value, toName.value)
      
      error.value = ''
      return true
    } catch (e) {
      error.value = e.response?.data?.message || '支付失败'
      return false
    } finally {
      loading.value = false
    }
  }

  async function fetchQrForHistory(item) {
      loading.value = true
      try {
        const qrRes = await getQrCode(item.id)
        qr.value = qrRes.data
        
        const QRCode = (await import('qrcode')).default
        qrImg.value = await QRCode.toDataURL(JSON.stringify(qr.value))
        
        // Restore display info for modal
        fromName.value = item.fromName
        toName.value = item.toName
        quote.value = { price: item.price }
        
        return true
      } catch (e) {
        if (e.response?.data?.code === 'ORDER_COMPLETED') {
           // Order is completed, update local status
           updateHistoryStatus(item.id, 'COMPLETED')
           error.value = '该车票已取出'
           return false
        }
        error.value = '无法获取二维码，可能订单已过期'
        return false
      } finally {
        loading.value = false
      }
  }

  function closeQr() {
    qr.value = null
  }

  return {
    // State
    fromCode,
    toCode,
    fromName,
    toName,
    selectionMode,
    quote,
    error,
    loading,
    order,
    qr,
    qrImg,
    history,
    
    // Actions
    setSelectionMode,
    handleStationSelect,
    fetchQuote,
    createOrderAction,
    payOrderAction,
    fetchQrForHistory,
    closeQr
  }
}
