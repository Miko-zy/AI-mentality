package com.aimental.dto;

/**
 * 管理员启停评估量表请求DTO
 */
public class ScaleStatusDTO {

    /**
     * 启用状态：0停用，1启用
     */
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
