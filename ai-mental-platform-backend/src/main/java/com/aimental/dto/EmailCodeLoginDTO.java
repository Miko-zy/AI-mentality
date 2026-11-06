package com.aimental.dto;

/**
 * 邮箱验证码登录请求DTO
 * 未注册邮箱首次验证码登录时自动完成注册
 */
public class EmailCodeLoginDTO {

    /**
     * 邮箱
     */
    private String email;

    /**
     * 邮箱验证码
     */
    private String code;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}