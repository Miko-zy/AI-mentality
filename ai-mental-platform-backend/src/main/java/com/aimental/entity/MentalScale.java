package com.aimental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 心理评估量表实体类（CD-RISC韧性量表）
 */
@TableName("mental_scale")
public class MentalScale implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 量表ID
     */
    @TableId(value = "scale_id", type = IdType.AUTO)
    private Long scaleId;

    /**
     * 量表名称
     */
    @TableField("scale_name")
    private String scaleName;

    /**
     * 量表编码
     */
    @TableField("scale_code")
    private String scaleCode;

    /**
     * 维度说明
     */
    @TableField("dimension_desc")
    private String dimensionDesc;

    /**
     * 总题目数
     */
    @TableField("total_question_num")
    private Integer totalQuestionNum;

    /**
     * 公开信度系数
     */
    @TableField("reliability")
    private BigDecimal reliability;

    /**
     * 启用状态：0停用，1启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 逻辑删除标记：0未删，1已删
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}
