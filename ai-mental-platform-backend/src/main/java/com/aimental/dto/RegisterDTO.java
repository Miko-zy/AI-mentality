package com.aimental.dto;

/**
 * 用户注册请求DTO
 * 用户名（存入昵称nickname字段）、邮箱、密码、邮箱验证码
 * 参数格式校验在Service层执行（见ParamValidator）
 */
public class RegisterDTO {

    /**
     * 用户名（注册后作为昵称保存）
     */
    private String username;

    /**
     * 邮箱（业务层AES-256加密存储）
     */
    private String email;

    /**
     * 密码（bcrypt加密保存）
     */
    private String password;

    /**
     * 邮箱验证码
     */
    private String code;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}