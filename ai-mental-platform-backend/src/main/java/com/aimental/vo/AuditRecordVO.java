package com.aimental.vo;

import com.aimental.entity.ContentAuditRecord;

import java.time.LocalDateTime;

/**
 * 内容审核记录视图对象
 * 关联消息内容由业务层AES解密后填充
 */
public class AuditRecordVO {

    /**
     * 审核记录ID
     */
    private Long auditId;

    /**
     * 被审核消息ID
     */
    private Long msgId;

    /**
     * 消息所属会话ID
     */
    private Long sessionId;

    /**
     * 审核结果：1通过，2不通过
     */
    private Integer auditResult;

    /**
     * 审核结果中文描述
     */
    private String auditResultText;

    /**
     * 违规标记：0正常，1违规
     */
    private Integer violateFlag;

    /**
     * 违规标记中文描述
     */
    private String violateText;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 审核人（admin表ID）
     */
    private Long auditorId;

    /**
     * 审核人账号
     */
    private String auditorName;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 被审核消息内容（解密后的明文）
     */
    private String msgContent;

    /**
     * 被审核消息发送时间
     */
    private LocalDateTime msgSendTime;

    /**
     * 由审核记录实体转换为视图对象（消息内容解密由业务层完成）
     *
     * @param record 审核记录实体
     * @return 审核记录视图对象
     */
    public static AuditRecordVO of(ContentAuditRecord record) {
        AuditRecordVO vo = new AuditRecordVO();
        vo.setAuditId(record.getAuditId());
        vo.setMsgId(record.getMsgId());
        vo.setSessionId(record.getSessionId());
        vo.setAuditResult(record.getAuditResult());
        vo.setAuditResultText(record.getAuditResult() != null && record.getAuditResult() == 1 ? "通过" : "不通过");
        vo.setViolateFlag(record.getViolateFlag());
        vo.setViolateText(record.getViolateFlag() != null && record.getViolateFlag() == 1 ? "违规" : "正常");
        vo.setAuditRemark(record.getAuditRemark());
        vo.setAuditorId(record.getAuditorId());
        vo.setAuditTime(record.getAuditTime());
        return vo;
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
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

    public Integer getAuditResult() {
        return auditResult;
    }

    public void setAuditResult(Integer auditResult) {
        this.auditResult = auditResult;
    }

    public String getAuditResultText() {
        return auditResultText;
    }

    public void setAuditResultText(String auditResultText) {
        this.auditResultText = auditResultText;
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

    public String getAuditRemark() {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }

    public Long getAuditorId() {
        return auditorId;
    }

    public void setAuditorId(Long auditorId) {
        this.auditorId = auditorId;
    }

    public String getAuditorName() {
        return auditorName;
    }

    public void setAuditorName(String auditorName) {
        this.auditorName = auditorName;
    }

    public LocalDateTime getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public LocalDateTime getMsgSendTime() {
        return msgSendTime;
    }

    public void setMsgSendTime(LocalDateTime msgSendTime) {
        this.msgSendTime = msgSendTime;
    }
}
