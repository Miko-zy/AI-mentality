// AI陪伴对话模块类型定义
// 与后端 DTO/VO 一一对应（CreateSessionDTO / SendChatMessageDTO /
// AiSessionVO / ChatMessageVO / SendMessageResultVO）

import type { DateTimeValue } from '@/types'

/**
 * AI对话风格取值（与后端 CreateSessionDTO 校验白名单一致）
 * empathetic共情倾听 / professional专业咨询 / companion陪伴聊天 / coach成长教练
 */
export type AiStyle = 'empathetic' | 'professional' | 'companion' | 'coach'

/**
 * 创建AI会话请求（对应后端 CreateSessionDTO）
 */
export interface CreateSessionParams {
  /** 会话名称（2-50字符） */
  sessionName: string
  /** AI对话风格 */
  aiStyle: AiStyle
}

/**
 * 更新会话请求（对应后端 UpdateSessionDTO）
 * 会话名称与AI风格至少传一个
 */
export interface UpdateSessionParams {
  /** 新会话名称（2-50字符，可选） */
  sessionName?: string
  /** 新的AI对话风格（可选） */
  aiStyle?: AiStyle
}

/**
 * 发送对话消息请求（对应后端 SendChatMessageDTO）
 */
export interface SendChatMessageParams {
  /** 会话ID */
  sessionId: number
  /** 消息内容（非空，≤2000字符） */
  content: string
}

/**
 * AI会话（对应后端 AiSessionVO）
 */
export interface AiSessionItem {
  /** 会话ID */
  sessionId: number
  /** 会话名称 */
  sessionName: string
  /** AI对话风格 */
  aiStyle: AiStyle
  /** 创建时间 */
  createTime: DateTimeValue
  /** 最后更新时间 */
  updateTime: DateTimeValue
  /** 最后一条消息预览（无消息为null） */
  lastMessage: string | null
  /** 最后一条消息时间（无消息为null） */
  lastMessageTime: DateTimeValue
  /** 未读消息数（预留字段，可能为null） */
  unreadCount: number | null
}

/**
 * 对话消息（对应后端 ChatMessageVO）
 */
export interface ChatMessageItem {
  /** 消息ID */
  msgId: number
  /** 会话ID */
  sessionId: number
  /** 发送方类型：0用户，1AI */
  senderType: number
  /** 消息内容（已解密） */
  content: string
  /** 发送时间 */
  sendTime: DateTimeValue
  /** 内容审核状态：0待审，1通过，2不通过 */
  auditStatus: number
  /** 违规标记：0正常，1违规 */
  violateFlag: number
}

/**
 * 风险检测结果（对应后端 SendMessageResultVO.RiskDetectResultVO）
 * 返回 null 表示无风险，非 null 时按 riskLevel 给出提示
 */
export interface RiskDetectResult {
  /** 是否检测到风险 */
  hasRisk: boolean
  /** 风险等级：1低风险，2中风险，3高风险 */
  riskLevel: number
  /** 风险描述 */
  riskDesc: string
}

/**
 * 发送消息结果（对应后端 SendMessageResultVO）
 */
export interface SendMessageResult {
  /** 保存后的用户消息 */
  userMessage: ChatMessageItem
  /** AI回复消息 */
  aiMessage: ChatMessageItem
  /** 风险检测结果（null=无风险） */
  riskDetectResult: RiskDetectResult | null
}

/**
 * 流式对话完成事件（对应后端 SSE 的 done 事件）
 */
export interface StreamDonePayload {
  /** 保存后的AI回复消息 */
  aiMessage: ChatMessageItem
  /** 风险检测结果 */
  riskDetectResult: RiskDetectResult | null
}
