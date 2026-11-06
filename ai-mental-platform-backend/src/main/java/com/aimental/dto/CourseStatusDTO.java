package com.aimental.dto;

/**
 * 管理员课程上架/下架请求DTO
 * 对应接口：PUT /api/admin/course/{courseId}/status
 */
public class CourseStatusDTO {

    /**
     * 目标状态：0下架，1正常（必填）
     */
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
