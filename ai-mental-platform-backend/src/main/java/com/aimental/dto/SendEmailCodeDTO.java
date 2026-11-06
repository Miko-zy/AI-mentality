package com.aimental.dto;

/**
 * 发送邮箱验证码请求DTO
 * 验证码存入Redis（有效期5分钟），用于注册、邮箱验证码登录等场景
 */
public class SendEmailCodeDTO {

    /**
     * 邮箱
     */
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}