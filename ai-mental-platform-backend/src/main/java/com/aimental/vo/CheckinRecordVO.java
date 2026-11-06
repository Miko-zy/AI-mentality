package com.aimental.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡记录视图对象
 */
public class CheckinRecordVO {

    /**
     * 打卡ID
     */
    private Long checkinId;

    /**
     * 打卡日期
     */
    private LocalDate checkinDate;

    /**
     * 心情描述
     */
    private String moodDesc;

    /**
     * 打卡创建时间
     */
    private LocalDateTime createTime;

    public Long getCheckinId() {
        return checkinId;
    }

    public void setCheckinId(Long checkinId) {
        this.checkinId = checkinId;
    }

    public LocalDate getCheckinDate() {
        return checkinDate;
    }

    public void setCheckinDate(LocalDate checkinDate) {
        this.checkinDate = checkinDate;
    }

    public String getMoodDesc() {
        return moodDesc;
    }

    public void setMoodDesc(String moodDesc) {
        this.moodDesc = moodDesc;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}