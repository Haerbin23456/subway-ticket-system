<template>
  <div class="floating-panel">
    <div class="panel-header">
      <div class="station-display">
        <div 
          class="station-box" 
          :class="{ active: activeMode === 'from', 'active-from': activeMode === 'from' }"
          @click="$emit('switch-mode', 'from')"
        >
          <span class="label">起点</span>
          <div class="value-row">
            <span class="value" :class="{ placeholder: !fromName }">{{ fromName || '待选择' }}</span>
            <span class="search-btn" @click.stop="$emit('open-search', 'from')">
              <span class="icon"></span>
            </span>
          </div>
        </div>
        <div class="arrow">→</div>
        <div 
          class="station-box" 
          :class="{ active: activeMode === 'to', 'active-to': activeMode === 'to' }"
          @click="$emit('switch-mode', 'to')"
        >
          <span class="label">终点</span>
          <div class="value-row">
            <span class="value" :class="{ placeholder: !toName }">{{ toName || '待选择' }}</span>
            <span class="search-btn" @click.stop="$emit('open-search', 'to')">
              <span class="icon"></span>
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="panel-body">
      <div v-if="loading" class="loading-tips">正在计算票价...</div>
      
      <div v-else-if="quote" class="quote-result">
        <div class="price-row">
          <div class="price-box">
            <span class="currency">￥</span>
            <span class="amount">{{ quote.price }}</span>
          </div>
          <div class="details">
            <span class="tag">里程计价</span>
            <span class="info">约 {{ quote.segments }} 站</span>
          </div>
        </div>
        
        <!-- Route Timeline -->
        <RouteTimeline :steps="quote.steps" />

        <button class="btn btn-primary btn-lg" @click="$emit('order')">立即购票</button>
      </div>

      <div v-else-if="fromName && toName" class="action-area">
          <button class="btn btn-primary btn-block" @click="$emit('calculate')">计算路线</button>
      </div>

      <div v-else class="tips">
        请在地图上依次点击起点和终点
      </div>

      <div v-if="error" class="error-msg">{{ error }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  fromName: String,
  toName: String,
  loading: Boolean,
  quote: Object,
  error: String,
  activeMode: {
    type: String,
    default: 'from'
  }
})

defineEmits(['order', 'switch-mode', 'calculate', 'open-search'])

import RouteTimeline from './RouteTimeline.vue'
</script>

<style scoped>
.value-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.search-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f2f5;
  border-radius: 50%;
  margin-left: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.search-btn:active {
  background-color: #e6e8eb;
  transform: scale(0.95);
}
.search-btn .icon {
  width: 8px;
  height: 8px;
  border-top: 2px solid #909399;
  border-right: 2px solid #909399;
  transform: rotate(-45deg);
  margin-top: 4px; /* Push it down a bit to center visually */
}
.active .search-btn {
  background-color: #ecf5ff;
}
.active .search-btn .icon {
  border-color: #409eff;
}
/* Common styles moved to assets/base.css */
.action-area {
    width: 100%;
}
/* .btn-block moved to base.css */
.floating-panel {
  position: absolute;
  bottom: max(20px, env(safe-area-inset-bottom));
  left: 50%;
  transform: translateX(-50%);
  width: calc(100% - 40px);
  max-width: 450px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px); /* Safari support */
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  padding: 20px;
  box-sizing: border-box;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.4); /* Thinner border opacity */
  z-index: 100;
}

.station-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 4px;
  background: rgba(248, 249, 250, 0.5); /* Semi-transparent inner bg */
  border-radius: 12px;
}

.station-box { 
  display: flex; 
  flex-direction: column; 
  width: 42%; 
  padding: 8px 12px;
  border-radius: 10px;
  transition: all 0.2s;
  cursor: pointer;
  border: 2px solid transparent;
}
.station-box.active {
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.station-box.active-from {
  border-color: #07c160;
}
.station-box.active-to {
  border-color: #f56c6c;
}

.station-box .label { font-size: 12px; color: #999; margin-bottom: 4px; }
.station-box .value { font-size: 16px; font-weight: 600; color: #333; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.station-box .value.placeholder { color: #ccc; font-weight: 400; }
.arrow { color: #ccc; font-weight: bold; }

.panel-body { min-height: 60px; display: flex; flex-direction: column; justify-content: center; }

/* 
.loading-tips, .error-msg moved to base.css 
*/
.tips { text-align: center; color: #999; font-size: 14px; }

.quote-result { display: flex; flex-direction: column; align-items: stretch; }
.price-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.price-box { display: flex; align-items: baseline; color: #f56c6c; }
.price-box .currency { font-size: 14px; font-weight: bold; }
.price-box .amount { font-size: 32px; font-weight: bold; line-height: 1; }
.details { display: flex; flex-direction: column; margin-left: 12px; flex: 1; }
.details .tag { font-size: 10px; background: #ecf5ff; color: #409eff; padding: 2px 6px; border-radius: 4px; width: fit-content; margin-bottom: 2px; }
.details .info { font-size: 12px; color: #666; }

/* Removed Timeline Styles as they are now in RouteTimeline.vue */

/* .btn styles moved to base.css */
</style>
