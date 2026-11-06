package com.aimental.vo;

import java.time.LocalDateTime;

/**
 * 对话消息视图对象
 * 用于历史消息列表返回
 */
public class ChatMessageVO {

    /**
     * 消息ID
     */
    private Long msgId;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 发送方类型：0用户，1AI
     */
    private Integer senderType;

    /**
     * 消息内容（已解密）
     */
    private String content;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 内容审核状态：0待审，1通过，2不通过
     */
    private Integer auditStatus;

    /**
     * 违规标记：0正常，1违规
     */
    private Integer violateFlag;

    public ChatMessageVO() {
    }

    public Long getMsgId() {
        return msgId;
    }

    public void setMsgId(Long msgId) {
        this.msgId = msgId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getSenderType() {
        return senderType;
    }

    public void setSenderType(Integer senderType) {
        this.senderType = senderType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public Integer getViolateFlag() {
        return violateFlag;
    }

    public void setViolateFlag(Integer violateFlag) {
        this.violateFlag = violateFlag;
    }
}