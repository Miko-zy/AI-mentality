package com.aimental.vo;

import com.aimental.entity.CourseSection;

/**
 * 课程小节VO（用户端详情与管理员维护共用）
 */
public class SectionVO {

    /**
     * 小节ID
     */
    private Long sectionId;

    /**
     * 所属课程ID
     */
    private Long courseId;

    /**
     * 小节标题
     */
    private String sectionTitle;

    /**
     * 小节内容
     */
    private String sectionContent;

    /**
     * 建议学习时长(分钟)
     */
    private Integer studyMinutes;

    /**
     * 排序号
     */
    private Integer sortNum;

    /**
     * 由小节实体转换为VO
     *
     * @param section 小节实体
     * @return 小节VO
     */
    public static SectionVO of(CourseSection section) {
        SectionVO vo = new SectionVO();
        vo.setSectionId(section.getSectionId());
        vo.setCourseId(section.getCourseId());
        vo.setSectionTitle(section.getSectionTitle());
        vo.setSectionContent(section.getSectionContent());
        vo.setStudyMinutes(section.getStudyMinutes());
        vo.setSortNum(section.getSortNum());
        return vo;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

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
