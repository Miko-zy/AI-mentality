package com.aimental.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 大模型配置（OpenAI 兼容协议）
 * 所有厂商相关参数均通过配置注入，业务层不绑定具体厂商
 */
@Component
@ConfigurationProperties(prefix = "ai.llm")
public class AiLlmProperties {

    /**
     * 是否启用真实大模型调用：false 时使用本地模拟回复，便于无 Key 时跑通链路
     */
    private boolean enabled = false;

    /**
     * OpenAI 兼容接口基础地址（不带 /chat/completions）
     */
    private String baseUrl = "https://open.bigmodel.cn/api/paas/v4";

    /**
     * API Key，建议通过环境变量 AI_API_KEY 注入，禁止提交真实密钥
     */
    private String apiKey = "";

    /**
     * 模型名称（智谱免费模型 glm-4.7-flash）
     */
    private String model = "glm-4.7-flash";

    /**
     * 采样温度
     */
    private double temperature = 0.7;

    /**
     * 送入模型的历史对话轮数上限
     */
    private int maxHistoryRounds = 10;

    /**
     * 连接超时（秒）
     */
    private int connectTimeoutSeconds = 10;

    /**
     * 请求超时（秒）
     */
    private int requestTimeoutSeconds = 90;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxHistoryRounds() {
        return maxHistoryRounds;
    }

    public void setMaxHistoryRounds(int maxHistoryRounds) {
        this.maxHistoryRounds = maxHistoryRounds;
    }

    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
    }

    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }
}
