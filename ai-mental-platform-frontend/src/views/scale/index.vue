<template>
  <div class="scale-container">
    <div class="page-header">
      <h2 class="page-title">心理韧性评估</h2>
      <p class="page-desc">选择量表开始测评，了解你的心理韧性水平</p>
      <!-- 作答指导语 -->
      <p class="guide-tip">
        请根据过去一个月你的真实情况，对下面每一条描述选择符合程度，答案没有对错之分，请凭第一感受作答。
      </p>
    </div>

    <!-- ==================== 可用量表列表 ==================== -->
    <section class="scale-section">
      <h3 class="section-title">可用评估量表</h3>
      <div v-loading="scalesLoading" class="scale-grid">
        <div v-for="scale in scales" :key="scale.scaleId" class="scale-card">
          <div class="scale-card-head">
            <h4 class="scale-name">{{ scale.scaleName }}</h4>
            <el-tag size="small" type="success">{{ scale.statusText }}</el-tag>
          </div>
          <p class="scale-desc">{{ scale.dimensionDesc || '暂无维度说明' }}</p>
          <div class="scale-meta">
            <span class="meta-item">
              <el-icon><Tickets /></el-icon>
              <span>{{ scale.totalQuestionNum }} 题</span>
            </span>
            <span class="meta-item" v-if="scale.reliability !== null">
              <el-icon><DataLine /></el-icon>
              <span>信度 {{ scale.reliability }}</span>
            </span>
          </div>
          <el-button
            type="primary"
            class="start-btn"
            :disabled="scale.totalQuestionNum === 0"
            @click="handleStart(scale)"
          >
            开始测评
          </el-button>
        </div>
      </div>
      <el-empty
        v-if="!scalesLoading && scales.length === 0"
        description="暂无可用量表，请稍后再来"
      />
    </section>

    <!-- ==================== 历史评估记录 ==================== -->
    <section class="scale-section">
      <h3 class="section-title">历史评估记录</h3>
      <div v-loading="recordsLoading">
        <el-table :data="records" border stripe empty-text="暂无评估记录，完成首次测评后会展示在这里">
          <el-table-column prop="scaleName" label="量表名称" min-width="180" show-overflow-tooltip />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="recordStatusType(row.status)" size="small">
                {{ row.statusText }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="总分" width="100" align="center">
            <template #default="{ row }">
              <span>{{ row.totalScore !== null ? row.totalScore : '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="完成时间" width="180" align="center">
            <template #default="{ row }">
              <span>{{ formatDateTime(row.finishTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 3"
                type="primary"
                link
                @click="handleViewReport(row)"
              >
                查看报告
              </el-button>
              <el-button
                v-else
                type="warning"
                link
                @click="handleContinue(row)"
              >
                继续作答
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { AssessmentRecord, ScaleItem } from '@/types/scale'
import { getMyRecordsApi, getScaleListApi } from '@/api/scale'

const router = useRouter()

// ==================== 可用量表 ====================

// 量表列表
const scales = ref<ScaleItem[]>([])

// 量表列表加载态
const scalesLoading = ref(false)

// 加载可用量表列表
const loadScales = async () => {
  scalesLoading.value = true
  try {
    const res = await getScaleListApi()
    scales.value = res.data ?? []
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    scalesLoading.value = false
  }
}

// 开始测评：携带scaleId跳转答题页（答题页内创建评估记录）
const handleStart = (scale: ScaleItem) => {
  router.push({ path: '/scale/answer', query: { scaleId: String(scale.scaleId) } })
}

// ==================== 历史评估记录 ====================

// 历史记录列表
const records = ref<AssessmentRecord[]>([])

// 历史记录加载态
const recordsLoading = ref(false)

// 加载历史评估记录
const loadRecords = async () => {
  recordsLoading.value = true
  try {
    const res = await getMyRecordsApi()
    records.value = res.data ?? []
  } catch {
    // 请求错误提示已由 request.ts 拦截器统一处理
  } finally {
    recordsLoading.value = false
  }
}

// 查看已完成记录的评估报告
const handleViewReport = (record: AssessmentRecord) => {
  router.push(`/scale/report/${record.recordId}`)
}

// 继续作答未完成记录（携带recordId，答题页恢复进度）
const handleContinue = (record: AssessmentRecord) => {
  router.push({ path: '/scale/answer', query: { recordId: String(record.recordId) } })
}

// 记录状态标签颜色：1进行中 2已暂停 3已完成
const recordStatusType = (status: number): 'primary' | 'success' | 'warning' | 'info' => {
  if (status === 3) {
    return 'success'
  }
  if (status === 2) {
    return 'warning'
  }
  return 'primary'
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

// 首次进入：并行加载量表列表与历史记录
onMounted(() => {
  loadScales()
  loadRecords()
})
</script>

<style lang="scss" scoped>
.scale-container {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.page-header {
  margin-bottom: 20px;

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

  // 作答指导语说明条
  .guide-tip {
    margin-top: 12px;
    padding: 10px 14px;
    font-size: 13px;
    line-height: 1.7;
    color: #606266;
    background: #f4f6fa;
    border-left: 3px solid #409eff;
    border-radius: 4px;
  }
}

.scale-section {
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

// ==================== 量表卡片 ====================

.scale-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  min-height: 100px;
}

.scale-card {
  display: flex;
  flex-direction: column;
  padding: 20px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  }

  .scale-card-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    margin-bottom: 10px;

    .scale-name {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .scale-desc {
    font-size: 13px;
    color: #909399;
    line-height: 1.6;
    margin-bottom: 14px;
    flex: 1;
  }

  .scale-meta {
    display: flex;
    align-items: center;
    gap: 18px;
    margin-bottom: 16px;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #606266;
    }
  }

  .start-btn {
    width: 100%;
  }
}
</style>
