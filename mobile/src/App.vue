<template>
  <div class="app-container">
    <SubwayMap ref="mapRef" @select="handleSelect" />
    
    <FarePanel 
      :fromName="fromName" 
      :toName="toName" 
      :loading="loading" 
      :quote="quote" 
      :error="error"
      @order="orderAndPay"
    />

    <TicketModal 
      :show="!!qr" 
      :qrImg="qrImg" 
      :fromName="fromName" 
      :toName="toName" 
      :price="quote?.price ? String(quote.price) : ''"
      @close="qr = null"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import SubwayMap from './components/SubwayMap.vue'
import FarePanel from './components/FarePanel.vue'
import TicketModal from './components/TicketModal.vue'
import { quoteFare, createOrder, mockPay, getQrCode } from './api/ticket'

const mapRef = ref(null)

const fromCode = ref('')
const toCode = ref('')
const fromName = ref('')
const toName = ref('')

const quote = ref(null)
const error = ref('')
const loading = ref(false)
const order = ref(null)
const qr = ref(null)
const qrImg = ref('')

function handleSelect({ id, name }) {
  if (!fromCode.value || (fromCode.value && toCode.value)) {
    // New selection start
    fromCode.value = id
    fromName.value = name
    toCode.value = ''
    toName.value = ''
    quote.value = null
    qr.value = null
    error.value = ''
    
    mapRef.value.clearRoute()
  } else {
    // Set end
    toCode.value = id
    toName.value = name
    fetchQuote()
  }
}

async function fetchQuote() {
  if (!fromCode.value || !toCode.value) return
  loading.value = true
  try {
    const res = await quoteFare(fromCode.value, toCode.value)
    quote.value = res.data
    error.value = ''
    mapRef.value.showRoute(fromCode.value, toCode.value)
  } catch (e) {
    const msg = e.response?.data?.message || e.message || '计算失败'
    error.value = msg
    quote.value = null
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
</script>

<style>
/* Reset & Base */
body { margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: #f0f2f5; overflow: hidden; }
.app-container { position: relative; width: 100vw; height: 100vh; overflow: hidden; }
</style>
