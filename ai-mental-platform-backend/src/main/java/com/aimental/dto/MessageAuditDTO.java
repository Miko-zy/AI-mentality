package com.aimental.dto;

/**
 * 对话消息内容审核请求DTO
 */
public class MessageAuditDTO {

    /**
     * 审核结果：1通过，2不通过（必填）
     */
    private Integer auditResult;

    /**
     * 违规标记：0正常，1违规（可选，缺省由审核结果推导：不通过记1，通过记0）
     */
    private Integer violateFlag;

    /**
     * 审核备注（可选，不超过500字）
     */
    private String auditRemark;

    public Integer getAuditResult() {
        return auditResult;
    }

    public void setAuditResult(Integer auditResult) {
        this.auditResult = auditResult;
    }

    public Integer getViolateFlag() {
        return violateFlag;
    }

    public void setViolateFlag(Integer violateFlag) {
        this.violateFlag = violateFlag;
    }

    public String getAuditRemark() {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }
}
