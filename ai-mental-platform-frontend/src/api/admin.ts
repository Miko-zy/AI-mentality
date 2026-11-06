// 管理员后台接口模块
// 复用 utils/request.ts 封装：请求拦截器自动携带 `Authorization: Bearer {token}`
// 后端全部接口有 @PreAuthorize("hasRole('admin')")，非管理员返回HTTP 403（拦截器统一提示）
// 分页接口返回 PageResult{list,total,pageNum,pageSize}（后端 PageVO）
import { get, post, put, del } from '@/utils/request'
import { PageResult, Result } from '@/types'
import { ScaleItem } from '@/types/scale'
import { CourseItem, CourseSection } from '@/types/training'
import {
  AdminQuestionItem,
  AdminUserItem,
  AdminUserQuery,
  CourseSaveParams,
  CourseStatusParams,
  CrisisHandleParams,
  CrisisItem,
  CrisisQuery,
  FeedbackHandleParams,
  FeedbackItem,
  FeedbackQuery,
  NoticeItem,
  NoticeSaveParams,
  NoticeStatusParams,
  QuestionSaveParams,
  ScaleSaveParams,
  ScaleStatusParams,
  SectionSaveParams,
  UserStatusParams
} from '@/types/admin'

// ==================== 1. 普通用户账号管理 /admin/users ====================

/**
 * 分页查询用户（支持昵称模糊/邮箱精确关键字与账号状态筛选）
 * 后端：GET /api/admin/users
 *
 * @param params 分页与筛选参数
 * @returns 分页结果
 */
export function getAdminUsersApi(
  params: AdminUserQuery
): Promise<Result<PageResult<AdminUserItem>>> {
  return get<PageResult<AdminUserItem>>('/admin/users', params)
}

/**
 * 启用/禁用账号（禁用时同步删除Redis登录Token，在线用户JWT立即失效）
 * 后端：PUT /api/admin/users/{userId}/status
 *
 * @param userId 用户ID
 * @param data   目标状态
 * @returns 操作结果
 */
export function updateUserStatusApi(
  userId: number,
  data: UserStatusParams
): Promise<Result<void>> {
  return put<void>(`/admin/users/${userId}/status`, data)
}

// ==================== 2. 量表管理 /admin/scale ====================

/**
 * 查询全部量表（含停用，管理端列表，不分页）
 * 后端：GET /api/admin/scale
 *
 * @returns 量表列表
 */
export function getAdminScalesApi(): Promise<Result<ScaleItem[]>> {
  return get<ScaleItem[]>('/admin/scale')
}

/**
 * 新增量表
 * 后端：POST /api/admin/scale
 *
 * @param data 量表信息
 * @returns 新建的量表
 */
export function createScaleApi(data: ScaleSaveParams): Promise<Result<ScaleItem>> {
  return post<ScaleItem>('/admin/scale', data)
}

/**
 * 编辑量表（名称、编码、维度说明、题数、信度）
 * 后端：PUT /api/admin/scale/{scaleId}
 *
 * @param scaleId 量表ID
 * @param data    量表信息
 * @returns 编辑后的量表
 */
export function updateScaleApi(
  scaleId: number,
  data: ScaleSaveParams
): Promise<Result<ScaleItem>> {
  return put<ScaleItem>(`/admin/scale/${scaleId}`, data)
}

/**
 * 启用/停用量表
 * 后端：PUT /api/admin/scale/{scaleId}/status
 *
 * @param scaleId 量表ID
 * @param data    目标状态
 * @returns 操作结果
 */
export function updateScaleStatusApi(
  scaleId: number,
  data: ScaleStatusParams
): Promise<Result<void>> {
  return put<void>(`/admin/scale/${scaleId}/status`, data)
}

/**
 * 查询指定量表的全部题目（含选项JSON与计分规则）
 * 后端：GET /api/admin/scale/{scaleId}/questions
 *
 * @param scaleId 量表ID
 * @returns 题目列表
 */
export function getScaleQuestionsApi(scaleId: number): Promise<Result<AdminQuestionItem[]>> {
  return get<AdminQuestionItem[]>(`/admin/scale/${scaleId}/questions`)
}

/**
 * 新增量表题目（含选项与分值映射配置）
 * 后端：POST /api/admin/scale/{scaleId}/question
 *
 * @param scaleId 量表ID
 * @param data    题目信息
 * @returns 新建的题目
 */
export function addScaleQuestionApi(
  scaleId: number,
  data: QuestionSaveParams
): Promise<Result<AdminQuestionItem>> {
  return post<AdminQuestionItem>(`/admin/scale/${scaleId}/question`, data)
}

/**
 * 编辑量表题目（内容、选项、计分规则、排序）
 * 后端：PUT /api/admin/question/{questionId}
 *
 * @param questionId 题目ID
 * @param data       题目信息
 * @returns 编辑后的题目
 */
export function updateScaleQuestionApi(
  questionId: number,
  data: QuestionSaveParams
): Promise<Result<AdminQuestionItem>> {
  return put<AdminQuestionItem>(`/admin/question/${questionId}`, data)
}

/**
 * 删除量表题目（逻辑删除）
 * 后端：DELETE /api/admin/question/{questionId}
 *
 * @param questionId 题目ID
 * @returns 操作结果
 */
export function deleteScaleQuestionApi(questionId: number): Promise<Result<void>> {
  return del<void>(`/admin/question/${questionId}`)
}

// ==================== 3. 课程管理 /admin/course ====================

/**
 * 查询全部课程（含下架，管理端列表，不分页）
 * 后端：GET /api/admin/course
 *
 * @returns 课程列表
 */
export function getAdminCoursesApi(): Promise<Result<CourseItem[]>> {
  return get<CourseItem[]>('/admin/course')
}

/**
 * 新增课程
 * 后端：POST /api/admin/course
 *
 * @param data 课程信息
 * @returns 新建的课程
 */
export function createCourseApi(data: CourseSaveParams): Promise<Result<CourseItem>> {
  return post<CourseItem>('/admin/course', data)
}

/**
 * 编辑课程（不改上下架状态）
 * 后端：PUT /api/admin/course/{courseId}
 *
 * @param courseId 课程ID
 * @param data     课程信息
 * @returns 编辑后的课程
 */
export function updateCourseApi(
  courseId: number,
  data: CourseSaveParams
): Promise<Result<CourseItem>> {
  return put<CourseItem>(`/admin/course/${courseId}`, data)
}

/**
 * 课程上架/下架
 * 后端：PUT /api/admin/course/{courseId}/status
 *
 * @param courseId 课程ID
 * @param data     目标状态
 * @returns 操作结果
 */
export function updateCourseStatusApi(
  courseId: number,
  data: CourseStatusParams
): Promise<Result<void>> {
  return put<void>(`/admin/course/${courseId}/status`, data)
}

/**
 * 查询指定课程的全部小节（按排序号升序）
 * 后端：GET /api/admin/course/{courseId}/sections
 *
 * @param courseId 课程ID
 * @returns 小节列表
 */
export function getCourseSectionsApi(courseId: number): Promise<Result<CourseSection[]>> {
  return get<CourseSection[]>(`/admin/course/${courseId}/sections`)
}

/**
 * 新增课程小节（自动同步课程小节总数）
 * 后端：POST /api/admin/course/{courseId}/section
 *
 * @param courseId 课程ID
 * @param data     小节信息
 * @returns 新建的小节
 */
export function addCourseSectionApi(
  courseId: number,
  data: SectionSaveParams
): Promise<Result<CourseSection>> {
  return post<CourseSection>(`/admin/course/${courseId}/section`, data)
}

/**
 * 编辑课程小节（不改所属课程）
 * 后端：PUT /api/admin/course/section/{sectionId}
 *
 * @param sectionId 小节ID
 * @param data      小节信息
 * @returns 编辑后的小节
 */
export function updateCourseSectionApi(
  sectionId: number,
  data: SectionSaveParams
): Promise<Result<CourseSection>> {
  return put<CourseSection>(`/admin/course/section/${sectionId}`, data)
}

/**
 * 逻辑删除课程小节（自动同步课程小节总数）
 * 后端：DELETE /api/admin/course/section/{sectionId}
 *
 * @param sectionId 小节ID
 * @returns 操作结果
 */
export function deleteCourseSectionApi(sectionId: number): Promise<Result<void>> {
  return del<void>(`/admin/course/section/${sectionId}`)
}

// ==================== 4. 安全危机事件 /admin/crisis ====================

/**
 * 分页查询危机事件（补全用户昵称与关联风险消息内容）
 * 后端：GET /api/admin/crisis
 *
 * @param params 分页与筛选参数
 * @returns 分页结果
 */
export function getAdminCrisisApi(
  params: CrisisQuery
): Promise<Result<PageResult<CrisisItem>>> {
  return get<PageResult<CrisisItem>>('/admin/crisis', params)
}

/**
 * 处理危机事件（更新状态与备注，刷新处理时间）
 * 后端：PUT /api/admin/crisis/{crisisId}
 *
 * @param crisisId 危机记录ID
 * @param data     处理信息
 * @returns 操作结果
 */
export function handleCrisisApi(
  crisisId: number,
  data: CrisisHandleParams
): Promise<Result<void>> {
  return put<void>(`/admin/crisis/${crisisId}`, data)
}

// ==================== 5. 用户反馈处理 /admin/feedback ====================

/**
 * 分页查询用户反馈（补全反馈用户昵称）
 * 后端：GET /api/admin/feedback
 *
 * @param params 分页与筛选参数
 * @returns 分页结果
 */
export function getAdminFeedbackApi(
  params: FeedbackQuery
): Promise<Result<PageResult<FeedbackItem>>> {
  return get<PageResult<FeedbackItem>>('/admin/feedback', params)
}

/**
 * 处理反馈（status=1时replyContent必填；未传replyContent保留原回复）
 * 后端：PUT /api/admin/feedback/{feedbackId}
 *
 * @param feedbackId 反馈ID
 * @param data       处理信息
 * @returns 操作结果
 */
export function handleFeedbackApi(
  feedbackId: number,
  data: FeedbackHandleParams
): Promise<Result<void>> {
  return put<void>(`/admin/feedback/${feedbackId}`, data)
}

// ==================== 6. 系统公告 /admin/notices ====================

/**
 * 查询全部公告（倒序，补全发布管理员账号）
 * 后端：GET /api/admin/notices
 *
 * @returns 公告列表
 */
export function getAdminNoticesApi(): Promise<Result<NoticeItem[]>> {
  return get<NoticeItem[]>('/admin/notices')
}

/**
 * 新增公告（status=1时后端记录发布时间与当前管理员）
 * 后端：POST /api/admin/notices
 *
 * @param data 公告信息
 * @returns 新建的公告
 */
export function createNoticeApi(data: NoticeSaveParams): Promise<Result<NoticeItem>> {
  return post<NoticeItem>('/admin/notices', data)
}

/**
 * 编辑公告（仅改标题正文，不改状态）
 * 后端：PUT /api/admin/notices/{noticeId}
 *
 * @param noticeId 公告ID
 * @param data     公告标题与正文
 * @returns 编辑后的公告
 */
export function updateNoticeApi(
  noticeId: number,
  data: NoticeSaveParams
): Promise<Result<NoticeItem>> {
  return put<NoticeItem>(`/admin/notices/${noticeId}`, data)
}

/**
 * 修改公告状态（发布/停用走此接口，置1时刷新发布时间与发布人）
 * 后端：PUT /api/admin/notices/{noticeId}/status
 *
 * @param noticeId 公告ID
 * @param data     目标状态
 * @returns 操作结果
 */
export function updateNoticeStatusApi(
  noticeId: number,
  data: NoticeStatusParams
): Promise<Result<void>> {
  return put<void>(`/admin/notices/${noticeId}/status`, data)
}
