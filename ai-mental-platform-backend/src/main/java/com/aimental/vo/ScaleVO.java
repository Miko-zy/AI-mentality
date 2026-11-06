package com.aimental.vo;

import com.aimental.entity.MentalScale;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评估量表VO（列表展示，用户端与管理端共用）
 */
public class ScaleVO {

    /**
     * 量表ID
     */
    private Long scaleId;

    /**
     * 量表名称
     */
    private String scaleName;

    /**
     * 量表编码
     */
    private String scaleCode;

    /**
     * 维度说明
     */
    private String dimensionDesc;

    /**
     * 总题目数
     */
    private Integer totalQuestionNum;

    /**
     * 公开信度系数
     */
    private BigDecimal reliability;

    /**
     * 启用状态：0停用，1启用
     */
    private Integer status;

    /**
     * 状态文字：启用/停用
     */
    private String statusText;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 由量表实体转换为VO
     *
     * @param scale 量表实体
     * @return 量表VO
     */
    public static ScaleVO of(MentalScale scale) {
        ScaleVO vo = new ScaleVO();
        vo.setScaleId(scale.getScaleId());
        vo.setScaleName(scale.getScaleName());
        vo.setScaleCode(scale.getScaleCode());
        vo.setDimensionDesc(scale.getDimensionDesc());
        vo.setTotalQuestionNum(scale.getTotalQuestionNum());
        vo.setReliability(scale.getReliability());
        vo.setStatus(scale.getStatus());
        vo.setStatusText(scale.getStatus() != null && scale.getStatus() == 1 ? "启用" : "停用");
        vo.setCreateTime(scale.getCreateTime());
        return vo;
    }

    public Long getScaleId() {
        return scaleId;
    }

    public void setScaleId(Long scaleId) {
        this.scaleId = scaleId;
    }

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

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
