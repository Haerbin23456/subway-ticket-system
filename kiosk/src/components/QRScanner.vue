<template>
  <div class="scanner-wrapper">
    <div class="video-container">
      <video ref="videoRef" autoplay playsinline class="video"></video>
      <canvas ref="canvasRef" class="canvas"></canvas>
      
      <!-- Scan Overlay -->
      <div class="scan-overlay">
        <div class="scan-box">
          <div class="corner top-left"></div>
          <div class="corner top-right"></div>
          <div class="corner bottom-left"></div>
          <div class="corner bottom-right"></div>
          <div class="scan-line"></div>
        </div>
        <div class="scan-tip">将二维码对准框内</div>
      </div>
    </div>
    
    <div class="error" v-if="error">{{ error }}</div>
  </div>
</template>

<script setup>
import {onUnmounted, ref} from 'vue'
import jsQR from 'jsqr'

const emit = defineEmits(['scan'])

const videoRef = ref(null)
const canvasRef = ref(null)
const streamRef = ref(null)
const error = ref('')
let raf = null
let active = false

async function start() {
  if (active) return
  active = true
  error.value = ''
  
  const video = videoRef.value
  const canvas = canvasRef.value
  if (!video || !canvas) return

  const ctx = canvas.getContext('2d')
  
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
    streamRef.value = stream
    video.srcObject = stream
    await video.play()
    
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    
    const tick = () => {
      if (!active) return
      
      if (video.readyState === video.HAVE_ENOUGH_DATA) {
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
        const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
        const code = jsQR(imageData.data, imageData.width, imageData.height)
        
        if (code && code.data) {
          emit('scan', code.data)
          // Don't stop automatically, let parent decide
        }
      }
      raf = requestAnimationFrame(tick)
    }
    raf = requestAnimationFrame(tick)
  } catch (e) {
    console.error(e)
    error.value = '摄像头不可用或权限被拒绝'
    active = false
  }
}

function stop() {
  active = false
  if (raf) cancelAnimationFrame(raf)
  const s = streamRef.value
  if (s) {
    s.getTracks().forEach(t => t.stop())
    streamRef.value = null
  }
}

onUnmounted(() => {
  stop()
})

defineExpose({ start, stop })
</script>

<style scoped>
.scanner-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  background: #000;
  overflow: hidden;
}

.video-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.canvas {
  display: none;
}

.scan-overlay {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: rgba(0,0,0,0.4);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.scan-box {
  width: 520px;
  height: 520px;
  position: relative;
  box-shadow: 0 0 0 9999px rgba(0,0,0,0.5); /* Dim outside */
  border-radius: 8px;
}

.corner {
  position: absolute;
  width: 40px; height: 40px;
  border-color: #07c160;
  border-style: solid;
  border-width: 0;
}
.top-left { top: -2px; left: -2px; border-top-width: 6px; border-left-width: 6px; }
.top-right { top: -2px; right: -2px; border-top-width: 6px; border-right-width: 6px; }
.bottom-left { bottom: -2px; left: -2px; border-bottom-width: 6px; border-left-width: 6px; }
.bottom-right { bottom: -2px; right: -2px; border-bottom-width: 6px; border-right-width: 6px; }

.scan-line {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 2px;
  background: #07c160;
  box-shadow: 0 0 4px #07c160;
  animation: scanMove 2s infinite linear;
}

@keyframes scanMove {
  0% { transform: translateY(0); opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { transform: translateY(520px); opacity: 0; }
}

.scan-tip {
  color: #fff;
  margin-top: 30px;
  font-size: 24px;
  letter-spacing: 2px;
  text-shadow: 0 1px 2px rgba(0,0,0,0.5);
}

.error {
  position: absolute;
  bottom: 20px;
  left: 0; width: 100%;
  color: #f56c6c;
  text-align: center;
  font-weight: bold;
}
</style>
