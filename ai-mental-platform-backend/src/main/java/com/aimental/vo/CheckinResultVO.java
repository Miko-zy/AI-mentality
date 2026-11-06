package com.aimental.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 打卡结果VO（打卡接口与打卡状态查询接口共用）
 */
public class CheckinResultVO {

    /**
     * 今日是否已打卡
     */
    private Boolean todayChecked;

    /**
     * 心情描述（今日已打卡时返回，否则null）
     */
    private String moodDesc;

    /**
     * 本次打卡获得积分（未打卡的状态查询返回0）
     */
    private Integer checkinPoints;

    /**
     * 用户当前总积分
     */
    private Integer totalPoints;

    /**
     * 连续打卡天数（今日未打卡但昨日未断签时计到昨日）
     */
    private Integer streakDays;

    /**
     * 累计打卡总天数
     */
    private Integer totalDays;

    /**
     * 本次打卡新解锁的徽章（状态查询时为空列表）
     */
    private List<BadgeVO> newBadges = new ArrayList<BadgeVO>();

    public Boolean getTodayChecked() {
        return todayChecked;
    }

    public void setTodayChecked(Boolean todayChecked) {
        this.todayChecked = todayChecked;
    }

    public String getMoodDesc() {
        return moodDesc;
    }

    public void setMoodDesc(String moodDesc) {
        this.moodDesc = moodDesc;
    }

    public Integer getCheckinPoints() {
        return checkinPoints;
    }

    public void setCheckinPoints(Integer checkinPoints) {
        this.checkinPoints = checkinPoints;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getStreakDays() {
        return streakDays;
    }

    public void setStreakDays(Integer streakDays) {
        this.streakDays = streakDays;
    }

    public Integer getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
    }

    public List<BadgeVO> getNewBadges() {
        return newBadges;
    }

    public void setNewBadges(List<BadgeVO> newBadges) {
        this.newBadges = newBadges;
    }
}
