package com.aimental.vo;

/**
 * 登录用户信息视图对象
 * 邮箱、手机号返回前由业务层AES解密
 */
public class UserVO {

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
     * 角色（user）
     */
    private String role;

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}