import axios from 'axios'
import {ElMessage} from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

service.interceptors.response.use(
  response => response,
  error => {
    console.error('API Error:', error)
    const msg = error.response?.data?.message || error.message || '请求失败'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default service
