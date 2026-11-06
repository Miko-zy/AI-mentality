package com.aimental.vo;

import com.aimental.entity.CrisisRecord;

import java.time.LocalDateTime;

/**
 * 管理端危机事件视图对象
 * 用户昵称、关联消息内容由业务层解密后填充
 */
public class CrisisVO {

    /**
     * 危机记录ID
     */
    private Long crisisId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 关联消息ID
     */
    private Long msgId;

    /**
     * 关联消息内容（解密后的明文）
     */
    private String msgContent;

    /**
     * 风险等级1-3
     */
    private Integer riskLevel;

    /**
     * 风险等级中文描述
     */
    private String riskText;

    /**
     * 风险描述
     */
    private String riskDesc;

    /**
     * 处理状态：0待处理，1已处理
     */
    private Integer handleStatus;

    /**
     * 处理状态中文描述
     */
    private String handleStatusText;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 由危机记录实体转换为视图对象（敏感内容解密由业务层完成）
     *
     * @param crisis 危机记录实体
     * @return 危机事件视图对象
     */
    public static CrisisVO of(CrisisRecord crisis) {
        CrisisVO vo = new CrisisVO();
        vo.setCrisisId(crisis.getCrisisId());
        vo.setUserId(crisis.getUserId());
        vo.setMsgId(crisis.getMsgId());
        vo.setRiskLevel(crisis.getRiskLevel());
        vo.setRiskText(riskText(crisis.getRiskLevel()));
        vo.setRiskDesc(crisis.getRiskDesc());
        vo.setHandleStatus(crisis.getHandleStatus());
        vo.setHandleStatusText(crisis.getHandleStatus() != null && crisis.getHandleStatus() == 1 ? "已处理" : "待处理");
        vo.setHandleRemark(crisis.getHandleRemark());
        vo.setHandleTime(crisis.getHandleTime());
        vo.setCreateTime(crisis.getCreateTime());
        return vo;
    }

    /**
     * 风险等级转中文
     *
     * @param riskLevel 风险等级1-3
     * @return 中文描述
     */
    public static String riskText(Integer riskLevel) {
        if (riskLevel == null) {
            return "未知";
        }
        if (riskLevel == 1) {
            return "低风险";
        }
        if (riskLevel == 2) {
            return "中风险";
        }
        if (riskLevel == 3) {
            return "高风险";
        }
        return "未知";
    }

    public Long getCrisisId() {
        return crisisId;
    }

    public void setCrisisId(Long crisisId) {
        this.crisisId = crisisId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public Long getMsgId() {
        return msgId;
    }

    public void setMsgId(Long msgId) {
        this.msgId = msgId;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public Integer getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(Integer riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskText() {
        return riskText;
    }

    public void setRiskText(String riskText) {
        this.riskText = riskText;
    }

    public String getRiskDesc() {
        return riskDesc;
    }

    public void setRiskDesc(String riskDesc) {
        this.riskDesc = riskDesc;
    }

    public Integer getHandleStatus() {
        return handleStatus;
    }

    public void setHandleStatus(Integer handleStatus) {
        this.handleStatus = handleStatus;
    }

    public String getHandleStatusText() {
        return handleStatusText;
    }

    public void setHandleStatusText(String handleStatusText) {
        this.handleStatusText = handleStatusText;
    }

    public String getHandleRemark() {
        return handleRemark;
    }

    public void setHandleRemark(String handleRemark) {
        this.handleRemark = handleRemark;
    }

    public LocalDateTime getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(LocalDateTime handleTime) {
        this.handleTime = handleTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
