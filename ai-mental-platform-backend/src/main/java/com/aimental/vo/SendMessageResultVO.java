package com.aimental.vo;

/**
 * 发送消息返回结果
 * 包含用户消息和AI回复
 */
public class SendMessageResultVO {

    /**
     * 用户消息
     */
    private ChatMessageVO userMessage;

    /**
     * AI回复消息
     */
    private ChatMessageVO aiMessage;

    /**
     * 风险检测结果：null=无风险，否则包含风险等级和描述
     */
    private RiskDetectResultVO riskDetectResult;

    public SendMessageResultVO() {
    }

    public ChatMessageVO getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(ChatMessageVO userMessage) {
        this.userMessage = userMessage;
    }

    public ChatMessageVO getAiMessage() {
        return aiMessage;
    }

    public void setAiMessage(ChatMessageVO aiMessage) {
        this.aiMessage = aiMessage;
    }

    public RiskDetectResultVO getRiskDetectResult() {
        return riskDetectResult;
    }

    public void setRiskDetectResult(RiskDetectResultVO riskDetectResult) {
        this.riskDetectResult = riskDetectResult;
    }

    /**
     * 风险检测结果视图对象
     */
    public static class RiskDetectResultVO {

        /**
         * 是否检测到风险
         */
        private Boolean hasRisk;

        /**
         * 风险等级：1低风险，2中风险，3高风险
         */
        private Integer riskLevel;

        /**
         * 风险描述
         */
        private String riskDesc;

        public RiskDetectResultVO() {
        }

        public Boolean getHasRisk() {
            return hasRisk;
        }

        public void setHasRisk(Boolean hasRisk) {
            this.hasRisk = hasRisk;
        }

        public Integer getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(Integer riskLevel) {
            this.riskLevel = riskLevel;
        }

        public String getRiskDesc() {
            return riskDesc;
        }

        public void setRiskDesc(String riskDesc) {
            this.riskDesc = riskDesc;
        }
    }
}