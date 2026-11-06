// 心理韧性训练接口模块（课程学习 / 每日打卡 / 成就徽章）
// 复用 utils/request.ts 封装：请求拦截器自动在 Header 携带 `Authorization: Bearer {token}`
import { get, post, put } from '@/utils/request'
import { Result } from '@/types'
import {
  BadgeItem,
  CheckinParams,
  CheckinResult,
  CourseDetail,
  CourseItem,
  TrainingTask,
  UpdateProgressParams
} from '@/types/training'

/**
 * 获取全部上架训练课程列表
 * 后端：GET /api/course
 *
 * @returns 课程列表
 */
export function getCourseListApi(): Promise<Result<CourseItem[]>> {
  return get<CourseItem[]>('/course')
}

/**
 * 获取课程详情及小节列表（仅上架课程可查看）
 * 后端：GET /api/course/{courseId}
 *
 * @param courseId 课程ID
 * @returns 课程详情（含小节）
 */
export function getCourseDetailApi(courseId: number): Promise<Result<CourseDetail>> {
  return get<CourseDetail>(`/course/${courseId}`)
}

/**
 * 选择课程生成训练任务（幂等：已有任务直接返回，不重置进度）
 * 后端：POST /api/course/{courseId}/task
 *
 * @param courseId 课程ID
 * @returns 训练任务（学习进度）
 */
export function createCourseTaskApi(courseId: number): Promise<Result<TrainingTask>> {
  return post<TrainingTask>(`/course/${courseId}/task`)
}

/**
 * 查询本人全部训练任务（学习进度列表）
 * 后端：GET /api/course/tasks
 *
 * @returns 训练任务列表
 */
export function getMyTasksApi(): Promise<Result<TrainingTask[]>> {
  return get<TrainingTask[]>('/course/tasks')
}

/**
 * 学习小节，更新学习进度：
 * 当前停留小节（只进不退）、累计学习时长、完成率与课程完成状态
 * 后端：PUT /api/course/progress/{progressId}
 *
 * @param progressId 进度ID（任务ID）
 * @param data       本次学习的小节ID与学习时长
 * @returns 更新后的训练任务（含本次新解锁徽章）
 */
export function updateProgressApi(
  progressId: number,
  data: UpdateProgressParams
): Promise<Result<TrainingTask>> {
  return put<TrainingTask>(`/course/progress/${progressId}`, data)
}

/**
 * 每日打卡签到（同一用户同一天只能打卡一次）
 * 后端：POST /api/checkin
 *
 * @param data 打卡信息（心情描述，可选）
 * @returns 打卡结果（获得积分、总积分、连续天数、新解锁徽章）
 */
export function checkinApi(data: CheckinParams): Promise<Result<CheckinResult>> {
  return post<CheckinResult>('/checkin', data)
}

/**
 * 查询今日打卡状态与连续/累计打卡统计
 * 后端：GET /api/checkin
 *
 * @returns 今日打卡状态
 */
export function getCheckinStatusApi(): Promise<Result<CheckinResult>> {
  return get<CheckinResult>('/checkin')
}

/**
 * 查询当前用户已解锁的全部成就徽章（按解锁时间倒序）
 * 后端：GET /api/badge
 *
 * @returns 已解锁徽章列表
 */
export function getMyBadgesApi(): Promise<Result<BadgeItem[]>> {
  return get<BadgeItem[]>('/badge')
}
