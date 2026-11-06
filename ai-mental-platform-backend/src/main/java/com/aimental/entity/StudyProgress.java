package com.aimental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户学习进度实体类
 */
@TableName("study_progress")
public class StudyProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 进度ID
     */
    @TableId(value = "progress_id", type = IdType.AUTO)
    private Long progressId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 课程ID
     */
    @TableField("course_id")
    private Long courseId;

    /**
     * 已完成小节
     */
    @TableField("finished_section")
    private Integer finishedSection;

    /**
     * 完成百分比
     */
    @TableField("finish_rate")
    private BigDecimal finishRate;

    /**
     * 累计学习时长(秒)
     */
    @TableField("study_duration")
    private Integer studyDuration;

    /**
     * 最后学习时间
     */
    @TableField("last_study_time")
    private LocalDateTime lastStudyTime;

    /**
     * 逻辑删除标记：0未删，1已删
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    public Long getProgressId() {
        return progressId;
    }

    public void setProgressId(Long progressId) {
        this.progressId = progressId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Integer getFinishedSection() {
        return finishedSection;
    }

    public void setFinishedSection(Integer finishedSection) {
        this.finishedSection = finishedSection;
    }

    public BigDecimal getFinishRate() {
        return finishRate;
    }

    public void setFinishRate(BigDecimal finishRate) {
        this.finishRate = finishRate;
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

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}
