<template>
  <div class="answer-container">
    <!-- 顶部栏：退出 + 量表名 + 作答统计 -->
    <div class="answer-header">
      <div class="header-left">
        <el-button link class="exit-btn" @click="handleExit">
          <el-icon><Back /></el-icon>
          <span>退出测评</span>
        </el-button>
        <h3 class="header-title">{{ detail ? detail.scaleName : '测评' }}</h3>
        <el-tag v-if="detail" size="small" type="info">{{ detail.totalQuestionNum }} 题</el-tag>
      </div>
      <div class="header-right">
        <span class="stat-text">已作答 {{ answeredCount }} / {{ questions.length }} 题</span>
        <span v-if="saving" class="saving-text">进度保存中...</span>
      </div>
    </div>

    <!-- 作答完成度进度条 -->
    <div class="answer-progress">
      <el-progress :percentage="answeredPercent" :stroke-width="10" />
    </div>

    <!-- 题目区域 -->
    <div v-loading="pageLoading" class="answer-body">
      <div v-if="currentQuestion" class="question-card">
        <div class="question-head">
          <span class="question-index">{{ currentIndex + 1 }}</span>
          <span class="question-total">/ {{ questions.length }}</span>
          <span class="question-tip">请选择一个最符合的选项</span>
        </div>
        <h4 class="question-content">{{ currentQuestion.questionContent }}</h4>
        <el-radio-group v-model="currentAnswer" class="option-group">
          <el-radio
            v-for="opt in currentQuestion.options"
            :key="opt.value"
            :label="opt.value"
            class="option-radio"
          >
            {{ opt.label }}
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 底部导航：上一题 / 下一题 / 提交答卷 -->
      <div v-if="currentQuestion" class="answer-footer">
        <el-button :disabled="currentIndex === 0" @click="prevQuestion">
          <el-icon><ArrowLeft /></el-icon>
          <span>上一题</span>
        </el-button>
        <el-button
          v-if="!isLastQuestion"
          type="primary"
          @click="nextQuestion"
        >
          <span>下一题</span>
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button
          v-else
          type="success"
          :loading="submitting"
          @click="handleSubmit"
        >
          提交答卷
        </el-button>
      </div>
      <p v-if="currentQuestion && isLastQuestion" class="submit-tip">
        完成全部题目后即可提交，提交前进度会自动保存
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AnswerItem, ScaleDetail, ScaleQuestion } from '@/types/scale'
import {
  createScaleRecordApi,
  getRecordProgressApi,
  getScaleDetailApi,
  saveAnswersApi,
  submitAnswersApi
} from '@/api/scale'

const route = useRoute()
const router = useRouter()

// 页面初始化加载态（取题目、创建/恢复记录）
const pageLoading = ref(false)

// 进度自动保存中
const saving = ref(false)

// 提交答卷中
const submitting = ref(false)

// 量表详情（题目与选项）
const detail = ref<ScaleDetail | null>(null)

// 当前评估记录ID（创建或恢复后写入）
const recordId = ref<number | null>(null)

// 当前题索引
const currentIndex = ref(0)

// 作答映射：题目ID -> 所选选项值（undefined表示未作答）
const answers = ref<Record<number, number | undefined>>({})

// 是否已提交（提交后离开页面不再触发保存）
const submitted = ref(false)

// 自动保存防抖定时器句柄
let saveTimer: number | undefined = undefined

// ==================== 计算属性 ====================

// 题目列表
const questions = computed<ScaleQuestion[]>(() => detail.value?.questions ?? [])

// 当前题目
const currentQuestion = computed<ScaleQuestion | null>(
  () => questions.value[currentIndex.value] || null
)

// 是否最后一题
const isLastQuestion = computed<boolean>(
  () => questions.value.length > 0 && currentIndex.value === questions.value.length - 1
)

// 已作答题数
const answeredCount = computed<number>(
  () => questions.value.filter((q) => answers.value[q.questionId] !== undefined).length
)

// 作答完成度百分比（进度条）
const answeredPercent = computed<number>(() =>
  questions.value.length > 0
    ? Math.round((answeredCount.value / questions.value.length) * 100)
    : 0
)

// 当前题作答值（读写当前题在answers中的选项，变化后触发防抖自动保存）
const currentAnswer = computed<number | null>({
  get: () => {
    const question = currentQuestion.value
    if (!question) {
      return null
    }
    const value = answers.value[question.questionId]
    return value === undefined ? null : value
  },
  set: (value) => {
    const question = currentQuestion.value
    if (!question || value === null || value === undefined) {
      return
    }
    answers.value = { ...answers.value, [question.questionId]: value }
    scheduleAutoSave()
  }
})

// ==================== 工具 ====================

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

// 将作答映射转为后端要求的作答列表（仅包含已作答题目）
const buildAnswerList = (): AnswerItem[] => {
  const list: AnswerItem[] = []
  for (const [questionId, optionValue] of Object.entries(answers.value)) {
    if (optionValue !== undefined) {
      list.push({ questionId: Number(questionId), optionValue })
    }
  }
  return list
}

// ==================== 进度保存 ====================

// 保存作答进度（静默，失败提示由拦截器统一处理）
const saveProgressNow = async () => {
  if (!recordId.value || submitted.value) {
    return
  }
  const payload = buildAnswerList()
  if (payload.length === 0) {
    return
  }
  saving.value = true
  try {
    await saveAnswersApi(recordId.value, { answers: payload })
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    saving.value = false
  }
}

// 防抖自动保存：选择答案后800毫秒保存一次，避免频繁请求
const scheduleAutoSave = () => {
  if (saveTimer !== undefined) {
    window.clearTimeout(saveTimer)
  }
  saveTimer = window.setTimeout(() => {
    saveTimer = undefined
    saveProgressNow()
  }, 800)
}

// ==================== 题目导航 ====================

// 上一题
const prevQuestion = () => {
  if (currentIndex.value > 0) {
    currentIndex.value -= 1
  }
}

// 下一题
const nextQuestion = () => {
  if (currentIndex.value < questions.value.length - 1) {
    currentIndex.value += 1
  }
}

// ==================== 提交答卷 ====================

// 提交答卷：校验全部作答 -> 二次确认 -> 提交 -> 跳转报告页
const handleSubmit = async () => {
  if (!recordId.value || submitting.value) {
    return
  }
  // 校验是否全部作答，未答完定位到第一道未答题
  const unansweredIndex = questions.value.findIndex(
    (q) => answers.value[q.questionId] === undefined
  )
  if (unansweredIndex >= 0) {
    ElMessage.warning(`还有 ${questions.value.length - answeredCount.value} 道题未作答`)
    currentIndex.value = unansweredIndex
    return
  }
  try {
    await ElMessageBox.confirm(
      '提交后将自动生成评估报告且不可再修改，确定提交吗？',
      '提交答卷',
      {
        type: 'warning',
        confirmButtonText: '确定提交',
        cancelButtonText: '继续检查'
      }
    )
  } catch {
    return // 用户取消提交
  }
  submitting.value = true
  try {
    // 提交前清掉未触发的防抖保存
    if (saveTimer !== undefined) {
      window.clearTimeout(saveTimer)
      saveTimer = undefined
    }
    await submitAnswersApi(recordId.value, { answers: buildAnswerList() })
    submitted.value = true
    ElMessage.success('提交成功，评估报告已生成')
    router.push(`/scale/report/${recordId.value}`)
  } catch (error) {
    // 提交失败错误弹窗提示（如答卷不完整、重复提交）
    ElMessageBox.alert(getErrorMessage(error), '提交失败', {
      type: 'error',
      confirmButtonText: '知道了'
    })
  } finally {
    submitting.value = false
  }
}

// ==================== 中途退出 ====================

// 退出测评：二次确认后返回列表页（离开页面时路由守卫自动保存当前进度）
const handleExit = async () => {
  if (detail.value) {
    try {
      await ElMessageBox.confirm(
        '作答进度会自动保存，稍后可在历史评估记录中继续作答。确定退出吗？',
        '退出测评',
        {
          type: 'warning',
          confirmButtonText: '保存并退出',
          cancelButtonText: '继续答题'
        }
      )
    } catch {
      return // 用户取消退出
    }
  }
  router.push('/scale')
}

// ==================== 初始化 ====================

// 新建评估记录：先取题目（量表停用/无题时不会创建记录），创建成功后把recordId写入地址栏，刷新可恢复
const startNewRecord = async (scaleId: number) => {
  const detailRes = await getScaleDetailApi(scaleId)
  const progressRes = await createScaleRecordApi({ scaleId })
  if (!progressRes.data) {
    throw new Error('创建评估记录失败')
  }
  detail.value = detailRes.data
  recordId.value = progressRes.data.recordId
  router.replace({
    path: '/scale/answer',
    query: { recordId: String(progressRes.data.recordId) }
  })
}

// 恢复已有评估记录：拉取进度回填作答，并定位到第一道未答题
const resumeRecord = async (targetRecordId: number) => {
  const progressRes = await getRecordProgressApi(targetRecordId)
  const progress = progressRes.data
  if (!progress) {
    throw new Error('评估记录不存在')
  }
  // 已完成的记录直接跳转报告页
  if (progress.status === 3) {
    router.replace(`/scale/report/${progress.recordId}`)
    return
  }
  const detailRes = await getScaleDetailApi(progress.scaleId)
  detail.value = detailRes.data
  recordId.value = progress.recordId

  // 回填已保存作答
  const restored: Record<number, number | undefined> = {}
  for (const item of progress.answers ?? []) {
    restored[item.questionId] = item.optionValue
  }
  answers.value = restored

  // 定位到第一道未答题（全部已答则回到第一题检查）
  const firstUnanswered = detailRes.data.questions.findIndex(
    (q) => restored[q.questionId] === undefined
  )
  currentIndex.value = firstUnanswered >= 0 ? firstUnanswered : 0
}

// 页面初始化：按地址栏参数区分"新建测评"与"恢复作答"
const initPage = async () => {
  const recordIdParam = route.query.recordId ? Number(route.query.recordId) : null
  const scaleIdParam = route.query.scaleId ? Number(route.query.scaleId) : null
  if (recordIdParam !== null && !Number.isNaN(recordIdParam)) {
    pageLoading.value = true
    try {
      await resumeRecord(recordIdParam)
    } catch {
      // 错误提示已由拦截器处理，返回列表页
      router.replace('/scale')
    } finally {
      pageLoading.value = false
    }
  } else if (scaleIdParam !== null && !Number.isNaN(scaleIdParam)) {
    pageLoading.value = true
    try {
      await startNewRecord(scaleIdParam)
    } catch {
      // 错误提示已由拦截器处理，返回列表页
      router.replace('/scale')
    } finally {
      pageLoading.value = false
    }
  } else {
    ElMessage.error('参数缺失，无法开始测评')
    router.replace('/scale')
  }
}

// ==================== 生命周期与离开守卫 ====================

onMounted(() => {
  initPage()
})

// 组件卸载时清理防抖定时器
onUnmounted(() => {
  if (saveTimer !== undefined) {
    window.clearTimeout(saveTimer)
    saveTimer = undefined
  }
})

// 离开页面（退出/刷新/跳报告页）时：清理定时器并保存未提交的作答进度（提交后不再保存）
onBeforeRouteLeave(() => {
  if (saveTimer !== undefined) {
    window.clearTimeout(saveTimer)
    saveTimer = undefined
  }
  if (!submitted.value && recordId.value && buildAnswerList().length > 0) {
    saveProgressNow()
  }
  return true
})
</script>

<style lang="scss" scoped>
.answer-container {
  width: 100%;
  max-width: 860px;
  margin: 0 auto;
  padding: 0 16px 48px;
}

// ==================== 顶部栏 ====================

.answer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 0;
  flex-wrap: wrap;

  .header-left {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;
  }

  .exit-btn {
    color: #909399;
    font-size: 14px;

    &:hover {
      color: #409eff;
    }
  }

  .header-title {
    font-size: 17px;
    font-weight: 600;
    color: #303133;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .stat-text {
    font-size: 13px;
    color: #606266;
  }

  .saving-text {
    font-size: 12px;
    color: #e6a23c;
  }
}

.answer-progress {
  margin-bottom: 18px;
}

// ==================== 题目卡片 ====================

.answer-body {
  min-height: 320px;
}

.question-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 28px 32px;
}

.question-head {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 14px;

  .question-index {
    font-size: 28px;
    font-weight: 700;
    color: #409eff;
  }

  .question-total {
    font-size: 15px;
    color: #c0c4cc;
  }

  .question-tip {
    margin-left: auto;
    font-size: 12px;
    color: #c0c4cc;
  }
}

.question-content {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
  line-height: 1.7;
  margin-bottom: 22px;
}

// ==================== 选项 ====================

.option-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-radio {
  display: flex;
  align-items: center;
  width: 100%;
  margin-right: 0;
  padding: 13px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.15s;

  :deep(.el-radio__label) {
    white-space: normal;
    word-break: break-all;
    line-height: 1.6;
  }

  &:hover {
    border-color: #409eff;
    background: #ecf5ff;
  }

  &.is-checked {
    border-color: #409eff;
    background: #ecf5ff;
    font-weight: 600;
  }
}

// ==================== 底部导航 ====================

.answer-footer {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 24px;

  .el-button {
    min-width: 120px;
    height: 42px;
  }
}

.submit-tip {
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 12px;
}

// ==================== 响应式 ====================

@media screen and (max-width: 768px) {
  .question-card {
    padding: 20px 16px;
  }

  .answer-footer {
    gap: 12px;

    .el-button {
      min-width: 100px;
    }
  }
}
</style>
