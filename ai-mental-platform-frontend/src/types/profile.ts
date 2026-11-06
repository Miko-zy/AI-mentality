// 个人中心模块类型定义
// 与后端 DTO/VO 一一对应（UserProfileVO / UpdateUserInfoDTO / ChangePwdDTO /
// CheckinRecordVO / BadgeVO / FeedbackVO / FeedbackSubmitDTO）

// 引入共享的时间字段类型（定义在 @/types），供本模块字段使用，并重导出保持引用兼容
import type { DateTimeValue } from '@/types'

export type { DateTimeValue }

/**
 * 个人档案（对应后端 UserProfileVO）
 * 基础信息 + 心理健康成长档案统计
 */
export interface UserProfile {
  /** 用户ID */
  userId: number
  /** 昵称 */
  nickname: string
  /** 头像URL */
  avatarUrl: string
  /** 邮箱（解密后返回） */
  email: string
  /** 手机号（解密后返回，未填写为空） */
  phone: string
  /** 实名认证标记：0未认证，1已认证 */
  realAuthFlag: number
  /** 账号状态：0禁用，1正常 */
  accountStatus: number
  /** 总积分 */
  totalPoints: number
  /** 用户等级 */
  userLevel: number
  /** 注册时间 */
  registerTime: DateTimeValue
  /** 角色（user/admin） */
  role: string
  /** 累计评估次数 */
  assessCount: number
  /** 累计训练完成数量（完成率100%的课程数） */
  courseFinishCount: number
  /** 累计打卡次数 */
  checkinCount: number
  /** 已解锁徽章数量 */
  badgeCount: number
}

/**
 * 编辑个人资料请求（对应后端 UpdateUserInfoDTO）
 * 同时作为编辑资料对话框的表单模型
 * 后端规则：昵称2-20、头像地址≤255、手机号/邮箱仅非空时校验（传空则忽略该字段不更新）
 */
export interface UpdateProfileParams {
  /** 昵称 */
  nickname: string
  /** 头像URL */
  avatarUrl: string
  /** 实名认证标记：0未认证，1已认证 */
  realAuthFlag: number
  /** 手机号（选填） */
  phone: string
  /** 邮箱 */
  email: string
}

/**
 * 修改密码请求（对应后端 ChangePwdDTO）
 */
export interface ChangePasswordParams {
  /** 旧密码 */
  oldPassword: string
  /** 新密码（6-20位，含字母和数字，且不能与旧密码相同） */
  newPassword: string
}

/**
 * 修改密码表单模型
 * confirmPassword 仅前端做一致性校验，不发送给后端
 */
export interface ChangePasswordForm extends ChangePasswordParams {
  /** 确认新密码（仅前端） */
  confirmPassword: string
}

/**
 * 打卡记录（对应后端 CheckinRecordVO）
 */
export interface CheckinRecordItem {
  /** 打卡ID */
  checkinId: number
  /** 打卡日期 */
  checkinDate: DateTimeValue
  /** 心情描述 */
  moodDesc: string
  /** 打卡创建时间 */
  createTime: DateTimeValue
}

/**
 * 成就徽章（对应后端 BadgeVO，含解锁状态）
 */
export interface BadgeItem {
  /** 徽章ID */
  badgeId: number
  /** 徽章名称 */
  badgeName: string
  /** 图标地址 */
  badgeIcon: string
  /** 解锁条件（结构化格式，如 CHECKIN_STREAK:3） */
  obtainCondition: string
  /** 徽章说明 */
  description: string
  /** 是否已解锁 */
  unlocked: boolean
  /** 解锁时间（未解锁为null） */
  obtainTime: DateTimeValue
}

/**
 * 提交用户反馈请求（对应后端 FeedbackSubmitDTO）
 */
export interface FeedbackSubmitParams {
  /** 反馈内容（非空，≤500字符） */
  content: string
}

/**
 * 用户反馈（对应后端 FeedbackVO，含管理员回复）
 */
export interface FeedbackItem {
  /** 反馈ID */
  feedbackId: number
  /** 反馈内容 */
  feedbackContent: string
  /** 状态：0待处理，1已回复，2关闭 */
  status: number
  /** 管理员回复（未回复为null） */
  replyContent: string | null
  /** 提交时间 */
  createTime: DateTimeValue
  /** 更新时间（管理员回复后更新，未回复为null） */
  updateTime: DateTimeValue
}

/**
 * 反馈状态展示项
 */
export interface FeedbackStatusInfo {
  /** 状态文案 */
  text: string
  /** el-tag 颜色类型 */
  type: 'success' | 'warning' | 'info'
}
