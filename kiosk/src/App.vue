<template>
  <div class="wrap">
    <div class="card">
      <div class="title">取票机扫码演示</div>
      
      <QRScanner @scan="handleScan" ref="scannerRef" />

      <div class="result-area">
        <div class="result" v-if="decoded">
          <div class="label">扫码内容：</div>
          <div class="value">{{ decoded }}</div>
        </div>
        <div class="result" v-if="validate">
          <div class="label">验证结果：</div>
          <div class="value" :class="{ valid: validate.valid, invalid: !validate.valid }">
            {{ validate.valid ? '有效' : '无效' }}
            <span v-if="validate.reason">({{ validate.reason }})</span>
          </div>
        </div>
        <div class="result" v-if="issue">
          <div class="label">出票状态：</div>
          <div class="value">{{ issue.issued ? '出票成功' : '出票失败' }}</div>
        </div>
        <div class="error" v-if="error">{{ error }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import axios from 'axios'
import QRScanner from './components/QRScanner.vue'

const api = axios.create({ baseURL: '/api' })
const scannerRef = ref(null)

const decoded = ref('')
const validate = ref(null)
const issue = ref(null)
const error = ref('')

async function handleScan(data) {
  console.log('Scanned:', data)
  decoded.value = data
  validate.value = null
  issue.value = null
  error.value = ''
  
  try {
    let payload;
    try {
      payload = JSON.parse(data)
    } catch (e) {
      error.value = '二维码格式错误'
      return
    }

    const v = await api.post('/kiosk/validate', payload)
    validate.value = v.data
    
    if (v.data && v.data.valid) {
      const res = await api.post('/tickets/issue', payload)
      issue.value = res.data
    }
  } catch (e) {
    console.error(e)
    error.value = '请求失败: ' + (e.response?.data?.message || e.message)
  }
}
</script>

<style>
body { margin: 0; background: #f0f2f5; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; }
.wrap { max-width: 600px; margin: 24px auto; padding: 16px; }
.card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1); padding: 24px; }
.title { font-weight: 600; font-size: 18px; margin-bottom: 24px; text-align: center; color: #303133; }
.result-area { margin-top: 24px; border-top: 1px solid #ebeef5; padding-top: 16px; }
.result { display: flex; margin-bottom: 12px; align-items: baseline; }
.label { width: 80px; color: #909399; font-size: 14px; flex-shrink: 0; }
.value { color: #303133; font-size: 14px; word-break: break-all; }
.valid { color: #67c23a; font-weight: bold; }
.invalid { color: #f56c6c; font-weight: bold; }
.error { color: #f56c6c; margin-top: 12px; text-align: center; font-size: 14px; }
</style>
