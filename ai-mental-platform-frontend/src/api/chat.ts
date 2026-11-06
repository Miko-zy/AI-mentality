// AI陪伴对话接口模块
// 普通请求复用 utils/request.ts（自动携带 Authorization: Bearer {token}）
// 流式请求使用原生 fetch + ReadableStream（EventSource 无法自定义请求头，因此不用它）
import { del, get, post, put } from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { PageResult, Result } from '@/types'
import {
  AiSessionItem,
  ChatMessageItem,
  CreateSessionParams,
  SendMessageResult,
  SendChatMessageParams,
  StreamDonePayload,
  UpdateSessionParams
} from '@/types/chat'

/**
 * 创建AI会话
 * 后端：POST /api/ai/session
 *
 * @param data 会话名称（2-50字符）与AI对话风格
 * @returns 创建的会话信息
 */
export function createSessionApi(data: CreateSessionParams): Promise<Result<AiSessionItem>> {
  return post<AiSessionItem>('/ai/session', data)
}

/**
 * 更新会话（改名 / 切换AI风格）
 * 后端：PUT /api/ai/session/{sessionId}
 *
 * @param sessionId 会话ID
 * @param data      会话名称与AI风格（至少传一个）
 * @returns 更新后的会话信息
 */
export function updateSessionApi(
  sessionId: number,
  data: UpdateSessionParams
): Promise<Result<AiSessionItem>> {
  return put<AiSessionItem>(`/ai/session/${sessionId}`, data)
}

/**
 * 获取当前用户全部会话列表（按最后更新时间倒序，含最后一条消息预览）
 * 后端：GET /api/ai/session
 *
 * @returns 会话列表
 */
export function getSessionListApi(): Promise<Result<AiSessionItem[]>> {
  return get<AiSessionItem[]>('/ai/session')
}

/**
 * 分页获取会话历史消息（按发送时间正序）
 * 后端：GET /api/ai/session/{sessionId}/messages
 *
 * @param sessionId 会话ID
 * @param page      页码（从1开始，默认1）
 * @param size      每页条数（默认20，最大100）
 * @returns 消息分页结果
 */
export function getMessageHistoryApi(
  sessionId: number,
  page: number,
  size: number
): Promise<Result<PageResult<ChatMessageItem>>> {
  return get<PageResult<ChatMessageItem>>(`/ai/session/${sessionId}/messages`, { page, size })
}

/**
 * 发送对话消息（非流式，流式失败时作为回退）
 * 后端：POST /api/ai/session/message
 *
 * @param data 会话ID与消息内容（≤2000字符）
 * @returns 发送结果（含用户消息、AI回复、风险检测结果）
 */
export function sendChatMessageApi(data: SendChatMessageParams): Promise<Result<SendMessageResult>> {
  return post<SendMessageResult>('/ai/session/message', data)
}

/**
 * 删除（结束）会话，逻辑删除，历史消息保留
 * 后端：DELETE /api/ai/session/{sessionId}
 *
 * @param sessionId 会话ID
 * @returns 操作结果
 */
export function endSessionApi(sessionId: number): Promise<Result<void>> {
  return del<void>(`/ai/session/${sessionId}`)
}

/**
 * 流式对话回调
 */
export interface StreamChatHandlers {
  /** 收到用户消息入库结果（可用真实 msgId 替换前端乐观消息） */
  onMeta?: (payload: { userMessage: ChatMessageItem }) => void
  /** 收到增量文本 */
  onDelta: (delta: string) => void
  /** 流式完成（含最终AI消息与风险结果） */
  onDone: (payload: StreamDonePayload) => void
}

/**
 * 解析单个 SSE 事件块（event / data 行组合）
 *
 * @param block    SSE 文本块
 * @param handlers 事件回调
 */
const parseSseBlock = (block: string, handlers: StreamChatHandlers): void => {
  let eventName = 'message'
  const dataLines: string[] = []

  for (const rawLine of block.split('\n')) {
    const line = rawLine.replace(/\r$/, '')
    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trimStart())
    }
  }

  if (dataLines.length === 0) {
    return
  }
  const raw = dataLines.join('\n')
  if (raw === '[DONE]') {
    return
  }

  let payload: any
  try {
    payload = JSON.parse(raw)
  } catch {
    // 非 JSON 的 data（如心跳）直接忽略
    return
  }

  if (eventName === 'meta') {
    handlers.onMeta?.(payload)
    return
  }
  if (eventName === 'done') {
    handlers.onDone(payload)
    return
  }
  if (eventName === 'error') {
    throw new Error(payload?.message || 'AI回复失败')
  }
  // 默认事件（message）或 delta 事件
  if (typeof payload?.delta === 'string') {
    handlers.onDelta(payload.delta)
  }
}

/**
 * 流式发送对话消息（SSE）
 * 后端：POST /api/ai/session/message/stream
 *
 * @param data     会话ID与消息内容
 * @param handlers 流式事件回调
 * @param signal   可选中止信号
 */
export async function streamChatMessage(
  data: SendChatMessageParams,
  handlers: StreamChatHandlers,
  signal?: AbortSignal
): Promise<void> {
  const userStore = useUserStore()
  const response = await fetch('/api/ai/session/message/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json;charset=utf-8',
      Authorization: `Bearer ${userStore.token}`
    },
    body: JSON.stringify(data),
    signal
  })

  const contentType = response.headers.get('content-type') || ''
  if (!response.ok || !response.body || !contentType.includes('text/event-stream')) {
    let message = `流式请求失败（HTTP ${response.status}）`
    try {
      const result = await response.json()
      if (result?.message) {
        message = result.message
      }
    } catch {
      // 非 JSON 错误体，保留默认提示
    }
    throw new Error(message)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  let finished = false

  // 包装 onDone，用于判断流是否正常结束
  const wrappedHandlers: StreamChatHandlers = {
    onMeta: handlers.onMeta,
    onDelta: handlers.onDelta,
    onDone: (payload) => {
      finished = true
      handlers.onDone(payload)
    }
  }

  while (true) {
    const { done, value } = await reader.read()
    if (done) {
      break
    }
    buffer += decoder.decode(value, { stream: true })
    buffer = buffer.replace(/\r\n/g, '\n')

    let boundary = buffer.indexOf('\n\n')
    while (boundary >= 0) {
      const block = buffer.slice(0, boundary)
      buffer = buffer.slice(boundary + 2)
      if (block.trim()) {
        parseSseBlock(block, wrappedHandlers)
      }
      boundary = buffer.indexOf('\n\n')
    }
  }

  // 处理最后一个事件块（服务端末尾可能没有空行）
  if (buffer.trim()) {
    parseSseBlock(buffer, wrappedHandlers)
  }
  if (!finished) {
    throw new Error('流式连接已中断，请重试')
  }
}
