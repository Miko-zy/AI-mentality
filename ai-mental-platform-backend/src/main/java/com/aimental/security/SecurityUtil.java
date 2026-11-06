package com.aimental.security;

import com.aimental.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类
 * 提供获取当前登录用户ID的能力
 */
public final class SecurityUtil {

    /**
     * 私有构造方法，工具类不允许实例化
     */
    private SecurityUtil() {
    }

    /**
     * 获取当前登录用户ID
     * 从JWT认证过滤器写入的SecurityContext中取出用户ID
     *
     * @return 当前登录用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new BusinessException(401, "未登录或登录已过期");
    }
}