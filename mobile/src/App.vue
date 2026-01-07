<template>
  <div class="app-container">
    <!-- 全屏地图背景 -->
    <div id="mysubway"></div>

    <!-- 底部悬浮操作面板 -->
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
          <button class="btn btn-primary btn-lg" @click="orderAndPay">立即购票</button>
        </div>

        <div v-else class="tips">
          请在地图上依次点击起点和终点
        </div>

        <div v-if="error" class="error-msg">{{ error }}</div>
      </div>
    </div>

    <!-- 二维码弹窗 -->
    <div v-if="qr" class="qr-modal-overlay" @click="qr = null">
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
            <span class="val price">￥{{ quote.price }}</span>
          </div>
        </div>
        <div class="modal-footer">
          <p>请在自助机扫描此码取票</p>
          <button class="btn btn-outline" @click="qr = null">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

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

let subwayInstance = null;

// Initialize Subway Map
onMounted(() => {
  setTimeout(() => {
      initSubway();
  }, 500);
});

function initSubway() {
    if (!window.subway) return;
    
    const mySubway = window.subway("mysubway", {
        adcode: 3301, // Hangzhou
        theme: "colorful",
        client: 0,
        doubleclick: {
            min: 0.5,
            max: 3.0
        }
    });
    
    subwayInstance = mySubway;

    // Event: Map fully loaded
    mySubway.event.on("subway.complete", function() {
        // Zoom out to see more of the map
        mySubway.scale(0.6); 
        // Optional: Center on a central station (approximate pixel coords for Hangzhou center)
        // Or let it stay at default center
    });

    mySubway.event.on("station.touch", function(ev, info) {
        const id = info.id; 
        const name = info.name;
        
        if (!fromCode.value || (fromCode.value && toCode.value)) {
            // New selection start
            fromCode.value = id;
            fromName.value = name;
            toCode.value = '';
            toName.value = '';
            quote.value = null;
            qr.value = null;
            error.value = '';
            
            // Clear route if exists
            mySubway.clearRoute();
        } else {
            // Set end
            toCode.value = id;
            toName.value = name;
            quoteFare();
        }
    });
}

async function quoteFare() {
  if (!fromCode.value || !toCode.value) return
  loading.value = true
  try {
    const res = await api.get('/fares/quote', { params: { from: fromCode.value, to: toCode.value } })
    quote.value = res.data
    error.value = ''
    
    // Visualize Route using AMAP API
    if (subwayInstance) {
        subwayInstance.route(fromCode.value, toCode.value);
    }
  } catch (e) {
    console.error('Fare quote error:', e);
    const msg = (e && e.response && e.response.data && e.response.data.message) 
        ? e.response.data.message 
        : (e.message || '计算失败，请重试');
    error.value = '错误: ' + msg;
    quote.value = null
  } finally {
    loading.value = false
  }
}

async function orderAndPay() {
  if (!fromCode.value || !toCode.value) return
  loading.value = true
  try {
    const createRes = await api.post('/orders', { from: fromCode.value, to: toCode.value })
    order.value = createRes.data
    await api.post('/payments/mock', { orderId: order.value.id })
    const qrRes = await api.get(`/orders/${order.value.id}/qrcode`)
    qr.value = qrRes.data
    const QRCode = (await import('qrcode')).default
    const data = JSON.stringify(qr.value)
    qrImg.value = await QRCode.toDataURL(data)
    error.value = ''
  } catch (e) {
    error.value = '下单失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style>
/* Reset & Base */
body { margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: #f0f2f5; overflow: hidden; }

/* Fullscreen Map */
.app-container { position: relative; width: 100vw; height: 100vh; overflow: hidden; }
#mysubway { width: 100%; height: 100%; background: #fff; }

/* Floating Panel */
.floating-panel {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 400px;
  background: rgba(0, 0, 0 0);
  backdrop-filter: blur(20px); /* Increased blur for better separation */
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15); /* Slightly stronger shadow */
  padding: 20px;
  box-sizing: border-box;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.8); /* Added border for definition */
  z-index: 100; /* Ensure panel is above map */
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
.btn-outline { background: transparent; border: 1px solid #ddd; color: #666; padding: 8px 16px; }

/* QR Modal */
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

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
</style>
