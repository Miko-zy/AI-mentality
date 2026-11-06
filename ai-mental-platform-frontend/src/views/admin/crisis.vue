<template>
  <div class="admin-page">
    <div class="page-card">
      <!-- ==================== 查询工具栏 ==================== -->
      <div class="toolbar">
        <el-select
          v-model="query.handleStatus"
          placeholder="处理状态"
          clearable
          class="toolbar-select"
          @change="handleSearch"
        >
          <el-option label="待处理" :value="0" />
          <el-option label="已处理" :value="1" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          <span>查询</span>
        </el-button>
        <el-button @click="handleReset">重置</el-button>
        <span class="toolbar-tip">高风险事件请及时跟进处理并记录处理备注</span>
      </div>

      <!-- ==================== 危机事件表格 ==================== -->
      <el-table
        v-loading="loading"
        :data="crisisList"
        border
        stripe
        :row-class-name="rowClassName"
      >
        <el-table-column prop="crisisId" label="ID" width="70" align="center" />
        <el-table-column label="用户" width="130" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.userNickname || `用户${row.userId}` }}</span>
          </template>
        </el-table-column>
        <el-table-column label="风险等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="riskTagType(row.riskLevel)" size="small">
              {{ row.riskText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="msgContent"
          label="关联风险消息"
          min-width="220"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span>{{ row.msgContent || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="riskDesc"
          label="风险描述"
          min-width="150"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span>{{ row.riskDesc || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="处理状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.handleStatus === 1 ? 'success' : 'danger'" size="small">
              {{ row.handleStatusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="handleRemark"
          label="处理备注"
          min-width="150"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span>{{ row.handleRemark || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="上报时间" width="160">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="处理时间" width="160">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.handleTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleOpenDialog(row)">
              {{ row.handleStatus === 1 ? '跟进' : '处理' }}
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

    <!-- ==================== 处理事件弹窗 ==================== -->
    <el-dialog
      v-model="dialogVisible"
      title="处理危机事件"
      width="620px"
      :close-on-click-modal="!submitting"
      @closed="handleDialogClosed"
    >
      <!-- 事件摘要 -->
      <div v-if="currentRow" class="crisis-summary">
        <div class="summary-row">
          <span class="summary-label">上报用户</span>
          <span>{{ currentRow.userNickname || `用户${currentRow.userId}` }}</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">风险等级</span>
          <el-tag :type="riskTagType(currentRow.riskLevel)" size="small">
            {{ currentRow.riskText }}
          </el-tag>
        </div>
        <div class="summary-row">
          <span class="summary-label">关联消息</span>
          <span class="summary-msg">{{ currentRow.msgContent || '-' }}</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">风险描述</span>
          <span class="summary-msg">{{ currentRow.riskDesc || '-' }}</span>
        </div>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
        class="crisis-form"
      >
        <el-form-item label="处理状态" prop="handleStatus">
          <el-radio-group v-model="form.handleStatus">
            <el-radio :label="0">标记为待处理</el-radio>
            <el-radio :label="1">标记为已处理</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注" prop="handleRemark">
          <el-input
            v-model="form.handleRemark"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="填写跟进措施、回访情况等（最长500字，不填保留原备注）"
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
import { CrisisItem } from '@/types/admin'
import { getAdminCrisisApi, handleCrisisApi } from '@/api/admin'

// 表格加载态
const loading = ref(false)

// 操作提交中
const submitting = ref(false)

// 当前页危机事件列表
const crisisList = ref<CrisisItem[]>([])

// 总记录数（后端分页）
const total = ref(0)

// 查询与分页参数
const query = reactive<{
  /** 处理状态筛选：0/1，undefined为全部 */
  handleStatus: number | undefined
  /** 页码 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
}>({
  handleStatus: undefined,
  pageNum: 1,
  pageSize: 10
})

// ==================== 数据加载 ====================

// 分页加载危机事件
const loadCrisis = async () => {
  loading.value = true
  try {
    const res = await getAdminCrisisApi({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      handleStatus: query.handleStatus
    })
    crisisList.value = res.data?.list ?? []
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
  loadCrisis()
}

// 重置查询条件
const handleReset = () => {
  query.handleStatus = undefined
  query.pageNum = 1
  loadCrisis()
}

// 每页条数变化
const handleSizeChange = (size: number) => {
  query.pageSize = size
  query.pageNum = 1
  loadCrisis()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  query.pageNum = page
  loadCrisis()
}

// 风险等级标签颜色：1低 2中 3高
const riskTagType = (level: number): 'info' | 'warning' | 'danger' => {
  if (level >= 3) {
    return 'danger'
  }
  if (level === 2) {
    return 'warning'
  }
  return 'info'
}

// 待处理行高亮
const rowClassName = ({ row }: { row: CrisisItem }): string =>
  row.handleStatus === 0 ? 'pending-row' : ''

// ==================== 处理弹窗 ====================

// 弹窗显示状态
const dialogVisible = ref(false)

// 当前处理中的事件
const currentRow = ref<CrisisItem | null>(null)

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const form = reactive<{
  /** 目标处理状态：0待处理，1已处理 */
  handleStatus: number
  /** 处理备注 */
  handleRemark: string
}>({
  handleStatus: 0,
  handleRemark: ''
})

// 表单校验规则
const rules: FormRules = {
  handleStatus: [
    { required: true, message: '请选择处理状态', trigger: 'change' }
  ],
  handleRemark: [
    { max: 500, message: '处理备注最长500字', trigger: 'blur' }
  ]
}

// 打开处理弹窗（回填当前状态与原备注）
const handleOpenDialog = (row: CrisisItem) => {
  currentRow.value = row
  form.handleStatus = row.handleStatus
  form.handleRemark = row.handleRemark ?? ''
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
    const remark = form.handleRemark.trim()
    await handleCrisisApi(currentRow.value.crisisId, {
      handleStatus: form.handleStatus,
      // 空备注不传，后端保留原备注
      handleRemark: remark ? remark : undefined
    })
    ElMessage.success('处理成功')
    dialogVisible.value = false
    loadCrisis()
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
  loadCrisis()
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
  flex-wrap: wrap;

  .toolbar-select {
    width: 150px;
  }

  .toolbar-tip {
    font-size: 12px;
    color: #e6a23c;
    margin-left: 6px;
  }
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

// 待处理行浅红底色（el-table 行类名穿透）
.page-card :deep(.el-table .pending-row td.el-table__cell) {
  background-color: #fef0f0;
}

// 事件摘要
.crisis-summary {
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

    .summary-msg {
      word-break: break-all;
    }
  }
}

.crisis-form {
  margin-top: 4px;
}
</style>

