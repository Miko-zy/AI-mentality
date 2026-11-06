<template>
  <el-dialog
    :model-value="modelValue"
    title="每日打卡签到"
    width="480px"
    class="checkin-dialog"
    :close-on-click-modal="!submitting"
    @update:model-value="emit('update:modelValue', $event)"
    @open="handleOpen"
  >
    <div v-loading="loading" class="checkin-body">
      <!-- ==================== 打卡统计 ==================== -->
      <div class="stats-row">
        <div class="stat-item">
          <div class="stat-num">{{ status?.streakDays ?? 0 }}</div>
          <div class="stat-label">连续打卡(天)</div>
        </div>
        <div class="stat-item">
          <div class="stat-num">{{ status?.totalDays ?? 0 }}</div>
          <div class="stat-label">累计打卡(天)</div>
        </div>
        <div class="stat-item">
          <div class="stat-num">{{ status?.totalPoints ?? 0 }}</div>
          <div class="stat-label">总积分</div>
        </div>
      </div>

      <!-- ==================== 未打卡：心情输入 ==================== -->
      <template v-if="!checked">
        <el-input
          v-model="moodDesc"
          type="textarea"
          :rows="3"
          :maxlength="200"
          show-word-limit
          placeholder="今天心情如何？记录一下吧（可选，200字以内）"
        />
        <p class="checkin-tip">每日打卡可获得积分，连续打卡可解锁成就徽章</p>
      </template>

      <!-- ==================== 已打卡提示 ==================== -->
      <div v-else class="checked-box">
        <el-icon class="checked-icon"><CircleCheckFilled /></el-icon>
        <div class="checked-info">
          <p class="checked-title">今日已打卡</p>
          <p v-if="moodText" class="checked-mood">今日心情：{{ moodText }}</p>
        </div>
      </div>

      <!-- ==================== 打卡成功结果：积分与新解锁徽章 ==================== -->
      <div v-if="result" class="result-box">
        <p class="result-points">
          打卡成功，获得 <span class="points-num">+{{ result.checkinPoints }}</span> 积分
        </p>
        <template v-if="result.newBadges.length > 0">
          <p class="result-sub">新解锁徽章</p>
          <div class="badge-grid">
            <div v-for="badge in result.newBadges" :key="badge.badgeId" class="badge-item">
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
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- ==================== 底部操作 ==================== -->
    <template #footer>
      <el-button :disabled="submitting" @click="handleClose">关闭</el-button>
      <el-button
        type="primary"
        :loading="submitting"
        :disabled="checked || loading"
        @click="handleCheckin"
      >
        {{ checked ? '今日已打卡' : '立即打卡' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { CheckinResult } from '@/types/training'
import { checkinApi, getCheckinStatusApi } from '@/api/training'

// ==================== 组件契约 ====================

// 父组件通过 v-model 控制弹窗显示（模板内可直接使用 modelValue）
defineProps<{
  /** 弹窗是否可见 */
  modelValue: boolean
}>()

// 关闭弹窗通知父组件更新v-model；打卡成功通知父组件刷新统计与徽章
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

// ==================== 状态 ====================

// 今日打卡状态加载中
const loading = ref(false)

// 打卡提交中
const submitting = ref(false)

// 今日打卡状态（GET /checkin）
const status = ref<CheckinResult | null>(null)

// 本次打卡结果（POST /checkin 成功后展示积分与新徽章）
const result = ref<CheckinResult | null>(null)

// 心情描述输入
const moodDesc = ref('')

// 今日是否已打卡（状态查询或本次打卡成功后为true）
const checked = computed<boolean>(() => status.value?.todayChecked ?? false)

// 展示的心情文案：本次打卡结果优先，其次为状态查询返回值
const moodText = computed<string>(() => {
  const fromResult = result.value?.moodDesc
  if (fromResult) {
    return fromResult
  }
  return status.value?.moodDesc ?? ''
})

// ==================== 打卡逻辑 ====================

// 弹窗打开时：重置结果并加载今日打卡状态
const handleOpen = async () => {
  result.value = null
  moodDesc.value = ''
  loading.value = true
  try {
    const res = await getCheckinStatusApi()
    status.value = res.data ?? null
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    loading.value = false
  }
}

// 提交打卡：心情描述可选，空值不传该字段
const handleCheckin = async () => {
  if (submitting.value || checked.value) {
    return
  }
  const mood = moodDesc.value.trim()
  submitting.value = true
  try {
    const res = await checkinApi(mood ? { moodDesc: mood } : {})
    if (res.data) {
      status.value = res.data
      result.value = res.data
      // 通知父组件刷新打卡统计与徽章列表
      emit('success')
    }
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理（如：今日已打卡，请勿重复打卡）
  } finally {
    submitting.value = false
  }
}

// 关闭弹窗
const handleClose = () => {
  if (submitting.value) {
    return
  }
  emit('update:modelValue', false)
}
</script>

<style lang="scss" scoped>
.checkin-body {
  min-height: 160px;
}

// ==================== 打卡统计 ====================

.stats-row {
  display: flex;
  justify-content: space-around;
  padding: 16px 8px;
  background: #f5f7fa;
  border-radius: 10px;
  margin-bottom: 16px;

  .stat-item {
    text-align: center;
  }

  .stat-num {
    font-size: 24px;
    font-weight: 700;
    color: #409eff;
  }

  .stat-label {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}

.checkin-tip {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 8px;
  text-align: center;
}

// ==================== 已打卡提示 ====================

.checked-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f0f9eb;
  border: 1px solid #d9f2d2;
  border-radius: 10px;

  .checked-icon {
    font-size: 34px;
    color: #67c23a;
    flex-shrink: 0;
  }

  .checked-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 4px;
  }

  .checked-mood {
    font-size: 13px;
    color: #909399;
    line-height: 1.6;
    word-break: break-all;
  }
}

// ==================== 打卡结果 ====================

.result-box {
  margin-top: 16px;
  padding: 16px;
  background: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 10px;

  .result-points {
    font-size: 15px;
    color: #303133;
    text-align: center;
    margin-bottom: 4px;

    .points-num {
      font-size: 22px;
      font-weight: 700;
      color: #e6a23c;
    }
  }

  .result-sub {
    font-size: 13px;
    font-weight: 600;
    color: #606266;
    text-align: center;
    margin-top: 12px;
    margin-bottom: 10px;
  }
}

// ==================== 徽章展示 ====================

.badge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
}

.badge-item {
  text-align: center;
  padding: 12px 8px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;

  .badge-icon-wrap {
    width: 48px;
    height: 48px;
    margin: 0 auto 8px;
  }

  .badge-icon {
    width: 48px;
    height: 48px;
  }

  .badge-icon-fallback {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #fdf6ec;
    border-radius: 50%;
    color: #e6a23c;
    font-size: 24px;
  }

  .badge-name {
    font-size: 13px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .badge-desc {
    font-size: 11px;
    color: #909399;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
  }
}
</style>
