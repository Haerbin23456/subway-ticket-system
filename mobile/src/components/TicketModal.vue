<template>
  <div v-if="show" class="qr-modal-overlay" @click="$emit('close')">
    <div class="qr-modal" @click.stop>
      <div class="modal-title">出票成功</div>
      <div class="qr-container">
        <img :src="qrImg" alt="QR" />
      </div>
      <div class="ticket-info">
        <div class="info-item">
          <span class="label">起点</span>
          <span class="val">{{ fromName }}</span>
        </div>
        <div class="info-item">
          <span class="label">终点</span>
          <span class="val">{{ toName }}</span>
        </div>
        <div class="info-item">
          <span class="label">票价</span>
          <span class="val price">￥{{ price }}</span>
        </div>
      </div>
      <div class="modal-footer">
        <p>请在自助机扫描此码取票</p>
        <button class="btn btn-outline" @click="$emit('close')">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  show: Boolean,
  qrImg: String,
  fromName: String,
  toName: String,
  price: String
})

defineEmits(['close'])
</script>

<style scoped>
.qr-modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.6);
  z-index: 999;
  display: flex; align-items: center; justify-content: center;
  animation: fadeIn 0.2s;
}

.qr-modal {
  background: white;
  width: 80%;
  max-width: 320px;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
  animation: slideUp 0.3s;
}

.modal-title { font-size: 18px; font-weight: bold; margin-bottom: 16px; color: #333; }
.qr-container { background: #f8f9fa; padding: 16px; border-radius: 12px; margin-bottom: 16px; display: inline-block; }
.qr-container img { width: 180px; height: 180px; display: block; }

.ticket-info { text-align: left; margin-bottom: 20px; border-top: 1px dashed #eee; padding-top: 16px; }
.info-item { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px; }
.info-item .label { color: #999; }
.info-item .val { font-weight: 500; color: #333; }
.info-item .val.price { color: #f56c6c; font-weight: bold; font-size: 16px; }

.modal-footer p { font-size: 12px; color: #999; margin-bottom: 16px; }

.btn { border: none; border-radius: 8px; cursor: pointer; font-weight: 600; }
.btn-outline { background: transparent; border: 1px solid #ddd; color: #666; padding: 8px 16px; }

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
</style>
