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

  function setSelectionMode(mode) {
    selectionMode.value = mode
  }

  function resetQuote() {
    quote.value = null
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
      const msg = e.response?.data?.message || e.message || '计算失败'
      error.value = msg
      quote.value = null
      return false
    } finally {
      loading.value = false
    }
  }

  async function orderAndPay() {
    if (!fromCode.value || !toCode.value) return
    loading.value = true
    try {
      const createRes = await createOrder({ from: fromCode.value, to: toCode.value })
      order.value = createRes.data
      
      await mockPay(order.value.id)
      
      const qrRes = await getQrCode(order.value.id)
      qr.value = qrRes.data
      
      const QRCode = (await import('qrcode')).default
      qrImg.value = await QRCode.toDataURL(JSON.stringify(qr.value))
      error.value = ''
    } catch (e) {
      error.value = e.response?.data?.message || '下单失败'
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
    
    // Actions
    setSelectionMode,
    handleStationSelect,
    fetchQuote,
    orderAndPay,
    closeQr
  }
}
