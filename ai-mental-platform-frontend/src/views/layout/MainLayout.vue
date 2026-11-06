<template>
  <el-container class="main-layout">
    <!-- ==================== 左侧固定导航（悬浮，不随内容滚动） ==================== -->
    <el-aside width="200px" class="layout-aside">
      <div class="logo">
        <span class="logo-emoji">🧠</span>
        <span class="logo-text">心理韧性平台</span>
      </div>
      <el-menu :default-active="activeMenu" router class="layout-menu">
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI陪伴</span>
        </el-menu-item>
        <el-menu-item index="/scale">
          <el-icon><Document /></el-icon>
          <span>心理评估</span>
        </el-menu-item>
        <el-menu-item index="/training">
          <el-icon><Reading /></el-icon>
          <span>训练课程</span>
        </el-menu-item>
        <el-menu-item v-if="userStore.isAdmin" index="/admin">
          <el-icon><Setting /></el-icon>
          <span>管理后台</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- ==================== 右侧：顶栏 + 内容 ==================== -->
    <el-container class="layout-right">
      <el-header height="56px" class="layout-header">
        <span class="header-title">AI陪伴与心理韧性成长平台</span>
        <div class="header-right">
          <el-button link class="header-link" @click="router.push('/')">
            <el-icon><HomeFilled /></el-icon>
            <span>返回主页</span>
          </el-button>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="30" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || '用' }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" command="admin">管理后台</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区独立滚动，各子页面自带居中排版与内边距 -->
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  HomeFilled,
  ChatDotRound,
  Document,
  Reading,
  Setting,
  ArrowDown
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前高亮菜单：
// 评估子页（答题/报告）归到"心理评估"，训练子页（课程详情）归到"训练课程"
const activeMenu = computed<string>(() => {
  const path = route.path
  if (path.startsWith('/scale')) {
    return '/scale'
  }
  if (path.startsWith('/training')) {
    return '/training'
  }
  return path
})

// 顶部用户下拉命令
const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'admin') {
    router.push('/admin')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      })
      .catch(() => {})
  }
}
</script>

<style lang="scss" scoped>
.main-layout {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

// ==================== 左侧固定导航 ====================

.layout-aside {
  background: #304156;
  overflow-y: auto;
  overflow-x: hidden;
  flex-shrink: 0;

  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    height: 56px;
    background: #263445;
    color: #fff;

    .logo-emoji {
      font-size: 20px;
    }

    .logo-text {
      font-size: 15px;
      font-weight: 600;
      letter-spacing: 1px;
    }
  }

  .layout-menu {
    border-right: none;

    .el-menu-item {
      &.is-active {
        background-color: #409eff;
        color: #fff;
      }
    }
  }
}

// ==================== 顶栏 ====================

.layout-right {
  background: #f5f7fa;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
  flex-shrink: 0;

  .header-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .header-link {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #409eff;
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    color: #606266;

    .username {
      font-size: 14px;
      max-width: 160px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

// ==================== 内容区（独立滚动） ====================

.layout-main {
  padding: 0;
  overflow-y: auto;
}
</style>
