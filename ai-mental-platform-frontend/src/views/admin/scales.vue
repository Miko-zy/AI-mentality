<template>
  <div class="admin-page">
    <div class="page-card">
      <!-- ==================== 工具栏 ==================== -->
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          <span>新增量表</span>
        </el-button>
      </div>

      <!-- ==================== 量表表格 ==================== -->
      <el-table v-loading="loading" :data="pagedScales" border stripe>
        <el-table-column prop="scaleId" label="ID" width="70" align="center" />
        <el-table-column
          prop="scaleName"
          label="量表名称"
          min-width="170"
          show-overflow-tooltip
        />
        <el-table-column prop="scaleCode" label="编码" width="140" show-overflow-tooltip />
        <el-table-column
          prop="dimensionDesc"
          label="维度说明"
          min-width="180"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span>{{ row.dimensionDesc || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalQuestionNum" label="题数" width="70" align="center" />
        <el-table-column label="信度系数" width="90" align="center">
          <template #default="{ row }">
            <span>{{ row.reliability !== null ? row.reliability : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleOpenQuestions(row)">
              题目管理
            </el-button>
            <el-button
              link
              :type="row.status === 1 ? 'danger' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- ==================== 本地分页（列表接口全量返回） ==================== -->
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="scales.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </div>

    <!-- ==================== 新增/编辑量表弹窗 ==================== -->
    <el-dialog
      v-model="scaleDialogVisible"
      :title="isScaleEdit ? '编辑量表' : '新增量表'"
      width="620px"
      :close-on-click-modal="!submitting"
      @closed="handleScaleDialogClosed"
    >
      <el-form
        ref="scaleFormRef"
        :model="scaleForm"
        :rules="scaleRules"
        label-width="100px"
      >
        <el-form-item label="量表名称" prop="scaleName">
          <el-input
            v-model="scaleForm.scaleName"
            maxlength="100"
            show-word-limit
            placeholder="如：心理韧性量表（CD-RISC简化版）"
          />
        </el-form-item>
        <el-form-item label="量表编码" prop="scaleCode">
          <el-input
            v-model="scaleForm.scaleCode"
            maxlength="50"
            show-word-limit
            placeholder="全局唯一，如 CD_RISC_SHORT"
          />
        </el-form-item>
        <el-form-item label="维度说明" prop="dimensionDesc">
          <el-input
            v-model="scaleForm.dimensionDesc"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="如：坚韧性、力量性、乐观性三个维度"
          />
        </el-form-item>
        <el-form-item label="总题目数" prop="totalQuestionNum">
          <el-input-number
            v-model="scaleForm.totalQuestionNum"
            :min="0"
            :max="999"
            controls-position="right"
          />
          <span class="form-inline-tip">题目可在“题目管理”中维护</span>
        </el-form-item>
        <el-form-item label="信度系数" prop="reliability">
          <el-input-number
            v-model="scaleForm.reliability"
            :min="0"
            :max="1"
            :step="0.01"
            :precision="3"
            controls-position="right"
          />
          <span class="form-inline-tip">公开信度系数，取值0-1</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="submitting" @click="scaleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitScale">
          {{ isScaleEdit ? '保存修改' : '确定新增' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 题目管理弹窗 ==================== -->
    <el-dialog
      v-model="questionDialogVisible"
      :title="`题目管理 - ${currentScale?.scaleName || ''}`"
      width="960px"
      class="question-dialog"
    >
      <div class="question-toolbar">
        <el-button type="primary" @click="handleAddQuestion">
          <el-icon><Plus /></el-icon>
          <span>新增题目</span>
        </el-button>
        <span class="question-tip">选项JSON示例：[{"value":1,"label":"完全不符合"}]</span>
      </div>
      <el-table
        v-loading="questionsLoading"
        :data="questions"
        border
        stripe
        max-height="420"
      >
        <el-table-column prop="questionId" label="ID" width="70" align="center" />
        <el-table-column
          prop="questionContent"
          label="题目内容"
          min-width="220"
          show-overflow-tooltip
        />
        <el-table-column
          prop="optionJson"
          label="选项JSON"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          prop="scoreRule"
          label="计分规则"
          min-width="160"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span>{{ row.scoreRule || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortNum" label="排序" width="70" align="center" />
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEditQuestion(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDeleteQuestion(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty
        v-if="!questionsLoading && questions.length === 0"
        description="该量表暂无题目"
        :image-size="70"
      />
    </el-dialog>

    <!-- ==================== 新增/编辑题目弹窗 ==================== -->
    <el-dialog
      v-model="questionFormVisible"
      :title="isQuestionEdit ? '编辑题目' : '新增题目'"
      width="700px"
      :close-on-click-modal="!submitting"
      @closed="handleQuestionDialogClosed"
    >
      <el-form
        ref="questionFormRef"
        :model="questionForm"
        :rules="questionRules"
        label-width="90px"
      >
        <el-form-item label="题目内容" prop="questionContent">
          <el-input
            v-model="questionForm.questionContent"
            type="textarea"
            :rows="4"
            maxlength="2000"
            show-word-limit
            placeholder="请输入题目内容（1-2000字）"
          />
        </el-form-item>
        <el-form-item label="选项JSON" prop="optionJson">
          <el-input
            v-model="questionForm.optionJson"
            type="textarea"
            :rows="5"
            placeholder='[{"value":1,"label":"完全不符合"},{"value":2,"label":"不太符合"}, ...]'
          />
        </el-form-item>
        <el-form-item label="计分规则" prop="scoreRule">
          <el-input
            v-model="questionForm.scoreRule"
            type="textarea"
            :rows="3"
            placeholder='如 {"dimension":"toughness","scoreMap":{"1":1,"2":2,"3":3,"4":4,"5":5}}，可留空'
          />
        </el-form-item>
        <el-form-item label="排序号" prop="sortNum">
          <el-input-number
            v-model="questionForm.sortNum"
            :min="0"
            :max="9999"
            controls-position="right"
          />
          <span class="form-inline-tip">小的在前</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="submitting" @click="questionFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitQuestion">
          {{ isQuestionEdit ? '保存修改' : '确定新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { ScaleItem } from '@/types/scale'
import { AdminQuestionItem, ScaleSaveParams } from '@/types/admin'
import {
  addScaleQuestionApi,
  createScaleApi,
  deleteScaleQuestionApi,
  getAdminScalesApi,
  getScaleQuestionsApi,
  updateScaleApi,
  updateScaleQuestionApi,
  updateScaleStatusApi
} from '@/api/admin'

// 列表加载态
const loading = ref(false)

// 表单/操作提交中（量表与题目表单共用）
const submitting = ref(false)

// 全量量表列表（接口不分页，前端本地分页）
const scales = ref<ScaleItem[]>([])

// 本地分页页码
const pageNum = ref(1)

// 本地分页每页条数
const pageSize = ref(10)

// 当前页切片
const pagedScales = computed<ScaleItem[]>(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return scales.value.slice(start, start + pageSize.value)
})

// ==================== 量表列表 ====================

// 加载全部量表（含停用）
const loadScales = async () => {
  loading.value = true
  try {
    const res = await getAdminScalesApi()
    scales.value = res.data ?? []
    // 数据量变化后防止当前页越界
    const maxPage = Math.max(1, Math.ceil(scales.value.length / pageSize.value))
    if (pageNum.value > maxPage) {
      pageNum.value = maxPage
    }
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    loading.value = false
  }
}

// ==================== 新增/编辑量表 ====================

// 量表弹窗显示状态
const scaleDialogVisible = ref(false)

// 是否编辑模式
const isScaleEdit = ref(false)

// 编辑中的量表ID
const editScaleId = ref<number | null>(null)

// 量表表单引用
const scaleFormRef = ref<FormInstance>()

// 量表表单数据
const scaleForm = reactive<{
  /** 量表名称 */
  scaleName: string
  /** 量表编码（全局唯一） */
  scaleCode: string
  /** 维度说明 */
  dimensionDesc: string
  /** 总题目数 */
  totalQuestionNum: number
  /** 公开信度系数（0-1） */
  reliability: number | null
}>({
  scaleName: '',
  scaleCode: '',
  dimensionDesc: '',
  totalQuestionNum: 0,
  reliability: null
})

// 量表表单校验规则
const scaleRules: FormRules = {
  scaleName: [
    { required: true, message: '请输入量表名称', trigger: 'blur' },
    { max: 100, message: '量表名称最长100字', trigger: 'blur' }
  ],
  scaleCode: [
    { required: true, message: '请输入量表编码', trigger: 'blur' },
    { max: 50, message: '量表编码最长50字', trigger: 'blur' }
  ],
  dimensionDesc: [
    { max: 255, message: '维度说明最长255字', trigger: 'blur' }
  ],
  totalQuestionNum: [
    { required: true, message: '请填写总题目数', trigger: 'change' }
  ],
  reliability: [
    {
      validator: (_rule, value, callback) => {
        if (value === null || value === undefined) {
          callback() // 信度可留空
          return
        }
        const num = Number(value)
        if (Number.isNaN(num) || num < 0 || num > 1) {
          callback(new Error('信度系数必须在0-1之间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 打开新增量表弹窗
const handleAdd = () => {
  isScaleEdit.value = false
  editScaleId.value = null
  scaleForm.scaleName = ''
  scaleForm.scaleCode = ''
  scaleForm.dimensionDesc = ''
  scaleForm.totalQuestionNum = 0
  scaleForm.reliability = null
  scaleDialogVisible.value = true
}

// 打开编辑量表弹窗
const handleEdit = (row: ScaleItem) => {
  isScaleEdit.value = true
  editScaleId.value = row.scaleId
  scaleForm.scaleName = row.scaleName
  scaleForm.scaleCode = row.scaleCode
  scaleForm.dimensionDesc = row.dimensionDesc ?? ''
  scaleForm.totalQuestionNum = row.totalQuestionNum ?? 0
  scaleForm.reliability = row.reliability
  scaleDialogVisible.value = true
}

// 量表弹窗关闭后重置校验状态
const handleScaleDialogClosed = () => {
  scaleFormRef.value?.clearValidate()
}

// 提交量表表单：新增走 POST，编辑走 PUT
const handleSubmitScale = async () => {
  if (submitting.value || !scaleFormRef.value) {
    return
  }
  try {
    await scaleFormRef.value.validate()
  } catch {
    return // 校验未通过
  }
  const params: ScaleSaveParams = {
    scaleName: scaleForm.scaleName.trim(),
    scaleCode: scaleForm.scaleCode.trim(),
    dimensionDesc: scaleForm.dimensionDesc.trim(),
    totalQuestionNum: scaleForm.totalQuestionNum,
    reliability: scaleForm.reliability
  }
  submitting.value = true
  try {
    if (isScaleEdit.value && editScaleId.value !== null) {
      await updateScaleApi(editScaleId.value, params)
      ElMessage.success('量表已更新')
    } else {
      await createScaleApi(params)
      ElMessage.success('量表新增成功')
    }
    scaleDialogVisible.value = false
    loadScales()
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理（如编码重复）
  } finally {
    submitting.value = false
  }
}

// ==================== 启用/停用 ====================

// 启用/停用量表（停用后用户端不可见、不可开始测评）
const handleToggleStatus = async (row: ScaleItem) => {
  const target = row.status === 1 ? 0 : 1
  const actionText = target === 0 ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(
      target === 0
        ? `停用后「${row.scaleName}」在用户端不可见且无法开始测评，确定停用吗？`
        : `启用后「${row.scaleName}」将在用户端可见，确定启用吗？`,
      `${actionText}量表`,
      {
        type: 'warning',
        confirmButtonText: `确定${actionText}`,
        cancelButtonText: '取消'
      }
    )
  } catch {
    return // 用户取消操作
  }
  submitting.value = true
  try {
    await updateScaleStatusApi(row.scaleId, { status: target })
    ElMessage.success(`${actionText}成功`)
    loadScales()
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    submitting.value = false
  }
}

// ==================== 题目管理弹窗 ====================

// 题目弹窗显示状态
const questionDialogVisible = ref(false)

// 题目列表加载态
const questionsLoading = ref(false)

// 当前管理题目的量表
const currentScale = ref<ScaleItem | null>(null)

// 题目列表
const questions = ref<AdminQuestionItem[]>([])

// 打开题目管理弹窗并加载题目
const handleOpenQuestions = async (row: ScaleItem) => {
  currentScale.value = row
  questionDialogVisible.value = true
  questionsLoading.value = true
  try {
    const res = await getScaleQuestionsApi(row.scaleId)
    questions.value = res.data ?? []
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    questionsLoading.value = false
  }
}

// 重新加载当前量表的题目
const reloadQuestions = async () => {
  if (!currentScale.value) {
    return
  }
  questionsLoading.value = true
  try {
    const res = await getScaleQuestionsApi(currentScale.value.scaleId)
    questions.value = res.data ?? []
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    questionsLoading.value = false
  }
}

// ==================== 新增/编辑题目 ====================

// 题目表单弹窗显示状态
const questionFormVisible = ref(false)

// 是否编辑题目
const isQuestionEdit = ref(false)

// 编辑中的题目ID
const editQuestionId = ref<number | null>(null)

// 题目表单引用
const questionFormRef = ref<FormInstance>()

// 题目表单数据
const questionForm = reactive<{
  /** 题目内容 */
  questionContent: string
  /** 选项JSON字符串 */
  optionJson: string
  /** 计分规则JSON字符串 */
  scoreRule: string
  /** 排序号 */
  sortNum: number
}>({
  questionContent: '',
  optionJson: '',
  scoreRule: '',
  sortNum: 0
})

// 题目表单校验规则（选项/计分规则做前端JSON格式校验）
const questionRules: FormRules = {
  questionContent: [
    { required: true, message: '请输入题目内容', trigger: 'blur' },
    { max: 2000, message: '题目内容最长2000字', trigger: 'blur' }
  ],
  optionJson: [
    {
      validator: (_rule, value, callback) => {
        const text = String(value ?? '').trim()
        if (!text) {
          callback(new Error('请输入选项JSON'))
          return
        }
        try {
          JSON.parse(text)
          callback()
        } catch {
          callback(new Error('选项JSON格式不正确，请检查引号与括号'))
        }
      },
      trigger: 'blur'
    }
  ],
  scoreRule: [
    {
      validator: (_rule, value, callback) => {
        const text = String(value ?? '').trim()
        if (!text) {
          callback() // 计分规则可留空
          return
        }
        try {
          JSON.parse(text)
          callback()
        } catch {
          callback(new Error('计分规则JSON格式不正确'))
        }
      },
      trigger: 'blur'
    }
  ],
  sortNum: [
    { required: true, message: '请填写排序号', trigger: 'change' }
  ]
}

// 打开新增题目弹窗
const handleAddQuestion = () => {
  if (!currentScale.value) {
    return
  }
  isQuestionEdit.value = false
  editQuestionId.value = null
  questionForm.questionContent = ''
  questionForm.optionJson = ''
  questionForm.scoreRule = ''
  questionForm.sortNum = 0
  questionFormVisible.value = true
}

// 打开编辑题目弹窗（回填原始JSON配置）
const handleEditQuestion = (row: AdminQuestionItem) => {
  isQuestionEdit.value = true
  editQuestionId.value = row.questionId
  questionForm.questionContent = row.questionContent
  questionForm.optionJson = row.optionJson
  questionForm.scoreRule = row.scoreRule ?? ''
  questionForm.sortNum = row.sortNum ?? 0
  questionFormVisible.value = true
}

// 题目弹窗关闭后重置校验状态
const handleQuestionDialogClosed = () => {
  questionFormRef.value?.clearValidate()
}

// 提交题目表单：新增走 POST /scale/{scaleId}/question，编辑走 PUT /question/{id}
const handleSubmitQuestion = async () => {
  if (submitting.value || !currentScale.value || !questionFormRef.value) {
    return
  }
  try {
    await questionFormRef.value.validate()
  } catch {
    return // 校验未通过
  }
  const params = {
    questionContent: questionForm.questionContent.trim(),
    optionJson: questionForm.optionJson.trim(),
    // 空计分规则不传，后端存空
    scoreRule: questionForm.scoreRule.trim() || undefined,
    sortNum: questionForm.sortNum
  }
  submitting.value = true
  try {
    if (isQuestionEdit.value && editQuestionId.value !== null) {
      await updateScaleQuestionApi(editQuestionId.value, params)
      ElMessage.success('题目已更新')
    } else {
      await addScaleQuestionApi(currentScale.value.scaleId, params)
      ElMessage.success('题目新增成功')
    }
    questionFormVisible.value = false
    reloadQuestions()
    // 题数变化时同步刷新列表展示
    loadScales()
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    submitting.value = false
  }
}

// ==================== 删除题目 ====================

// 删除题目（逻辑删除，二次确认）
const handleDeleteQuestion = async (row: AdminQuestionItem) => {
  if (submitting.value) {
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定删除题目「${row.questionContent.slice(0, 30)}」吗？删除后用户端作答页将不再展示该题。`,
      '删除题目',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
  } catch {
    return // 用户取消操作
  }
  submitting.value = true
  try {
    await deleteScaleQuestionApi(row.questionId)
    ElMessage.success('删除成功')
    reloadQuestions()
    loadScales()
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    submitting.value = false
  }
}

// ==================== 时间格式化 ====================

/**
 * 数字补零为两位
 *
 * @param n 数字
 * @returns 两位字符串
 */
const pad2 = (n: number): string => String(n).padStart(2, '0')

/**
 * 格式化日期时间为 "yyyy-MM-dd HH:mm:ss"
 * 兼容后端时间字段的三种序列化格式（空格串 / ISO串 / 时间戳数组）
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

// ==================== 初始化 ====================

onMounted(() => {
  loadScales()
})
</script>

<style lang="scss" scoped>
.admin-page {
  width: 100%;
}

.page-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 18px 20px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.form-inline-tip {
  font-size: 12px;
  color: #c0c4cc;
  margin-left: 10px;
}

// 题目管理弹窗
.question-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;

  .question-tip {
    font-size: 12px;
    color: #909399;
    word-break: break-all;
    text-align: right;
  }
}
</style>

