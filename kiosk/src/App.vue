<template>
  <div class="kiosk-container">
    <!-- Header -->
    <header class="header">
      <div class="brand">🚇 地铁自助取票机</div>
      <div class="clock">{{ currentTime }}</div>
    </header>

    <!-- Main Content Area -->
    <main class="main-content">
      
      <!-- STATE: IDLE -->
      <div v-if="state === 'IDLE'" class="page-idle" @click="startScan">
        <div class="idle-icon">🎫</div>
        <div class="idle-title">互联网取票</div>
        <div class="idle-subtitle">点击屏幕开始扫码</div>
      </div>

      <!-- STATE: SCANNING -->
      <div v-else-if="state === 'SCANNING'" class="page-scan">
        <div class="scan-wrapper">
          <QRScanner @scan="handleScan" ref="scannerRef" />
        </div>
        <button class="btn-cancel" @click="resetState">取消</button>
      </div>

      <!-- STATE: PROCESSING -->
      <div v-else-if="state === 'PROCESSING'" class="page-process">
        <div class="spinner"></div>
        <div class="process-text">{{ processMsg }}</div>
      </div>

      <!-- STATE: SUCCESS -->
      <div v-else-if="state === 'SUCCESS'" class="page-success">
        <div class="success-icon">✅</div>
        <div class="success-title">出票成功</div>
        <div class="ticket-info">
          <div class="t-row">
            <span class="label">起点：</span>
            <span class="val">{{ ticketInfo?.fromStation }}</span>
          </div>
          <div class="t-row">
            <span class="label">终点：</span>
            <span class="val">{{ ticketInfo?.toStation }}</span>
          </div>
          <div class="t-row">
            <span class="label">票价：</span>
            <span class="val price">￥{{ ticketInfo?.price }}</span>
          </div>
        </div>
        <div class="countdown">将在 {{ countdown }} 秒后返回首页</div>
      </div>

      <!-- STATE: ERROR -->
      <div v-else-if="state === 'ERROR'" class="page-error">
        <div class="error-icon">❌</div>
        <div class="error-title">取票失败</div>
        <div class="error-msg">{{ errorMsg }}</div>
        <button class="btn-retry" @click="startScan">重试</button>
        <button class="btn-home" @click="resetState">返回首页</button>
      </div>

    </main>

    <!-- Footer -->
    <footer class="footer">
      <div>服务热线：12345</div>
      <div>设备编号：K-8801</div>
    </footer>
  </div>
</template>

<script setup>
import {nextTick, onMounted, onUnmounted, ref} from 'vue'
import axios from 'axios'
import QRScanner from './components/QRScanner.vue'

// State Machine: IDLE -> SCANNING -> PROCESSING -> SUCCESS/ERROR -> IDLE
const state = ref('IDLE')
const currentTime = ref('')
const processMsg = ref('')
const errorMsg = ref('')
const ticketInfo = ref(null)
const countdown = ref(5)

const scannerRef = ref(null)
const api = axios.create({ baseURL: '/api' })

let timer = null
let countdownTimer = null

function updateTime() {
  const now = new Date()
  currentTime.value = `${now.getHours().toString().padStart(2,'0')}:${now.getMinutes().toString().padStart(2,'0')}`
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (countdownTimer) clearInterval(countdownTimer)
})

function resetState() {
  state.value = 'IDLE'
  if (scannerRef.value) scannerRef.value.stop()
  if (countdownTimer) clearInterval(countdownTimer)
}

async function startScan() {
  state.value = 'SCANNING'
  await nextTick()
  if (scannerRef.value) scannerRef.value.start()
}

async function handleScan(data) {
  // Stop scanning immediately
  if (scannerRef.value) scannerRef.value.stop()
  
  state.value = 'PROCESSING'
  processMsg.value = '正在验证二维码...'
  
  try {
    let payload
    try {
      payload = JSON.parse(data)
    } catch (e) {
      throw new Error('二维码格式错误')
    }

    // 1. Validate
    const v = await api.post('/kiosk/validate', payload)
    if (!v.data || !v.data.valid) {
      throw new Error(v.data?.reason || '二维码无效或已使用')
    }

    // 2. Issue Ticket
    processMsg.value = '验证通过，正在出票...'
    // Simulate delay for better UX
    await new Promise(r => setTimeout(r, 800))
    
    const res = await api.post('/tickets/issue', payload)
    if (res.data && res.data.issued) {
      ticketInfo.value = res.data.ticket
      state.value = 'SUCCESS'
      startCountdown()
    } else {
      throw new Error('出票系统异常')
    }
    
  } catch (e) {
    state.value = 'ERROR'
    errorMsg.value = e.response?.data?.message || e.message || '系统繁忙'
  }
}

function startCountdown() {
  countdown.value = 5
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      resetState()
    }
  }, 1000)
}
</script>

<style>
/* Global Reset */
body { margin: 0; padding: 0; font-family: "PingFang SC", "Microsoft YaHei", sans-serif; background: #1a1a1a; color: #fff; overflow: hidden; user-select: none; }

.kiosk-container {
  display: flex; flex-direction: column; height: 100vh; width: 100vw;
  background: linear-gradient(135deg, #1f2329 0%, #2c3e50 100%);
}

.header {
  height: 80px; padding: 0 40px;
  display: flex; justify-content: space-between; align-items: center;
  background: rgba(0,0,0,0.2);
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.brand { font-size: 28px; font-weight: bold; letter-spacing: 2px; }
.clock { font-size: 24px; font-family: monospace; }

.main-content {
  flex: 1; position: relative;
  display: flex; justify-content: center; align-items: center;
}

.footer {
  height: 60px;
  display: flex; justify-content: space-between; align-items: center;
  padding: 0 40px;
  color: rgba(255,255,255,0.4);
  font-size: 14px;
}

/* Page: IDLE */
.page-idle {
  text-align: center; cursor: pointer;
  animation: pulse 2s infinite;
}
.idle-icon { font-size: 120px; margin-bottom: 20px; }
.idle-title { font-size: 48px; font-weight: bold; margin-bottom: 16px; }
.idle-subtitle { font-size: 24px; color: rgba(255,255,255,0.6); }

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.9; }
  50% { transform: scale(1.02); opacity: 1; }
  100% { transform: scale(1); opacity: 0.9; }
}

/* Page: SCANNING */
.page-scan { width: 100%; height: 100%; position: relative; }
.scan-wrapper { width: 100%; height: 100%; }
.btn-cancel {
  position: absolute; bottom: 40px; left: 50%; transform: translateX(-50%);
  padding: 16px 48px; border-radius: 40px; border: 2px solid rgba(255,255,255,0.3);
  background: rgba(0,0,0,0.4); color: #fff; font-size: 20px;
  cursor: pointer;
}

/* Page: PROCESSING */
.page-process { text-align: center; }
.spinner {
  width: 60px; height: 60px; border: 6px solid rgba(255,255,255,0.1);
  border-top-color: #409eff; border-radius: 50%;
  animation: spin 1s linear infinite; margin: 0 auto 30px;
}
.process-text { font-size: 24px; }
@keyframes spin { to { transform: rotate(360deg); } }

/* Page: SUCCESS */
.page-success {
  background: #fff; color: #333;
  width: 600px; padding: 60px; border-radius: 20px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0,0,0,0.5);
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.success-icon { font-size: 80px; margin-bottom: 20px; }
.success-title { font-size: 36px; font-weight: bold; color: #07c160; margin-bottom: 40px; }

.ticket-info { text-align: left; background: #f8f9fa; padding: 30px; border-radius: 12px; margin-bottom: 40px; }
.t-row { display: flex; justify-content: space-between; margin-bottom: 16px; font-size: 20px; border-bottom: 1px dashed #eee; padding-bottom: 16px; }
.t-row:last-child { border-bottom: none; margin-bottom: 0; padding-bottom: 0; }
.t-row .label { color: #999; }
.t-row .val { font-weight: 600; }
.t-row .val.price { color: #f56c6c; font-size: 28px; }

.countdown { color: #999; font-size: 16px; }

/* Page: ERROR */
.page-error {
  background: #fff; color: #333;
  width: 500px; padding: 60px; border-radius: 20px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0,0,0,0.5);
}
.error-icon { font-size: 80px; margin-bottom: 20px; }
.error-title { font-size: 32px; font-weight: bold; color: #f56c6c; margin-bottom: 20px; }
.error-msg { font-size: 18px; color: #666; margin-bottom: 40px; }
.btn-retry, .btn-home {
  padding: 16px 40px; border-radius: 8px; border: none; font-size: 18px; cursor: pointer; margin: 0 10px; font-weight: bold;
}
.btn-retry { background: #409eff; color: #fff; }
.btn-home { background: #f0f2f5; color: #666; }

@keyframes popIn {
  from { transform: scale(0.8); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
</style>
