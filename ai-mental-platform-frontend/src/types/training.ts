// 心理韧性训练模块类型定义
// 与后端 DTO/VO 一一对应（CourseVO / CourseDetailVO / SectionVO / TaskVO /
// BadgeVO / CheckinResultVO / UpdateProgressDTO / CheckinDTO）

import type { DateTimeValue } from '@/types'

/**
 * 训练课程（列表项，对应后端 CourseVO）
 */
export interface CourseItem {
  /** 课程ID */
  courseId: number
  /** 课程名称 */
  courseName: string
  /** 课程简介 */
  courseDesc: string
  /** 封面图地址 */
  coverUrl: string | null
  /** 小节总数 */
  totalSection: number
  /** 状态：0下架，1正常 */
  status: number
  /** 状态中文描述：已上架/已下架 */
  statusText: string
  /** 创建时间 */
  createTime: DateTimeValue
}

/**
 * 课程小节（对应后端 SectionVO）
 * sectionContent 为纯文本图文内容，可能为 null
 */
export interface CourseSection {
  /** 小节ID */
  sectionId: number
  /** 所属课程ID */
  courseId: number
  /** 小节标题 */
  sectionTitle: string
  /** 小节内容（纯文本，可能为null） */
  sectionContent: string | null
  /** 建议学习时长(分钟) */
  studyMinutes: number | null
  /** 排序号 */
  sortNum: number
}

/**
 * 课程详情（含小节列表，对应后端 CourseDetailVO）
 */
export interface CourseDetail {
  /** 课程ID */
  courseId: number
  /** 课程名称 */
  courseName: string
  /** 课程简介 */
  courseDesc: string
  /** 封面图地址 */
  coverUrl: string | null
  /** 小节总数 */
  totalSection: number
  /** 状态：0下架，1正常 */
  status: number
  /** 状态中文描述 */
  statusText: string
  /** 创建时间 */
  createTime: DateTimeValue
  /** 小节列表（按排序号升序） */
  sections: CourseSection[]
}

/**
 * 训练任务/学习进度（对应后端 TaskVO）
 * 一次"选择课程生成训练任务"对应一条学习进度记录
 */
export interface TrainingTask {
  /** 进度ID（即任务ID） */
  progressId: number
  /** 课程ID */
  courseId: number
  /** 课程名称 */
  courseName: string
  /** 课程封面图 */
  coverUrl: string | null
  /** 课程小节总数 */
  totalSection: number
  /** 当前停留小节（已完成小节数，最远到达的1-based位置） */
  finishedSection: number
  /** 完成百分比（0-100，保留2位小数） */
  finishRate: number
  /** 课程完成状态：0学习中，1已完成 */
  finishStatus: number
  /** 课程完成状态中文描述：学习中/已完成 */
  finishStatusText: string
  /** 累计学习时长(秒) */
  studyDuration: number
  /** 最后学习时间 */
  lastStudyTime: DateTimeValue
  /** 本次操作新解锁的徽章（无解锁为空列表） */
  newBadges: BadgeItem[]
}

/**
 * 成就徽章（对应后端 BadgeVO）
 * GET /badge 仅返回已解锁徽章；打卡/学习触发返回的为本次新解锁徽章
 */
export interface BadgeItem {
  /** 徽章ID */
  badgeId: number
  /** 徽章名称 */
  badgeName: string
  /** 图标地址 */
  badgeIcon: string | null
  /** 解锁条件（结构化格式 TYPE:VALUE，如 CHECKIN_STREAK:3） */
  obtainCondition: string
  /** 徽章说明 */
  description: string
  /** 是否已解锁：true已解锁，false未解锁 */
  unlocked: boolean
  /** 解锁时间（未解锁为null） */
  obtainTime: DateTimeValue
}

/**
 * 打卡状态/打卡结果（对应后端 CheckinResultVO）
 * GET /checkin 返回今日状态；POST /checkin 返回本次打卡结果
 */
export interface CheckinResult {
  /** 今日是否已打卡 */
  todayChecked: boolean
  /** 心情描述（今日已打卡时返回，否则null） */
  moodDesc: string | null
  /** 本次打卡获得积分（状态查询返回0） */
  checkinPoints: number
  /** 用户当前总积分 */
  totalPoints: number
  /** 连续打卡天数（今日未打卡但昨日未断签时计到昨日） */
  streakDays: number
  /** 累计打卡总天数 */
  totalDays: number
  /** 本次打卡新解锁的徽章（状态查询为空列表） */
  newBadges: BadgeItem[]
}

/**
 * 学习小节、更新进度请求（对应后端 UpdateProgressDTO）
 */
export interface UpdateProgressParams {
  /** 本次学习的小节ID（必填） */
  sectionId: number
  /** 本次学习时长（秒，0-7200，0表示仅标记停留小节） */
  studyDuration: number
}

/**
 * 每日打卡请求（对应后端 CheckinDTO）
 */
export interface CheckinParams {
  /** 心情描述（可选，最长200字） */
  moodDesc?: string
}
