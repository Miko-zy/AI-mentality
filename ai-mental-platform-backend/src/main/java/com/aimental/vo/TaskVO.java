package com.aimental.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 训练任务（学习进度）VO
 * 一次“选择课程生成训练任务”对应一条学习进度记录
 */
public class TaskVO {

    /**
     * 进度ID（即任务ID）
     */
    private Long progressId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程封面图
     */
    private String coverUrl;

    /**
     * 课程小节总数
     */
    private Integer totalSection;

    /**
     * 当前停留小节（已完成小节数）
     */
    private Integer finishedSection;

    /**
     * 完成百分比（0-100）
     */
    private java.math.BigDecimal finishRate;

    /**
     * 课程完成状态：0学习中，1已完成
     */
    private Integer finishStatus;

    /**
     * 课程完成状态中文描述
     */
    private String finishStatusText;

    /**
     * 累计学习时长(秒)
     */
    private Integer studyDuration;

    /**
     * 最后学习时间
     */
    private LocalDateTime lastStudyTime;

    /**
     * 本次操作新解锁的徽章（打卡/学习触发判定时返回，无解锁则为空列表）
     */
    private List<BadgeVO> newBadges = new ArrayList<BadgeVO>();

    public Long getProgressId() {
        return progressId;
    }

    public void setProgressId(Long progressId) {
        this.progressId = progressId;
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

    public Integer getFinishedSection() {
        return finishedSection;
    }

    public void setFinishedSection(Integer finishedSection) {
        this.finishedSection = finishedSection;
    }

    public java.math.BigDecimal getFinishRate() {
        return finishRate;
    }

    public void setFinishRate(java.math.BigDecimal finishRate) {
        this.finishRate = finishRate;
    }

    public Integer getFinishStatus() {
        return finishStatus;
    }

    public void setFinishStatus(Integer finishStatus) {
        this.finishStatus = finishStatus;
    }

    public String getFinishStatusText() {
        return finishStatusText;
    }

    public void setFinishStatusText(String finishStatusText) {
        this.finishStatusText = finishStatusText;
    }

    public Integer getStudyDuration() {
        return studyDuration;
    }

    public void setStudyDuration(Integer studyDuration) {
        this.studyDuration = studyDuration;
    }

    public LocalDateTime getLastStudyTime() {
        return lastStudyTime;
    }

    public void setLastStudyTime(LocalDateTime lastStudyTime) {
        this.lastStudyTime = lastStudyTime;
    }

    public List<BadgeVO> getNewBadges() {
        return newBadges;
    }

    public void setNewBadges(List<BadgeVO> newBadges) {
        this.newBadges = newBadges;
    }
}
