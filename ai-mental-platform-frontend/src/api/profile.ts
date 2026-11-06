// 个人中心接口模块
// 复用 utils/request.ts 封装：请求拦截器自动在 Header 携带 `Authorization: Bearer {token}`
import { get, post, put } from '@/utils/request'
import { PageResult, Result } from '@/types'
import {
  BadgeItem,
  ChangePasswordParams,
  CheckinRecordItem,
  FeedbackItem,
  FeedbackSubmitParams,
  UpdateProfileParams,
  UserProfile
} from '@/types/profile'

/**
 * 获取个人档案（基础信息 + 成长统计）
 * 后端：GET /api/user/profile
 *
 * @returns 个人档案
 */
export function getProfileApi(): Promise<Result<UserProfile>> {
  return get<UserProfile>('/user/profile')
}

/**
 * 编辑个人资料
 * 后端：PUT /api/user/profile
 *
 * @param data 编辑资料请求（昵称、头像、实名标记、手机号、邮箱）
 * @returns 更新结果
 */
export function updateProfileApi(data: UpdateProfileParams): Promise<Result<void>> {
  return put<void>('/user/profile', data)
}

/**
 * 修改密码
 * 后端：PUT /api/user/password
 *
 * @param data 旧密码与新密码
 * @returns 修改结果
 */
export function changePasswordApi(data: ChangePasswordParams): Promise<Result<void>> {
  return put<void>('/user/password', data)
}

/**
 * 分页查看本人打卡记录
 * 后端：GET /api/user/checkins
 *
 * @param pageNum  页码（从1开始）
 * @param pageSize 每页条数
 * @returns 打卡记录分页结果
 */
export function getCheckinsApi(
  pageNum: number,
  pageSize: number
): Promise<Result<PageResult<CheckinRecordItem>>> {
  return get<PageResult<CheckinRecordItem>>('/user/checkins', { pageNum, pageSize })
}

/**
 * 分页查看本人已解锁徽章
 * 后端：GET /api/user/badges
 *
 * @param pageNum  页码（从1开始）
 * @param pageSize 每页条数
 * @returns 徽章分页结果
 */
export function getMyBadgesApi(
  pageNum: number,
  pageSize: number
): Promise<Result<PageResult<BadgeItem>>> {
  return get<PageResult<BadgeItem>>('/user/badges', { pageNum, pageSize })
}

/**
 * 提交用户反馈
 * 后端：POST /api/feedback
 *
 * @param data 反馈内容（非空，≤500字符）
 * @returns 提交结果
 */
export function submitFeedbackApi(data: FeedbackSubmitParams): Promise<Result<void>> {
  return post<void>('/feedback', data)
}

/**
 * 分页查看本人反馈（含管理员回复）
 * 后端：GET /api/feedback
 *
 * @param pageNum  页码（从1开始）
 * @param pageSize 每页条数
 * @returns 反馈分页结果
 */
export function getMyFeedbackApi(
  pageNum: number,
  pageSize: number
): Promise<Result<PageResult<FeedbackItem>>> {
  return get<PageResult<FeedbackItem>>('/feedback', { pageNum, pageSize })
}
