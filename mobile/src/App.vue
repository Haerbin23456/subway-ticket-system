<template>
  <div class="app-container">
    <SubwayMap ref="mapRef" @select="onStationSelect" />
    
    <!-- History Button -->
    <button class="btn-history" @click="showHistory = true">
      <div class="icon-clock"></div>
      <span>订单</span>
    </button>

    <FarePanel 
      :fromName="fromName" 
      :toName="toName" 
      :loading="loading" 
      :quote="quote" 
      :error="error"
      :activeMode="selectionMode"
      @order="onOrderClick"
      @switch-mode="setSelectionMode"
      @calculate="onCalculate"
      @open-search="onOpenSearch"
    />

    <PaymentModal
      :show="showPayment"
      :order="order"
      :fromName="fromName"
      :toName="toName"
      :loading="loading"
      @close="showPayment = false"
      @pay="onPayConfirm"
    />

    <TicketModal 
      :show="!!qr" 
      :qrImg="qrImg" 
      :fromName="fromName" 
      :toName="toName" 
      :price="quote?.price ? String(quote.price) : ''"
      @close="closeQr"
    />
    
    <StationSearchModal 
      :show="showSearch"
      @close="showSearch = false"
      @select="onSearchResultSelect"
    />

    <HistoryModal
      :show="showHistory"
      :history="history"
      @close="showHistory = false"
      @select="onHistorySelect"
    />
  </div>
</template>

<script setup>
import {ref} from 'vue'
import SubwayMap from './components/SubwayMap.vue'
import FarePanel from './components/FarePanel.vue'
import TicketModal from './components/TicketModal.vue'
import StationSearchModal from './components/StationSearchModal.vue'
import PaymentModal from './components/PaymentModal.vue'
import HistoryModal from './components/HistoryModal.vue'
import {useTicket} from './composables/useTicket'

const mapRef = ref(null)
const showSearch = ref(false)
const showPayment = ref(false)
const showHistory = ref(false)

const {
  fromCode, toCode, fromName, toName, selectionMode,
  quote, error, loading, order, qr, qrImg, history,
  setSelectionMode, handleStationSelect, fetchQuote, 
  createOrderAction, payOrderAction, fetchQrForHistory, closeQr
} = useTicket()

async function onOrderClick() {
  const success = await createOrderAction()
  if (success) {
    showPayment.value = true
  }
}

async function onPayConfirm() {
  const success = await payOrderAction()
  if (success) {
    showPayment.value = false
    // TicketModal will show automatically because `qr` is set
  }
}

async function onHistorySelect(item) {
  const success = await fetchQrForHistory(item)
  if (success) {
    showHistory.value = false
    // TicketModal shows automatically
  }
}

function onStationSelect({ id, name }) {
  // Pass the CURRENT selectionMode before it might get updated inside handleStationSelect?
  // Actually handleStationSelect updates the state (fromName/toName).
  // We need to know WHICH mode was active to set the marker correctly.
  const currentMode = selectionMode.value;
  
  const { shouldClearRoute } = handleStationSelect(id, name)
  
  if (shouldClearRoute) {
    mapRef.value.clearRoute()
  }
  
  // Set Start/End marker on map visually
  if (mapRef.value) {
    if (currentMode === 'from') {
      mapRef.value.setStart(name);
    } else if (currentMode === 'to') {
      mapRef.value.setEnd(name);
    }
  }
}

function onOpenSearch() {
  showSearch.value = true
}

function onSearchResultSelect(station) {
  // station: { id, name, code, ... }
  
  // 1. Update selection state
  // IMPORTANT: Use station.code instead of station.id!
  // The backend FareService expects the 'code' field (e.g. "3301..."), not the DB primary key 'id' (e.g. 15).
  // The map API also uses 'code' as its ID.
  onStationSelect({ id: station.code, name: station.name })
  
  // 2. Center map on the selected station
  if (mapRef.value) {
    mapRef.value.centerStation(station.name)
  }
}

async function onCalculate() {
  const success = await fetchQuote()
  if (success) {
    // Pass NAMES to showRoute because the map API works best with names for visualization?
    // Actually earlier we passed fromCode/toCode. 
    // If the map API supports names, names are safer if codes are internal.
    // But let's stick to what likely works. 
    // If setStart(name) works, then route(name, name) should work too.
    // And fromCode is the Code.
    // Let's change to use Names for map route to be consistent with setStart/setEnd(name).
    mapRef.value.showRoute(fromName.value, toName.value)
  }
}
</script>

<style>
/* Reset & Base */
body { margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: #f0f2f5; overflow: hidden; }
.app-container { position: relative; width: 100vw; height: 100vh; overflow: hidden; }
</style>
