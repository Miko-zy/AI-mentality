package com.aimental.dto;

import cn.hutool.core.util.StrUtil;
import com.aimental.exception.BusinessException;

import java.util.Arrays;
import java.util.List;

/**
 * 更新会话请求DTO（支持改名与切换 AI 风格）
 */
public class UpdateSessionDTO {

    /**
     * 会话名称
     */
    private String sessionName;

    /**
     * AI 风格：empathetic / professional / companion / coach
     */
    private String aiStyle;

    /**
     * 允许的 AI 风格
     */
    private static final List<String> ALLOWED_STYLES = Arrays.asList("empathetic", "professional", "companion", "coach");

    /**
     * 参数校验
     */
    public void validate() {
        if (StrUtil.isBlank(sessionName) && StrUtil.isBlank(aiStyle)) {
            throw new BusinessException(400, "请至少修改会话名称或AI风格");
        }
        if (StrUtil.isNotBlank(sessionName)) {
            String name = sessionName.trim();
            if (name.length() < 2 || name.length() > 50) {
                throw new BusinessException(400, "会话名称长度需在2-50个字符之间");
            }
        }
        if (StrUtil.isNotBlank(aiStyle) && !ALLOWED_STYLES.contains(aiStyle)) {
            throw new BusinessException(400, "不支持的AI风格");
        }
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getAiStyle() {
        return aiStyle;
    }

    public void setAiStyle(String aiStyle) {
        this.aiStyle = aiStyle;
    }
}
