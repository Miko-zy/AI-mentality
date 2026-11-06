package com.aimental.vo;

import java.time.LocalDateTime;

/**
 * 用户反馈视图对象
 */
public class FeedbackVO {

    /**
     * 反馈ID
     */
    private Long feedbackId;

    /**
     * 反馈内容
     */
    private String feedbackContent;

    /**
     * 状态：0待处理，1已回复，2关闭
     */
    private Integer status;

    /**
     * 管理员回复
     */
    private String replyContent;

    /**
     * 提交时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间（管理员回复后更新）
     */
    private LocalDateTime updateTime;

    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public String getFeedbackContent() {
        return feedbackContent;
    }

    public void setFeedbackContent(String feedbackContent) {
        this.feedbackContent = feedbackContent;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}