import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'
import { Result, SUCCESS_CODE, UNAUTHORIZED_CODE, FORBIDDEN_CODE } from '@/types'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    // 如果token存在，自动携带token
    if (userStore.token && config.headers) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<Result<any>>) => {
    const res = response.data

    // 如果返回的不是标准Result格式，直接返回
    if (res === undefined || res.code === undefined) {
      return response
    }

    // 请求成功（拦截器内部将业务Result透传，此处断言为AxiosResponse以满足axios类型签名）
    if (res.code === SUCCESS_CODE) {
      return res as unknown as AxiosResponse<Result<any>>
    }

    // 未认证
    if (res.code === UNAUTHORIZED_CODE) {
      ElMessage.error('登录已过期，请重新登录')
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      return Promise.reject(new Error(res.message || '未认证'))
    }

    // 无权限
    if (res.code === FORBIDDEN_CODE) {
      ElMessage.error('没有访问权限')
      return Promise.reject(new Error(res.message || '无权限'))
    }

    // 其他错误
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    console.error('响应错误:', error)

    if (error.response) {
      const status = error.response.status

      if (status === UNAUTHORIZED_CODE) {
        ElMessage.error('登录已过期，请重新登录')
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      } else if (status === FORBIDDEN_CODE) {
        ElMessage.error('没有访问权限')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status === 500) {
        ElMessage.error('服务器内部错误')
      } else {
        ElMessage.error(error.response.data?.message || `请求错误(${status})`)
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error(error.message || '请求失败')
    }

    return Promise.reject(error)
  }
)

// GET请求
export function get<T>(url: string, params?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
  return service.get(url, { params, ...config })
}

// POST请求
export function post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
  return service.post(url, data, config)
}

// PUT请求
export function put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
  return service.put(url, data, config)
}

// DELETE请求
export function del<T>(url: string, params?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
  return service.delete(url, { params, ...config })
}

export default service
