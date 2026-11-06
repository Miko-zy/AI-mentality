<template>
  <div class="admin-page">
    <div class="page-card">
      <!-- ==================== 工具栏 ==================== -->
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          <span>新增公告</span>
        </el-button>
      </div>

      <!-- ==================== 公告表格 ==================== -->
      <el-table v-loading="loading" :data="pagedNotices" border stripe>
        <el-table-column prop="noticeId" label="ID" width="70" align="center" />
        <el-table-column
          prop="noticeTitle"
          label="公告标题"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          prop="noticeContent"
          label="公告正文"
          min-width="260"
          show-overflow-tooltip
        />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.publishTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发布人" width="110" align="center">
          <template #default="{ row }">
            <span>{{ row.adminName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status !== 1"
              link
              type="success"
              @click="handlePublish(row)"
            >
              发布
            </el-button>
            <el-button
              v-if="row.status === 1"
              link
              type="warning"
              @click="handleOffline(row)"
            >
              下架
            </el-button>
            <el-button
              v-if="row.status === 2"
              link
              type="info"
              @click="handleDraft(row)"
            >
              转草稿
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- ==================== 本地分页（列表接口全量返回） ==================== -->
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="notices.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </div>

    <!-- ==================== 新增/编辑公告弹窗 ==================== -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑公告' : '新增公告'"
      width="640px"
      :close-on-click-modal="!submitting"
      @closed="handleDialogClosed"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="公告标题" prop="noticeTitle">
          <el-input
            v-model="form.noticeTitle"
            maxlength="100"
            show-word-limit
            placeholder="请输入公告标题（1-100字）"
          />
        </el-form-item>
        <el-form-item label="公告正文" prop="noticeContent">
          <el-input
            v-model="form.noticeContent"
            type="textarea"
            :rows="8"
            maxlength="5000"
            show-word-limit
            placeholder="请输入公告正文（1-5000字）"
          />
        </el-form-item>
        <!-- 仅新增时可选初始状态；编辑不改状态（发布/下架走列表状态操作） -->
        <el-form-item v-if="!isEdit" label="初始状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">已发布</el-radio>
            <el-radio :label="2">下架</el-radio>
          </el-radio-group>
          <p class="form-tip">选择“已发布”时将立即记录发布时间与发布人</p>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="submitting" @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '确定新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { NoticeItem } from '@/types/admin'
import {
  createNoticeApi,
  getAdminNoticesApi,
  updateNoticeApi,
  updateNoticeStatusApi
} from '@/api/admin'

// 列表加载态
const loading = ref(false)

// 操作提交中（表单提交与状态切换共用）
const submitting = ref(false)

// 全量公告列表（接口不分页，前端本地分页）
const notices = ref<NoticeItem[]>([])

// 本地分页页码
const pageNum = ref(1)

// 本地分页每页条数
const pageSize = ref(10)

// 当前页切片
const pagedNotices = computed<NoticeItem[]>(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return notices.value.slice(start, start + pageSize.value)
})

// ==================== 列表加载 ====================

// 加载全部公告
const loadNotices = async () => {
  loading.value = true
  try {
    const res = await getAdminNoticesApi()
    notices.value = res.data ?? []
    // 数据量变化后防止当前页越界
    const maxPage = Math.max(1, Math.ceil(notices.value.length / pageSize.value))
    if (pageNum.value > maxPage) {
      pageNum.value = maxPage
    }
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    loading.value = false
  }
}

// 状态标签颜色：0草稿 1已发布 2下架
const statusTagType = (status: number): 'info' | 'success' | 'warning' => {
  if (status === 1) {
    return 'success'
  }
  if (status === 2) {
    return 'warning'
  }
  return 'info'
}

// ==================== 新增/编辑弹窗 ====================

// 弹窗显示状态
const dialogVisible = ref(false)

// 是否编辑模式（编辑不改状态）
const isEdit = ref(false)

// 编辑中的公告ID
const editId = ref<number | null>(null)

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const form = reactive<{
  /** 公告标题 */
  noticeTitle: string
  /** 公告正文 */
  noticeContent: string
  /** 初始状态（仅新增生效） */
  status: number
}>({
  noticeTitle: '',
  noticeContent: '',
  status: 0
})

// 表单校验规则
const rules: FormRules = {
  noticeTitle: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { max: 100, message: '标题最长100字', trigger: 'blur' }
  ],
  noticeContent: [
    { required: true, message: '请输入公告正文', trigger: 'blur' },
    { max: 5000, message: '正文最长5000字', trigger: 'blur' }
  ]
}

// 打开新增弹窗
const handleAdd = () => {
  isEdit.value = false
  editId.value = null
  form.noticeTitle = ''
  form.noticeContent = ''
  form.status = 0
  dialogVisible.value = true
}

// 打开编辑弹窗（仅回填标题正文）
const handleEdit = (row: NoticeItem) => {
  isEdit.value = true
  editId.value = row.noticeId
  form.noticeTitle = row.noticeTitle
  form.noticeContent = row.noticeContent
  form.status = row.status
  dialogVisible.value = true
}

// 弹窗关闭后重置校验状态
const handleDialogClosed = () => {
  formRef.value?.clearValidate()
}

// 提交表单：新增走 POST，编辑走 PUT（仅标题正文）
const handleSubmit = async () => {
  if (submitting.value || !formRef.value) {
    return
  }
  try {
    await formRef.value.validate()
  } catch {
    return // 校验未通过，表单内已展示错误
  }
  submitting.value = true
  try {
    if (isEdit.value && editId.value !== null) {
      await updateNoticeApi(editId.value, {
        noticeTitle: form.noticeTitle.trim(),
        noticeContent: form.noticeContent.trim()
      })
      ElMessage.success('公告已更新')
    } else {
      await createNoticeApi({
        noticeTitle: form.noticeTitle.trim(),
        noticeContent: form.noticeContent.trim(),
        status: form.status
      })
      ElMessage.success('公告新增成功')
    }
    dialogVisible.value = false
    loadNotices()
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    submitting.value = false
  }
}

// ==================== 状态切换（发布/下架/转草稿） ====================

/**
 * 统一的状态切换：二次确认后调用状态接口
 *
 * @param row    目标公告
 * @param status 目标状态
 * @param text   操作文案
 */
const changeStatus = async (row: NoticeItem, status: number, text: string) => {
  if (submitting.value) {
    return
  }
  try {
    await ElMessageBox.confirm(
      status === 1
        ? `发布后前台将立即展示该公告，确定${text}吗？`
        : status === 2
          ? `下架后前台不再展示该公告（发布时间保留），确定${text}吗？`
          : `确定${text}吗？`,
      `${text}确认`,
      {
        type: 'warning',
        confirmButtonText: `确定${text}`,
        cancelButtonText: '取消'
      }
    )
  } catch {
    return // 用户取消操作
  }
  submitting.value = true
  try {
    await updateNoticeStatusApi(row.noticeId, { status })
    ElMessage.success(`${text}成功`)
    loadNotices()
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    submitting.value = false
  }
}

// 发布（置为已发布，后端刷新发布时间与发布人）
const handlePublish = (row: NoticeItem) => changeStatus(row, 1, '发布')

// 下架（停用展示）
const handleOffline = (row: NoticeItem) => changeStatus(row, 2, '下架')

// 转草稿
const handleDraft = (row: NoticeItem) => changeStatus(row, 0, '转草稿')

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
  loadNotices()
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

.form-tip {
  font-size: 12px;
  color: #c0c4cc;
  line-height: 1.5;
  margin-top: 4px;
}
</style>

