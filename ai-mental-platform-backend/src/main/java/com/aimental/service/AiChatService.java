package com.aimental.service;

import com.aimental.dto.CreateSessionDTO;
import com.aimental.dto.SendChatMessageDTO;
import com.aimental.dto.UpdateSessionDTO;
import com.aimental.vo.AiSessionVO;
import com.aimental.vo.ChatMessageVO;
import com.aimental.vo.PageVO;
import com.aimental.vo.SendMessageResultVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI陪伴对话服务接口
 * 包含会话管理、消息收发、风险检测等核心业务
 */
public interface AiChatService {

    /**
     * 创建AI会话
     *
     * @param userId           当前登录用户ID
     * @param createSessionDTO 创建会话请求参数
     * @return 创建的会话信息
     */
    AiSessionVO createSession(Long userId, CreateSessionDTO createSessionDTO);

    /**
     * 发送对话消息（用户消息入库 -> 调用大模型 -> AI回复入库 -> 风险检测）
     *
     * @param userId              当前登录用户ID
     * @param sendChatMessageDTO 发送消息请求参数
     * @return 发送结果（含用户消息、AI回复、风险检测结果）
     */
    SendMessageResultVO sendMessage(Long userId, SendChatMessageDTO sendChatMessageDTO);

    /**
     * 流式发送对话消息（SSE 优先，事件：meta / delta / done / error）
     *
     * @param userId              当前登录用户ID
     * @param sendChatMessageDTO 发送消息请求参数
     * @return SSE 发射器
     */
    SseEmitter streamMessage(Long userId, SendChatMessageDTO sendChatMessageDTO);

    /**
     * 更新会话名称或AI风格
     *
     * @param userId          当前登录用户ID
     * @param sessionId       会话ID
     * @param updateSessionDTO 更新参数
     * @return 更新后的会话信息
     */
    AiSessionVO updateSession(Long userId, Long sessionId, UpdateSessionDTO updateSessionDTO);

    /**
     * 获取当前用户全部会话列表
     *
     * @param userId 当前登录用户ID
     * @return 会话列表（按更新时间倒序）
     */
    List<AiSessionVO> getSessionList(Long userId);

    /**
     * 分页获取某个会话下的历史聊天消息
     *
     * @param userId   当前登录用户ID
     * @param sessionId 会话ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 消息分页结果
     */
    PageVO<ChatMessageVO> getMessageList(Long userId, Long sessionId, long pageNum, long pageSize);

    /**
     * 结束会话（逻辑删除）
     *
     * @param userId   当前登录用户ID
     * @param sessionId 会话ID
     */
    void endSession(Long userId, Long sessionId);
}