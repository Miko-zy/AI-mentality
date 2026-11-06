package com.aimental.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评估结果报告VO（提交后生成：总分、三维度得分、评估等级、解读、成长建议）
 */
public class AssessmentReportVO {

    /**
     * 评估记录ID
     */
    private Long recordId;

    /**
     * 量表ID
     */
    private Long scaleId;

    /**
     * 量表名称
     */
    private String scaleName;

    /**
     * 评估状态：固定为3（已完成）
     */
    private Integer status;

    /**
     * 状态文字
     */
    private String statusText;

    /**
     * 总分
     */
    private Integer totalScore;

    /**
     * 满分（按当前题目选项与分值映射计算）
     */
    private Integer maxScore;

    /**
     * 得分率（百分比整数，0-100）
     */
    private Integer scorePercent;

    /**
     * 各维度得分（坚韧性、力量性、乐观性）
     */
    private List<DimensionScoreVO> dimensions;

    /**
     * 评估等级：高韧性/中等韧性/低韧性
     */
    private String level;

    /**
     * 评估解读文字
     */
    private String interpretation;

    /**
     * 成长建议
     */
    private String advice;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getScaleId() {
        return scaleId;
    }

    public void setScaleId(Long scaleId) {
        this.scaleId = scaleId;
    }

    public String getScaleName() {
        return scaleName;
    }

    public void setScaleName(String scaleName) {
        this.scaleName = scaleName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public Integer getScorePercent() {
        return scorePercent;
    }

    public void setScorePercent(Integer scorePercent) {
        this.scorePercent = scorePercent;
    }

    public List<DimensionScoreVO> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<DimensionScoreVO> dimensions) {
        this.dimensions = dimensions;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }

    /**
     * 单维度得分VO
     */
    public static class DimensionScoreVO {

        /**
         * 维度标识（如toughness/strength/optimism）
         */
        private String dimensionCode;

        /**
         * 维度中文名（坚韧性/力量性/乐观性）
         */
        private String dimensionName;

        /**
         * 该维度得分
         */
        private Integer score;

        /**
         * 该维度满分
         */
        private Integer maxScore;

        public String getDimensionCode() {
            return dimensionCode;
        }

        public void setDimensionCode(String dimensionCode) {
            this.dimensionCode = dimensionCode;
        }

        public String getDimensionName() {
            return dimensionName;
        }

        public void setDimensionName(String dimensionName) {
            this.dimensionName = dimensionName;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public Integer getMaxScore() {
            return maxScore;
        }

        public void setMaxScore(Integer maxScore) {
            this.maxScore = maxScore;
        }
    }
}
