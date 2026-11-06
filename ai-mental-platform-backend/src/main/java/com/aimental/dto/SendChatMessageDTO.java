package com.aimental.dto;

import cn.hutool.core.util.StrUtil;

/**
 * 发送对话消息请求DTO
 */
public class SendChatMessageDTO {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 用户消息内容
     */
    private String content;

    public SendChatMessageDTO() {
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 校验参数
     */
    public void validate() {
        if (sessionId == null) {
            throw new IllegalArgumentException("会话ID不能为空");
        }
        if (StrUtil.isBlank(content)) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        if (content.length() > 2000) {
            throw new IllegalArgumentException("消息内容长度不能超过2000字符");
        }
    }
}