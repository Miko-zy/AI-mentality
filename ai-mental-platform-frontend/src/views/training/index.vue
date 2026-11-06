<template>
  <div class="training-container">
    <div class="page-header">
      <div class="header-text">
        <h2 class="page-title">心理韧性训练</h2>
        <p class="page-desc">选择训练课程开始学习，坚持每日打卡解锁成就徽章</p>
      </div>
      <div class="header-action">
        <div class="checkin-stats" v-if="checkinStatus">
          <div class="stat-item">
            <span class="stat-num">{{ checkinStatus.streakDays }}</span>
            <span class="stat-label">连续(天)</span>
          </div>
          <div class="stat-item">
            <span class="stat-num">{{ checkinStatus.totalDays }}</span>
            <span class="stat-label">累计(天)</span>
          </div>
          <div class="stat-item">
            <span class="stat-num">{{ checkinStatus.totalPoints }}</span>
            <span class="stat-label">积分</span>
          </div>
        </div>
        <el-button
          :type="isTodayChecked ? 'success' : 'primary'"
          class="checkin-btn"
          @click="checkinVisible = true"
        >
          <el-icon v-if="isTodayChecked"><CircleCheckFilled /></el-icon>
          <el-icon v-else><Calendar /></el-icon>
          <span>{{ isTodayChecked ? '今日已打卡' : '每日打卡' }}</span>
        </el-button>
      </div>
    </div>

    <!-- ==================== 课程列表 ==================== -->
    <section class="training-section">
      <h3 class="section-title">训练课程</h3>
      <div v-loading="coursesLoading" class="course-grid">
        <div
          v-for="course in courses"
          :key="course.courseId"
          class="course-card"
          @click="handleEnterCourse(course)"
        >
          <div class="course-cover">
            <el-image
              v-if="course.coverUrl"
              :src="course.coverUrl"
              fit="cover"
              class="cover-img"
            >
              <template #error>
                <div class="cover-fallback"><el-icon><Picture /></el-icon></div>
              </template>
            </el-image>
            <div v-else class="cover-fallback"><el-icon><Picture /></el-icon></div>
            <el-tag
              v-if="taskMap[course.courseId]"
              :type="taskMap[course.courseId].finishStatus === 1 ? 'success' : 'primary'"
              size="small"
              effect="dark"
              class="cover-tag"
            >
              {{ taskMap[course.courseId].finishStatusText }}
            </el-tag>
          </div>
          <div class="course-body">
            <h4 class="course-name">{{ course.courseName }}</h4>
            <p class="course-desc">{{ course.courseDesc || '暂无课程简介' }}</p>
            <div class="course-meta">
              <span class="meta-item">
                <el-icon><Files /></el-icon>
                <span>{{ course.totalSection }} 个小节</span>
              </span>
              <span
                v-if="taskMap[course.courseId]"
                class="meta-item"
              >
                <el-icon><Clock /></el-icon>
                <span>{{ formatDuration(taskMap[course.courseId].studyDuration) }}</span>
              </span>
            </div>
            <!-- 已有学习任务时展示进度 -->
            <template v-if="taskMap[course.courseId]">
              <div class="course-progress-row">
                <span class="progress-text">
                  已学 {{ taskMap[course.courseId].finishedSection }}/{{ taskMap[course.courseId].totalSection }} 节
                </span>
                <span class="progress-rate">{{ Number(taskMap[course.courseId].finishRate) }}%</span>
              </div>
              <el-progress
                :percentage="progressOf(course.courseId)"
                :stroke-width="8"
                :show-text="false"
              />
            </template>
            <el-button type="primary" class="enter-btn">
              {{ taskMap[course.courseId] ? '继续学习' : '开始学习' }}
            </el-button>
          </div>
        </div>
      </div>
      <el-empty
        v-if="!coursesLoading && courses.length === 0"
        description="暂无上架课程，请稍后再来"
      />
    </section>

    <!-- ==================== 我的徽章 ==================== -->
    <section class="training-section">
      <h3 class="section-title">我的徽章</h3>
      <div v-loading="badgesLoading" class="badge-grid">
        <div v-for="badge in badges" :key="badge.badgeId" class="badge-card">
          <div class="badge-icon-wrap">
            <el-image
              v-if="badge.badgeIcon"
              :src="badge.badgeIcon"
              fit="contain"
              class="badge-icon"
            >
              <template #error>
                <div class="badge-icon-fallback"><el-icon><Medal /></el-icon></div>
              </template>
            </el-image>
            <div v-else class="badge-icon-fallback"><el-icon><Medal /></el-icon></div>
          </div>
          <p class="badge-name">{{ badge.badgeName }}</p>
          <p class="badge-desc">{{ badge.description }}</p>
          <p class="badge-time">解锁于 {{ formatDateTime(badge.obtainTime) }}</p>
        </div>
      </div>
      <el-empty
        v-if="!badgesLoading && badges.length === 0"
        description="还没有徽章，完成打卡与课程学习即可解锁"
      />
    </section>

    <!-- ==================== 每日打卡弹窗 ==================== -->
    <CheckinDialog v-model="checkinVisible" @success="handleCheckinSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import CheckinDialog from './CheckinDialog.vue'
import { BadgeItem, CheckinResult, CourseItem, TrainingTask } from '@/types/training'
import {
  getCheckinStatusApi,
  getCourseListApi,
  getMyBadgesApi,
  getMyTasksApi
} from '@/api/training'

const router = useRouter()

// ==================== 课程列表 ====================

// 课程列表
const courses = ref<CourseItem[]>([])

// 课程列表加载态
const coursesLoading = ref(false)

// 加载上架课程列表
const loadCourses = async () => {
  coursesLoading.value = true
  try {
    const res = await getCourseListApi()
    courses.value = res.data ?? []
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    coursesLoading.value = false
  }
}

// 点击课程卡片进入课程详情小节学习页
const handleEnterCourse = (course: CourseItem) => {
  router.push(`/training/detail/${course.courseId}`)
}

// ==================== 我的学习任务 ====================

// 我的训练任务列表（GET /course/tasks）
const tasks = ref<TrainingTask[]>([])

// 课程ID -> 训练任务 映射，用于课程卡片展示学习进度
const taskMap = computed<Record<number, TrainingTask>>(() => {
  const map: Record<number, TrainingTask> = {}
  for (const task of tasks.value) {
    map[task.courseId] = task
  }
  return map
})

// 加载我的训练任务
const loadTasks = async () => {
  try {
    const res = await getMyTasksApi()
    tasks.value = res.data ?? []
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  }
}

// 读取课程完成率（0-100整数，后端返回保留2位小数）
const rateOf = (courseId: number): number => {
  const task = taskMap.value[courseId]
  if (!task) {
    return 0
  }
  return Math.min(100, Math.round(Number(task.finishRate)))
}

// el-progress 百分比（与文字保持一致）
const progressOf = (courseId: number): number => rateOf(courseId)

// ==================== 我的徽章 ====================

// 已解锁徽章列表
const badges = ref<BadgeItem[]>([])

// 徽章列表加载态
const badgesLoading = ref(false)

// 加载已解锁徽章
const loadBadges = async () => {
  badgesLoading.value = true
  try {
    const res = await getMyBadgesApi()
    badges.value = res.data ?? []
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    badgesLoading.value = false
  }
}

// ==================== 打卡状态与弹窗 ====================

// 今日打卡状态
const checkinStatus = ref<CheckinResult | null>(null)

// 今日是否已打卡
const isTodayChecked = computed<boolean>(() => checkinStatus.value?.todayChecked ?? false)

// 打卡弹窗显示状态
const checkinVisible = ref(false)

// 加载今日打卡状态
const loadCheckinStatus = async () => {
  try {
    const res = await getCheckinStatusApi()
    checkinStatus.value = res.data ?? null
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  }
}

// 打卡成功后刷新统计与徽章（新徽章已在弹窗内展示）
const handleCheckinSuccess = () => {
  loadCheckinStatus()
  loadBadges()
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

// 首次进入：并行加载课程、任务、徽章、打卡状态
onMounted(() => {
  loadCourses()
  loadTasks()
  loadBadges()
  loadCheckinStatus()
})
</script>

<style lang="scss" scoped>
.training-container {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

// ==================== 页面头部 ====================

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;

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

.header-action {
  display: flex;
  align-items: center;
  gap: 14px;
}

.checkin-stats {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 14px;
  background: #f5f7fa;
  border-radius: 10px;

  .stat-item {
    display: flex;
    align-items: baseline;
    gap: 4px;
    padding: 0 10px;
    border-right: 1px solid #dcdfe6;

    &:last-child {
      border-right: none;
    }
  }

  .stat-num {
    font-size: 17px;
    font-weight: 700;
    color: #409eff;
  }

  .stat-label {
    font-size: 12px;
    color: #909399;
  }
}

.checkin-btn {
  .el-icon {
    margin-right: 6px;
  }
}

// ==================== 区块标题 ====================

.training-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 4px solid #409eff;
}

// ==================== 课程卡片 ====================

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  min-height: 100px;
}

.course-card {
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);

    .enter-btn {
      opacity: 1;
    }
  }

  .course-cover {
    position: relative;
    width: 100%;
    height: 150px;
    background: #f0f2f5;

    .cover-img {
      width: 100%;
      height: 100%;
      display: block;
    }

    .cover-fallback {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #c0c4cc;
      font-size: 36px;
      background: linear-gradient(135deg, #ecf5ff 0%, #f7fbff 100%);
    }

    .cover-tag {
      position: absolute;
      top: 10px;
      right: 10px;
    }
  }

  .course-body {
    display: flex;
    flex-direction: column;
    padding: 16px 18px 18px;
    flex: 1;

    .course-name {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .course-desc {
      font-size: 13px;
      color: #909399;
      line-height: 1.6;
      margin-bottom: 12px;
      flex: 1;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2;
      overflow: hidden;
    }

    .course-meta {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 10px;

      .meta-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #606266;
      }
    }
  }
}

.course-progress-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;

  .progress-text {
    font-size: 12px;
    color: #909399;
  }

  .progress-rate {
    font-size: 12px;
    font-weight: 600;
    color: #409eff;
  }
}

.enter-btn {
  width: 100%;
  margin-top: 14px;
  opacity: 0.9;
  transition: opacity 0.2s;
}

// ==================== 徽章卡片 ====================

.badge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 14px;
  min-height: 80px;
}

.badge-card {
  text-align: center;
  padding: 18px 14px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06);
  }

  .badge-icon-wrap {
    width: 56px;
    height: 56px;
    margin: 0 auto 10px;
  }

  .badge-icon {
    width: 56px;
    height: 56px;
  }

  .badge-icon-fallback {
    width: 56px;
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fdf6ec;
    border-radius: 50%;
    color: #e6a23c;
    font-size: 28px;
  }

  .badge-name {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 6px;
  }

  .badge-desc {
    font-size: 12px;
    color: #909399;
    line-height: 1.6;
    margin-bottom: 8px;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
  }

  .badge-time {
    font-size: 11px;
    color: #c0c4cc;
  }
}
</style>

