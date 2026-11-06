package com.aimental.security;

/**
 * 安全相关常量类
 */
public final class SecurityConstants {

    /**
     * 普通用户角色
     */
    public static final String ROLE_USER = "user";

    /**
     * 管理员角色
     */
    public static final String ROLE_ADMIN = "admin";

    /**
     * Spring Security角色权限前缀
     */
    public static final String ROLE_PREFIX = "ROLE_";

    /**
     * Redis中登录Token的Key前缀：login:token:角色:用户ID
     */
    public static final String TOKEN_KEY_PREFIX = "login:token:";

    /**
     * Redis中邮箱验证码的Key前缀：email:code:邮箱
     */
    public static final String EMAIL_CODE_KEY_PREFIX = "email:code:";

    /**
     * 邮箱验证码有效期（分钟）
     */
    public static final long EMAIL_CODE_TTL_MINUTES = 5L;

    /**
     * 私有构造方法，常量类不允许实例化
     */
    private SecurityConstants() {
    }

    /**
     * 构建Redis中登录Token的Key
     *
     * @param role   角色
     * @param userId 用户ID
     * @return Redis Key
     */
    public static String tokenKey(String role, Long userId) {
        return TOKEN_KEY_PREFIX + role + ":" + userId;
    }

    /**
     * 构建Redis中邮箱验证码的Key
     *
     * @param email 邮箱
     * @return Redis Key
     */
    public static String emailCodeKey(String email) {
        return EMAIL_CODE_KEY_PREFIX + email;
    }
}