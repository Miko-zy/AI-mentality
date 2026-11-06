<template>
  <div class="home-content">
    <div class="welcome">
      <h2>欢迎回来，{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}！</h2>
      <p>愿你今天也有好心情，与AI一起开启心灵成长之旅。</p>
    </div>

    <!-- 功能入口统计卡片（点击直达对应页面） -->
    <el-row :gutter="20" class="card-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="router.push('/chat')">
          <div class="stat-icon chat-icon">💬</div>
          <div class="stat-info">
            <div class="stat-number">AI对话</div>
            <div class="stat-label">随时倾诉 · 陪伴守护</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="router.push('/scale')">
          <div class="stat-icon assess-icon">📝</div>
          <div class="stat-info">
            <div class="stat-number">心理评估</div>
            <div class="stat-label">韧性测评 · 生成报告</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="router.push('/training')">
          <div class="stat-icon course-icon">📚</div>
          <div class="stat-info">
            <div class="stat-number">训练课程</div>
            <div class="stat-label">小节学习 · 每日打卡</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover" @click="router.push('/profile')">
          <div class="stat-icon badge-icon">🏆</div>
          <div class="stat-info">
            <div class="stat-number">个人中心</div>
            <div class="stat-label">资料维护 · 成长档案</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口说明 -->
    <el-card class="guide-card" shadow="never">
      <template #header>
        <div class="guide-header">
          <el-icon><Guide /></el-icon>
          <span>功能导航</span>
        </div>
      </template>
      <div class="guide-grid">
        <div class="guide-item" @click="router.push('/chat')">
          <span class="guide-emoji">💬</span>
          <div class="guide-text">
            <p class="guide-name">AI陪伴对话</p>
            <p class="guide-desc">与AI助手随时聊天，情绪低落时有人倾听</p>
          </div>
        </div>
        <div class="guide-item" @click="router.push('/scale')">
          <span class="guide-emoji">📝</span>
          <div class="guide-text">
            <p class="guide-name">心理韧性评估</p>
            <p class="guide-desc">完成量表作答，查看专业测评报告</p>
          </div>
        </div>
        <div class="guide-item" @click="router.push('/training')">
          <span class="guide-emoji">📚</span>
          <div class="guide-text">
            <p class="guide-name">韧性训练课程</p>
            <p class="guide-desc">分小节学习心理训练，坚持每日打卡成长</p>
          </div>
        </div>
        <div class="guide-item" @click="router.push('/profile')">
          <span class="guide-emoji">👤</span>
          <div class="guide-text">
            <p class="guide-name">个人中心</p>
            <p class="guide-desc">维护个人资料，查看评估记录与训练进度</p>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Guide } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
</script>

<style lang="scss" scoped>
.home-content {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.welcome {
  margin-bottom: 20px;

  h2 {
    font-size: 22px;
    color: #303133;
    margin-bottom: 8px;
  }

  p {
    color: #909399;
    font-size: 14px;
  }
}

// 功能入口卡片
.card-row {
  margin-bottom: 20px;

  .stat-card {
    cursor: pointer;

    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      padding: 20px;
    }

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      margin-right: 16px;

      &.chat-icon {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.assess-icon {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.course-icon {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }

      &.badge-icon {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
      }
    }

    .stat-info {
      .stat-number {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }

      .stat-label {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
}

// 功能导航卡片
.guide-card {
  border-radius: 8px;

  .guide-header {
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 600;
    color: #303133;
  }

  .guide-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 14px;
  }

  .guide-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: #409eff;
      background: #f5f9ff;
    }

    .guide-emoji {
      font-size: 26px;
    }

    .guide-text {
      .guide-name {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 3px;
      }

      .guide-desc {
        font-size: 12px;
        color: #909399;
        line-height: 1.5;
      }
    }
  }
}
</style>
