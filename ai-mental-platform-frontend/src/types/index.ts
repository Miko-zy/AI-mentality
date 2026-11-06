// 统一返回结果类型
export interface Result<T> {
  code: number
  message: string
  data: T
}

// 成功响应码
export const SUCCESS_CODE = 200

// 失败响应码
export const FAIL_CODE = 500

// 未认证响应码
export const UNAUTHORIZED_CODE = 401

// 无权限响应码
export const FORBIDDEN_CODE = 403

// 用户信息类型
export interface UserInfo {
  id: number
  username: string
  nickname?: string
  avatar?: string
  role: string
}

// 分页参数
export interface PageParams {
  pageNum: number
  pageSize: number
}

// 分页结果
export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

// 后端时间字段值
// 后端未定制 LocalDateTime 序列化，实际可能为：
// 1. "yyyy-MM-dd HH:mm:ss" 字符串（date-format 生效场景）
// 2. ISO 字符串 "yyyy-MM-ddTHH:mm:ss"
// 3. 时间戳数组 [年, 月, 日, 时, 分, 秒]（Jackson 默认 WRITE_DATES_AS_TIMESTAMPS）
// 页面渲染统一经 formatDate/formatDateTime 兼容处理
export type DateTimeValue = string | number[] | null
