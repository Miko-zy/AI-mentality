package com.aimental.vo;

/**
 * 登录成功返回视图对象
 */
public class LoginVO {

    /**
     * JWT Token（放行接口认证凭证）
     */
    private String token;

    /**
     * 登录用户信息
     */
    private UserVO user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }
}