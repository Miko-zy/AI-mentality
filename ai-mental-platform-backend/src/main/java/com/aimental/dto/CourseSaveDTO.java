package com.aimental.dto;

/**
 * 管理员新增/编辑训练课程请求DTO
 * 对应接口：POST /api/admin/course、PUT /api/admin/course/{courseId}
 */
public class CourseSaveDTO {

    /**
     * 课程名称（必填，最长100字）
     */
    private String courseName;

    /**
     * 课程简介（可选）
     */
    private String courseDesc;

    /**
     * 封面图地址（可选，最长255字）
     */
    private String coverUrl;

    /**
     * 小节总数（可选，>=0；管理员维护小节后会按实际小节数自动覆盖）
     */
    private Integer totalSection;

    /**
     * 状态：0下架，1正常（可选，新增默认1；编辑时不改状态，状态走独立接口）
     */
    private Integer status;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseDesc() {
        return courseDesc;
    }

    public void setCourseDesc(String courseDesc) {
        this.courseDesc = courseDesc;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Integer getTotalSection() {
        return totalSection;
    }

    public void setTotalSection(Integer totalSection) {
        this.totalSection = totalSection;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
