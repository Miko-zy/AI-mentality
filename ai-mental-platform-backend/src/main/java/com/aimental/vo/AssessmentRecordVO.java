package com.aimental.vo;

import java.time.LocalDateTime;

/**
 * 用户评估记录VO（历史记录列表项）
 * 未完成记录的totalScore与finishTime返回null，状态以status为准
 */
public class AssessmentRecordVO {

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
     * 总分（仅已完成记录返回，未完成为null）
     */
    private Integer totalScore;

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

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }
}
