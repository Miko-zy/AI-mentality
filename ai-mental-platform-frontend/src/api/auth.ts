// 登录注册接口模块
// 复用 utils/request.ts 封装：自动携带 Bearer Token、code===200 放行、否则拦截器统一报错
import { post } from '@/utils/request'
import { Result } from '@/types'
import { LoginForm, LoginVO, RegisterParams, SendEmailCodeParams } from '@/types/auth'

/**
 * 账号密码登录
 * 后端：POST /api/user/login
 *
 * @param data 登录表单（邮箱 + 密码）
 * @returns 登录结果（含 JWT Token 与用户信息）
 */
export function loginApi(data: LoginForm): Promise<Result<LoginVO>> {
  return post<LoginVO>('/user/login', data)
}

/**
 * 用户注册
 * 后端：POST /api/user/register
 *
 * @param data 注册参数（用户名、邮箱、验证码、密码）
 * @returns 注册结果（注册即自动登录，含 JWT Token）
 */
export function registerApi(data: RegisterParams): Promise<Result<LoginVO>> {
  return post<LoginVO>('/user/register', data)
}

/**
 * 发送邮箱验证码
 * 后端：POST /api/user/email-code
 * 验证码存入 Redis 有效期 5 分钟；本地联调未接 SMTP 时验证码打印在后端控制台日志
 *
 * @param data 邮箱
 * @returns 发送结果
 */
export function sendEmailCodeApi(data: SendEmailCodeParams): Promise<Result<void>> {
  return post<void>('/user/email-code', data)
}
