<template>
  <div class="report-container">
    <div v-loading="pageLoading" class="report-body">
      <template v-if="report">
        <!-- ==================== 报告头部 ==================== -->
        <div class="report-head">
          <div class="head-info">
            <h2 class="report-title">心理韧性评估报告</h2>
            <p class="scale-name">{{ report.scaleName }}</p>
            <p class="finish-time">完成时间：{{ formatDateTime(report.finishTime) }}</p>
          </div>
          <el-button @click="handleBack">
            <el-icon><Back /></el-icon>
            <span>返回量表列表</span>
          </el-button>
        </div>

        <!-- ==================== 总分与等级 ==================== -->
        <div class="score-card">
          <div class="score-left">
            <div class="score-circle">
              <el-progress
                type="dashboard"
                :percentage="report.scorePercent ?? 0"
                :width="170"
                :stroke-width="14"
                color="#409eff"
              />
              <div class="score-circle-text">
                <div class="score-num">
                  {{ report.totalScore }}
                  <span class="score-max">/{{ report.maxScore }}</span>
                </div>
                <div class="score-label">总分</div>
              </div>
            </div>
          </div>
          <div class="score-right">
            <div class="level-row">
              <span class="level-label">评估等级</span>
              <el-tag :type="levelTagType" size="large" effect="dark" class="level-tag">
                {{ report.level }}
              </el-tag>
            </div>
            <div class="percent-row">
              <span class="level-label">得分率</span>
              <span class="percent-value">{{ report.scorePercent }}%</span>
            </div>
            <p class="score-hint">
              本报告基于 {{ report.scaleName }} 自动计分生成，分数越高表示心理韧性水平越好。
            </p>
          </div>
        </div>

        <!-- ==================== 三个维度得分 ==================== -->
        <section class="report-section">
          <h3 class="section-title">维度得分</h3>
          <div class="dimension-list">
            <div
              v-for="dim in report.dimensions"
              :key="dim.dimensionCode"
              class="dimension-item"
            >
              <div class="dim-head">
                <span class="dim-name">{{ dim.dimensionName }}</span>
                <span class="dim-score">{{ dim.score }} / {{ dim.maxScore }}</span>
              </div>
              <el-progress
                :percentage="dimensionPercent(dim)"
                :stroke-width="12"
                :color="dimensionColor(dim)"
              />
            </div>
          </div>
        </section>

        <!-- ==================== 评估解读 ==================== -->
        <section class="report-section">
          <h3 class="section-title">评估解读</h3>
          <div class="text-block">{{ report.interpretation || '暂无解读内容' }}</div>
        </section>

        <!-- ==================== 成长建议 ==================== -->
        <section class="report-section">
          <h3 class="section-title">成长建议</h3>
          <div class="text-block advice-block">{{ report.advice || '暂无建议内容' }}</div>
        </section>

        <!-- ==================== 底部操作 ==================== -->
        <div class="report-footer">
          <el-button type="primary" @click="handleBack">返回量表列表</el-button>
        </div>
      </template>

      <!-- 报告加载失败时的占位 -->
      <el-empty
        v-if="!pageLoading && !report"
        description="未找到该评估报告，请确认记录已完成"
      >
        <el-button type="primary" @click="handleBack">返回量表列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getReportApi } from '@/api/scale'
import { AssessmentReport, DimensionScore } from '@/types/scale'

const router = useRouter()
const route = useRoute()

// 页面加载态
const pageLoading = ref(false)

// 评估报告数据
const report = ref<AssessmentReport | null>(null)

// 评估记录ID（路由参数 /scale/report/:recordId）
const recordId = computed<number>(() => Number(route.params.recordId))

// ==================== 评估等级标签颜色 ====================
// 后端等级文案：高韧性 / 中等韧性 / 低韧性
const levelTagType = computed<'success' | 'warning' | 'danger' | 'info'>(() => {
  const level = report.value?.level ?? ''
  if (level.includes('高')) {
    return 'success'
  }
  if (level.includes('中')) {
    return 'warning'
  }
  if (level.includes('低')) {
    return 'danger'
  }
  return 'info'
})

// ==================== 维度得分计算 ====================

/**
 * 计算单维度得分率百分比（0-100）
 *
 * @param dim 维度得分
 * @returns 百分比整数
 */
const dimensionPercent = (dim: DimensionScore): number => {
  if (!dim.maxScore || dim.maxScore <= 0) {
    return 0
  }
  return Math.min(100, Math.round((dim.score / dim.maxScore) * 100))
}

/**
 * 根据维度得分率返回进度条颜色
 *
 * @param dim 维度得分
 * @returns 颜色值
 */
const dimensionColor = (dim: DimensionScore): string => {
  const percent = dimensionPercent(dim)
  if (percent >= 70) {
    return '#67c23a'
  }
  if (percent >= 40) {
    return '#e6a23c'
  }
  return '#f56c6c'
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

// ==================== 加载与跳转 ====================

// 加载评估报告
const loadReport = async () => {
  if (!recordId.value || Number.isNaN(recordId.value)) {
    ElMessage.error('评估记录参数缺失')
    router.replace('/scale')
    return
  }
  pageLoading.value = true
  try {
    const res = await getReportApi(recordId.value)
    report.value = res.data ?? null
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理，报告为空时展示空状态
  } finally {
    pageLoading.value = false
  }
}

// 返回量表列表页（历史记录在同一页面）
const handleBack = () => {
  router.push('/scale')
}

// 首次进入加载报告
onMounted(() => {
  loadReport()
})
</script>

<style lang="scss" scoped>
.report-container {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.report-body {
  min-height: 400px;
}

// ==================== 报告头部 ====================

.report-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;

  .report-title {
    font-size: 22px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 6px;
  }

  .scale-name {
    font-size: 15px;
    color: #606266;
    margin-bottom: 4px;
  }

  .finish-time {
    font-size: 13px;
    color: #909399;
  }
}

// ==================== 总分卡片 ====================

.score-card {
  display: flex;
  align-items: center;
  gap: 40px;
  padding: 30px 36px;
  background: linear-gradient(135deg, #ecf5ff 0%, #f7fbff 100%);
  border: 1px solid #d9ecff;
  border-radius: 14px;
  margin-bottom: 24px;

  .score-left {
    flex-shrink: 0;
  }

  .score-circle {
    position: relative;
    width: 170px;
    height: 170px;
  }

  .score-circle-text {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;

    .score-num {
      font-size: 34px;
      font-weight: 700;
      color: #303133;

      .score-max {
        font-size: 16px;
        font-weight: 400;
        color: #909399;
      }
    }

    .score-label {
      font-size: 13px;
      color: #909399;
      margin-top: 2px;
    }
  }

  .score-right {
    flex: 1;
    min-width: 0;
  }

  .level-row,
  .percent-row {
    display: flex;
    align-items: center;
    gap: 14px;
    margin-bottom: 14px;
  }

  .level-label {
    font-size: 14px;
    color: #909399;
    width: 60px;
    flex-shrink: 0;
  }

  .level-tag {
    font-size: 16px;
    height: 34px;
    line-height: 34px;
    padding: 0 18px;
  }

  .percent-value {
    font-size: 18px;
    font-weight: 600;
    color: #409eff;
  }

  .score-hint {
    font-size: 12px;
    color: #909399;
    line-height: 1.7;
    margin-top: 4px;
  }
}

// ==================== 通用区块 ====================

.report-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 14px;
  padding-left: 10px;
  border-left: 4px solid #409eff;
}

// ==================== 维度得分 ====================

.dimension-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.dimension-item {
  padding: 18px 20px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;

  .dim-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    .dim-name {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
    }

    .dim-score {
      font-size: 14px;
      color: #606266;
      font-weight: 600;
    }
  }
}

// ==================== 解读与建议文本 ====================

.text-block {
  padding: 20px 24px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  font-size: 14px;
  color: #606266;
  line-height: 1.9;
  white-space: pre-wrap;
  word-break: break-word;
}

.advice-block {
  background: #f5fbf4;
  border-color: #d9efe2;
}

// ==================== 底部操作 ====================

.report-footer {
  display: flex;
  justify-content: center;
  margin-top: 32px;

  .el-button {
    min-width: 160px;
    height: 44px;
  }
}

// ==================== 响应式 ====================

@media screen and (max-width: 768px) {
  .score-card {
    flex-direction: column;
    gap: 20px;
    padding: 24px 16px;
    text-align: center;

    .level-row,
    .percent-row {
      justify-content: center;
    }
  }
}
</style>

