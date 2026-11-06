package com.aimental.vo;

import com.aimental.entity.StudyCourse;

import java.time.LocalDateTime;

/**
 * 训练课程VO（列表展示用）
 */
public class CourseVO {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程简介
     */
    private String courseDesc;

    /**
     * 封面图地址
     */
    private String coverUrl;

    /**
     * 小节总数
     */
    private Integer totalSection;

    /**
     * 状态：0下架，1正常
     */
    private Integer status;

    /**
     * 状态中文描述
     */
    private String statusText;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 由课程实体转换为VO
     *
     * @param course 课程实体
     * @return 课程VO
     */
    public static CourseVO of(StudyCourse course) {
        CourseVO vo = new CourseVO();
        vo.setCourseId(course.getCourseId());
        vo.setCourseName(course.getCourseName());
        vo.setCourseDesc(course.getCourseDesc());
        vo.setCoverUrl(course.getCoverUrl());
        vo.setTotalSection(course.getTotalSection());
        vo.setStatus(course.getStatus());
        vo.setStatusText(course.getStatus() != null && course.getStatus() == 1 ? "已上架" : "已下架");
        vo.setCreateTime(course.getCreateTime());
        return vo;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

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

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
