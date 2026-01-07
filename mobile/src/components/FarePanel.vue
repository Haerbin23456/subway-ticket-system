<template>
  <div class="floating-panel">
    <div class="panel-header">
      <div class="station-display">
        <div class="station-box">
          <span class="label">起点</span>
          <span class="value" :class="{ placeholder: !fromName }">{{ fromName || '点击地图站点' }}</span>
        </div>
        <div class="arrow">→</div>
        <div class="station-box">
          <span class="label">终点</span>
          <span class="value" :class="{ placeholder: !toName }">{{ toName || '点击地图站点' }}</span>
        </div>
      </div>
    </div>

    <div class="panel-body">
      <div v-if="loading" class="loading-tips">正在计算票价...</div>
      
      <div v-else-if="quote" class="quote-result">
        <div class="price-box">
          <span class="currency">￥</span>
          <span class="amount">{{ quote.price }}</span>
        </div>
        <div class="details">
          <span class="tag">里程计价</span>
          <span class="info">约 {{ quote.segments }} 站</span>
        </div>
        <button class="btn btn-primary btn-lg" @click="$emit('order')">立即购票</button>
      </div>

      <div v-else class="tips">
        请在地图上依次点击起点和终点
      </div>

      <div v-if="error" class="error-msg">{{ error }}</div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

defineProps({
  fromName: String,
  toName: String,
  loading: Boolean,
  quote: Object,
  error: String
})

defineEmits(['order'])
</script>

<style scoped>
.floating-panel {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
  padding: 20px;
  box-sizing: border-box;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.8);
  z-index: 100;
}

.station-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 12px;
}

.station-box { display: flex; flex-direction: column; width: 42%; }
.station-box .label { font-size: 12px; color: #999; margin-bottom: 4px; }
.station-box .value { font-size: 16px; font-weight: 600; color: #333; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.station-box .value.placeholder { color: #ccc; font-weight: 400; }
.arrow { color: #ccc; font-weight: bold; }

.panel-body { min-height: 60px; display: flex; flex-direction: column; justify-content: center; }

.tips { text-align: center; color: #999; font-size: 14px; }
.loading-tips { text-align: center; color: #409eff; }
.error-msg { color: #f56c6c; text-align: center; font-size: 12px; margin-top: 8px; }

.quote-result { display: flex; align-items: center; justify-content: space-between; }
.price-box { display: flex; align-items: baseline; color: #f56c6c; }
.price-box .currency { font-size: 14px; font-weight: bold; }
.price-box .amount { font-size: 32px; font-weight: bold; line-height: 1; }
.details { display: flex; flex-direction: column; margin-left: 12px; flex: 1; }
.details .tag { font-size: 10px; background: #ecf5ff; color: #409eff; padding: 2px 6px; border-radius: 4px; width: fit-content; margin-bottom: 2px; }
.details .info { font-size: 12px; color: #666; }

.btn { border: none; border-radius: 8px; cursor: pointer; font-weight: 600; transition: opacity 0.2s; }
.btn:active { opacity: 0.8; }
.btn-primary { background: #409eff; color: white; box-shadow: 0 4px 12px rgba(64,158,255,0.3); }
.btn-lg { padding: 12px 24px; font-size: 16px; }
</style>
