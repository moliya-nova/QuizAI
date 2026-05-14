import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: 'http://localhost:8080/api', // 指向后端接口
  timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(config => {
  const token = localStorage.getItem('adminToken')
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器
request.interceptors.response.use(response => {
  let res = response.data
  if (res.code === 200) {
    return res
  } else {
    if (!response.config.silent) {
      ElMessage.error(res.message || '请求失败')
    }
    return Promise.reject(new Error(res.message))
  }
}, error => {
  if (!error.config?.silent) {
    ElMessage.error('网络错误')
  }
  return Promise.reject(error)
})

export default request
