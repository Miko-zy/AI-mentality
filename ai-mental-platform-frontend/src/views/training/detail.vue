<template>
  <div class="detail-container">
    <div v-loading="pageLoading" class="detail-body">
      <template v-if="detail">
        <!-- ==================== 顶部：返回 + 课程信息 + 学习进度 ==================== -->
        <div class="detail-header">
          <div class="header-left">
            <el-button link class="back-btn" @click="handleBack">
              <el-icon><Back /></el-icon>
              <span>返回课程列表</span>
            </el-button>
            <h2 class="course-name">{{ detail.courseName }}</h2>
            <el-tag
              v-if="task"
              :type="task.finishStatus === 1 ? 'success' : 'primary'"
              size="small"
            >
              {{ task.finishStatusText }}
            </el-tag>
          </div>
          <div v-if="task" class="header-progress">
            <span class="progress-text">
              已学 {{ task.finishedSection }}/{{ task.totalSection }} 节
            </span>
            <el-progress
              class="progress-bar"
              :percentage="finishRate"
              :stroke-width="8"
            />
            <span class="duration-text">
              <el-icon><Clock /></el-icon>
              <span>累计 {{ formatDuration(task.studyDuration) }}</span>
            </span>
          </div>
        </div>

        <!-- ==================== 学习主体：左侧小节导航 + 右侧图文内容 ==================== -->
        <div class="learn-layout">
          <aside class="section-nav">
            <p class="nav-head">课程小节</p>
            <div
              v-for="(section, index) in detail.sections"
              :key="section.sectionId"
              class="nav-item"
              :class="{
                active: index === currentIndex,
                finished: task !== null && index < task.finishedSection
              }"
              @click="handleSwitchSection(index)"
            >
              <span class="nav-index">
                <el-icon v-if="task !== null && index < task.finishedSection && index !== currentIndex">
                  <Check />
                </el-icon>
                <template v-else>{{ index + 1 }}</template>
              </span>
              <span class="nav-title-text">{{ section.sectionTitle }}</span>
            </div>
            <p v-if="detail.sections.length === 0" class="nav-empty">暂无小节</p>
          </aside>

          <main class="section-content">
            <template v-if="currentSection">
              <div class="content-head">
                <h3 class="content-title">
                  第{{ currentIndex + 1 }}节 · {{ currentSection.sectionTitle }}
                </h3>
                <el-tag
                  v-if="currentSection.studyMinutes"
                  size="small"
                  type="info"
                >
                  建议学习 {{ currentSection.studyMinutes }} 分钟
                </el-tag>
              </div>

              <!-- 图文课程内容（纯文本保留换行） -->
              <div class="content-text">
                {{ currentSection.sectionContent || '本小节暂无正文内容' }}
              </div>

              <!-- 底部小节切换 -->
              <div class="content-footer">
                <el-button
                  :disabled="currentIndex === 0"
                  @click="handleSwitchSection(currentIndex - 1)"
                >
                  <el-icon><ArrowLeft /></el-icon>
                  <span>上一节</span>
                </el-button>
                <span class="section-counter">
                  {{ currentIndex + 1 }} / {{ detail.sections.length }}
                </span>
                <el-button
                  v-if="!isLastSection"
                  type="primary"
                  @click="handleSwitchSection(currentIndex + 1)"
                >
                  <span>下一节</span>
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
                <el-button v-else type="success" plain disabled>
                  已学完全部小节
                </el-button>
              </div>
            </template>

            <!-- 课程无小节时的空状态 -->
            <el-empty
              v-else
              description="该课程暂无小节内容"
            />
          </main>
        </div>
      </template>

      <!-- 课程加载失败（不存在或已下架）时的空状态 -->
      <el-empty
        v-if="!pageLoading && !detail"
        description="课程不存在或已下架"
      >
        <el-button type="primary" @click="handleBack">返回课程列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { BadgeItem, CourseDetail, CourseSection, TrainingTask } from '@/types/training'
import { createCourseTaskApi, getCourseDetailApi, updateProgressApi } from '@/api/training'

const route = useRoute()
const router = useRouter()

// 页面初始化加载态（取课程详情与训练任务）
const pageLoading = ref(false)

// 课程详情（含小节列表）
const detail = ref<CourseDetail | null>(null)

// 训练任务（学习进度），创建任务与每次进度上报后由后端返回更新
const task = ref<TrainingTask | null>(null)

// 当前小节索引
const currentIndex = ref(0)

// 当前小节进入时间戳（毫秒），用于计算本节停留时长
let sectionEnterAt = 0

// ==================== 计算属性 ====================

// 课程ID（路由参数 /training/detail/:courseId）
const courseId = computed<number>(() => Number(route.params.courseId))

// 小节列表
const sections = computed<CourseSection[]>(() => detail.value?.sections ?? [])

// 当前小节
const currentSection = computed<CourseSection | null>(
  () => sections.value[currentIndex.value] ?? null
)

// 是否最后一小节
const isLastSection = computed<boolean>(
  () => sections.value.length > 0 && currentIndex.value >= sections.value.length - 1
)

// 课程完成率（后端保留2位小数，展示取0-100整数）
const finishRate = computed<number>(() =>
  Math.min(100, Math.round(Number(task.value?.finishRate ?? 0)))
)

// ==================== 进度上报 ====================

/**
 * 新解锁徽章系统通知（展示徽章）
 *
 * @param newBadges 本次操作新解锁的徽章列表
 */
const notifyNewBadges = (newBadges: BadgeItem[]) => {
  // 最多弹3条，避免连续学习时通知刷屏
  for (const badge of newBadges.slice(0, 3)) {
    ElNotification({
      title: '解锁新徽章',
      message: `${badge.badgeName}：${badge.description}`,
      type: 'success',
      duration: 6000
    })
  }
}

/**
 * 上报某小节的学习进度（静默，失败提示由拦截器统一处理）
 * 后端按"最远到达位置只进不退"更新完成率，并累计学习时长、判定徽章解锁
 *
 * @param sectionId     学习的小节ID
 * @param elapsedSeconds 本次停留时长（秒，自动限制在0-7200）
 * @returns 后端返回的最新训练任务（不使用await场景可忽略）
 */
const reportProgress = async (
  sectionId: number,
  elapsedSeconds: number
): Promise<void> => {
  if (!task.value) {
    return // 训练任务创建失败时仅浏览，不上报
  }
  const duration = Math.max(0, Math.min(7200, Math.round(elapsedSeconds)))
  try {
    const res = await updateProgressApi(task.value.progressId, {
      sectionId,
      studyDuration: duration
    })
    if (res.data) {
      const newBadges = res.data.newBadges ?? []
      task.value = res.data
      if (newBadges.length > 0) {
        notifyNewBadges(newBadges)
      }
    }
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  }
}

// ==================== 小节切换 ====================

// 切换小节：先上报离开小节的停留时长，再切换展示新小节
const handleSwitchSection = (index: number) => {
  const list = sections.value
  if (index === currentIndex.value || index < 0 || index >= list.length) {
    return
  }
  const prevSection = currentSection.value
  if (prevSection && sectionEnterAt > 0) {
    const elapsedSeconds = (Date.now() - sectionEnterAt) / 1000
    // 静默上报上一小节（后端只进不退，快速切换也不会回退进度）
    void reportProgress(prevSection.sectionId, elapsedSeconds)
  }
  currentIndex.value = index
  sectionEnterAt = Date.now()
}

// 返回课程列表（离开守卫会自动上报当前小节进度）
const handleBack = () => {
  router.push('/training')
}

// ==================== 初始化 ====================

// 页面初始化：取课程详情 -> 创建/获取训练任务（幂等）-> 恢复学习位置
const initPage = async () => {
  if (!courseId.value || Number.isNaN(courseId.value)) {
    ElMessage.error('课程参数缺失')
    router.replace('/training')
    return
  }
  pageLoading.value = true
  try {
    const detailRes = await getCourseDetailApi(courseId.value)
    detail.value = detailRes.data ?? null
    if (!detail.value) {
      return // 课程不存在或已下架，展示空状态
    }

    // 选择课程生成训练任务（已有任务直接返回，不重置进度）；失败仍可浏览小节
    try {
      const taskRes = await createCourseTaskApi(courseId.value)
      task.value = taskRes.data ?? null
    } catch {
      task.value = null
    }

    // 恢复学习位置：finishedSection 为最远到达的1-based位置
    const finished = task.value?.finishedSection ?? 0
    const total = sections.value.length
    currentIndex.value =
      total > 0 ? Math.min(Math.max(finished - 1, 0), total - 1) : 0
    sectionEnterAt = Date.now()
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
    detail.value = null
  } finally {
    pageLoading.value = false
  }
}

// ==================== 工具方法 ====================

/**
 * 把累计学习时长（秒）格式化为"x小时x分钟"
 *
 * @param seconds 秒数
 * @returns 可读时长文本
 */
const formatDuration = (seconds: number | null | undefined): string => {
  const total = seconds ?? 0
  const hours = Math.floor(total / 3600)
  const minutes = Math.floor((total % 3600) / 60)
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  }
  return `${minutes}分钟`
}

// ==================== 生命周期与离开守卫 ====================

onMounted(() => {
  initPage()
})

// 离开页面（返回列表/刷新/跳转）时上报当前小节的停留时长与位置
onBeforeRouteLeave(() => {
  const section = currentSection.value
  if (section && sectionEnterAt > 0 && task.value) {
    const elapsedSeconds = (Date.now() - sectionEnterAt) / 1000
    void reportProgress(section.sectionId, elapsedSeconds)
  }
  return true
})
</script>

<style lang="scss" scoped>
.detail-container {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 16px 16px 48px;
}

.detail-body {
  min-height: 480px;
}

// ==================== 顶部栏 ====================

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 8px 0 16px;
  flex-wrap: wrap;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.back-btn {
  color: #909399;
  font-size: 14px;

  &:hover {
    color: #409eff;
  }
}

.course-name {
  font-size: 19px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-progress {
  display: flex;
  align-items: center;
  gap: 12px;

  .progress-text {
    font-size: 13px;
    color: #606266;
    white-space: nowrap;
  }

  .progress-bar {
    width: 160px;
  }

  .duration-text {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #909399;
    white-space: nowrap;
  }
}

// ==================== 学习布局 ====================

.learn-layout {
  display: flex;
  gap: 18px;
  align-items: flex-start;
}

// ---------- 左侧小节导航 ----------

.section-nav {
  width: 240px;
  flex-shrink: 0;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 14px 10px;
  position: sticky;
  top: 16px;

  .nav-head {
    font-size: 13px;
    font-weight: 600;
    color: #909399;
    padding: 0 10px 10px;
    border-bottom: 1px solid #f0f2f5;
    margin-bottom: 8px;
  }

  .nav-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 10px;
    border-radius: 8px;
    cursor: pointer;
    transition: background 0.15s;

    &:hover {
      background: #f5f7fa;
    }

    &.active {
      background: #ecf5ff;

      .nav-index {
        background: #409eff;
        color: #fff;
        border-color: #409eff;
      }

      .nav-title-text {
        color: #409eff;
        font-weight: 600;
      }
    }

    &.finished .nav-title-text {
      color: #909399;
    }

    .nav-index {
      width: 24px;
      height: 24px;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 1px solid #dcdfe6;
      border-radius: 50%;
      font-size: 12px;
      color: #909399;
      background: #fff;
    }

    &.finished .nav-index {
      background: #f0f9eb;
      border-color: #b3e19d;
      color: #67c23a;
    }

    .nav-title-text {
      font-size: 13px;
      color: #606266;
      line-height: 1.5;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
    }
  }

  .nav-empty {
    font-size: 13px;
    color: #c0c4cc;
    text-align: center;
    padding: 14px 0;
  }
}

// ---------- 右侧内容区 ----------

.section-content {
  flex: 1;
  min-width: 0;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 24px 28px;
}

.content-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid #f0f2f5;

  .content-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    line-height: 1.6;
  }
}

.content-text {
  font-size: 15px;
  color: #303133;
  line-height: 2;
  white-space: pre-wrap;
  word-break: break-word;
  min-height: 240px;
}

// ---------- 底部切换 ----------

.content-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 26px;
  padding-top: 18px;
  border-top: 1px solid #f0f2f5;

  .section-counter {
    font-size: 13px;
    color: #909399;
  }

  .el-button {
    min-width: 116px;
    height: 40px;
  }
}

// ==================== 响应式 ====================

@media screen and (max-width: 900px) {
  .learn-layout {
    flex-direction: column;

    .section-nav {
      width: 100%;
      position: static;
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      padding: 12px;

      .nav-head {
        width: 100%;
        padding-bottom: 8px;
        margin-bottom: 4px;
      }

      .nav-item {
        padding: 8px 10px;
        background: #f5f7fa;

        .nav-title-text {
          -webkit-line-clamp: 1;
          white-space: nowrap;
        }
      }
    }

    .section-content {
      width: 100%;
      padding: 18px 16px;
    }
  }

  .detail-header {
    .header-progress {
      width: 100%;

      .progress-bar {
        flex: 1;
      }
    }
  }
}
</style>

