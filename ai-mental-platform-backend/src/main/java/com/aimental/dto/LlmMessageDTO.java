package com.aimental.dto;

/**
 * OpenAI 兼容协议的消息结构
 * role 取值：system / user / assistant
 */
public class LlmMessageDTO {

    /**
     * 角色
     */
    private String role;

    /**
     * 文本内容
     */
    private String content;

    public LlmMessageDTO() {
    }

    public LlmMessageDTO(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
