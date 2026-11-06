// 管理员后台模块类型定义
// 与后端管理端 DTO/VO 一一对应（AdminUserVO / ScaleSaveDTO / QuestionSaveDTO /
// CourseSaveDTO / SectionSaveDTO / CrisisVO / CrisisHandleDTO /
// AdminFeedbackVO / FeedbackHandleDTO / NoticeVO / NoticeSaveDTO 等）
// 量表/课程/小节基础实体复用用户端类型 ScaleItem / CourseItem / CourseSection

import type { DateTimeValue } from '@/types'

// ==================== 用户管理 ====================

/**
 * 管理端用户（对应后端 AdminUserVO，email/phone 为AES解密后的明文）
 */
export interface AdminUserItem {
  /** 用户ID */
  userId: number
  /** 邮箱（解密后的明文） */
  email: string | null
  /** 手机号（解密后的明文） */
  phone: string | null
  /** 昵称 */
  nickname: string
  /** 头像URL */
  avatarUrl: string | null
  /** 账号状态：0禁用，1正常 */
  accountStatus: number
  /** 账号状态中文描述 */
  statusText: string
  /** 实名认证标记：0未认证，1已认证 */
  realAuthFlag: number
  /** 用户等级 */
  userLevel: number
  /** 总积分 */
  totalPoints: number
  /** 注册时间 */
  registerTime: DateTimeValue
  /** 最后登录时间 */
  lastLoginTime: DateTimeValue
}

/**
 * 用户分页查询参数（GET /admin/users）
 */
export interface AdminUserQuery {
  /** 页码，从1开始 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
  /** 关键字：昵称模糊 或 邮箱精确 */
  keyword?: string
  /** 账号状态：0禁用，1正常，可选 */
  accountStatus?: number
}

/**
 * 启用/禁用账号请求（对应后端 UserStatusDTO）
 */
export interface UserStatusParams {
  /** 目标状态：0禁用，1正常 */
  accountStatus: number
}

// ==================== 量表管理 ====================

/**
 * 新增/编辑量表请求（对应后端 ScaleSaveDTO）
 * 参数格式校验在后端 Service 层执行
 */
export interface ScaleSaveParams {
  /** 量表名称（1-100字符） */
  scaleName: string
  /** 量表编码（1-50字符，全局唯一） */
  scaleCode: string
  /** 维度说明（如：坚韧、力量、乐观三个维度，共25题） */
  dimensionDesc?: string
  /** 总题目数 */
  totalQuestionNum?: number
  /** 公开信度系数（0-1之间，如0.890） */
  reliability?: number | null
  /** 启用状态：0停用，1启用（新增时缺省为1，编辑由状态接口维护） */
  status?: number
}

/**
 * 启用/停用量表请求（对应后端 ScaleStatusDTO）
 */
export interface ScaleStatusParams {
  /** 目标状态：0停用，1启用 */
  status: number
}

/**
 * 管理端量表题目（对应后端 AdminQuestionVO，返回 optionJson 与 scoreRule 原始配置）
 */
export interface AdminQuestionItem {
  /** 题目ID */
  questionId: number
  /** 所属量表ID */
  scaleId: number
  /** 题目内容 */
  questionContent: string
  /** 选项JSON：[{"value":1,"label":"完全不符合"}, ...] */
  optionJson: string
  /** 计分规则JSON：{"dimension":"toughness","scoreMap":{"1":1,...}} */
  scoreRule: string | null
  /** 排序号 */
  sortNum: number
}

/**
 * 新增/编辑题目请求（对应后端 QuestionSaveDTO）
 */
export interface QuestionSaveParams {
  /** 题目内容（1-2000字符） */
  questionContent: string
  /** 选项JSON字符串 */
  optionJson: string
  /** 计分规则JSON字符串（可省略） */
  scoreRule?: string
  /** 排序号（缺省为0） */
  sortNum?: number
}

// ==================== 课程管理 ====================

/**
 * 新增/编辑课程请求（对应后端 CourseSaveDTO）
 */
export interface CourseSaveParams {
  /** 课程名称（必填，最长100字） */
  courseName: string
  /** 课程简介（可选） */
  courseDesc?: string
  /** 封面图地址（可选，最长255字） */
  coverUrl?: string
  /** 小节总数（可选，>=0；维护小节后按实际小节数自动覆盖） */
  totalSection?: number
  /** 状态：0下架，1正常（新增缺省1，编辑不改状态） */
  status?: number
}

/**
 * 课程上架/下架请求（对应后端 CourseStatusDTO）
 */
export interface CourseStatusParams {
  /** 目标状态：0下架，1正常 */
  status: number
}

/**
 * 新增/编辑课程小节请求（对应后端 SectionSaveDTO）
 */
export interface SectionSaveParams {
  /** 小节标题（必填，最长200字） */
  sectionTitle: string
  /** 小节内容（可选，最长10000字） */
  sectionContent?: string
  /** 建议学习时长（分钟，1-600，缺省5） */
  studyMinutes?: number
  /** 排序号（缺省0，小的在前） */
  sortNum?: number
}

// ==================== 安全危机事件 ====================

/**
 * 管理端危机事件（对应后端 CrisisVO，用户昵称与消息内容为解密后明文）
 */
export interface CrisisItem {
  /** 危机记录ID */
  crisisId: number
  /** 用户ID */
  userId: number
  /** 用户昵称 */
  userNickname: string | null
  /** 关联消息ID */
  msgId: number | null
  /** 关联消息内容（解密后的明文） */
  msgContent: string | null
  /** 风险等级1-3 */
  riskLevel: number
  /** 风险等级中文描述 */
  riskText: string
  /** 风险描述 */
  riskDesc: string | null
  /** 处理状态：0待处理，1已处理 */
  handleStatus: number
  /** 处理状态中文描述 */
  handleStatusText: string
  /** 处理备注 */
  handleRemark: string | null
  /** 处理时间 */
  handleTime: DateTimeValue
  /** 创建时间 */
  createTime: DateTimeValue
}

/**
 * 危机事件分页查询参数（GET /admin/crisis）
 */
export interface CrisisQuery {
  /** 页码，从1开始 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
  /** 处理状态：0待处理，1已处理，可选 */
  handleStatus?: number
}

/**
 * 处理危机事件请求（对应后端 CrisisHandleDTO）
 */
export interface CrisisHandleParams {
  /** 目标处理状态：0待处理，1已处理 */
  handleStatus: number
  /** 处理备注（最长500字，未传保留原值） */
  handleRemark?: string
}

// ==================== 用户反馈处理 ====================

/**
 * 管理端用户反馈（对应后端 AdminFeedbackVO）
 */
export interface FeedbackItem {
  /** 反馈ID */
  feedbackId: number
  /** 反馈用户ID */
  userId: number
  /** 反馈用户昵称 */
  userNickname: string | null
  /** 反馈内容 */
  feedbackContent: string
  /** 状态：0待处理，1已回复，2关闭 */
  status: number
  /** 状态中文描述 */
  statusText: string
  /** 管理员回复内容 */
  replyContent: string | null
  /** 提交时间 */
  createTime: DateTimeValue
  /** 更新时间（管理员回复后更新） */
  updateTime: DateTimeValue
}

/**
 * 反馈分页查询参数（GET /admin/feedback）
 */
export interface FeedbackQuery {
  /** 页码，从1开始 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
  /** 状态：0待处理，1已回复，2关闭，可选 */
  status?: number
}

/**
 * 处理反馈请求（对应后端 FeedbackHandleDTO）
 * status=1 时 replyContent 必填；未传 replyContent 保留原回复
 */
export interface FeedbackHandleParams {
  /** 目标状态：0待处理，1已回复，2关闭 */
  status: number
  /** 回复内容（最长1000字） */
  replyContent?: string
}

// ==================== 系统公告管理 ====================

/**
 * 系统公告（对应后端 NoticeVO，adminName 由后端补充）
 */
export interface NoticeItem {
  /** 公告ID */
  noticeId: number
  /** 公告标题 */
  noticeTitle: string
  /** 公告正文 */
  noticeContent: string
  /** 发布时间 */
  publishTime: DateTimeValue
  /** 发布管理员ID */
  adminId: number | null
  /** 发布管理员账号 */
  adminName: string | null
  /** 状态：0草稿，1已发布，2下架 */
  status: number
  /** 状态中文描述 */
  statusText: string
}

/**
 * 新增公告请求（对应后端 NoticeSaveDTO）
 */
export interface NoticeSaveParams {
  /** 公告标题（必填，最长100字） */
  noticeTitle: string
  /** 公告正文（必填，最长5000字） */
  noticeContent: string
  /** 状态：0草稿，1已发布，2下架（缺省0；置1时后端记录发布时间与发布人） */
  status?: number
}

/**
 * 修改公告状态请求（对应后端 NoticeStatusDTO）
 * 发布/停用走此接口
 */
export interface NoticeStatusParams {
  /** 目标状态：0草稿，1已发布，2下架 */
  status: number
}
