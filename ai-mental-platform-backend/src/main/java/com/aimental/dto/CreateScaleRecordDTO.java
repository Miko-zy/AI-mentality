package com.aimental.dto;

/**
 * 新建评估记录请求DTO
 * 用户选定量表后创建一条评估记录开始测评
 * 参数格式校验在Service层执行
 */
public class CreateScaleRecordDTO {

    /**
     * 量表ID
     */
    private Long scaleId;

    public Long getScaleId() {
        return scaleId;
    }

    public void setScaleId(Long scaleId) {
        this.scaleId = scaleId;
    }
}
