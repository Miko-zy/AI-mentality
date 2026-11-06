package com.aimental.dto;

import java.math.BigDecimal;

/**
 * 管理员新增/编辑评估量表请求DTO
 * 参数格式校验在Service层执行
 */
public class ScaleSaveDTO {

    /**
     * 量表名称（1-100字符）
     */
    private String scaleName;

    /**
     * 量表编码（1-50字符，全局唯一）
     */
    private String scaleCode;

    /**
     * 维度说明（如：坚韧、力量、乐观三个维度，共25题）
     */
    private String dimensionDesc;

    /**
     * 总题目数
     */
    private Integer totalQuestionNum;

    /**
     * 公开信度系数（0-1之间，如0.890）
     */
    private BigDecimal reliability;

    /**
     * 启用状态：0停用，1启用（新增时缺省为1，编辑时由状态接口维护）
     */
    private Integer status;

    public String getScaleName() {
        return scaleName;
    }

    public void setScaleName(String scaleName) {
        this.scaleName = scaleName;
    }

    public String getScaleCode() {
        return scaleCode;
    }

    public void setScaleCode(String scaleCode) {
        this.scaleCode = scaleCode;
    }

    public String getDimensionDesc() {
        return dimensionDesc;
    }

    public void setDimensionDesc(String dimensionDesc) {
        this.dimensionDesc = dimensionDesc;
    }

    public Integer getTotalQuestionNum() {
        return totalQuestionNum;
    }

    public void setTotalQuestionNum(Integer totalQuestionNum) {
        this.totalQuestionNum = totalQuestionNum;
    }

    public BigDecimal getReliability() {
        return reliability;
    }

    public void setReliability(BigDecimal reliability) {
        this.reliability = reliability;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
