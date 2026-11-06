package com.aimental.vo;

import com.aimental.entity.ChatMessage;

import java.time.LocalDateTime;

/**
 * 管理端对话消息视图对象
 * 消息内容由业务层AES解密后填充
 */
public class MessageVO {

    /**
     * 消息ID
     */
    private Long msgId;

    /**
     * 所属会话ID
     */
    private Long sessionId;

    /**
     * 消息所属用户ID
     */
    private Long userId;

    /**
     * 发送方类型：0用户，1AI
     */
    private Integer senderType;

    /**
     * 发送方类型中文描述
     */
    private String senderText;

    /**
     * 消息内容（解密后的明文）
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
     * 内容审核状态中文描述
     */
    private String auditStatusText;

    /**
     * 违规标记：0正常，1违规
     */
    private Integer violateFlag;

    /**
     * 违规标记中文描述
     */
    private String violateText;

    /**
     * 由消息实体转换为视图对象（内容解密由业务层完成）
     *
     * @param message 消息实体
     * @return 消息视图对象
     */
    public static MessageVO of(ChatMessage message) {
        MessageVO vo = new MessageVO();
        vo.setMsgId(message.getMsgId());
        vo.setSessionId(message.getSessionId());
        vo.setSenderType(message.getSenderType());
        vo.setSenderText(message.getSenderType() != null && message.getSenderType() == 1 ? "AI" : "用户");
        vo.setSendTime(message.getSendTime());
        vo.setAuditStatus(message.getAuditStatus());
        vo.setAuditStatusText(statusText(message.getAuditStatus()));
        vo.setViolateFlag(message.getViolateFlag());
        vo.setViolateText(message.getViolateFlag() != null && message.getViolateFlag() == 1 ? "违规" : "正常");
        return vo;
    }

    /**
     * 审核状态转中文
     *
     * @param auditStatus 审核状态
     * @return 中文描述
     */
    public static String statusText(Integer auditStatus) {
        if (auditStatus == null) {
            return "未知";
        }
        if (auditStatus == 0) {
            return "待审";
        }
        if (auditStatus == 1) {
            return "通过";
        }
        if (auditStatus == 2) {
            return "不通过";
        }
        return "未知";
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getSenderType() {
        return senderType;
    }

    public void setSenderType(Integer senderType) {
        this.senderType = senderType;
    }

    public String getSenderText() {
        return senderText;
    }

    public void setSenderText(String senderText) {
        this.senderText = senderText;
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

    public String getAuditStatusText() {
        return auditStatusText;
    }

    public void setAuditStatusText(String auditStatusText) {
        this.auditStatusText = auditStatusText;
    }

    public Integer getViolateFlag() {
        return violateFlag;
    }

    public void setViolateFlag(Integer violateFlag) {
        this.violateFlag = violateFlag;
    }

    public String getViolateText() {
        return violateText;
    }

    public void setViolateText(String violateText) {
        this.violateText = violateText;
    }
}
