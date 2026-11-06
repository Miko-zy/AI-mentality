// 心理韧性评估模块类型定义
// 与后端 DTO/VO 一一对应（ScaleVO / ScaleDetailVO / CreateScaleRecordDTO /
// AnswerSubmitDTO / AssessmentProgressVO / AssessmentRecordVO / AssessmentReportVO）

import type { DateTimeValue } from '@/types'

/**
 * 评估量表（列表项，对应后端 ScaleVO）
 */
export interface ScaleItem {
  /** 量表ID */
  scaleId: number
  /** 量表名称 */
  scaleName: string
  /** 量表编码 */
  scaleCode: string
  /** 维度说明（如：坚韧性、力量性、乐观性） */
  dimensionDesc: string
  /** 总题目数 */
  totalQuestionNum: number
  /** 公开信度系数 */
  reliability: number | null
  /** 启用状态：0停用，1启用 */
  status: number
  /** 状态文字：启用/停用 */
  statusText: string
  /** 创建时间 */
  createTime: DateTimeValue
}

/**
 * 量表选项（对应后端 ScaleDetailVO.OptionItemVO）
 */
export interface ScaleOption {
  /** 选项值（作答时提交的分值） */
  value: number
  /** 选项文字 */
  label: string
}

/**
 * 量表题目（对应后端 ScaleDetailVO.QuestionItemVO）
 */
export interface ScaleQuestion {
  /** 题目ID */
  questionId: number
  /** 题目内容 */
  questionContent: string
  /** 排序号 */
  sortNum: number
  /** 选项列表 */
  options: ScaleOption[]
}

/**
 * 量表详情（含题目与选项，对应后端 ScaleDetailVO）
 */
export interface ScaleDetail {
  /** 量表ID */
  scaleId: number
  /** 量表名称 */
  scaleName: string
  /** 量表编码 */
  scaleCode: string
  /** 维度说明 */
  dimensionDesc: string
  /** 总题目数（以实际题目数量为准） */
  totalQuestionNum: number
  /** 公开信度系数 */
  reliability: number | null
  /** 题目列表（按排序号升序） */
  questions: ScaleQuestion[]
}

/**
 * 新建评估记录请求（对应后端 CreateScaleRecordDTO）
 */
export interface CreateRecordParams {
  /** 量表ID */
  scaleId: number
}

/**
 * 单题作答项（对应后端 AnswerSubmitDTO.AnswerItem / AssessmentProgressVO.AnswerItemVO）
 */
export interface AnswerItem {
  /** 题目ID */
  questionId: number
  /** 所选选项值（与题目选项value对应） */
  optionValue: number
}

/**
 * 保存作答/提交答卷请求（对应后端 AnswerSubmitDTO）
 * 中途暂停保存（PUT answers）与最终提交计分（POST submit）共用
 */
export interface AnswerSubmitParams {
  /** 作答列表 */
  answers: AnswerItem[]
}

/**
 * 评估作答进度（对应后端 AssessmentProgressVO）
 * 创建记录、暂停保存、恢复作答时返回
 */
export interface AssessmentProgress {
  /** 评估记录ID */
  recordId: number
  /** 量表ID */
  scaleId: number
  /** 量表名称 */
  scaleName: string
  /** 评估状态：1进行中，2已暂停，3已完成 */
  status: number
  /** 状态文字 */
  statusText: string
  /** 量表总题数 */
  totalQuestionNum: number
  /** 已作答内容（暂停/恢复时回显，未作答为null） */
  answers: AnswerItem[] | null
  /** 完成时间（未完成为null） */
  finishTime: DateTimeValue
}

/**
 * 历史评估记录（对应后端 AssessmentRecordVO）
 * 未完成记录的 totalScore 与 finishTime 为 null
 */
export interface AssessmentRecord {
  /** 评估记录ID */
  recordId: number
  /** 量表ID */
  scaleId: number
  /** 量表名称 */
  scaleName: string
  /** 评估状态：1进行中，2已暂停，3已完成 */
  status: number
  /** 状态文字 */
  statusText: string
  /** 总分（仅已完成记录返回） */
  totalScore: number | null
  /** 完成时间（仅已完成记录返回） */
  finishTime: DateTimeValue
}

/**
 * 单维度得分（对应后端 AssessmentReportVO.DimensionScoreVO）
 */
export interface DimensionScore {
  /** 维度标识（toughness坚韧性 / strength力量性 / optimism乐观性） */
  dimensionCode: string
  /** 维度中文名 */
  dimensionName: string
  /** 该维度得分 */
  score: number
  /** 该维度满分 */
  maxScore: number
}

/**
 * 评估结果报告（对应后端 AssessmentReportVO）
 * 提交后生成：总分、三维度得分、评估等级、解读、成长建议
 */
export interface AssessmentReport {
  /** 评估记录ID */
  recordId: number
  /** 量表ID */
  scaleId: number
  /** 量表名称 */
  scaleName: string
  /** 评估状态：固定为3（已完成） */
  status: number
  /** 状态文字 */
  statusText: string
  /** 总分 */
  totalScore: number
  /** 满分 */
  maxScore: number
  /** 得分率（0-100） */
  scorePercent: number
  /** 各维度得分（坚韧性、力量性、乐观性） */
  dimensions: DimensionScore[]
  /** 评估等级：高韧性/中等韧性/低韧性 */
  level: string
  /** 评估解读文字 */
  interpretation: string
  /** 成长建议 */
  advice: string
  /** 完成时间 */
  finishTime: DateTimeValue
}
