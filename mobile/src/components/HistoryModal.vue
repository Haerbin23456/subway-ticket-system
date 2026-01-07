<template>
  <div v-if="show" class="modal-overlay" @click="$emit('close')">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <div class="title">我的订单</div>
        <div class="close" @click="$emit('close')">×</div>
      </div>
      
      <div class="history-list">
        <div v-if="history.length === 0" class="empty-tip">暂无购票记录</div>
        
        <div 
          v-for="item in history" 
          :key="item.id" 
          class="history-item"
          @click="$emit('select', item)"
        >
          <div class="item-header">
            <span class="time">{{ formatTime(item.createdAt) }}</span>
            <span class="status" :class="{ completed: item.status === 'COMPLETED' }">
              {{ item.status === 'COMPLETED' ? '已出票' : '已支付' }}
            </span>
          </div>
          <div class="route">
            <span class="station">{{ item.fromName }}</span>
            <span class="arrow">→</span>
            <span class="station">{{ item.toName }}</span>
          </div>
          <div class="price">￥{{ item.price }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  show: Boolean,
  history: Array
})

defineEmits(['close', 'select'])

function formatTime(isoStr) {
  if (!isoStr) return ''
  const date = new Date(isoStr)
  return `${date.getMonth()+1}月${date.getDate()}日 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.6);
  z-index: 1000;
  display: flex; align-items: flex-end;
}

.modal-content {
  background: #f8f9fa;
  width: 100%;
  height: 70vh; /* Taller than payment modal */
  border-radius: 16px 16px 0 0;
  display: flex; flex-direction: column;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px;
  background: #fff;
  border-radius: 16px 16px 0 0;
  border-bottom: 1px solid #eee;
}
.title { font-size: 18px; font-weight: bold; color: #333; }
.close { font-size: 24px; color: #999; cursor: pointer; padding: 0 8px; }

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.empty-tip {
  text-align: center; color: #999; margin-top: 40px; font-size: 14px;
}

.history-item {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
  active: scale(0.98);
  transition: transform 0.1s;
}
.history-item:active { transform: scale(0.98); }

.item-header {
  display: flex; justify-content: space-between;
  margin-bottom: 12px;
  font-size: 12px;
}
.time { color: #999; }
.status { color: #07c160; font-weight: 500; }
.status.completed { color: #909399; }

.route {
  display: flex; align-items: center;
  font-size: 16px; font-weight: 600; color: #333;
  margin-bottom: 8px;
}
.route .arrow { margin: 0 8px; color: #ccc; font-weight: normal; }

.price {
  text-align: right;
  color: #f56c6c;
  font-weight: bold;
  font-size: 14px;
}
</style>