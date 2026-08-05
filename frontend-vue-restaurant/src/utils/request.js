import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
const baseURL = '/api'

const instance = axios.create({
    baseURL,
    timeout: 10000
})

instance.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('takeout') ? JSON.parse(localStorage.getItem('takeout')).token : ''
        if (token) {
            config.headers.Authorization = token
        }
        return config
    },
    (err) => Promise.reject(err)
)

// 响应拦截器
instance.interceptors.response.use(
    (res) => {
        // 假设后端返回的 Result 对象中，成功时 code 可能是 200 或者其他值
        // 或者后端可能没有 code 字段，而是直接在 message 中返回结果
        // 这里简化处理，直接返回响应
        // return res
        const response = res.data
        if (response.code === 200) {
            return response
        } else {
            ElMessage.error(response.message || '操作失败')
            return Promise.reject(response)
        }
    },
    (err) => {
        if (err.response?.status === 401) {
            router.push('/login')
        }
        ElMessage.error(err.response?.data?.message || '服务异常')
        return Promise.reject(err)
    }
)

export default instance
export { baseURL }
