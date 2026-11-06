package com.aimental.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 评估量表详情VO（含全部题目与选项，用户端作答页面使用）
 */
public class ScaleDetailVO {

    /**
     * 量表ID
     */
    private Long scaleId;

    /**
     * 量表名称
     */
    private String scaleName;

    /**
     * 量表编码
     */
    private String scaleCode;

    /**
     * 维度说明
     */
    private String dimensionDesc;

    /**
     * 总题目数（以实际题目数量为准）
     */
    private Integer totalQuestionNum;

    /**
     * 公开信度系数
     */
    private BigDecimal reliability;

    /**
     * 题目列表（按排序号升序）
     */
    private List<QuestionItemVO> questions;

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

    public String getScaleCode() {
        return scaleCode;
    }

    public void setScaleCode(String scaleCode) {
        this.scaleCode = scaleCode;
    }

    public String getDimensionDesc() {
        return dimensionDesc;
    }

    public void setDimensionDesc(String dimensionDesc) {
        this.dimensionDesc = dimensionDesc;
    }

    public Integer getTotalQuestionNum() {
        return totalQuestionNum;
    }

    public void setTotalQuestionNum(Integer totalQuestionNum) {
        this.totalQuestionNum = totalQuestionNum;
    }

    public BigDecimal getReliability() {
        return reliability;
    }

    public void setReliability(BigDecimal reliability) {
        this.reliability = reliability;
    }

    public List<QuestionItemVO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionItemVO> questions) {
        this.questions = questions;
    }

    /**
     * 题目条目VO
     */
    public static class QuestionItemVO {

        /**
         * 题目ID
         */
        private Long questionId;

        /**
         * 题目内容
         */
        private String questionContent;

        /**
         * 排序号
         */
        private Integer sortNum;

        /**
         * 选项列表
         */
        private List<OptionItemVO> options;

        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public String getQuestionContent() {
            return questionContent;
        }

        public void setQuestionContent(String questionContent) {
            this.questionContent = questionContent;
        }

        public Integer getSortNum() {
            return sortNum;
        }

        public void setSortNum(Integer sortNum) {
            this.sortNum = sortNum;
        }

        public List<OptionItemVO> getOptions() {
            return options;
        }

        public void setOptions(List<OptionItemVO> options) {
            this.options = options;
        }
    }

    /**
     * 选项条目VO
     */
    public static class OptionItemVO {

        /**
         * 选项值（作答时提交的分值）
         */
        private Integer value;

        /**
         * 选项文字
         */
        private String label;

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
