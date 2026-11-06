package com.aimental.vo;

import java.time.LocalDateTime;

/**
 * 用户个人档案视图对象
 * 基础信息 + 成长统计（累计评估次数、累计训练完成数量等）
 */
public class UserProfileVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 邮箱（解密后返回）
     */
    private String email;

    /**
     * 手机号（解密后返回）
     */
    private String phone;

    /**
     * 实名认证标记：0未认证，1已认证
     */
    private Integer realAuthFlag;

    /**
     * 账号状态：0禁用，1正常
     */
    private Integer accountStatus;

    /**
     * 总积分
     */
    private Integer totalPoints;

    /**
     * 用户等级
     */
    private Integer userLevel;

    /**
     * 注册时间
     */
    private LocalDateTime registerTime;

    /**
     * 角色（user）
     */
    private String role;

    /**
     * 累计评估次数
     */
    private Long assessCount;

    /**
     * 累计训练完成数量（完成率100%的课程数）
     */
    private Long courseFinishCount;

    /**
     * 累计打卡次数
     */
    private Long checkinCount;

    /**
     * 已解锁徽章数量
     */
    private Long badgeCount;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getRealAuthFlag() {
        return realAuthFlag;
    }

    public void setRealAuthFlag(Integer realAuthFlag) {
        this.realAuthFlag = realAuthFlag;
    }

    public Integer getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(Integer accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(Integer userLevel) {
        this.userLevel = userLevel;
    }

    public LocalDateTime getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(LocalDateTime registerTime) {
        this.registerTime = registerTime;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getAssessCount() {
        return assessCount;
    }

    public void setAssessCount(Long assessCount) {
        this.assessCount = assessCount;
    }

    public Long getCourseFinishCount() {
        return courseFinishCount;
    }

    public void setCourseFinishCount(Long courseFinishCount) {
        this.courseFinishCount = courseFinishCount;
    }

    public Long getCheckinCount() {
        return checkinCount;
    }

    public void setCheckinCount(Long checkinCount) {
        this.checkinCount = checkinCount;
    }

    public Long getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(Long badgeCount) {
        this.badgeCount = badgeCount;
    }
}