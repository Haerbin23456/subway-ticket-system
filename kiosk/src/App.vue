<template>
  <div class="wrap">
    <div class="card">
      <div class="title">取票机扫码演示</div>
      <div class="row">
        <video id="video" autoplay playsinline class="video"></video>
        <canvas id="canvas" class="canvas"></canvas>
      </div>
      <div class="actions">
        <button class="btn" @click="start">启动摄像头</button>
        <button class="btn" style="margin-left:8px" @click="stop">停止</button>
      </div>
      <div class="result" v-if="decoded">
        <div>载荷：{{ decoded }}</div>
      </div>
      <div class="result" v-if="validate">
        <div>验证：{{ validate.valid }}（{{ validate.reason }}）</div>
      </div>
      <div class="result" v-if="issue">
        <div>出票：{{ issue.issued }}</div>
      </div>
      <div class="error" v-if="error">{{ error }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import jsQR from 'jsqr'

const api = axios.create({ baseURL: '/api' })
const streamRef = ref(null)
const decoded = ref('')
const validate = ref(null)
const issue = ref(null)
const error = ref('')
let raf = null

async function start() {
  error.value = ''
  validate.value = null
  issue.value = null
  decoded.value = ''
  const video = document.getElementById('video')
  const canvas = document.getElementById('canvas')
  const ctx = canvas.getContext('2d')
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
    streamRef.value = stream
    video.srcObject = stream
    await video.play()
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    const tick = async () => {
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
      const code = jsQR(imageData.data, imageData.width, imageData.height)
      if (code && code.data) {
        decoded.value = code.data
        try {
          const payload = JSON.parse(code.data)
          const v = await api.post('/kiosk/validate', payload)
          validate.value = v.data
          if (v.data && v.data.valid) {
            const res = await api.post('/tickets/issue', payload)
            issue.value = res.data
          }
        } catch (e) {
          error.value = '解析或验证失败'
        }
        cancelAnimationFrame(raf)
        return
      }
      raf = requestAnimationFrame(tick)
    }
    raf = requestAnimationFrame(tick)
  } catch (e) {
    error.value = '摄像头不可用'
  }
}

function stop() {
  if (raf) cancelAnimationFrame(raf)
  const s = streamRef.value
  if (s) s.getTracks().forEach(t => t.stop())
}
</script>

<style>
.wrap { max-width: 800px; margin: 24px auto; padding: 16px; }
.card { background: #fff; border-radius: 8px; box-shadow: 0 1px 8px rgba(0,0,0,0.06); padding: 16px; }
.title { font-weight: 600; margin-bottom: 12px; }
.row { display: flex; gap: 12px; }
.video, .canvas { width: 100%; max-width: 360px; background: #000; }
.actions { margin-top: 12px; }
.btn { padding: 8px 12px; border: none; border-radius: 6px; background: #409eff; color: #fff; cursor: pointer; }
.result { margin-top: 8px; padding: 8px; background: #f0f9eb; border: 1px solid #e1f3d8; border-radius: 6px; }
.error { color: #f56c6c; margin-top: 8px; }
</style>
