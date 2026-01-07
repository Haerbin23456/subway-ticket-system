<template>
  <div class="scanner-container">
    <div class="row">
      <video ref="videoRef" autoplay playsinline class="video"></video>
      <canvas ref="canvasRef" class="canvas"></canvas>
    </div>
    <div class="actions">
      <button class="btn" @click="start">启动摄像头</button>
      <button class="btn btn-stop" @click="stop">停止</button>
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

async function start() {
  error.value = ''
  const video = videoRef.value
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } })
    streamRef.value = stream
    video.srcObject = stream
    await video.play()
    
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    
    const tick = () => {
      if (video.readyState === video.HAVE_ENOUGH_DATA) {
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
        const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
        const code = jsQR(imageData.data, imageData.width, imageData.height)
        
        if (code && code.data) {
          emit('scan', code.data)
          // 扫码成功后暂停扫描，防止重复触发，但保持摄像头开启
          // 如果需要连续扫码，可以不 return，或者由父组件控制
          cancelAnimationFrame(raf) 
          return
        }
      }
      raf = requestAnimationFrame(tick)
    }
    raf = requestAnimationFrame(tick)
  } catch (e) {
    console.error(e)
    error.value = '摄像头不可用或权限被拒绝'
  }
}

function stop() {
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
.row { display: flex; gap: 12px; justify-content: center; }
.video, .canvas { width: 100%; max-width: 360px; background: #000; border-radius: 4px; }
/* canvas 实际不需要显示，可以隐藏，或者用于调试 */
.canvas { display: none; } 
.actions { margin-top: 12px; text-align: center; }
.btn { padding: 8px 16px; border: none; border-radius: 6px; background: #409eff; color: #fff; cursor: pointer; font-size: 14px; }
.btn-stop { margin-left: 8px; background: #f56c6c; }
.error { color: #f56c6c; margin-top: 8px; text-align: center; }
</style>
