package com.aimental.dto;

/**
 * 危机事件处理请求DTO
 */
public class CrisisHandleDTO {

    /**
     * 处理状态：0待处理，1已处理（必填）
     */
    private Integer handleStatus;

    /**
     * 处理备注（可选，不超过500字）
     */
    private String handleRemark;

    public Integer getHandleStatus() {
        return handleStatus;
    }

    public void setHandleStatus(Integer handleStatus) {
        this.handleStatus = handleStatus;
    }

    public String getHandleRemark() {
        return handleRemark;
    }

    public void setHandleRemark(String handleRemark) {
        this.handleRemark = handleRemark;
    }
}
