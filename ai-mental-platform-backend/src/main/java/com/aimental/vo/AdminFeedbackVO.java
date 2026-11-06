package com.aimental.vo;

import com.aimental.entity.UserFeedback;

import java.time.LocalDateTime;

/**
 * 管理端用户反馈视图对象（含反馈用户信息）
 */
public class AdminFeedbackVO {

    /**
     * 反馈ID
     */
    private Long feedbackId;

    /**
     * 反馈用户ID
     */
    private Long userId;

    /**
     * 反馈用户昵称
     */
    private String userNickname;

    /**
     * 反馈内容
     */
    private String feedbackContent;

    /**
     * 状态：0待处理，1已回复，2关闭
     */
    private Integer status;

    /**
     * 状态中文描述
     */
    private String statusText;

    /**
     * 管理员回复内容
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

    /**
     * 由反馈实体转换为视图对象（不含用户昵称，由业务层补充）
     *
     * @param feedback 反馈实体
     * @return 管理端反馈视图对象
     */
    public static AdminFeedbackVO of(UserFeedback feedback) {
        AdminFeedbackVO vo = new AdminFeedbackVO();
        vo.setFeedbackId(feedback.getFeedbackId());
        vo.setUserId(feedback.getUserId());
        vo.setFeedbackContent(feedback.getFeedbackContent());
        vo.setStatus(feedback.getStatus());
        vo.setStatusText(statusText(feedback.getStatus()));
        vo.setReplyContent(feedback.getReplyContent());
        vo.setCreateTime(feedback.getCreateTime());
        vo.setUpdateTime(feedback.getUpdateTime());
        return vo;
    }

    /**
     * 反馈状态转中文
     *
     * @param status 状态
     * @return 中文描述
     */
    public static String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        if (status == 0) {
            return "待处理";
        }
        if (status == 1) {
            return "已回复";
        }
        if (status == 2) {
            return "关闭";
        }
        return "未知";
    }

    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
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

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
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
