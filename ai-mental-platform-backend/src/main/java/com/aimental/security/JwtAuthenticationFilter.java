package com.aimental.security;

import com.aimental.util.JwtUtil;
import com.aimental.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * 每个请求经过此过滤器，校验Token有效性并与Redis中的登录凭证比对，
 * 校验通过后将用户ID和角色写入SecurityContext，供后续业务获取当前登录用户
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * JWT工具类
     */
    private final JwtUtil jwtUtil;

    /**
     * Redis工具类
     */
    private final RedisUtil redisUtil;

    /**
     * Token请求头名称
     */
    @Value("${jwt.header}")
    private String header;

    /**
     * Token前缀
     */
    @Value("${jwt.prefix}")
    private String prefix;

    /**
     * 构造方法，注入JWT工具类和Redis工具类
     *
     * @param jwtUtil   JWT工具类
     * @param redisUtil Redis工具类
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, RedisUtil redisUtil) {
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
    }

    /**
     * 过滤器核心处理逻辑
     *
     * @param request     请求对象
     * @param response    响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException      IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 从请求头中解析Token
        String token = resolveToken(request);

        // 仅当上下文未认证且Token存在时进行认证
        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String role = jwtUtil.getRoleFromToken(token);
                    // 与Redis中保存的登录凭证比对，确保Token未失效（退出登录后Token立即失效）
                    String storedToken = redisUtil.get(SecurityConstants.tokenKey(role, userId));
                    if (userId != null && role != null && token.equals(storedToken)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userId, null,
                                Collections.singletonList(new SimpleGrantedAuthority(SecurityConstants.ROLE_PREFIX + role)));
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        log.warn("JWT Token与Redis登录凭证不一致，拒绝访问: userId={}, role={}", userId, role);
                    }
                }
            } catch (Exception e) {
                // Token校验或Redis访问异常时拒绝认证，由Security配置统一返回401
                log.error("JWT认证校验异常: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头解析Token
     *
     * @param request 请求对象
     * @return Token字符串（无Token时返回null）
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(header);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(prefix + " ")) {
            // 去除"Bearer "前缀
            return bearerToken.substring(prefix.length()).trim();
        }
        return bearerToken;
    }
}