package com.aimental.dto;

/**
 * 编辑个人资料请求DTO
 * 昵称、头像、实名认证标记、手机号、邮箱（手机号与邮箱业务层AES-256加密存储）
 * 参数格式校验在Service层执行（见ParamValidator）
 */
public class UpdateUserInfoDTO {

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 实名认证标记：0未认证，1已认证
     */
    private Integer realAuthFlag;

    /**
     * 手机号（业务层AES-256加密存储）
     */
    private String phone;

    /**
     * 邮箱（业务层AES-256加密存储）
     */
    private String email;

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

    public Integer getRealAuthFlag() {
        return realAuthFlag;
    }

    public void setRealAuthFlag(Integer realAuthFlag) {
        this.realAuthFlag = realAuthFlag;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}