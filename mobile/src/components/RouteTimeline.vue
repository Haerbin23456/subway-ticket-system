<template>
  <div v-if="steps && steps.length > 0" class="route-timeline">
    <div v-for="(step, index) in steps" :key="index">
      <div class="route-step">
        <div class="line-container">
          <div class="step-line-badge" :style="{ backgroundColor: step.lineColor }">
            {{ formatLineName(step.lineName).main }}
          </div>
          <div v-if="formatLineName(step.lineName).direction" class="step-line-direction">
            {{ formatLineName(step.lineName).direction }}
          </div>
        </div>
        <div class="step-content">
          <div class="step-stations">
            {{ step.fromStation }} <span class="arrow">→</span> {{ step.toStation }}
          </div>
          <div class="step-meta">
            {{ step.stationCount }}站
          </div>
        </div>
      </div>
      <div v-if="index < steps.length - 1" class="transfer-icon">
        ↓ 换乘
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  steps: Array
})

function formatLineName(name) {
  if (!name) return { main: '', direction: '' }
  // Split "3号线 (石马-星桥)" into "3号线" and "(石马-星桥)"
  const match = name.match(/^(.*?)\s*(\(.*\))$/)
  if (match) {
    return { main: match[1], direction: match[2] }
  }
  return { main: name, direction: '' }
}
</script>

<style scoped>
.route-timeline {
  margin: 12px 0 16px;
  padding: 12px;
  background: rgba(248, 249, 250, 0.6); /* Semi-transparent timeline */
  border-radius: 8px;
  max-height: 200px;
  overflow-y: auto;
}
.route-step {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  position: relative;
}
.route-step:last-child { margin-bottom: 0; }

.line-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 12px;
  min-width: 60px;
}

.step-line-badge {
  font-size: 10px;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  text-align: center;
  text-shadow: 0 1px 2px rgba(0,0,0,0.3);
  white-space: nowrap;
}

.step-line-direction {
  font-size: 9px;
  color: #666;
  margin-top: 4px;
  white-space: nowrap;
  transform: scale(0.9);
}
.step-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.step-stations { font-size: 12px; color: #333; font-weight: 500; }
.step-stations .arrow { color: #999; margin: 0 4px; }
.step-meta { font-size: 10px; color: #999; }
.transfer-icon {
  width: 100%;
  text-align: center;
  font-size: 10px;
  color: #999;
  margin: 4px 0;
  border-top: 1px dashed #eee;
  padding-top: 2px;
}
</style>
