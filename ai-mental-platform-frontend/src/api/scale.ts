// 心理韧性评估接口模块
// 复用 utils/request.ts 封装：请求拦截器自动在 Header 携带 `Authorization: Bearer {token}`
import { get, post, put } from '@/utils/request'
import { Result } from '@/types'
import {
  AnswerSubmitParams,
  AssessmentProgress,
  AssessmentRecord,
  AssessmentReport,
  CreateRecordParams,
  ScaleDetail,
  ScaleItem
} from '@/types/scale'

/**
 * 获取全部可用（启用中）评估量表列表
 * 后端：GET /api/scale
 *
 * @returns 量表列表
 */
export function getScaleListApi(): Promise<Result<ScaleItem[]>> {
  return get<ScaleItem[]>('/scale')
}

/**
 * 获取量表详情（题目+选项）
 * 后端：GET /api/scale/{scaleId}
 *
 * @param scaleId 量表ID
 * @returns 量表详情
 */
export function getScaleDetailApi(scaleId: number): Promise<Result<ScaleDetail>> {
  return get<ScaleDetail>(`/scale/${scaleId}`)
}

/**
 * 新建评估记录，开始测评
 * 后端：POST /api/scale/record
 *
 * @param data 量表ID
 * @returns 评估进度（记录ID）
 */
export function createScaleRecordApi(data: CreateRecordParams): Promise<Result<AssessmentProgress>> {
  return post<AssessmentProgress>('/scale/record', data)
}

/**
 * 查询本人全部历史评估记录（按记录ID倒序）
 * 后端：GET /api/scale/records
 *
 * @returns 评估记录列表
 */
export function getMyRecordsApi(): Promise<Result<AssessmentRecord[]>> {
  return get<AssessmentRecord[]>('/scale/records')
}

/**
 * 查询评估记录作答进度（暂停后恢复作答）
 * 后端：GET /api/scale/record/{recordId}
 *
 * @param recordId 评估记录ID
 * @returns 评估进度（含已保存作答）
 */
export function getRecordProgressApi(recordId: number): Promise<Result<AssessmentProgress>> {
  return get<AssessmentProgress>(`/scale/record/${recordId}`)
}

/**
 * 保存作答进度（支持中途暂停）
 * 后端：PUT /api/scale/record/{recordId}/answers
 *
 * @param recordId 评估记录ID
 * @param data     已作答内容
 * @returns 评估进度
 */
export function saveAnswersApi(
  recordId: number,
  data: AnswerSubmitParams
): Promise<Result<AssessmentProgress>> {
  return put<AssessmentProgress>(`/scale/record/${recordId}/answers`, data)
}

/**
 * 提交答卷并自动计分、生成评估报告
 * 后端：POST /api/scale/record/{recordId}/submit
 *
 * @param recordId 评估记录ID
 * @param data     全部作答内容
 * @returns 评估结果报告
 */
export function submitAnswersApi(
  recordId: number,
  data: AnswerSubmitParams
): Promise<Result<AssessmentReport>> {
  return post<AssessmentReport>(`/scale/record/${recordId}/submit`, data)
}

/**
 * 查看历史评估报告
 * 后端：GET /api/scale/record/{recordId}/report
 *
 * @param recordId 评估记录ID
 * @returns 评估结果报告
 */
export function getReportApi(recordId: number): Promise<Result<AssessmentReport>> {
  return get<AssessmentReport>(`/scale/record/${recordId}/report`)
}
