// 登录注册模块类型定义
// 与后端 DTO/VO 一一对应（context-path 为 /api，由 axios 实例 baseURL 统一拼接）

/**
 * 账号密码登录请求（对应后端 LoginDTO）
 * 账号即邮箱，邮箱在后端 AES-256 加密存储
 */
export interface LoginForm {
  /** 邮箱账号 */
  email: string
  /** 密码 */
  password: string
}

/**
 * 注册表单（页面表单模型）
 * confirmPassword 仅前端做一致性校验，不发送给后端
 */
export interface RegisterForm {
  /** 用户名（注册后作为昵称保存） */
  username: string
  /** 邮箱 */
  email: string
  /** 6位数字验证码 */
  code: string
  /** 密码（6-20位，同时包含字母和数字） */
  password: string
  /** 确认密码（仅前端） */
  confirmPassword: string
}

/**
 * 注册请求参数（对应后端 RegisterDTO）
 * 不含 confirmPassword
 */
export interface RegisterParams {
  /** 用户名 */
  username: string
  /** 邮箱 */
  email: string
  /** 邮箱验证码 */
  code: string
  /** 密码 */
  password: string
}

/**
 * 发送邮箱验证码请求（对应后端 SendEmailCodeDTO）
 * 验证码存入 Redis，有效期 5 分钟
 */
export interface SendEmailCodeParams {
  /** 邮箱 */
  email: string
}

/**
 * 登录用户信息（对应后端 UserVO）
 * 邮箱、手机号由后端解密后返回
 */
export interface LoginUserVO {
  /** 用户ID */
  userId: number
  /** 昵称 */
  nickname: string
  /** 头像URL */
  avatarUrl: string
  /** 邮箱（明文） */
  email: string
  /** 手机号（明文） */
  phone: string
  /** 实名认证标记：0未认证，1已认证 */
  realAuthFlag: number
  /** 账号状态：0禁用，1正常 */
  accountStatus: number
  /** 总积分 */
  totalPoints: number
  /** 用户等级 */
  userLevel: number
  /** 角色 */
  role: string
}

/**
 * 登录/注册返回结果（对应后端 LoginVO）
 * 注册接口同样返回该结构，注册成功即自动登录
 */
export interface LoginVO {
  /** JWT Token（放行接口认证凭证） */
  token: string
  /** 登录用户信息 */
  user: LoginUserVO
}
