package com.aimental.service;

import com.aimental.dto.CreateSessionDTO;
import com.aimental.dto.SendChatMessageDTO;
import com.aimental.vo.AiSessionVO;
import com.aimental.vo.ChatMessageVO;
import com.aimental.vo.PageVO;
import com.aimental.vo.SendMessageResultVO;

import java.util.List;

/**
 * AI陪伴对话服务接口
 * 负责会话管理、消息收发、历史查询、风险检测等业务
 */
public interface AiSessionService {

    /**
     * 创建AI会话
     *
     * @param createSessionDTO 创建会话请求
     * @return 创建的会话视图对象
     */
    AiSessionVO createSession(CreateSessionDTO createSessionDTO);

    /**
     * 发送对话消息（保存用户消息 + 调用大模型 + 保存AI回复 + 风险检测）
     *
     * @param sendChatMessageDTO 发送消息请求
     * @return 发送结果（含用户消息、AI回复、风险检测结果）
     */
    SendMessageResultVO sendMessage(SendChatMessageDTO sendChatMessageDTO);

    /**
     * 获取当前用户全部会话列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 会话列表分页结果
     */
    PageVO<AiSessionVO> getSessionList(Integer page, Integer size);

    /**
     * 获取某个会话下全部历史聊天消息
     *
     * @param sessionId 会话ID
     * @param page      页码
     * @param size      每页大小
     * @return 消息列表分页结果
     */
    PageVO<ChatMessageVO> getMessageHistory(Long sessionId, Integer page, Integer size);

    /**
     * 结束会话（逻辑删除，is_deleted=1）
     *
     * @param sessionId 会话ID
     */
    void endSession(Long sessionId);

    /**
     * 获取会话详情（用于校验权限）
     *
     * @param sessionId 会话ID
     * @return 会话实体
     */
    com.aimental.entity.AiSession getSessionById(Long sessionId);
}