<template>
  <div class="chat-container">
    <!-- ==================== 会话侧边栏 ==================== -->
    <aside class="chat-sidebar">
      <div class="sidebar-header">
        <h3 class="sidebar-title">会话列表</h3>
        <el-button type="primary" size="small" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          <span>新建会话</span>
        </el-button>
      </div>
      <div v-loading="sessionsLoading" class="session-list">
        <div
          v-for="session in sessions"
          :key="session.sessionId"
          class="session-item"
          :class="{ 'session-item--active': session.sessionId === activeSessionId }"
          @click="handleSelectSession(session.sessionId)"
        >
          <div class="session-info">
            <div class="session-name">{{ session.sessionName }}</div>
            <div class="session-preview">{{ session.lastMessage || '暂无消息' }}</div>
            <div class="session-time">
              {{ formatDateTime(session.lastMessageTime || session.createTime) }}
            </div>
          </div>
          <el-button
            class="session-edit"
            text
            title="编辑会话"
            @click.stop="openRenameDialog(session)"
          >
            <el-icon><EditPen /></el-icon>
          </el-button>
          <el-button
            class="session-del"
            text
            title="删除会话"
            @click.stop="handleDeleteSession(session)"
          >
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
        <el-empty
          v-if="!sessionsLoading && sessions.length === 0"
          description="还没有会话"
          :image-size="72"
        />
      </div>
    </aside>

    <!-- ==================== 主聊天窗口 ==================== -->
    <main class="chat-main">
      <div class="chat-header">
        <div class="chat-header-info">
          <h3 class="chat-title">{{ activeSession ? activeSession.sessionName : 'AI陪伴' }}</h3>
          <el-tag v-if="activeSession" size="small" type="info">
            {{ aiStyleLabel(activeSession.aiStyle) }}
          </el-tag>
        </div>
        <p class="chat-header-sub">
          在这里倾诉与交流，我会一直陪伴你 · AI 内容仅供情绪陪伴，不构成医疗建议
        </p>
      </div>

      <!-- 心理健康风险提示（发送消息后由后端风险检测返回） -->
      <el-alert
        v-if="riskAlert"
        class="risk-alert"
        :type="riskAlert.riskLevel === 3 ? 'error' : 'warning'"
        :title="riskText(riskAlert)"
        show-icon
        :closable="true"
        @close="riskAlert = null"
      />

      <!-- 消息区域 -->
      <div ref="messageAreaRef" class="message-area" v-loading="messagesLoading">
        <!-- 加载更早消息（历史按时间正序分页，向前翻页） -->
        <div v-if="currentPage > 1" class="load-earlier">
          <el-button text size="small" :loading="loadingEarlier" @click="loadEarlierMessages">
            加载更早消息
          </el-button>
        </div>

        <!-- 未选择会话 -->
        <el-empty
          v-if="!activeSession && !messagesLoading"
          description="选择左侧会话，或新建一个会话开始交流"
          :image-size="100"
        >
          <el-button type="primary" @click="openCreateDialog">
            <el-icon><Plus /></el-icon>
            <span>新建会话</span>
          </el-button>
        </el-empty>

        <!-- 会话内暂无消息 -->
        <el-empty
          v-if="activeSession && !messagesLoading && messages.length === 0"
          description="暂无消息，发送第一条消息开始对话"
          :image-size="100"
        />

        <!-- 消息气泡：0用户（右侧），1AI（左侧） -->
        <div
          v-for="msg in messages"
          :key="msg.msgId"
          class="msg-row"
          :class="msg.senderType === 0 ? 'msg-row--user' : 'msg-row--ai'"
        >
          <div
            class="msg-avatar"
            :class="msg.senderType === 0 ? 'msg-avatar--user' : 'msg-avatar--ai'"
          >
            {{ msg.senderType === 0 ? '我' : 'AI' }}
          </div>
          <div class="msg-body">
            <div
              class="msg-bubble"
              :class="msg.senderType === 0 ? 'msg-bubble--user' : 'msg-bubble--ai'"
            >
              <p class="msg-content">{{ msg.content }}</p>
            </div>
            <div class="msg-time">{{ formatDateTime(msg.sendTime) }}</div>
          </div>
        </div>

        <!-- AI正在流式回复（逐字展示，收到 done 后转为正式消息） -->
        <div
          v-if="sending && sendingSessionId === activeSessionId"
          class="msg-row msg-row--ai"
        >
          <div class="msg-avatar msg-avatar--ai">AI</div>
          <div class="msg-body">
            <div v-if="streamingText" class="msg-bubble msg-bubble--ai">
              <p class="msg-content">{{ streamingText }}<span class="stream-cursor">▍</span></p>
            </div>
            <div v-else class="msg-bubble msg-bubble--ai typing">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
            <div class="msg-time">{{ streamingText ? '正在回复...' : '正在思考...' }}</div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="chat-input-wrap" @keydown="handleKeydown">
        <div class="chat-input">
          <el-input
            v-model="chatInput"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 6 }"
            :maxlength="2000"
            show-word-limit
            :disabled="!activeSession"
            :placeholder="inputPlaceholder"
          />
          <el-button
            class="send-btn"
            type="primary"
            :loading="sending"
            :disabled="!activeSession || chatInput.trim() === ''"
            @click="handleSend"
          >
            <el-icon v-if="!sending"><Promotion /></el-icon>
            <span>发送</span>
          </el-button>
        </div>
        <p class="chat-tip">Enter 发送，Shift + Enter 换行；单条消息最多 2000 字</p>
      </div>
    </main>

    <!-- ==================== 新建会话对话框 ==================== -->
    <el-dialog v-model="createDialogVisible" title="新建会话" width="440px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="90px">
        <el-form-item label="会话名称" prop="sessionName">
          <el-input
            v-model.trim="createForm.sessionName"
            maxlength="50"
            show-word-limit
            placeholder="2-50个字符"
          />
        </el-form-item>
        <el-form-item label="对话风格" prop="aiStyle">
          <el-radio-group v-model="createForm.aiStyle">
            <el-radio label="empathetic">共情倾听</el-radio>
            <el-radio label="professional">专业咨询</el-radio>
            <el-radio label="companion">陪伴聊天</el-radio>
            <el-radio label="coach">成长教练</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreateSession">
          创建
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 编辑会话对话框 ==================== -->
    <el-dialog v-model="renameDialogVisible" title="编辑会话" width="440px" destroy-on-close>
      <el-form ref="renameFormRef" :model="renameForm" :rules="createRules" label-width="90px">
        <el-form-item label="会话名称" prop="sessionName">
          <el-input
            v-model.trim="renameForm.sessionName"
            maxlength="50"
            show-word-limit
            placeholder="2-50个字符"
          />
        </el-form-item>
        <el-form-item label="对话风格" prop="aiStyle">
          <el-radio-group v-model="renameForm.aiStyle">
            <el-radio label="empathetic">共情倾听</el-radio>
            <el-radio label="professional">专业咨询</el-radio>
            <el-radio label="companion">陪伴聊天</el-radio>
            <el-radio label="coach">成长教练</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="renaming" @click="handleRenameSession">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import {
  AiSessionItem,
  AiStyle,
  ChatMessageItem,
  CreateSessionParams,
  RiskDetectResult
} from '@/types/chat'
import {
  createSessionApi,
  endSessionApi,
  getMessageHistoryApi,
  getSessionListApi,
  sendChatMessageApi,
  streamChatMessage,
  updateSessionApi
} from '@/api/chat'

// 历史消息每页条数（后端默认20，最大100）
const MESSAGE_PAGE_SIZE = 20

// ==================== 会话列表 ====================

// 会话列表（后端按最后更新时间倒序返回）
const sessions = ref<AiSessionItem[]>([])

// 会话列表加载态
const sessionsLoading = ref(false)

// 当前激活会话ID（null=未选择）
const activeSessionId = ref<number | null>(null)

// 当前会话（由会话列表派生，用于侧栏高亮与标题展示）
const activeSession = computed<AiSessionItem | null>(
  () => sessions.value.find((item) => item.sessionId === activeSessionId.value) || null
)

// ==================== 消息 ====================

// 当前会话消息列表
const messages = ref<ChatMessageItem[]>([])

// 消息加载态
const messagesLoading = ref(false)

// 当前所处页码（历史消息按发送时间正序分页，首次进入定位到最后一页）
const currentPage = ref(1)

// 加载更早消息加载态
const loadingEarlier = ref(false)

// 消息区域DOM引用（用于滚动到底部）
const messageAreaRef = ref<HTMLDivElement>()

// 输入框内容
const chatInput = ref('')

// 发送中（等待AI回复）
const sending = ref(false)

// 正在发送消息的会话ID（切换会话后不展示他处的"正在输入"）
const sendingSessionId = ref<number | null>(null)

// 心理健康风险提示（后端风险检测结果，null=无）
const riskAlert = ref<RiskDetectResult | null>(null)

// 流式回复中的增量文本（空字符串时展示"正在思考"动画）
const streamingText = ref('')

// ==================== 新建会话 ====================

// 新建会话对话框可见性
const createDialogVisible = ref(false)

// 创建请求加载态
const creating = ref(false)

// 新建会话表单引用
const createFormRef = ref<FormInstance>()

// 新建会话表单（默认值降低输入成本）
const createForm = reactive<CreateSessionParams>({
  sessionName: '新对话',
  aiStyle: 'empathetic'
})

// 新建会话表单校验规则（与后端 CreateSessionDTO.validate 规则一致）
const createRules: FormRules = {
  sessionName: [
    { required: true, whitespace: true, message: '请输入会话名称', trigger: 'blur' },
    { min: 2, max: 50, message: '会话名称长度需在2-50字符之间', trigger: 'blur' }
  ],
  aiStyle: [{ required: true, message: '请选择AI对话风格', trigger: 'change' }]
}

// ==================== 编辑会话 ====================

// 编辑会话对话框可见性
const renameDialogVisible = ref(false)

// 保存中
const renaming = ref(false)

// 编辑会话表单引用
const renameFormRef = ref<FormInstance>()

// 当前编辑的会话
const renameTarget = ref<AiSessionItem | null>(null)

// 编辑会话表单（名称与风格均提交）
const renameForm = reactive<{ sessionName: string; aiStyle: AiStyle }>({
  sessionName: '',
  aiStyle: 'empathetic'
})

// ==================== 展示映射与工具 ====================

// AI对话风格名称映射（与后端白名单一致）
const AI_STYLE_MAP: Record<AiStyle, string> = {
  empathetic: '共情倾听',
  professional: '专业咨询',
  companion: '陪伴聊天',
  coach: '成长教练'
}

// 风险等级名称映射：1低风险，2中风险，3高风险
const RISK_LEVEL_MAP: Record<number, string> = {
  1: '低风险',
  2: '中风险',
  3: '高风险'
}

// 输入框占位提示（未选择会话时提示先创建）
const inputPlaceholder = computed<string>(() =>
  activeSession.value ? '输入消息，Enter 发送，Shift + Enter 换行' : '请先创建或选择一个会话'
)

/**
 * AI对话风格展示名称
 *
 * @param style 风格取值
 * @returns 中文名称
 */
const aiStyleLabel = (style: AiStyle): string => {
  return AI_STYLE_MAP[style] || style
}

/**
 * 组装风险提示文案
 *
 * @param risk 风险检测结果
 * @returns 提示文案
 */
const riskText = (risk: RiskDetectResult): string => {
  const level = RISK_LEVEL_MAP[risk.riskLevel] || '风险'
  const desc = risk.riskDesc || '检测到可能需要关注的表达，请照顾好自己，必要时寻求专业帮助'
  return `心理健康提示（${level}）：${desc}`
}

/**
 * 数字补零为两位
 *
 * @param n 数字
 * @returns 两位字符串
 */
const pad2 = (n: number): string => String(n).padStart(2, '0')

/**
 * 格式化日期时间为 "yyyy-MM-dd HH:mm:ss"
 * 后端时间字段可能序列化为 "yyyy-MM-dd HH:mm:ss"、ISO "yyyy-MM-ddTHH:mm:ss"
 * 或时间戳数组 [年,月,日,时,分,秒]，此处统一兼容处理
 *
 * @param value 时间字段值
 * @returns 格式化后的时间字符串，空值返回 "-"
 */
const formatDateTime = (value: unknown): string => {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  if (Array.isArray(value)) {
    const parts = value as number[]
    if (parts.length >= 3) {
      const datePart = `${parts[0]}-${pad2(parts[1])}-${pad2(parts[2])}`
      if (parts.length >= 6) {
        return `${datePart} ${pad2(parts[3])}:${pad2(parts[4])}:${pad2(parts[5])}`
      }
      return datePart
    }
    return '-'
  }
  const text = String(value)
  if (text.includes('T')) {
    return text.replace('T', ' ').split('.')[0]
  }
  return text.length > 19 ? text.substring(0, 19) : text
}

/**
 * 生成本地当前时间字符串（用于乐观消息的展示时间，避免时区偏移）
 *
 * @returns "yyyy-MM-dd HH:mm:ss"
 */
const localNow = (): string => {
  const now = new Date()
  return `${now.getFullYear()}-${pad2(now.getMonth() + 1)}-${pad2(now.getDate())} ${pad2(
    now.getHours()
  )}:${pad2(now.getMinutes())}:${pad2(now.getSeconds())}`
}

/**
 * 提取错误信息：优先取后端业务错误 message，其次取 HTTP 错误响应体 message，最后兜底
 *
 * @param error 捕获到的错误对象
 * @returns 展示给用户的错误文案
 */
const getErrorMessage = (error: unknown): string => {
  const err = error as { response?: { data?: { message?: string } }; message?: string }
  return err?.response?.data?.message || err?.message || '请求失败，请稍后重试'
}

// ==================== 滚动 ====================

// 滚动消息区域到底部（DOM更新后执行）
const scrollToBottom = () => {
  nextTick(() => {
    const el = messageAreaRef.value
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
}

// ==================== 会话加载与切换 ====================

// 加载会话列表
const loadSessions = async () => {
  sessionsLoading.value = true
  try {
    const res = await getSessionListApi()
    sessions.value = res.data ?? []
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    sessionsLoading.value = false
  }
}

// 加载当前会话消息：先取第一页拿总数，历史较多时直接定位最后一页（展示最新消息）
const loadMessages = async () => {
  const sessionId = activeSessionId.value
  if (!sessionId) {
    return
  }
  messagesLoading.value = true
  messages.value = []
  currentPage.value = 1
  try {
    const firstPage = await getMessageHistoryApi(sessionId, 1, MESSAGE_PAGE_SIZE)
    // 会话已切换，丢弃过期请求结果
    if (activeSessionId.value !== sessionId) {
      return
    }
    const total = firstPage.data?.total ?? 0
    const totalPages = Math.max(1, Math.ceil(total / MESSAGE_PAGE_SIZE))
    if (totalPages <= 1) {
      messages.value = firstPage.data?.list ?? []
      currentPage.value = 1
    } else {
      const lastPage = await getMessageHistoryApi(sessionId, totalPages, MESSAGE_PAGE_SIZE)
      if (activeSessionId.value === sessionId) {
        messages.value = lastPage.data?.list ?? []
        currentPage.value = totalPages
      }
    }
    scrollToBottom()
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    // 仅在仍是当前会话时关闭加载态，避免过期请求干扰新会话
    if (activeSessionId.value === sessionId) {
      messagesLoading.value = false
    }
  }
}

// 切换会话：重置风险提示并加载该会话消息
const handleSelectSession = (sessionId: number) => {
  if (activeSessionId.value === sessionId) {
    return
  }
  activeSessionId.value = sessionId
  riskAlert.value = null
  loadMessages()
}

// 加载更早消息（向前翻一页并prepend）
const loadEarlierMessages = async () => {
  const sessionId = activeSessionId.value
  if (!sessionId || currentPage.value <= 1 || loadingEarlier.value) {
    return
  }
  loadingEarlier.value = true
  try {
    const res = await getMessageHistoryApi(sessionId, currentPage.value - 1, MESSAGE_PAGE_SIZE)
    if (activeSessionId.value === sessionId) {
      const earlier = res.data?.list ?? []
      messages.value = [...earlier, ...messages.value]
      currentPage.value -= 1
    }
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    loadingEarlier.value = false
  }
}

// ==================== 删除会话 ====================

// 删除（结束）当前会话：确认后调用接口，若删的是激活会话则清空并自动选中剩余第一个
const handleDeleteSession = async (session: AiSessionItem) => {
  try {
    await ElMessageBox.confirm(
      `删除后该会话将不再显示（历史消息保留），确定删除“${session.sessionName}”吗？`,
      '删除会话',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      }
    )
  } catch {
    return // 用户取消删除
  }
  try {
    await endSessionApi(session.sessionId)
    ElMessage.success('会话已删除')
    await loadSessions()
    if (activeSessionId.value === session.sessionId) {
      activeSessionId.value = null
      messages.value = []
      currentPage.value = 1
      riskAlert.value = null
      if (sessions.value.length > 0) {
        handleSelectSession(sessions.value[0].sessionId)
      }
    }
  } catch (error) {
    // 删除失败错误弹窗提示（如会话不存在或无权访问）
    ElMessageBox.alert(getErrorMessage(error), '删除失败', {
      type: 'error',
      confirmButtonText: '知道了'
    })
  }
}

// ==================== 新建会话 ====================

// 打开新建会话对话框：重置为默认值
const openCreateDialog = () => {
  createForm.sessionName = '新对话'
  createForm.aiStyle = 'empathetic'
  createDialogVisible.value = true
}

// 新建会话：校验通过后创建，成功后刷新列表并选中新会话
const handleCreateSession = () => {
  createFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) {
      return
    }
    creating.value = true
    try {
      const res = await createSessionApi({
        sessionName: createForm.sessionName.trim(),
        aiStyle: createForm.aiStyle
      })
      createDialogVisible.value = false
      ElMessage.success('会话创建成功')
      await loadSessions()
      if (res.data) {
        // 直接选中新会话（新会话无消息，本地清空即可，无需请求）
        activeSessionId.value = res.data.sessionId
        riskAlert.value = null
        messages.value = []
        currentPage.value = 1
      }
    } catch (error) {
      // 创建失败错误弹窗提示（如名称重复、风格不支持）
      ElMessageBox.alert(getErrorMessage(error), '创建失败', {
        type: 'error',
        confirmButtonText: '知道了'
      })
    } finally {
      creating.value = false
    }
  })
}

// ==================== 编辑会话 ====================

// 打开编辑会话对话框：回填当前名称与风格
const openRenameDialog = (session: AiSessionItem) => {
  renameTarget.value = session
  renameForm.sessionName = session.sessionName
  renameForm.aiStyle = session.aiStyle
  renameDialogVisible.value = true
}

// 保存会话名称与风格：成功后刷新侧栏，标题与标签自动更新
const handleRenameSession = () => {
  renameFormRef.value?.validate(async (valid: boolean) => {
    if (!valid || !renameTarget.value) {
      return
    }
    renaming.value = true
    try {
      await updateSessionApi(renameTarget.value.sessionId, {
        sessionName: renameForm.sessionName.trim(),
        aiStyle: renameForm.aiStyle
      })
      renameDialogVisible.value = false
      ElMessage.success('会话已更新')
      await loadSessions()
    } catch (error) {
      ElMessageBox.alert(getErrorMessage(error), '更新失败', {
        type: 'error',
        confirmButtonText: '知道了'
      })
    } finally {
      renaming.value = false
    }
  })
}

// ==================== 发送消息 ====================

// 发送消息：优先流式（SSE），流式不可用时回退非流式；失败回滚并回填输入框
const handleSend = async () => {
  const content = chatInput.value.trim()
  if (!content || sending.value) {
    return
  }
  if (!activeSessionId.value) {
    ElMessage.warning('请先创建或选择一个会话')
    return
  }
  if (content.length > 2000) {
    ElMessage.warning('消息内容不能超过2000字符')
    return
  }

  const sessionId = activeSessionId.value
  const optimisticId = -Date.now()

  sending.value = true
  sendingSessionId.value = sessionId
  riskAlert.value = null
  chatInput.value = ''
  streamingText.value = ''

  // 追加本地乐观消息（真实msgId由流式meta事件或非流式回退结果替换）
  messages.value.push({
    msgId: optimisticId,
    sessionId,
    senderType: 0,
    content,
    sendTime: localNow(),
    auditStatus: 0,
    violateFlag: 0
  })
  scrollToBottom()

  // 是否已收到服务端 meta 事件（收到后说明用户消息已入库，不能再走非流式回退，避免重复写入）
  let metaReceived = false
  let streamError: unknown = null

  try {
    await streamChatMessage(
      { sessionId, content },
      {
        onMeta: (payload) => {
          metaReceived = true
          const index = messages.value.findIndex((item) => item.msgId === optimisticId)
          if (index >= 0 && payload?.userMessage) {
            messages.value[index] = payload.userMessage
          }
        },
        onDelta: (delta) => {
          streamingText.value += delta
          if (activeSessionId.value === sessionId) {
            scrollToBottom()
          }
        },
        onDone: (payload) => {
          streamingText.value = ''
          if (activeSessionId.value === sessionId) {
            if (payload?.aiMessage) {
              messages.value.push(payload.aiMessage)
            }
            if (payload?.riskDetectResult?.hasRisk) {
              riskAlert.value = payload.riskDetectResult
            }
            scrollToBottom()
          }
        }
      }
    )
  } catch (error) {
    streamError = error
    streamingText.value = ''

    // 仅在尚未收到任何服务端事件时回退非流式，避免用户消息重复入库
    if (!metaReceived) {
      try {
        const res = await sendChatMessageApi({ sessionId, content })
        if (activeSessionId.value === sessionId) {
          const index = messages.value.findIndex((item) => item.msgId === optimisticId)
          if (index >= 0 && res.data?.userMessage) {
            messages.value[index] = res.data.userMessage
          }
          if (res.data?.aiMessage) {
            messages.value.push(res.data.aiMessage)
          }
          if (res.data?.riskDetectResult?.hasRisk) {
            riskAlert.value = res.data.riskDetectResult
          }
          scrollToBottom()
        }
        streamError = null
      } catch (fallbackError) {
        streamError = fallbackError
      }
    }
  } finally {
    if (streamError) {
      // 未入库则回滚乐观消息并回填输入框；已入库则保留用户消息，仅提示错误
      if (!metaReceived && activeSessionId.value === sessionId) {
        const index = messages.value.findIndex((item) => item.msgId === optimisticId)
        if (index >= 0) {
          messages.value.splice(index, 1)
        }
        chatInput.value = content
      }
      ElMessage.error(getErrorMessage(streamError))
    }
    sending.value = false
    sendingSessionId.value = null
    streamingText.value = ''
    // 刷新侧栏排序与最后消息预览
    loadSessions()
  }
}

// 输入区键盘事件：Enter 发送，Shift + Enter 换行
const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    handleSend()
  }
}

// ==================== 初始化 ====================

// 首次进入：加载会话列表并自动选中最近一个会话
onMounted(async () => {
  await loadSessions()
  if (sessions.value.length > 0) {
    handleSelectSession(sessions.value[0].sessionId)
  }
})
</script>

<style lang="scss" scoped>
.chat-container {
  display: flex;
  width: 100%;
  /* 嵌入前台统一布局后：占满布局内容区高度（原为100vh全屏） */
  height: 100%;
  overflow: hidden;
  background: #f5f7fa;
}

// ==================== 会话侧边栏 ====================

.chat-sidebar {
  width: 270px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-right: 1px solid #ebeef5;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;

  .sidebar-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  min-height: 120px;
}

.session-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;

  &:hover {
    background: #f5f7fa;

    .session-del,
    .session-edit {
      opacity: 1;
    }
  }

  &.session-item--active {
    background: #ecf5ff;

    .session-name {
      color: #409eff;
      font-weight: 600;
    }
  }

  .session-info {
    flex: 1;
    min-width: 0;

    .session-name {
      font-size: 14px;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-bottom: 4px;
    }

    .session-preview {
      font-size: 12px;
      color: #909399;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-bottom: 4px;
    }

    .session-time {
      font-size: 11px;
      color: #c0c4cc;
    }
  }

  .session-del {
    flex-shrink: 0;
    opacity: 0;
    color: #f56c6c;
    transition: opacity 0.15s;
  }

  .session-edit {
    flex-shrink: 0;
    opacity: 0;
    color: #909399;
    transition: opacity 0.15s;
  }
}

// ==================== 主聊天窗口 ====================

.chat-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 14px 24px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;

  .chat-header-info {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .chat-title {
    font-size: 17px;
    font-weight: 600;
    color: #303133;
  }

  .chat-header-sub {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}

.risk-alert {
  margin: 12px 24px 0;
  border-radius: 8px;
}

// ==================== 消息区域 ====================

.message-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  min-height: 0;
}

.load-earlier {
  text-align: center;
  margin-bottom: 12px;
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 16px;
}

.msg-row--user {
  flex-direction: row-reverse;

  .msg-body {
    align-items: flex-end;
  }

  .msg-time {
    text-align: right;
  }
}

.msg-row--ai {
  .msg-body {
    align-items: flex-start;
  }
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  user-select: none;
}

.msg-avatar--user {
  background: #409eff;
}

.msg-avatar--ai {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.msg-body {
  display: flex;
  flex-direction: column;
  max-width: 68%;
}

.msg-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  word-break: break-all;

  .msg-content {
    font-size: 14px;
    line-height: 1.6;
    white-space: pre-wrap;
    margin: 0;
  }
}

.msg-bubble--user {
  background: #409eff;
  border-top-right-radius: 4px;

  .msg-content {
    color: #fff;
  }
}

.msg-bubble--ai {
  background: #fff;
  border: 1px solid #ebeef5;
  border-top-left-radius: 4px;

  .msg-content {
    color: #303133;
  }
}

.msg-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
}

// AI正在输入动画
.typing {
  display: flex;
  align-items: center;
  gap: 5px;
  height: 20px;

  .dot {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: #c0c4cc;
    animation: typing-bounce 1.2s infinite ease-in-out;

    &:nth-child(2) {
      animation-delay: 0.2s;
    }

    &:nth-child(3) {
      animation-delay: 0.4s;
    }
  }
}

// 流式回复光标
.stream-cursor {
  display: inline-block;
  margin-left: 2px;
  color: #409eff;
  animation: cursor-blink 1s step-end infinite;
}

@keyframes cursor-blink {
  0%,
  50% {
    opacity: 1;
  }

  51%,
  100% {
    opacity: 0;
  }
}

@keyframes typing-bounce {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.5;
  }

  30% {
    transform: translateY(-5px);
    opacity: 1;
  }
}

// ==================== 输入区 ====================

.chat-input-wrap {
  background: #fff;
  border-top: 1px solid #ebeef5;
  padding: 12px 24px 10px;
}

.chat-input {
  display: flex;
  align-items: flex-end;
  gap: 12px;

  .el-textarea {
    flex: 1;
  }
}

.send-btn {
  height: 40px;
  padding: 0 22px;
}

.chat-tip {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 6px;
  text-align: right;
}

// ==================== 响应式 ====================

@media screen and (max-width: 768px) {
  .chat-sidebar {
    width: 190px;
  }

  .msg-body {
    max-width: 78%;
  }

  .chat-header,
  .chat-input-wrap {
    padding-left: 14px;
    padding-right: 14px;
  }

  .message-area {
    padding: 16px 14px;
  }

  .risk-alert {
    margin-left: 14px;
    margin-right: 14px;
  }
}
</style>
