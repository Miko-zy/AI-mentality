<template>
  <div class="admin-page">
    <div class="page-card">
      <!-- ==================== 查询工具栏 ==================== -->
      <div class="toolbar">
        <el-select
          v-model="query.status"
          placeholder="处理状态"
          clearable
          class="toolbar-select"
          @change="handleSearch"
        >
          <el-option label="待处理" :value="0" />
          <el-option label="已回复" :value="1" />
          <el-option label="关闭" :value="2" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          <span>查询</span>
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <!-- ==================== 反馈表格 ==================== -->
      <el-table v-loading="loading" :data="feedbackList" border stripe>
        <el-table-column prop="feedbackId" label="ID" width="70" align="center" />
        <el-table-column label="反馈用户" width="130" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.userNickname || `用户${row.userId}` }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="feedbackContent"
          label="反馈内容"
          min-width="240"
          show-overflow-tooltip
        />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="replyContent"
          label="管理员回复"
          min-width="200"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span>{{ row.replyContent || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="160">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.updateTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleOpenDialog(row)">
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- ==================== 分页 ==================== -->
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- ==================== 处理反馈弹窗 ==================== -->
    <el-dialog
      v-model="dialogVisible"
      title="处理用户反馈"
      width="640px"
      :close-on-click-modal="!submitting"
      @closed="handleDialogClosed"
    >
      <!-- 反馈摘要 -->
      <div v-if="currentRow" class="feedback-summary">
        <div class="summary-row">
          <span class="summary-label">反馈用户</span>
          <span>{{ currentRow.userNickname || `用户${currentRow.userId}` }}</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">反馈内容</span>
          <span class="summary-text">{{ currentRow.feedbackContent }}</span>
        </div>
        <div v-if="currentRow.replyContent" class="summary-row">
          <span class="summary-label">原回复</span>
          <span class="summary-text">{{ currentRow.replyContent }}</span>
        </div>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
        class="feedback-form"
      >
        <el-form-item label="处理状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">待处理</el-radio>
            <el-radio :label="1">已回复</el-radio>
            <el-radio :label="2">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="回复内容" prop="replyContent">
          <el-input
            v-model="form.replyContent"
            type="textarea"
            :rows="5"
            maxlength="1000"
            show-word-limit
            placeholder="选择“已回复”时必须填写；留空则保留原回复"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="submitting" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定处理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { FeedbackItem } from '@/types/admin'
import { getAdminFeedbackApi, handleFeedbackApi } from '@/api/admin'

// 表格加载态
const loading = ref(false)

// 操作提交中
const submitting = ref(false)

// 当前页反馈列表
const feedbackList = ref<FeedbackItem[]>([])

// 总记录数（后端分页）
const total = ref(0)

// 查询与分页参数
const query = reactive<{
  /** 状态筛选：0/1/2，undefined为全部 */
  status: number | undefined
  /** 页码 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
}>({
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

// ==================== 数据加载 ====================

// 分页加载用户反馈
const loadFeedback = async () => {
  loading.value = true
  try {
    const res = await getAdminFeedbackApi({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      status: query.status
    })
    feedbackList.value = res.data?.list ?? []
    total.value = Number(res.data?.total ?? 0)
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    loading.value = false
  }
}

// 查询：回到第一页
const handleSearch = () => {
  query.pageNum = 1
  loadFeedback()
}

// 重置查询条件
const handleReset = () => {
  query.status = undefined
  query.pageNum = 1
  loadFeedback()
}

// 每页条数变化
const handleSizeChange = (size: number) => {
  query.pageSize = size
  query.pageNum = 1
  loadFeedback()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  query.pageNum = page
  loadFeedback()
}

// 状态标签颜色：0待处理 1已回复 2关闭
const statusTagType = (status: number): 'warning' | 'success' | 'info' => {
  if (status === 1) {
    return 'success'
  }
  if (status === 2) {
    return 'info'
  }
  return 'warning'
}

// ==================== 处理弹窗 ====================

// 弹窗显示状态
const dialogVisible = ref(false)

// 当前处理中的反馈
const currentRow = ref<FeedbackItem | null>(null)

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const form = reactive<{
  /** 目标状态：0待处理，1已回复，2关闭 */
  status: number
  /** 回复内容 */
  replyContent: string
}>({
  status: 0,
  replyContent: ''
})

// 表单校验规则（status=1时回复内容必填，对应后端校验）
const rules: FormRules = {
  status: [
    { required: true, message: '请选择处理状态', trigger: 'change' }
  ],
  replyContent: [
    {
      validator: (_rule, value, callback) => {
        const text = String(value ?? '').trim()
        if (form.status === 1 && !text) {
          callback(new Error('选择“已回复”时必须填写回复内容'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    },
    {
      max: 1000,
      message: '回复内容最长1000字',
      trigger: 'blur'
    }
  ]
}

// 打开处理弹窗（回填当前状态与原回复）
const handleOpenDialog = (row: FeedbackItem) => {
  currentRow.value = row
  form.status = row.status
  form.replyContent = row.replyContent ?? ''
  dialogVisible.value = true
}

// 弹窗关闭后重置校验状态
const handleDialogClosed = () => {
  formRef.value?.clearValidate()
}

// 提交处理结果
const handleSubmit = async () => {
  if (submitting.value || !currentRow.value || !formRef.value) {
    return
  }
  try {
    await formRef.value.validate()
  } catch {
    return // 校验未通过
  }
  submitting.value = true
  try {
    const reply = form.replyContent.trim()
    await handleFeedbackApi(currentRow.value.feedbackId, {
      status: form.status,
      // 空回复不传，后端保留原回复
      replyContent: reply ? reply : undefined
    })
    ElMessage.success('处理成功')
    dialogVisible.value = false
    loadFeedback()
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
  loadFeedback()
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

  .toolbar-select {
    width: 150px;
  }
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

// 反馈摘要
.feedback-summary {
  padding: 12px 14px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;

  .summary-row {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    font-size: 13px;
    color: #303133;
    line-height: 1.7;

    .summary-label {
      width: 70px;
      flex-shrink: 0;
      color: #909399;
    }

    .summary-text {
      word-break: break-all;
      white-space: pre-wrap;
    }
  }
}

.feedback-form {
  margin-top: 4px;
}
</style>

