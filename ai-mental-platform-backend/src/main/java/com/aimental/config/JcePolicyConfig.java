package com.aimental.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.security.Security;

/**
 * JCE加密策略配置类
 * 解决JDK 8下AES-256等强加密算法受限的问题
 */
@Configuration
public class JcePolicyConfig {

    private static final Logger log = LoggerFactory.getLogger(JcePolicyConfig.class);

    /**
     * 应用启动时设置JCE无限制权限策略
     */
    @PostConstruct
    public void init() {
        try {
            Security.setProperty("crypto.policy", "unlimited");
            log.info("JCE加密策略已设置为无限制模式");
        } catch (Exception e) {
            log.warn("设置JCE加密策略失败，如遇加密相关问题请检查JDK版本或手动安装JCE无限制权限策略文件", e);
        }
    }
}
