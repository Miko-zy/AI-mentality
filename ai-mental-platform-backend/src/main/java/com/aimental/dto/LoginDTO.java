package com.aimental.dto;

/**
 * 账号密码登录请求DTO
 * 账号即邮箱（邮箱在业务层AES-256加密存储，登录时按加密值匹配）
 */
public class LoginDTO {

    /**
     * 邮箱账号
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}