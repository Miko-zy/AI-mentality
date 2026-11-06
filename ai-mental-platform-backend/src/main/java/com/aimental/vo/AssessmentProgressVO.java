package com.aimental.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评估作答进度VO（创建记录、暂停保存、恢复作答时返回）
 */
public class AssessmentProgressVO {

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
     * 评估状态：1进行中，2已暂停，3已完成
     */
    private Integer status;

    /**
     * 状态文字：进行中/已暂停/已完成
     */
    private String statusText;

    /**
     * 量表总题数
     */
    private Integer totalQuestionNum;

    /**
     * 已作答内容（暂停/恢复时回显）
     */
    private List<AnswerItemVO> answers;

    /**
     * 完成时间（仅已完成记录返回，未完成为null）
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

    public Integer getTotalQuestionNum() {
        return totalQuestionNum;
    }

    public void setTotalQuestionNum(Integer totalQuestionNum) {
        this.totalQuestionNum = totalQuestionNum;
    }

    public List<AnswerItemVO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerItemVO> answers) {
        this.answers = answers;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }

    /**
     * 单题作答VO
     */
    public static class AnswerItemVO {

        /**
         * 题目ID
         */
        private Long questionId;

        /**
         * 所选选项值
         */
        private Integer optionValue;

        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public Integer getOptionValue() {
            return optionValue;
        }

        public void setOptionValue(Integer optionValue) {
            this.optionValue = optionValue;
        }
    }
}
