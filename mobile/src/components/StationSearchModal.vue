<template>
  <div v-if="show" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="search-header">
        <div class="search-type-tabs">
           <div 
             :class="['tab', searchType === 'station' ? 'active' : '']" 
             @click="changeSearchType('station')"
           >搜站点</div>
           <div 
             :class="['tab', searchType === 'place' ? 'active' : '']" 
             @click="changeSearchType('place')"
           >搜地点</div>
        </div>
        <div class="input-wrapper">
          <input 
            ref="searchInput"
            v-model="keyword"
            type="text" 
            :placeholder="searchType === 'station' ? '输入站点名称搜索...' : '输入地名(如: 西湖)搜索附近站点...'" 
            class="search-input"
            @input="onInput"
          >
          <button class="close-btn" @click="$emit('close')">取消</button>
        </div>
      </div>
      
      <div class="search-results">
        <div v-if="loading" class="loading">搜索中...</div>
        
        <!-- Station Results -->
        <template v-if="searchType === 'station'">
          <div v-if="!loading && results.length === 0" class="no-result">无匹配站点</div>
          <div 
            v-for="station in results" 
            :key="station.id" 
            class="result-item"
            @click="selectStation(station)"
          >
            <div class="station-info">
              <div class="station-name">{{ station.name }}</div>
              <div v-if="station.lineName" class="line-display-row">
                <span class="line-badge-small" :style="{ backgroundColor: station.lineColor }">
                  {{ formatLineName(station.lineName).main }}
                </span>
                <span v-if="formatLineName(station.lineName).direction" class="line-direction-small">
                  {{ formatLineName(station.lineName).direction }}
                </span>
              </div>
            </div>
          </div>
        </template>

        <!-- Place Results -->
        <template v-if="searchType === 'place'">
          <!-- If a place is selected, show nearby stations -->
          <div v-if="selectedPlace" class="nearby-section">
             <div class="nearby-header">
               <span class="nearby-title">距离 <b>{{ selectedPlace.name }}</b> 最近的站点:</span>
               <button class="back-btn" @click="selectedPlace = null">返回列表</button>
             </div>
             <div 
               v-for="st in nearbyStations" 
               :key="st.id" 
               class="result-item nearby-item"
               @click="selectStation(st)"
             >
               <div class="station-info">
                 <div class="station-name">{{ st.name }}</div>
                 <div v-if="st.lineName" class="line-display-row">
                   <span class="line-badge-small" :style="{ backgroundColor: st.lineColor }">
                     {{ formatLineName(st.lineName).main }}
                   </span>
                   <span v-if="formatLineName(st.lineName).direction" class="line-direction-small">
                     {{ formatLineName(st.lineName).direction }}
                   </span>
                 </div>
                 <div class="station-distance">约 {{ st.distanceText }}</div>
               </div>
               <div class="select-hint">选择</div>
             </div>
          </div>

          <!-- Otherwise show place suggestions -->
          <div v-else>
            <div v-if="!loading && places.length === 0 && keyword" class="no-result">无匹配地点</div>
            <div 
              v-for="place in places" 
              :key="place.id" 
              class="result-item place-item"
              @click="onSelectPlace(place)"
            >
              <div class="place-icon">📍</div>
              <div class="place-info">
                <div class="place-name">{{ place.name }}</div>
                <div class="place-addr">{{ place.district }}{{ (typeof place.address === 'string' ? place.address : '') }}</div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import {nextTick, onMounted, ref, watch} from 'vue'
import {getAllStations, searchStations} from '../api/station'

const props = defineProps({
  show: Boolean
})

const emit = defineEmits(['close', 'select'])

const searchType = ref('station') // 'station' | 'place'
const keyword = ref('')
const results = ref([])
const places = ref([])
const loading = ref(false)
const searchInput = ref(null)
const allStations = ref([])
const selectedPlace = ref(null)
const nearbyStations = ref([])

let debounceTimer = null
let autoComplete = null

onMounted(async () => {
  // Load all stations once for distance calculation
  try {
    const res = await getAllStations()
    allStations.value = res.data
  } catch (e) {
    console.error('Failed to load stations for proximity search', e)
  }

  // Initialize AMap AutoComplete if available
  if (window.AMap) {
    window.AMap.plugin(['AMap.AutoComplete'], () => {
      autoComplete = new window.AMap.AutoComplete({
        city: '杭州' // Default city
      })
    })
  }
})

watch(() => props.show, (newVal) => {
  if (newVal) {
    keyword.value = ''
    selectedPlace.value = null
    places.value = []
    // Load default list for station search
    if (searchType.value === 'station') {
      fetchStations('')
    }
    nextTick(() => {
      if (searchInput.value) searchInput.value.focus()
    })
  }
})

function changeSearchType(type) {
  searchType.value = type
  keyword.value = ''
  selectedPlace.value = null
  results.value = []
  places.value = []
  if (type === 'station') {
    fetchStations('')
  }
  nextTick(() => {
    if (searchInput.value) searchInput.value.focus()
  })
}

function onInput() {
  if (debounceTimer) clearTimeout(debounceTimer)
  
  loading.value = true
  debounceTimer = setTimeout(() => {
    if (searchType.value === 'station') {
      fetchStations(keyword.value)
    } else {
      fetchPlaces(keyword.value)
    }
  }, 300)
}

async function fetchStations(kw) {
  loading.value = true
  try {
    const res = await searchStations(kw)
    results.value = res.data
  } catch (e) {
    console.error(e)
    results.value = []
  } finally {
    loading.value = false
  }
}

function fetchPlaces(kw) {
  if (!kw) {
    places.value = []
    loading.value = false
    return
  }
  
  if (!autoComplete) {
    loading.value = false
    return
  }

  autoComplete.search(kw, (status, result) => {
    if (status === 'complete' && result.tips) {
      places.value = result.tips.filter(tip => tip.location) // Only show tips with coordinates
    } else {
      places.value = []
    }
    loading.value = false
  })
}

function onSelectPlace(place) {
  selectedPlace.value = place
  const { lng, lat } = place.location
  
  // Calculate distance to all stations and pick top 5
  nearbyStations.value = allStations.value
      .filter(st => st.lng && st.lat)
      .map(st => {
        const dist = getDistance(lat, lng, st.lat, st.lng)
        return {
          ...st,
          distance: dist,
          distanceText: dist > 1000 ? (dist / 1000).toFixed(1) + 'km' : Math.round(dist) + 'm'
        }
      })
      .sort((a, b) => a.distance - b.distance)
      .slice(0, 5)
}

function getDistance(lat1, lng1, lat2, lng2) {
  const R = 6371e3 // metres
  const p1 = lat1 * Math.PI/180
  const p2 = lat2 * Math.PI/180
  const dp = (lat2-lat1) * Math.PI/180
  const dl = (lng2-lng1) * Math.PI/180

  const a = Math.sin(dp/2) * Math.sin(dp/2) +
            Math.cos(p1) * Math.cos(p2) *
            Math.sin(dl/2) * Math.sin(dl/2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))

  return R * c // in metres
}

function selectStation(station) {
  emit('select', station)
  emit('close')
}

function formatLineName(name) {
  if (!name) return { main: '', direction: '' }
  const match = name.match(/^(.*?)\s*(\(.*\))$/)
  if (match) {
    return { main: match[1], direction: match[2] }
  }
  return { main: name, direction: '' }
}
</script>

<script>
// For CSS
export default {
  name: 'StationSearchModal'
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  z-index: 2000;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.modal-content {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  width: 100%;
  height: 85vh;
  border-radius: 20px 20px 0 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.search-header {
  padding: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.search-type-tabs {
  display: flex;
  background: rgba(0, 0, 0, 0.05);
  padding: 4px;
  border-radius: 10px;
  margin-bottom: 16px;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 8px;
  font-size: 14px;
  color: #666;
  border-radius: 8px;
  transition: all 0.2s;
}

.tab.active {
  background: #fff;
  color: #1a4695;
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.input-wrapper {
  display: flex;
  align-items: center;
}

.search-input {
  flex: 1;
  padding: 12px 16px;
  font-size: 16px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  background: #fff;
  outline: none;
}

.close-btn {
  margin-left: 12px;
  background: none;
  border: none;
  font-size: 16px;
  color: #1a4695;
  font-weight: 500;
}

.search-results {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px;
}

.loading, .no-result {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.result-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  cursor: pointer;
}

.result-item:active {
  background-color: rgba(0, 0, 0, 0.05);
}

.station-info, .place-info {
  flex: 1;
}

.station-name, .place-name {
  font-size: 17px;
  color: #333;
  font-weight: 500;
  margin-bottom: 4px;
}

.line-display-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 4px;
}

.line-badge-small {
  padding: 2px 6px;
  background: #1a4695; /* Default fallback */
  color: #fff;
  font-size: 10px;
  border-radius: 4px;
  margin-bottom: 4px;
  text-shadow: 0 1px 1px rgba(0,0,0,0.2);
}

.line-direction-small {
  font-size: 11px;
  color: #666;
}

.place-addr {
  font-size: 13px;
  color: #999;
}

.place-icon {
  font-size: 20px;
  margin-right: 12px;
  opacity: 0.7;
}

.nearby-section {
  padding-top: 10px;
}

.nearby-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  margin-bottom: 10px;
}

.nearby-title {
  font-size: 14px;
  color: #666;
}

.nearby-title b {
  color: #333;
}

.back-btn {
  background: rgba(26, 70, 149, 0.1);
  color: #1a4695;
  border: none;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 13px;
}

.station-distance {
  font-size: 13px;
  color: #ff9800;
  font-weight: bold;
}

.select-hint {
  font-size: 13px;
  color: #1a4695;
  border: 1px solid #1a4695;
  padding: 2px 8px;
  border-radius: 4px;
}
</style>