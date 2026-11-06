package com.aimental.controller;

import com.aimental.common.Result;
import com.aimental.dto.CreateSessionDTO;
import com.aimental.dto.SendChatMessageDTO;
import com.aimental.dto.UpdateSessionDTO;
import com.aimental.security.SecurityUtil;
import com.aimental.service.AiChatService;
import com.aimental.vo.AiSessionVO;
import com.aimental.vo.ChatMessageVO;
import com.aimental.vo.PageVO;
import com.aimental.vo.SendMessageResultVO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI陪伴对话控制器
 * 提供会话管理、消息收发、历史查询等RESTful接口
 * 全部接口需要JWT鉴权，用户只能访问自己的会话和消息
 */
@RestController
@RequestMapping("/ai/session")
public class AiSessionController {

    private final AiChatService aiChatService;

    public AiSessionController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /**
     * 创建AI会话
     *
     * @param createSessionDTO 创建会话请求（sessionName、aiStyle）
     * @return 创建的会话信息
     */
    @PostMapping
    public Result<AiSessionVO> createSession(@RequestBody CreateSessionDTO createSessionDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        AiSessionVO sessionVO = aiChatService.createSession(userId, createSessionDTO);
        return Result.success("创建会话成功", sessionVO);
    }

    /**
     * 发送对话消息
     * 流程：保存用户消息 -> 调用大模型 -> 保存AI回复 -> 风险检测 -> 返回结果
     *
     * @param sendChatMessageDTO 发送消息请求（sessionId、content）
     * @return 发送结果（含用户消息、AI回复、风险检测结果）
     */
    @PostMapping("/message")
    public Result<SendMessageResultVO> sendMessage(@RequestBody SendChatMessageDTO sendChatMessageDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        SendMessageResultVO result = aiChatService.sendMessage(userId, sendChatMessageDTO);
        return Result.success("发送成功", result);
    }

    /**
     * 流式发送对话消息（SSE）
     * 事件协议：meta（用户消息）/ delta（增量文本）/ done（AI消息+风险结果）/ error（错误）
     *
     * @param sendChatMessageDTO 发送消息请求（sessionId、content）
     * @return SSE 发射器
     */
    @PostMapping(value = "/message/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessage(@RequestBody SendChatMessageDTO sendChatMessageDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        return aiChatService.streamMessage(userId, sendChatMessageDTO);
    }

    /**
     * 更新会话（改名 / 切换AI风格）
     *
     * @param sessionId        会话ID
     * @param updateSessionDTO 更新参数（sessionName、aiStyle 至少传一个）
     * @return 更新后的会话信息
     */
    @PutMapping("/{sessionId}")
    public Result<AiSessionVO> updateSession(@PathVariable Long sessionId,
                                             @RequestBody UpdateSessionDTO updateSessionDTO) {
        Long userId = SecurityUtil.getCurrentUserId();
        AiSessionVO sessionVO = aiChatService.updateSession(userId, sessionId, updateSessionDTO);
        return Result.success("会话已更新", sessionVO);
    }

    /**
     * 获取当前用户全部会话列表
     * 按最后更新时间倒序排列，包含最后一条消息预览
     *
     * @return 会话列表
     */
    @GetMapping
    public Result<List<AiSessionVO>> getSessionList() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<AiSessionVO> sessions = aiChatService.getSessionList(userId);
        return Result.success(sessions);
    }

    /**
     * 分页获取某个会话下的历史聊天消息
     *
     * @param sessionId 会话ID
     * @param page      页码（默认1）
     * @param size      每页条数（默认20，最大100）
     * @return 消息分页结果
     */
    @GetMapping("/{sessionId}/messages")
    public Result<PageVO<ChatMessageVO>> getMessageHistory(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        // 校验分页参数
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 100) size = 100;

        PageVO<ChatMessageVO> messagePage = aiChatService.getMessageList(userId, sessionId, page, size);
        return Result.success(messagePage);
    }

    /**
     * 结束会话（逻辑删除，is_deleted=1）
     * 结束后会话不再出现在列表中，但历史消息保留
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/{sessionId}")
    public Result<Void> endSession(@PathVariable Long sessionId) {
        Long userId = SecurityUtil.getCurrentUserId();
        aiChatService.endSession(userId, sessionId);
        return Result.success();
    }
}