<template>
  <div class="admin-page">
    <div class="page-card">
      <!-- ==================== 查询工具栏 ==================== -->
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="昵称模糊 或 邮箱精确"
          clearable
          class="toolbar-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="query.accountStatus"
          placeholder="账号状态"
          clearable
          class="toolbar-select"
        >
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          <span>查询</span>
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <!-- ==================== 用户表格 ==================== -->
      <el-table
        v-loading="loading"
        :data="users"
        border
        stripe
        class="user-table"
      >
        <el-table-column prop="userId" label="ID" width="70" align="center" />
        <el-table-column label="头像" width="70" align="center">
          <template #default="{ row }">
            <el-avatar :size="32" :src="row.avatarUrl || undefined">
              {{ (row.nickname || '?').charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column
          prop="nickname"
          label="昵称"
          min-width="120"
          show-overflow-tooltip
        />
        <el-table-column
          prop="email"
          label="邮箱"
          min-width="170"
          show-overflow-tooltip
        />
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">
            <span>{{ row.phone || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="userLevel" label="等级" width="70" align="center" />
        <el-table-column prop="totalPoints" label="积分" width="80" align="center" />
        <el-table-column label="实名认证" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.realAuthFlag === 1 ? 'success' : 'info'" size="small">
              {{ row.realAuthFlag === 1 ? '已认证' : '未认证' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="账号状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.accountStatus === 1 ? 'success' : 'danger'" size="small">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="160">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.registerTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="160">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.lastLoginTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              :type="row.accountStatus === 1 ? 'danger' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.accountStatus === 1 ? '禁用' : '启用' }}
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AdminUserItem } from '@/types/admin'
import { getAdminUsersApi, updateUserStatusApi } from '@/api/admin'

// 表格加载态
const loading = ref(false)

// 当前页用户列表
const users = ref<AdminUserItem[]>([])

// 总记录数（后端分页）
const total = ref(0)

// 查询与分页参数
const query = reactive<{
  /** 关键字：昵称模糊 或 邮箱精确 */
  keyword: string
  /** 账号状态筛选：0/1，undefined为全部 */
  accountStatus: number | undefined
  /** 页码 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
}>({
  keyword: '',
  accountStatus: undefined,
  pageNum: 1,
  pageSize: 10
})

// ==================== 数据加载 ====================

// 分页加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getAdminUsersApi({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: query.keyword.trim() || undefined,
      accountStatus: query.accountStatus
    })
    users.value = res.data?.list ?? []
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
  loadUsers()
}

// 重置查询条件
const handleReset = () => {
  query.keyword = ''
  query.accountStatus = undefined
  query.pageNum = 1
  loadUsers()
}

// 每页条数变化
const handleSizeChange = (size: number) => {
  query.pageSize = size
  query.pageNum = 1
  loadUsers()
}

// 页码变化
const handleCurrentChange = (page: number) => {
  query.pageNum = page
  loadUsers()
}

// ==================== 启用/禁用 ====================

// 启用/禁用账号（禁用时后端同步删除Redis登录Token，在线用户立即掉线）
const handleToggleStatus = async (row: AdminUserItem) => {
  const target = row.accountStatus === 1 ? 0 : 1
  const actionText = target === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(
      target === 0
        ? `禁用后该用户在线登录将立即失效，确定禁用「${row.nickname}」吗？`
        : `确定启用「${row.nickname}」账号吗？`,
      `${actionText}账号`,
      {
        type: 'warning',
        confirmButtonText: `确定${actionText}`,
        cancelButtonText: '取消'
      }
    )
  } catch {
    return // 用户取消操作
  }
  try {
    await updateUserStatusApi(row.userId, { accountStatus: target })
    ElMessage.success(`${actionText}成功`)
    loadUsers()
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
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
  loadUsers()
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

  .toolbar-input {
    width: 230px;
  }

  .toolbar-select {
    width: 140px;
  }
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>

