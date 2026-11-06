<template>
  <el-container class="admin-layout">
    <!-- ==================== 左侧导航 ==================== -->
    <el-aside width="220px" class="admin-aside">
      <div class="logo">
        <el-icon :size="20"><Setting /></el-icon>
        <span class="logo-text">管理后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        class="admin-menu"
      >
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/scales">
          <el-icon><DataLine /></el-icon>
          <span>量表管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/courses">
          <el-icon><Reading /></el-icon>
          <span>课程管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/crisis">
          <el-icon><Warning /></el-icon>
          <span>安全危机事件</span>
        </el-menu-item>
        <el-menu-item index="/admin/feedback">
          <el-icon><ChatDotRound /></el-icon>
          <span>反馈处理</span>
        </el-menu-item>
        <el-menu-item index="/admin/notices">
          <el-icon><Bell /></el-icon>
          <span>系统公告</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- ==================== 右侧内容 ==================== -->
    <el-container class="admin-right">
      <el-header height="56px" class="admin-header">
        <span class="header-title">AI陪伴与心理韧性成长平台</span>
        <div class="header-right">
          <span class="header-user">
            <el-icon><UserFilled /></el-icon>
            <span>{{ displayUserName }}</span>
          </span>
          <el-button link class="header-btn" @click="goFront">返回前台</el-button>
          <el-button link class="header-btn" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前高亮菜单（子路由路径，如 /admin/users）
const activeMenu = computed<string>(() => route.path)

// 展示的当前管理员名称（昵称优先，其次账号名）
const displayUserName = computed<string>(() => {
  const info = userStore.userInfo
  return info?.nickname || info?.username || '管理员'
})

// 返回前台首页
const goFront = () => {
  router.push('/')
}

// 退出登录：清理本地凭证并跳转登录页
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.admin-layout {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

// ==================== 左侧导航 ====================

.admin-aside {
  background: #304156;
  overflow-y: auto;
  overflow-x: hidden;

  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    height: 56px;
    background: #263445;
    color: #fff;

    .logo-text {
      font-size: 16px;
      font-weight: 600;
      letter-spacing: 2px;
    }
  }

  .admin-menu {
    border-right: none;

    .el-menu-item {
      &.is-active {
        background-color: #409eff;
        color: #fff;
      }
    }
  }
}

// ==================== 右侧头部 ====================

.admin-right {
  background: #f0f2f5;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 0 20px;

  .header-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  .header-user {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 13px;
    color: #606266;
  }

  .header-btn {
    font-size: 13px;
    color: #409eff;
  }
}

// ==================== 内容区 ====================

.admin-main {
  padding: 20px;
  overflow-y: auto;
}
</style>
