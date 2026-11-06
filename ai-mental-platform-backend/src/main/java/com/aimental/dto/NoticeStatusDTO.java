package com.aimental.dto;

/**
 * 系统公告状态修改请求DTO（发布/停用）
 */
public class NoticeStatusDTO {

    /**
     * 公告状态：0草稿，1已发布，2下架（必填）
     */
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
