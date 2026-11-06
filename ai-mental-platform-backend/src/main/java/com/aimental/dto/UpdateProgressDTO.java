package com.aimental.dto;

/**
 * 学习小节、更新进度请求DTO
 * 对应接口：PUT /api/course/progress/{progressId}
 */
public class UpdateProgressDTO {

    /**
     * 本次学习的小节ID（必填）
     */
    private Long sectionId;

    /**
     * 本次学习时长（秒，0-7200，可为0表示仅标记停留小节）
     */
    private Integer studyDuration;

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public Integer getStudyDuration() {
        return studyDuration;
    }

    public void setStudyDuration(Integer studyDuration) {
        this.studyDuration = studyDuration;
    }
}
