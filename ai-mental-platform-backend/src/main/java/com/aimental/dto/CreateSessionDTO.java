package com.aimental.dto;

import cn.hutool.core.util.StrUtil;

/**
 * 创建AI会话请求DTO
 * 包含自定义会话名称、AI对话风格
 */
public class CreateSessionDTO {

    /**
     * 会话名称（用户自定义，2-50字符）
     */
    private String sessionName;

    /**
     * AI对话风格：empathetic(共情倾听)、professional(专业咨询)、companion(陪伴聊天)、coach(成长教练)
     */
    private String aiStyle;

    public CreateSessionDTO() {
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

    /**
     * 校验参数
     */
    public void validate() {
        if (StrUtil.isBlank(sessionName)) {
            throw new IllegalArgumentException("会话名称不能为空");
        }
        if (sessionName.length() < 2 || sessionName.length() > 50) {
            throw new IllegalArgumentException("会话名称长度需在2-50字符之间");
        }
        if (StrUtil.isBlank(aiStyle)) {
            throw new IllegalArgumentException("AI对话风格不能为空");
        }
        String[] validStyles = {"empathetic", "professional", "companion", "coach"};
        boolean valid = false;
        for (String style : validStyles) {
            if (style.equals(aiStyle)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new IllegalArgumentException("不支持的AI对话风格：" + aiStyle);
        }
    }
}