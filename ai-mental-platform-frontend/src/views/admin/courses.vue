<template>
  <div class="admin-page">
    <div class="page-card">
      <!-- ==================== 工具栏 ==================== -->
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          <span>新增课程</span>
        </el-button>
      </div>

      <!-- ==================== 课程表格 ==================== -->
      <el-table v-loading="loading" :data="pagedCourses" border stripe>
        <el-table-column prop="courseId" label="ID" width="70" align="center" />
        <el-table-column label="封面" width="90" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.coverUrl"
              :src="row.coverUrl"
              fit="cover"
              class="cover-thumb"
              :preview-src-list="[row.coverUrl]"
              preview-teleported
            />
            <span v-else class="cover-none">无</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="courseName"
          label="课程名称"
          min-width="170"
          show-overflow-tooltip
        />
        <el-table-column
          prop="courseDesc"
          label="课程简介"
          min-width="220"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span>{{ row.courseDesc || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalSection" label="小节数" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
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
            <el-button link type="warning" @click="handleOpenSections(row)">
              小节管理
            </el-button>
            <el-button
              link
              :type="row.status === 1 ? 'danger' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- ==================== 本地分页（列表接口全量返回） ==================== -->
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="courses.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </div>

    <!-- ==================== 新增/编辑课程弹窗 ==================== -->
    <el-dialog
      v-model="courseDialogVisible"
      :title="isCourseEdit ? '编辑课程' : '新增课程'"
      width="620px"
      :close-on-click-modal="!submitting"
      @closed="handleCourseDialogClosed"
    >
      <el-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="courseRules"
        label-width="100px"
      >
        <el-form-item label="课程名称" prop="courseName">
          <el-input
            v-model="courseForm.courseName"
            maxlength="100"
            show-word-limit
            placeholder="如：正念减压训练营"
          />
        </el-form-item>
        <el-form-item label="课程简介" prop="courseDesc">
          <el-input
            v-model="courseForm.courseDesc"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="课程简介（可选）"
          />
        </el-form-item>
        <el-form-item label="封面图地址" prop="coverUrl">
          <el-input
            v-model="courseForm.coverUrl"
            maxlength="255"
            placeholder="图片URL（可选）"
          />
        </el-form-item>
        <el-form-item label="小节总数" prop="totalSection">
          <el-input-number
            v-model="courseForm.totalSection"
            :min="0"
            :max="999"
            controls-position="right"
          />
          <span class="form-inline-tip">在“小节管理”中维护小节后会按实际数量自动覆盖</span>
        </el-form-item>
        <!-- 仅新增时可选初始状态；编辑不改上下架状态（走列表状态操作） -->
        <el-form-item v-if="!isCourseEdit" label="初始状态">
          <el-radio-group v-model="courseForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="submitting" @click="courseDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitCourse">
          {{ isCourseEdit ? '保存修改' : '确定新增' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 小节管理弹窗 ==================== -->
    <el-dialog
      v-model="sectionDialogVisible"
      :title="`小节管理 - ${currentCourse?.courseName || ''}`"
      width="920px"
    >
      <div class="section-toolbar">
        <el-button type="primary" @click="handleAddSection">
          <el-icon><Plus /></el-icon>
          <span>新增小节</span>
        </el-button>
      </div>
      <el-table
        v-loading="sectionsLoading"
        :data="sections"
        border
        stripe
        max-height="420"
      >
        <el-table-column prop="sectionId" label="ID" width="70" align="center" />
        <el-table-column prop="sortNum" label="排序" width="70" align="center" />
        <el-table-column
          prop="sectionTitle"
          label="小节标题"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column label="建议时长" width="100" align="center">
          <template #default="{ row }">
            <span>{{ row.studyMinutes !== null ? `${row.studyMinutes} 分钟` : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="sectionContent"
          label="小节内容"
          min-width="220"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span>{{ row.sectionContent || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEditSection(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDeleteSection(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty
        v-if="!sectionsLoading && sections.length === 0"
        description="该课程暂无小节"
        :image-size="70"
      />
    </el-dialog>

    <!-- ==================== 新增/编辑小节弹窗 ==================== -->
    <el-dialog
      v-model="sectionFormVisible"
      :title="isSectionEdit ? '编辑小节' : '新增小节'"
      width="680px"
      :close-on-click-modal="!submitting"
      @closed="handleSectionFormClosed"
    >
      <el-form
        ref="sectionFormRef"
        :model="sectionForm"
        :rules="sectionRules"
        label-width="100px"
      >
        <el-form-item label="小节标题" prop="sectionTitle">
          <el-input
            v-model="sectionForm.sectionTitle"
            maxlength="200"
            show-word-limit
            placeholder="请输入小节标题（1-200字）"
          />
        </el-form-item>
        <el-form-item label="小节内容" prop="sectionContent">
          <el-input
            v-model="sectionForm.sectionContent"
            type="textarea"
            :rows="7"
            maxlength="10000"
            show-word-limit
            placeholder="请输入图文正文（纯文本，可选，最长10000字）"
          />
        </el-form-item>
        <el-form-item label="建议时长" prop="studyMinutes">
          <el-input-number
            v-model="sectionForm.studyMinutes"
            :min="1"
            :max="600"
            controls-position="right"
          />
          <span class="form-inline-tip">分钟，1-600</span>
        </el-form-item>
        <el-form-item label="排序号" prop="sortNum">
          <el-input-number
            v-model="sectionForm.sortNum"
            :min="0"
            :max="9999"
            controls-position="right"
          />
          <span class="form-inline-tip">小的在前</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="submitting" @click="sectionFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitSection">
          {{ isSectionEdit ? '保存修改' : '确定新增' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { CourseItem, CourseSection } from '@/types/training'
import { CourseSaveParams } from '@/types/admin'
import {
  addCourseSectionApi,
  createCourseApi,
  deleteCourseSectionApi,
  getAdminCoursesApi,
  getCourseSectionsApi,
  updateCourseApi,
  updateCourseSectionApi,
  updateCourseStatusApi
} from '@/api/admin'

// 列表加载态
const loading = ref(false)

// 表单/操作提交中（课程与小节表单共用）
const submitting = ref(false)

// 全量课程列表（接口不分页，前端本地分页）
const courses = ref<CourseItem[]>([])

// 本地分页页码
const pageNum = ref(1)

// 本地分页每页条数
const pageSize = ref(10)

// 当前页切片
const pagedCourses = computed<CourseItem[]>(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return courses.value.slice(start, start + pageSize.value)
})

// ==================== 课程列表 ====================

// 加载全部课程（含下架）
const loadCourses = async () => {
  loading.value = true
  try {
    const res = await getAdminCoursesApi()
    courses.value = res.data ?? []
    // 数据量变化后防止当前页越界
    const maxPage = Math.max(1, Math.ceil(courses.value.length / pageSize.value))
    if (pageNum.value > maxPage) {
      pageNum.value = maxPage
    }
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    loading.value = false
  }
}

// ==================== 新增/编辑课程 ====================

// 课程弹窗显示状态
const courseDialogVisible = ref(false)

// 是否编辑模式
const isCourseEdit = ref(false)

// 编辑中的课程ID
const editCourseId = ref<number | null>(null)

// 课程表单引用
const courseFormRef = ref<FormInstance>()

// 课程表单数据
const courseForm = reactive<{
  /** 课程名称 */
  courseName: string
  /** 课程简介 */
  courseDesc: string
  /** 封面图地址 */
  coverUrl: string
  /** 小节总数 */
  totalSection: number
  /** 初始状态：1上架 0下架（仅新增生效） */
  status: number
}>({
  courseName: '',
  courseDesc: '',
  coverUrl: '',
  totalSection: 0,
  status: 1
})

// 课程表单校验规则
const courseRules: FormRules = {
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { max: 100, message: '课程名称最长100字', trigger: 'blur' }
  ],
  courseDesc: [
    { max: 500, message: '课程简介最长500字', trigger: 'blur' }
  ],
  coverUrl: [
    { max: 255, message: '封面图地址最长255字', trigger: 'blur' }
  ],
  totalSection: [
    { required: true, message: '请填写小节总数', trigger: 'change' }
  ]
}

// 打开新增课程弹窗
const handleAdd = () => {
  isCourseEdit.value = false
  editCourseId.value = null
  courseForm.courseName = ''
  courseForm.courseDesc = ''
  courseForm.coverUrl = ''
  courseForm.totalSection = 0
  courseForm.status = 1
  courseDialogVisible.value = true
}

// 打开编辑课程弹窗（不改上下架状态）
const handleEdit = (row: CourseItem) => {
  isCourseEdit.value = true
  editCourseId.value = row.courseId
  courseForm.courseName = row.courseName
  courseForm.courseDesc = row.courseDesc ?? ''
  courseForm.coverUrl = row.coverUrl ?? ''
  courseForm.totalSection = row.totalSection ?? 0
  courseForm.status = row.status
  courseDialogVisible.value = true
}

// 课程弹窗关闭后重置校验状态
const handleCourseDialogClosed = () => {
  courseFormRef.value?.clearValidate()
}

// 提交课程表单：新增走 POST，编辑走 PUT（仅课程信息）
const handleSubmitCourse = async () => {
  if (submitting.value || !courseFormRef.value) {
    return
  }
  try {
    await courseFormRef.value.validate()
  } catch {
    return // 校验未通过
  }
  const params: CourseSaveParams = {
    courseName: courseForm.courseName.trim(),
    courseDesc: courseForm.courseDesc.trim(),
    coverUrl: courseForm.coverUrl.trim(),
    totalSection: courseForm.totalSection
  }
  if (!isCourseEdit.value) {
    // 新增时携带初始状态，编辑不传（后端编辑不改状态）
    params.status = courseForm.status
  }
  submitting.value = true
  try {
    if (isCourseEdit.value && editCourseId.value !== null) {
      await updateCourseApi(editCourseId.value, params)
      ElMessage.success('课程已更新')
    } else {
      await createCourseApi(params)
      ElMessage.success('课程新增成功')
    }
    courseDialogVisible.value = false
    loadCourses()
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    submitting.value = false
  }
}

// ==================== 上架/下架 ====================

// 上架/下架课程（下架后用户端课程列表不可见）
const handleToggleStatus = async (row: CourseItem) => {
  const target = row.status === 1 ? 0 : 1
  const actionText = target === 0 ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(
      target === 0
        ? `下架后「${row.courseName}」在用户端不可见，已在学用户无法继续进入，确定下架吗？`
        : `确定将「${row.courseName}」重新上架吗？`,
      `${actionText}课程`,
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
    await updateCourseStatusApi(row.courseId, { status: target })
    ElMessage.success(`${actionText}成功`)
    loadCourses()
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    submitting.value = false
  }
}

// ==================== 小节管理弹窗 ====================

// 小节弹窗显示状态
const sectionDialogVisible = ref(false)

// 小节列表加载态
const sectionsLoading = ref(false)

// 当前管理小节的课程
const currentCourse = ref<CourseItem | null>(null)

// 小节列表（按排序号升序，后端返回）
const sections = ref<CourseSection[]>([])

// 打开小节管理弹窗并加载小节
const handleOpenSections = async (row: CourseItem) => {
  currentCourse.value = row
  sectionDialogVisible.value = true
  sectionsLoading.value = true
  try {
    const res = await getCourseSectionsApi(row.courseId)
    sections.value = res.data ?? []
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    sectionsLoading.value = false
  }
}

// 重新加载当前课程的小节
const reloadSections = async () => {
  if (!currentCourse.value) {
    return
  }
  sectionsLoading.value = true
  try {
    const res = await getCourseSectionsApi(currentCourse.value.courseId)
    sections.value = res.data ?? []
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    sectionsLoading.value = false
  }
}

// ==================== 新增/编辑小节 ====================

// 小节表单弹窗显示状态
const sectionFormVisible = ref(false)

// 是否编辑小节
const isSectionEdit = ref(false)

// 编辑中的小节ID
const editSectionId = ref<number | null>(null)

// 小节表单引用
const sectionFormRef = ref<FormInstance>()

// 小节表单数据
const sectionForm = reactive<{
  /** 小节标题 */
  sectionTitle: string
  /** 小节内容（纯文本图文） */
  sectionContent: string
  /** 建议学习时长（分钟） */
  studyMinutes: number
  /** 排序号 */
  sortNum: number
}>({
  sectionTitle: '',
  sectionContent: '',
  studyMinutes: 5,
  sortNum: 0
})

// 小节表单校验规则
const sectionRules: FormRules = {
  sectionTitle: [
    { required: true, message: '请输入小节标题', trigger: 'blur' },
    { max: 200, message: '小节标题最长200字', trigger: 'blur' }
  ],
  sectionContent: [
    { max: 10000, message: '小节内容最长10000字', trigger: 'blur' }
  ],
  studyMinutes: [
    {
      validator: (_rule, value, callback) => {
        const num = Number(value)
        if (Number.isNaN(num) || num < 1 || num > 600) {
          callback(new Error('建议学习时长应在1-600分钟之间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  sortNum: [
    { required: true, message: '请填写排序号', trigger: 'change' }
  ]
}

// 打开新增小节弹窗
const handleAddSection = () => {
  if (!currentCourse.value) {
    return
  }
  isSectionEdit.value = false
  editSectionId.value = null
  sectionForm.sectionTitle = ''
  sectionForm.sectionContent = ''
  sectionForm.studyMinutes = 5
  sectionForm.sortNum = 0
  sectionFormVisible.value = true
}

// 打开编辑小节弹窗（回填原内容）
const handleEditSection = (row: CourseSection) => {
  isSectionEdit.value = true
  editSectionId.value = row.sectionId
  sectionForm.sectionTitle = row.sectionTitle
  sectionForm.sectionContent = row.sectionContent ?? ''
  sectionForm.studyMinutes = row.studyMinutes ?? 5
  sectionForm.sortNum = row.sortNum ?? 0
  sectionFormVisible.value = true
}

// 小节弹窗关闭后重置校验状态
const handleSectionFormClosed = () => {
  sectionFormRef.value?.clearValidate()
}

// 提交小节表单：新增走 POST /course/{courseId}/section，编辑走 PUT /course/section/{id}
const handleSubmitSection = async () => {
  if (submitting.value || !currentCourse.value || !sectionFormRef.value) {
    return
  }
  try {
    await sectionFormRef.value.validate()
  } catch {
    return // 校验未通过
  }
  const params = {
    sectionTitle: sectionForm.sectionTitle.trim(),
    // 空内容不传，后端存空
    sectionContent: sectionForm.sectionContent.trim() || undefined,
    studyMinutes: sectionForm.studyMinutes,
    sortNum: sectionForm.sortNum
  }
  submitting.value = true
  try {
    if (isSectionEdit.value && editSectionId.value !== null) {
      await updateCourseSectionApi(editSectionId.value, params)
      ElMessage.success('小节已更新')
    } else {
      await addCourseSectionApi(currentCourse.value.courseId, params)
      ElMessage.success('小节新增成功')
    }
    sectionFormVisible.value = false
    reloadSections()
    // 小节总数由后端自动同步，刷新列表展示
    loadCourses()
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    submitting.value = false
  }
}

// ==================== 删除小节 ====================

// 逻辑删除小节（后端自动同步课程小节总数，二次确认）
const handleDeleteSection = async (row: CourseSection) => {
  if (submitting.value) {
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定删除小节「${row.sectionTitle}」吗？删除后课程小节总数将自动同步。`,
      '删除小节',
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
    await deleteCourseSectionApi(row.sectionId)
    ElMessage.success('删除成功')
    reloadSections()
    loadCourses()
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
  loadCourses()
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

.cover-thumb {
  width: 64px;
  height: 40px;
  border-radius: 4px;
  vertical-align: middle;
}

.cover-none {
  font-size: 12px;
  color: #c0c4cc;
}

// 小节管理弹窗
.section-toolbar {
  margin-bottom: 14px;
}
</style>

