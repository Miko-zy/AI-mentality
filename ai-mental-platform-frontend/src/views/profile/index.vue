<template>
  <div class="profile-container">
    <div class="page-header">
      <h2 class="page-title">个人中心</h2>
      <p class="page-desc">管理个人资料，查看心理健康成长档案，提交意见反馈</p>
    </div>

    <el-tabs v-model="activeTab" class="profile-tabs" @tab-change="handleTabChange">
      <!-- 基本资料：展示用户基础信息，提供编辑资料与修改密码入口 -->
      <el-tab-pane label="基本资料" name="info">
        <div v-loading="profileLoading" class="info-card">
          <div class="info-user">
            <el-avatar :size="80" :src="profile.avatarUrl" class="info-avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="info-user-text">
              <h3 class="info-nickname">{{ profile.nickname }}</h3>
              <p class="info-email">{{ profile.email }}</p>
            </div>
            <div class="info-actions">
              <el-button type="primary" @click="openEditDialog">
                <el-icon><Edit /></el-icon>
                <span>编辑资料</span>
              </el-button>
              <el-button @click="openPwdDialog">
                <el-icon><Key /></el-icon>
                <span>修改密码</span>
              </el-button>
            </div>
          </div>
          <el-descriptions :column="2" border class="info-descriptions">
            <el-descriptions-item label="用户ID">{{ profile.userId }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ profile.nickname }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ profile.email }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ profile.phone || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="用户等级">Lv.{{ profile.userLevel }}</el-descriptions-item>
            <el-descriptions-item label="总积分">{{ profile.totalPoints }}</el-descriptions-item>
            <el-descriptions-item label="账号状态">
              <el-tag
                :type="profile.accountStatus === 1 ? 'success' : 'danger'"
                size="small"
              >
                {{ profile.accountStatus === 1 ? '正常' : '已禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="实名认证">
              {{ profile.realAuthFlag === 1 ? '已认证' : '未认证' }}
            </el-descriptions-item>
            <el-descriptions-item label="注册时间">
              {{ formatDateTime(profile.registerTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              {{ profile.role === 'admin' ? '管理员' : '普通用户' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-tab-pane>

      <!-- 心理健康成长档案：累计评估次数、累计训练完成数等成长统计 -->
      <el-tab-pane label="成长档案" name="growth">
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-icon stat-icon--assess">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-num">{{ profile.assessCount }}</div>
              <div class="stat-label">累计评估次数</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon stat-icon--course">
              <el-icon><Reading /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-num">{{ profile.courseFinishCount }}</div>
              <div class="stat-label">累计训练完成数</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon stat-icon--checkin">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-num">{{ profile.checkinCount }}</div>
              <div class="stat-label">累计打卡次数</div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon stat-icon--badge">
              <el-icon><Medal /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-num">{{ profile.badgeCount }}</div>
              <div class="stat-label">已解锁徽章数</div>
            </div>
          </div>
        </div>
        <el-empty
          v-if="
            profile.assessCount === 0 &&
            profile.courseFinishCount === 0 &&
            profile.checkinCount === 0 &&
            profile.badgeCount === 0
          "
          description="还没有成长记录，完成首次评估、训练与打卡后会在这里累积"
        />
        <p class="growth-tip">成长档案由系统自动统计：心理评估、课程训练完成与每日打卡都会实时累计。</p>
      </el-tab-pane>

      <!-- 打卡记录列表 -->
      <el-tab-pane label="打卡记录" name="checkin">
        <div v-loading="checkinLoading">
          <el-table
            :data="checkinList"
            border
            stripe
            empty-text="暂无打卡记录，完成打卡后会展示在这里"
          >
            <el-table-column type="index" label="序号" width="70" align="center" />
            <el-table-column label="打卡日期" width="140" align="center">
              <template #default="{ row }">
                <span>{{ formatDate(row.checkinDate) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="心情描述" min-width="240" show-overflow-tooltip>
              <template #default="{ row }">
                <span>{{ row.moodDesc || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="打卡时间" width="180" align="center">
              <template #default="{ row }">
                <span>{{ formatDateTime(row.createTime) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-if="checkinTotal > 0"
            class="pagination"
            layout="total, prev, pager, next"
            :current-page="checkinPageNum"
            :page-size="CHECKIN_PAGE_SIZE"
            :total="checkinTotal"
            :hide-on-single-page="true"
            @current-change="handleCheckinPageChange"
          />
        </div>
      </el-tab-pane>

      <!-- 已解锁成就徽章 -->
      <el-tab-pane label="成就徽章" name="badge">
        <div v-loading="badgeLoading">
          <div v-if="badgeList.length > 0" class="badge-grid">
            <div v-for="badge in badgeList" :key="badge.badgeId" class="badge-card">
              <el-image
                v-if="badge.badgeIcon"
                :src="badge.badgeIcon"
                fit="contain"
                class="badge-icon"
              >
                <template #placeholder>
                  <div class="badge-icon-fallback">
                    <el-icon><Medal /></el-icon>
                  </div>
                </template>
                <template #error>
                  <div class="badge-icon-fallback">
                    <el-icon><Medal /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-else class="badge-icon badge-icon-fallback">
                <el-icon><Medal /></el-icon>
              </div>
              <h4 class="badge-name">{{ badge.badgeName }}</h4>
              <p class="badge-desc">{{ badge.description }}</p>
              <p class="badge-time">解锁于 {{ formatDateTime(badge.obtainTime) }}</p>
            </div>
          </div>
          <el-empty
            v-if="!badgeLoading && badgeList.length === 0"
            description="还没有解锁徽章，继续加油"
          />
          <el-pagination
            v-if="badgeTotal > 0"
            class="pagination"
            layout="total, prev, pager, next"
            :current-page="badgePageNum"
            :page-size="BADGE_PAGE_SIZE"
            :total="badgeTotal"
            :hide-on-single-page="true"
            @current-change="handleBadgePageChange"
          />
        </div>
      </el-tab-pane>

      <!-- 意见反馈：提交反馈 + 本人反馈列表（含管理员回复） -->
      <el-tab-pane label="意见反馈" name="feedback">
        <div class="feedback-submit">
          <h3 class="section-title">提交反馈</h3>
          <el-form ref="feedbackFormRef" :model="feedbackForm" :rules="feedbackRules">
            <el-form-item prop="content">
              <el-input
                v-model="feedbackForm.content"
                type="textarea"
                :rows="4"
                maxlength="500"
                show-word-limit
                placeholder="请输入您的意见或建议（最多500字）"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="feedbackSubmitting"
                @click="handleFeedbackSubmit"
              >
                提交反馈
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        <el-divider content-position="left">我的反馈</el-divider>
        <div v-loading="feedbackLoading" class="feedback-list">
          <div v-for="item in feedbackList" :key="item.feedbackId" class="feedback-item">
            <div class="feedback-item-head">
              <el-tag :type="statusInfo(item.status).type" size="small">
                {{ statusInfo(item.status).text }}
              </el-tag>
              <span class="feedback-time">提交于 {{ formatDateTime(item.createTime) }}</span>
            </div>
            <p class="feedback-content">{{ item.feedbackContent }}</p>
            <div v-if="item.replyContent" class="feedback-reply">
              <div class="feedback-reply-head">
                <el-icon><ChatDotRound /></el-icon>
                <span class="feedback-reply-title">管理员回复</span>
                <span class="feedback-time">{{ formatDateTime(item.updateTime) }}</span>
              </div>
              <p class="feedback-reply-content">{{ item.replyContent }}</p>
            </div>
            <p v-else-if="item.status === 0" class="feedback-wait">
              管理员尚未回复，请耐心等待
            </p>
            <p v-else class="feedback-wait">该反馈已关闭</p>
          </div>
          <el-empty
            v-if="!feedbackLoading && feedbackList.length === 0"
            description="暂无反馈记录"
          />
          <el-pagination
            v-if="feedbackTotal > 0"
            class="pagination"
            layout="total, prev, pager, next"
            :current-page="feedbackPageNum"
            :page-size="FEEDBACK_PAGE_SIZE"
            :total="feedbackTotal"
            :hide-on-single-page="true"
            @current-change="handleFeedbackPageChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑个人资料对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑个人资料" width="480px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="90px">
        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model.trim="editForm.nickname"
            maxlength="20"
            show-word-limit
            placeholder="2-20个字符"
          />
        </el-form-item>
        <el-form-item label="头像地址" prop="avatarUrl">
          <el-input
            v-model.trim="editForm.avatarUrl"
            maxlength="255"
            placeholder="https:// 开头的图片地址（可留空）"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model.trim="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model.trim="editForm.phone"
            maxlength="11"
            placeholder="选填，11位手机号"
          />
        </el-form-item>
        <el-form-item label="实名认证" prop="realAuthFlag">
          <el-radio-group v-model="editForm.realAuthFlag">
            <el-radio :label="0">未认证</el-radio>
            <el-radio :label="1">已认证</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="handleSaveProfile">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="pwdDialogVisible" title="修改密码" width="480px" destroy-on-close>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
            v-model="pwdForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入旧密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="pwdForm.newPassword"
            type="password"
            show-password
            placeholder="6-20位，需包含字母和数字"
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="pwdForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdSaving" @click="handleChangePassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  BadgeItem,
  ChangePasswordForm,
  CheckinRecordItem,
  FeedbackItem,
  FeedbackStatusInfo,
  FeedbackSubmitParams,
  UpdateProfileParams,
  UserProfile
} from '@/types/profile'
import {
  changePasswordApi,
  getCheckinsApi,
  getMyBadgesApi,
  getMyFeedbackApi,
  getProfileApi,
  submitFeedbackApi,
  updateProfileApi
} from '@/api/profile'

const userStore = useUserStore()

// 当前选中的页签：info基本资料 growth成长档案 checkin打卡记录 badge成就徽章 feedback意见反馈
const activeTab = ref('info')

// ==================== 个人档案（基础信息 + 成长统计） ====================

// 个人档案（先给默认值，避免模板渲染空字段报错）
const profile = reactive<UserProfile>({
  userId: 0,
  nickname: '',
  avatarUrl: '',
  email: '',
  phone: '',
  realAuthFlag: 0,
  accountStatus: 1,
  totalPoints: 0,
  userLevel: 0,
  registerTime: null,
  role: 'user',
  assessCount: 0,
  courseFinishCount: 0,
  checkinCount: 0,
  badgeCount: 0
})

// 档案加载态
const profileLoading = ref(false)

// ==================== 编辑资料对话框 ====================

// 编辑资料对话框可见性
const editDialogVisible = ref(false)

// 编辑资料保存加载态
const editSaving = ref(false)

// 编辑资料表单引用
const editFormRef = ref<FormInstance>()

// 编辑资料表单
const editForm = reactive<UpdateProfileParams>({
  nickname: '',
  avatarUrl: '',
  realAuthFlag: 0,
  phone: '',
  email: ''
})

// 手机号格式（与后端 ParamValidator.isPhone 规则一致：1[3-9]开头的11位数字）
const PHONE_PATTERN = /^1[3-9]\d{9}$/

// 邮箱格式（与后端 ParamValidator.isEmail 规则一致）
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// 新密码规则（与后端 ParamValidator.isPassword 规则一致：6-20位且同时包含字母和数字）
const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).{6,20}$/

// 手机号选填校验：仅填写时校验格式，留空表示不修改
const validatePhone = (_rule: any, value: any, callback: any) => {
  if (value && !PHONE_PATTERN.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

// 邮箱格式校验（自定义写法，与后端提示保持一致）
const validateEmail = (_rule: any, value: any, callback: any) => {
  if (!value) {
    callback(new Error('请输入邮箱'))
  } else if (!EMAIL_PATTERN.test(value)) {
    callback(new Error('邮箱格式不正确'))
  } else {
    callback()
  }
}

// 编辑资料表单校验规则（与后端 updateUserInfo 校验规则逐条对齐）
const editRules: FormRules = {
  nickname: [
    { required: true, whitespace: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度需在2-20个字符之间', trigger: 'blur' }
  ],
  avatarUrl: [
    { max: 255, message: '头像地址长度不能超过255个字符', trigger: 'blur' }
  ],
  email: [{ validator: validateEmail, trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }]
}

// ==================== 修改密码对话框 ====================

// 修改密码对话框可见性
const pwdDialogVisible = ref(false)

// 修改密码保存加载态
const pwdSaving = ref(false)

// 修改密码表单引用
const pwdFormRef = ref<FormInstance>()

// 修改密码表单（confirmPassword 仅前端使用）
const pwdForm = reactive<ChangePasswordForm>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 新密码校验：格式（6-20位含字母数字）+ 不能与旧密码相同（与后端规则一致）
const validateNewPassword = (_rule: any, value: any, callback: any) => {
  if (!PASSWORD_PATTERN.test(value)) {
    callback(new Error('密码长度需在6-20位之间，且需包含字母和数字'))
  } else if (value === pwdForm.oldPassword) {
    callback(new Error('新密码不能与旧密码相同'))
  } else {
    callback()
  }
}

// 确认新密码一致性校验
const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 修改密码表单校验规则
const pwdRules: FormRules = {
  oldPassword: [
    { required: true, whitespace: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// ==================== 打卡记录 ====================

// 每页条数
const CHECKIN_PAGE_SIZE = 10

// 打卡记录列表
const checkinList = ref<CheckinRecordItem[]>([])

// 打卡记录总数
const checkinTotal = ref(0)

// 打卡记录当前页码
const checkinPageNum = ref(1)

// 打卡记录加载态
const checkinLoading = ref(false)

// 打卡记录是否已加载过（切换页签时懒加载）
const checkinLoaded = ref(false)

// ==================== 成就徽章 ====================

// 每页条数
const BADGE_PAGE_SIZE = 12

// 已解锁徽章列表
const badgeList = ref<BadgeItem[]>([])

// 徽章总数
const badgeTotal = ref(0)

// 徽章当前页码
const badgePageNum = ref(1)

// 徽章加载态
const badgeLoading = ref(false)

// 徽章是否已加载过
const badgeLoaded = ref(false)

// ==================== 意见反馈 ====================

// 每页条数
const FEEDBACK_PAGE_SIZE = 5

// 反馈表单
const feedbackForm = reactive<FeedbackSubmitParams>({
  content: ''
})

// 反馈表单引用
const feedbackFormRef = ref<FormInstance>()

// 反馈表单校验规则（与后端规则一致：非空、不超过500字符）
const feedbackRules: FormRules = {
  content: [
    { required: true, whitespace: true, message: '请输入反馈内容', trigger: 'blur' },
    { max: 500, message: '反馈内容不能超过500个字符', trigger: 'blur' }
  ]
}

// 反馈提交加载态
const feedbackSubmitting = ref(false)

// 反馈列表
const feedbackList = ref<FeedbackItem[]>([])

// 反馈总数
const feedbackTotal = ref(0)

// 反馈当前页码
const feedbackPageNum = ref(1)

// 反馈列表加载态
const feedbackLoading = ref(false)

// 反馈是否已加载过
const feedbackLoaded = ref(false)

// 反馈状态展示映射：0待处理 1已回复 2关闭
const FEEDBACK_STATUS_MAP: Record<number, FeedbackStatusInfo> = {
  0: { text: '待处理', type: 'warning' },
  1: { text: '已回复', type: 'success' },
  2: { text: '已关闭', type: 'info' }
}

// ==================== 通用工具 ====================

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
 * 格式化日期为 "yyyy-MM-dd"
 *
 * @param value 时间字段值
 * @returns 格式化后的日期字符串，空值返回 "-"
 */
const formatDate = (value: unknown): string => {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  if (Array.isArray(value)) {
    const parts = value as number[]
    if (parts.length >= 3) {
      return `${parts[0]}-${pad2(parts[1])}-${pad2(parts[2])}`
    }
    return '-'
  }
  return String(value).substring(0, 10)
}

/**
 * 获取反馈状态展示项
 *
 * @param status 反馈状态：0待处理，1已回复，2关闭
 * @returns 状态文案与标签颜色
 */
const statusInfo = (status: number): FeedbackStatusInfo => {
  return FEEDBACK_STATUS_MAP[status] || { text: '未知状态', type: 'info' }
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

// ==================== 个人档案加载 ====================

// 将档案中的昵称、头像同步回 pinia，保证全局展示一致
const syncStoreUserInfo = () => {
  const current = userStore.userInfo
  if (!current) {
    return
  }
  userStore.setUserInfo({
    ...current,
    username: profile.nickname,
    nickname: profile.nickname,
    avatar: profile.avatarUrl
  })
}

// 加载个人档案（基础信息 + 成长统计），成功后同步 store
const loadProfile = async () => {
  profileLoading.value = true
  try {
    const res = await getProfileApi()
    if (res.data) {
      Object.assign(profile, res.data)
      syncStoreUserInfo()
    }
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    profileLoading.value = false
  }
}

// ==================== 编辑资料 ====================

// 打开编辑资料对话框：用当前档案填充表单
const openEditDialog = () => {
  editForm.nickname = profile.nickname
  editForm.avatarUrl = profile.avatarUrl
  editForm.email = profile.email
  editForm.phone = profile.phone
  editForm.realAuthFlag = profile.realAuthFlag
  editDialogVisible.value = true
}

// 保存个人资料：校验通过后提交，成功后刷新档案（档案加载时会同步 store）
const handleSaveProfile = () => {
  editFormRef.value?.validate((valid: boolean) => {
    if (!valid) {
      return
    }
    editSaving.value = true
    updateProfileApi({
      nickname: editForm.nickname.trim(),
      avatarUrl: editForm.avatarUrl.trim(),
      realAuthFlag: editForm.realAuthFlag,
      phone: editForm.phone.trim(),
      email: editForm.email.trim()
    })
      .then(() => {
        ElMessage.success('个人资料更新成功')
        editDialogVisible.value = false
        loadProfile()
      })
      .catch((error: unknown) => {
        // 保存失败错误弹窗提示（如昵称被占用、邮箱已被使用等）
        ElMessageBox.alert(getErrorMessage(error), '保存失败', {
          type: 'error',
          confirmButtonText: '知道了'
        })
      })
      .finally(() => {
        editSaving.value = false
      })
  })
}

// ==================== 修改密码 ====================

// 打开修改密码对话框：清空上次输入
const openPwdDialog = () => {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdDialogVisible.value = true
}

// 修改密码：校验通过后提交，成功后关闭对话框
const handleChangePassword = () => {
  pwdFormRef.value?.validate((valid: boolean) => {
    if (!valid) {
      return
    }
    pwdSaving.value = true
    changePasswordApi({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
      .then(() => {
        ElMessage.success('密码修改成功')
        pwdDialogVisible.value = false
        pwdForm.oldPassword = ''
        pwdForm.newPassword = ''
        pwdForm.confirmPassword = ''
      })
      .catch((error: unknown) => {
        // 修改失败错误弹窗提示（如旧密码不正确）
        ElMessageBox.alert(getErrorMessage(error), '修改失败', {
          type: 'error',
          confirmButtonText: '知道了'
        })
      })
      .finally(() => {
        pwdSaving.value = false
      })
  })
}

// ==================== 打卡记录 ====================

// 加载打卡记录（分页）
const loadCheckins = async () => {
  checkinLoading.value = true
  try {
    const res = await getCheckinsApi(checkinPageNum.value, CHECKIN_PAGE_SIZE)
    checkinList.value = res.data?.list ?? []
    checkinTotal.value = res.data?.total ?? 0
    checkinLoaded.value = true
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    checkinLoading.value = false
  }
}

// 打卡记录翻页
const handleCheckinPageChange = (page: number) => {
  checkinPageNum.value = page
  loadCheckins()
}

// ==================== 成就徽章 ====================

// 加载已解锁徽章（分页）
const loadBadges = async () => {
  badgeLoading.value = true
  try {
    const res = await getMyBadgesApi(badgePageNum.value, BADGE_PAGE_SIZE)
    badgeList.value = res.data?.list ?? []
    badgeTotal.value = res.data?.total ?? 0
    badgeLoaded.value = true
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    badgeLoading.value = false
  }
}

// 徽章列表翻页
const handleBadgePageChange = (page: number) => {
  badgePageNum.value = page
  loadBadges()
}

// ==================== 意见反馈 ====================

// 加载本人反馈列表（分页，含管理员回复）
const loadFeedback = async () => {
  feedbackLoading.value = true
  try {
    const res = await getMyFeedbackApi(feedbackPageNum.value, FEEDBACK_PAGE_SIZE)
    feedbackList.value = res.data?.list ?? []
    feedbackTotal.value = res.data?.total ?? 0
    feedbackLoaded.value = true
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    feedbackLoading.value = false
  }
}

// 反馈列表翻页
const handleFeedbackPageChange = (page: number) => {
  feedbackPageNum.value = page
  loadFeedback()
}

// 提交反馈：校验通过后提交，成功后清空表单并刷新列表第一页
const handleFeedbackSubmit = () => {
  feedbackFormRef.value?.validate((valid: boolean) => {
    if (!valid) {
      return
    }
    feedbackSubmitting.value = true
    submitFeedbackApi({ content: feedbackForm.content.trim() })
      .then((res) => {
        ElMessage.success(res.message || '反馈提交成功，感谢您的反馈')
        feedbackForm.content = ''
        feedbackPageNum.value = 1
        loadFeedback()
      })
      .catch((error: unknown) => {
        // 提交失败错误弹窗提示
        ElMessageBox.alert(getErrorMessage(error), '提交失败', {
          type: 'error',
          confirmButtonText: '知道了'
        })
      })
      .finally(() => {
        feedbackSubmitting.value = false
      })
  })
}

// ==================== 页签切换（懒加载） ====================

// 切换页签时首次加载对应数据，失败后 loaded 标记保持 false，下次切换会重试
const handleTabChange = (name: string | number) => {
  const tab = String(name)
  if (tab === 'checkin' && !checkinLoaded.value) {
    loadCheckins()
  }
  if (tab === 'badge' && !badgeLoaded.value) {
    loadBadges()
  }
  if (tab === 'feedback' && !feedbackLoaded.value) {
    loadFeedback()
  }
}

// 首次进入页面加载个人档案（成长统计随档案返回，无需额外请求）
onMounted(() => {
  loadProfile()
})
</script>

<style lang="scss" scoped>
.profile-container {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.page-header {
  margin-bottom: 16px;

  .page-title {
    font-size: 22px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 6px;
  }

  .page-desc {
    font-size: 14px;
    color: #909399;
  }
}

.profile-tabs {
  :deep(.el-tabs__content) {
    padding-top: 8px;
  }
}

// 分页统一右对齐
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

// ==================== 基本资料 ====================

.info-card {
  padding: 8px 0;
}

.info-user {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
  margin-bottom: 20px;

  .info-avatar {
    flex-shrink: 0;
    background: #409eff;
    color: #fff;
    font-size: 32px;

    .el-icon {
      font-size: 32px;
    }
  }

  .info-user-text {
    flex: 1;
    min-width: 0;

    .info-nickname {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .info-email {
      font-size: 13px;
      color: #909399;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .info-actions {
    flex-shrink: 0;
    display: flex;
    gap: 12px;
  }
}

.info-descriptions {
  :deep(.el-descriptions__label) {
    width: 96px;
  }
}

// ==================== 成长档案 ====================

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: #fff;
    flex-shrink: 0;
  }

  .stat-icon--assess {
    background: linear-gradient(135deg, #409eff, #66b3ff);
  }

  .stat-icon--course {
    background: linear-gradient(135deg, #67c23a, #85ce61);
  }

  .stat-icon--checkin {
    background: linear-gradient(135deg, #e6a23c, #f0c78a);
  }

  .stat-icon--badge {
    background: linear-gradient(135deg, #f56c6c, #f89898);
  }

  .stat-num {
    font-size: 26px;
    font-weight: 700;
    color: #303133;
    line-height: 1.2;
  }

  .stat-label {
    font-size: 13px;
    color: #909399;
    margin-top: 4px;
  }
}

.growth-tip {
  font-size: 13px;
  color: #909399;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px 16px;
}

// ==================== 成就徽章 ====================

.badge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.badge-card {
  padding: 20px 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  text-align: center;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  }

  .badge-icon {
    width: 64px;
    height: 64px;
    margin: 0 auto 12px;
  }

  .badge-icon-fallback {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: #fdf6ec;
    color: #e6a23c;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30px;
    margin: 0 auto;
  }

  .badge-name {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 6px;
  }

  .badge-desc {
    font-size: 12px;
    color: #909399;
    line-height: 1.5;
    min-height: 36px;
    margin-bottom: 8px;
  }

  .badge-time {
    font-size: 12px;
    color: #c0c4cc;
  }
}

// ==================== 意见反馈 ====================

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.feedback-submit {
  padding-bottom: 4px;
}

.feedback-list {
  min-height: 120px;
}

.feedback-item {
  padding: 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  margin-bottom: 12px;

  .feedback-item-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;
  }

  .feedback-time {
    font-size: 12px;
    color: #c0c4cc;
  }

  .feedback-content {
    font-size: 14px;
    color: #303133;
    line-height: 1.6;
    word-break: break-all;
    white-space: pre-wrap;
  }

  .feedback-reply {
    margin-top: 12px;
    padding: 12px;
    background: #f0f9eb;
    border-radius: 8px;

    .feedback-reply-head {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #67c23a;
      font-size: 13px;
      margin-bottom: 6px;

      .feedback-reply-title {
        font-weight: 600;
      }

      .feedback-time {
        margin-left: auto;
      }
    }

    .feedback-reply-content {
      font-size: 13px;
      color: #606266;
      line-height: 1.6;
      word-break: break-all;
      white-space: pre-wrap;
    }
  }

  .feedback-wait {
    margin-top: 10px;
    font-size: 12px;
    color: #c0c4cc;
  }
}

// ==================== 响应式 ====================

@media screen and (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .info-user {
    flex-wrap: wrap;

    .info-actions {
      width: 100%;
      justify-content: flex-end;
    }
  }
}
</style>
