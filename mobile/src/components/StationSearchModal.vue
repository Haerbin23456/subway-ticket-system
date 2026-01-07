<template>
  <div v-if="show" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="search-header">
        <input 
          ref="searchInput"
          v-model="keyword"
          type="text" 
          placeholder="输入站点名称搜索..." 
          class="search-input"
          @input="onInput"
        >
        <button class="close-btn" @click="$emit('close')">取消</button>
      </div>
      
      <div class="search-results">
        <div v-if="loading" class="loading">搜索中...</div>
        <div v-else-if="results.length === 0" class="no-result">无匹配站点</div>
        
        <div 
          v-for="station in results" 
          :key="station.id" 
          class="result-item"
          @click="selectStation(station)"
        >
          <!-- <div class="station-icon">🚇</div> Removed icon as per user request -->
          <div class="station-info">
            <div class="station-name">{{ station.name }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {nextTick, ref, watch} from 'vue'
import {searchStations} from '../api/station'

const props = defineProps({
  show: Boolean
})

const emit = defineEmits(['close', 'select'])

const keyword = ref('')
const results = ref([])
const loading = ref(false)
const searchInput = ref(null)
let debounceTimer = null

watch(() => props.show, (newVal) => {
  if (newVal) {
    keyword.value = ''
    // Load default list (empty search)
    fetchStations('')
    nextTick(() => {
      if (searchInput.value) searchInput.value.focus()
    })
  }
})

function onInput() {
  if (debounceTimer) clearTimeout(debounceTimer)
  
  loading.value = true
  debounceTimer = setTimeout(() => {
    fetchStations(keyword.value)
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

function selectStation(station) {
  emit('select', station)
  emit('close')
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
  justify-content: flex-end; /* Slide up from bottom */
}

.modal-content {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  width: 100%;
  height: 80vh; /* Occupy most of the screen */
  border-radius: 16px 16px 0 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.search-header {
  padding: 16px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  background: transparent;
}

.search-input {
  flex: 1;
  padding: 10px 14px;
  font-size: 16px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.5);
  outline: none;
}
.search-input:focus {
  background: rgba(255, 255, 255, 0.8);
  border-color: #409eff;
}

.close-btn {
  margin-left: 12px;
  background: none;
  border: none;
  font-size: 16px;
  color: #666;
  cursor: pointer;
}

.search-results {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px;
}

.loading, .no-result {
  text-align: center;
  padding: 20px;
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

/*
.station-icon {
  font-size: 20px;
  margin-right: 12px;
}
*/

.station-info {
  flex: 1;
}

.station-name {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}
</style>
