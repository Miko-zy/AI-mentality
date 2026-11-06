package com.aimental.service;

import com.aimental.dto.LlmMessageDTO;

import java.util.List;
import java.util.function.Consumer;

/**
 * 大模型对话客户端统一接口
 * 业务层只依赖本接口，换厂商时新增实现或只改配置即可
 */
public interface LlmChatClient {

    /**
     * 是否启用了真实大模型调用（未配置 Key 时返回 false，走本地模拟）
     */
    boolean isRealCallEnabled();

    /**
     * 非流式对话
     *
     * @param messages 完整消息列表（含 system / 历史 / 当前用户消息）
     * @return AI 回复文本
     */
    String chat(List<LlmMessageDTO> messages);

    /**
     * 流式对话
     *
     * @param messages 完整消息列表
     * @param onDelta  增量文本回调
     * @throws Exception 调用失败时抛出，由业务层决定兜底策略
     */
    void streamChat(List<LlmMessageDTO> messages, Consumer<String> onDelta) throws Exception;
}
