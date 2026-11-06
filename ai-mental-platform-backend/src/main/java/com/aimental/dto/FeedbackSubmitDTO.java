package com.aimental.dto;

/**
 * 提交用户反馈请求DTO
 * 内容非空、长度校验在Service层执行
 */
public class FeedbackSubmitDTO {

    /**
     * 反馈内容
     */
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}