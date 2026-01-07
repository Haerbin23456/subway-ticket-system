import axios from 'axios'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

service.interceptors.response.use(
  response => response,
  error => {
    // 简单的错误处理，实际项目可以集成 Toast
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export default service
