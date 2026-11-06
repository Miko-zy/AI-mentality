package com.aimental.dto;

/**
 * 用户反馈处理请求DTO
 */
public class FeedbackHandleDTO {

    /**
     * 管理员回复内容（状态置为1已回复时必填，不超过1000字）
     */
    private String replyContent;

    /**
     * 处理后状态：0待处理，1已回复，2关闭（必填）
     */
    private Integer status;

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
