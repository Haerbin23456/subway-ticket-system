<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal-content">
      <div class="modal-header">
        <div class="title">确认支付</div>
        <div class="close" @click="$emit('close')">×</div>
      </div>
      
      <div class="order-info">
        <div class="route-info">
          <span class="station">{{ fromName }}</span>
          <span class="arrow">→</span>
          <span class="station">{{ toName }}</span>
        </div>
        <div class="amount">
          <span class="symbol">￥</span>
          <span class="val">{{ order?.price || '0.00' }}</span>
        </div>
      </div>

      <div class="payment-methods">
        <div 
          class="method-item" 
          :class="{ active: selectedMethod === 'wechat' }"
          @click="selectedMethod = 'wechat'"
        >
          <div class="icon wechat"></div>
          <div class="name">微信支付</div>
          <div class="radio"></div>
        </div>
        <div 
          class="method-item" 
          :class="{ active: selectedMethod === 'alipay' }"
          @click="selectedMethod = 'alipay'"
        >
          <div class="icon alipay"></div>
          <div class="name">支付宝</div>
          <div class="radio"></div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-pay" :disabled="loading" @click="handlePay">
          <span v-if="loading">支付中...</span>
          <span v-else>立即支付 ￥{{ order?.price || '0.00' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue'

const props = defineProps({
  show: Boolean,
  order: Object,
  fromName: String,
  toName: String,
  loading: Boolean
})

const emit = defineEmits(['close', 'pay'])

const selectedMethod = ref('wechat')

function handlePay() {
  emit('pay')
}
</script>

<style scoped>
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.6);
  z-index: 1000;
  display: flex; align-items: flex-end; /* Bottom sheet style */
}

.modal-content {
  background: #fff;
  width: 100%;
  border-radius: 16px 16px 0 0;
  padding: 20px;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px;
}
.title { font-size: 18px; font-weight: bold; color: #333; }
.close { font-size: 24px; color: #999; cursor: pointer; padding: 0 8px; }

.order-info {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #eee;
}
.route-info { font-size: 16px; color: #666; margin-bottom: 12px; }
.route-info .arrow { margin: 0 8px; color: #ccc; }
.amount { color: #333; font-weight: bold; }
.amount .symbol { font-size: 20px; }
.amount .val { font-size: 36px; }

.payment-methods { margin-bottom: 30px; }
.method-item {
  display: flex; align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
}
.method-item:last-child { border-bottom: none; }

.icon { width: 24px; height: 24px; border-radius: 4px; margin-right: 12px; }
.icon.wechat { background: #07c160; }
.icon.alipay { background: #1677ff; }

.name { flex: 1; font-size: 16px; color: #333; }

.radio {
  width: 20px; height: 20px;
  border: 2px solid #ddd;
  border-radius: 50%;
  position: relative;
}
.method-item.active .radio {
  border-color: #07c160;
  background: #07c160;
}
.method-item.active .radio::after {
  content: '';
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  width: 8px; height: 8px;
  background: #fff;
  border-radius: 50%;
}

.btn-pay {
  width: 100%;
  padding: 14px;
  background: #07c160;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
}
.btn-pay:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-pay:active { opacity: 0.9; }
</style>