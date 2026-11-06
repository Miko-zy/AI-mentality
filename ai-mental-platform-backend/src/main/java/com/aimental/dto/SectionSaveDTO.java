package com.aimental.dto;

/**
 * 管理员新增/编辑课程小节请求DTO
 * 对应接口：POST /api/admin/course/{courseId}/section、PUT /api/admin/course/section/{sectionId}
 */
public class SectionSaveDTO {

    /**
     * 小节标题（必填，最长200字）
     */
    private String sectionTitle;

    /**
     * 小节内容（可选，最长10000字）
     */
    private String sectionContent;

    /**
     * 建议学习时长（分钟，1-600，可选默认5）
     */
    private Integer studyMinutes;

    /**
     * 排序号（可选，默认0，小的在前）
     */
    private Integer sortNum;

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public String getSectionContent() {
        return sectionContent;
    }

    public void setSectionContent(String sectionContent) {
        this.sectionContent = sectionContent;
    }

    public Integer getStudyMinutes() {
        return studyMinutes;
    }

    public void setStudyMinutes(Integer studyMinutes) {
        this.studyMinutes = studyMinutes;
    }

    public Integer getSortNum() {
        return sortNum;
    }

    public void setSortNum(Integer sortNum) {
        this.sortNum = sortNum;
    }
}
