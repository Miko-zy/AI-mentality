package com.aimental.vo;

import com.aimental.entity.User;

import java.time.LocalDateTime;

/**
 * 管理端普通用户视图对象
 * 邮箱、手机号由业务层AES解密后填充
 */
public class AdminUserVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 邮箱（解密后的明文）
     */
    private String email;

    /**
     * 手机号（解密后的明文）
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 账号状态：0禁用，1正常
     */
    private Integer accountStatus;

    /**
     * 账号状态中文描述
     */
    private String statusText;

    /**
     * 实名认证标记：0未认证，1已认证
     */
    private Integer realAuthFlag;

    /**
     * 用户等级
     */
    private Integer userLevel;

    /**
     * 总积分
     */
    private Integer totalPoints;

    /**
     * 注册时间
     */
    private LocalDateTime registerTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 由用户实体转换为视图对象（敏感字段解密由业务层完成）
     *
     * @param user 用户实体
     * @return 管理端用户视图对象
     */
    public static AdminUserVO of(User user) {
        AdminUserVO vo = new AdminUserVO();
        vo.setUserId(user.getUserId());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setAccountStatus(user.getAccountStatus());
        vo.setStatusText(user.getAccountStatus() != null && user.getAccountStatus() == 1 ? "正常" : "禁用");
        vo.setRealAuthFlag(user.getRealAuthFlag());
        vo.setUserLevel(user.getUserLevel());
        vo.setTotalPoints(user.getTotalPoints());
        vo.setRegisterTime(user.getRegisterTime());
        vo.setLastLoginTime(user.getLastLoginTime());
        return vo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Integer getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(Integer accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Integer getRealAuthFlag() {
        return realAuthFlag;
    }

    public void setRealAuthFlag(Integer realAuthFlag) {
        this.realAuthFlag = realAuthFlag;
    }

    public Integer getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(Integer userLevel) {
        this.userLevel = userLevel;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public LocalDateTime getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(LocalDateTime registerTime) {
        this.registerTime = registerTime;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}
