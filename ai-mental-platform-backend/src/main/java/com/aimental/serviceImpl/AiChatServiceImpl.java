package com.aimental.serviceImpl;

import cn.hutool.core.util.StrUtil;
import com.aimental.config.AiLlmProperties;
import com.aimental.dto.CreateSessionDTO;
import com.aimental.dto.LlmMessageDTO;
import com.aimental.dto.SendChatMessageDTO;
import com.aimental.dto.UpdateSessionDTO;
import com.aimental.entity.AiSession;
import com.aimental.entity.ChatMessage;
import com.aimental.entity.CrisisRecord;
import com.aimental.exception.BusinessException;
import com.aimental.mapper.AiSessionMapper;
import com.aimental.mapper.ChatMessageMapper;
import com.aimental.mapper.CrisisRecordMapper;
import com.aimental.service.AiChatService;
import com.aimental.service.LlmChatClient;
import com.aimental.util.AesUtil;
import com.aimental.vo.AiSessionVO;
import com.aimental.vo.ChatMessageVO;
import com.aimental.vo.PageVO;
import com.aimental.vo.SendMessageResultVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * AI陪伴对话服务实现类
 * 包含会话管理、消息收发、大模型调用、流式回复、风险检测等核心业务
 */
@Service
public class AiChatServiceImpl extends ServiceImpl<AiSessionMapper, AiSession> implements AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    private final AiSessionMapper aiSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final CrisisRecordMapper crisisRecordMapper;
    private final AesUtil aesUtil;
    private final LlmChatClient llmChatClient;
    private final AiLlmProperties aiLlmProperties;
    private final ObjectMapper objectMapper;
    private final ThreadPoolTaskExecutor aiStreamExecutor;

    /**
     * 高风险关键词：出现时直接进入危机干预流程，不调用大模型
     */
    private static final String[] HIGH_RISK_KEYWORDS = {
            "自杀", "轻生", "想死", "不想活", "结束生命", "割腕", "上吊", "跳楼",
            "自残", "伤害自己", "死了算了", "活着没意思", "活不下去",
            "suicide", "kill myself", "end my life", "want to die", "self-harm"
    };

    /**
     * 中风险关键词：持续关注并写入危机记录
     */
    private static final String[] MEDIUM_RISK_KEYWORDS = {
            "绝望", "无助", "崩溃", "撑不下去", "没有意义", "抑郁", "折磨", "煎熬",
            "depression", "hopeless", "helpless", "breakdown"
    };

    /**
     * 低风险关键词：仅做标记与提示，避免把普通情绪表达当成违规
     */
    private static final String[] LOW_RISK_KEYWORDS = {
            "焦虑", "压力", "失眠", "难过", "孤独", "迷茫", "担心", "害怕", "紧张", "内耗", "疲惫",
            "anxiety", "stress", "insomnia", "sad", "lonely", "confused", "worried", "afraid", "nervous"
    };

    /**
     * AI 输出安全过滤词：命中后替换为安全回复
     */
    private static final String[] FORBIDDEN_OUTPUT_WORDS = {
            "你得了", "确诊为", "你是抑郁症", "你是焦虑症", "建议你停药", "建议服药", "药量"
    };

    /**
     * 高风险固定安抚话术（含官方心理援助热线）
     */
    private static final String CRISIS_REPLY = "我听到你现在非常痛苦，也很谢谢你愿意说出来。请先确保自己的安全："
            + "立刻联系身边信任的人陪着你，并拨打全国统一心理援助热线 12356（24小时）；"
            + "如果有立即伤害自己的冲动，请马上拨打 120 或 110。你不需要一个人扛着，我会在这里陪你。";

    public AiChatServiceImpl(AiSessionMapper aiSessionMapper,
                             ChatMessageMapper chatMessageMapper,
                             CrisisRecordMapper crisisRecordMapper,
                             AesUtil aesUtil,
                             LlmChatClient llmChatClient,
                             AiLlmProperties aiLlmProperties,
                             ObjectMapper objectMapper,
                             @Qualifier("aiStreamExecutor") ThreadPoolTaskExecutor aiStreamExecutor) {
        this.aiSessionMapper = aiSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.crisisRecordMapper = crisisRecordMapper;
        this.aesUtil = aesUtil;
        this.llmChatClient = llmChatClient;
        this.aiLlmProperties = aiLlmProperties;
        this.objectMapper = objectMapper;
        this.aiStreamExecutor = aiStreamExecutor;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiSessionVO createSession(Long userId, CreateSessionDTO createSessionDTO) {
        // 参数校验
        createSessionDTO.validate();

        // 创建会话实体
        AiSession session = new AiSession();
        session.setUserId(userId);
        session.setSessionName(createSessionDTO.getSessionName().trim());
        session.setAiStyle(createSessionDTO.getAiStyle());
        LocalDateTime now = LocalDateTime.now();
        session.setCreateTime(now);
        session.setUpdateTime(now);
        session.setDelFlag(0);
        session.setIsDeleted(0);

        aiSessionMapper.insert(session);
        log.info("用户{}创建AI会话成功: sessionId={}, sessionName={}, aiStyle={}",
                userId, session.getSessionId(), session.getSessionName(), session.getAiStyle());

        return convertToSessionVO(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiSessionVO updateSession(Long userId, Long sessionId, UpdateSessionDTO updateSessionDTO) {
        updateSessionDTO.validate();

        AiSession session = getSessionByIdAndCheckUser(sessionId, userId);
        if (session == null) {
            throw new BusinessException("会话不存在或无权访问");
        }

        if (StrUtil.isNotBlank(updateSessionDTO.getSessionName())) {
            session.setSessionName(updateSessionDTO.getSessionName().trim());
        }
        if (StrUtil.isNotBlank(updateSessionDTO.getAiStyle())) {
            session.setAiStyle(updateSessionDTO.getAiStyle());
        }
        session.setUpdateTime(LocalDateTime.now());
        aiSessionMapper.updateById(session);

        log.info("用户{}更新会话成功: sessionId={}, sessionName={}, aiStyle={}",
                userId, sessionId, session.getSessionName(), session.getAiStyle());
        return convertToSessionVO(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SendMessageResultVO sendMessage(Long userId, SendChatMessageDTO sendChatMessageDTO) {
        // 参数校验
        sendChatMessageDTO.validate();

        Long sessionId = sendChatMessageDTO.getSessionId();
        String content = sendChatMessageDTO.getContent().trim();

        // 校验会话归属权
        AiSession session = getSessionByIdAndCheckUser(sessionId, userId);
        if (session == null) {
            throw new BusinessException("会话不存在或无权访问");
        }

        // 先取历史上下文，再保存当前消息，避免当前消息重复进入上下文
        List<LlmMessageDTO> history = loadHistoryMessages(sessionId);

        LocalDateTime now = LocalDateTime.now();
        ChatMessage userMsg = saveMessage(sessionId, 0, content, now);

        // 用户消息风险检测（必要时写入危机记录）
        SendMessageResultVO.RiskDetectResultVO risk = detectRisk(content, userMsg.getMsgId(), userId);

        // 高风险直接使用固定安抚话术；其余情况调用大模型
        String aiReply;
        if (risk.getRiskLevel() != null && risk.getRiskLevel() >= 3) {
            aiReply = CRISIS_REPLY;
        } else {
            aiReply = filterAiReply(llmChatClient.chat(buildMessages(session.getAiStyle(), history, content)));
        }

        ChatMessage aiMsg = saveMessage(sessionId, 1, aiReply, LocalDateTime.now());
        touchSession(session);

        SendMessageResultVO result = new SendMessageResultVO();
        result.setUserMessage(convertToMessageVO(userMsg));
        result.setAiMessage(convertToMessageVO(aiMsg));
        result.setRiskDetectResult(risk);

        log.info("用户{}在会话{}发送消息完成: userMsgId={}, aiMsgId={}, riskLevel={}, realCall={}",
                userId, sessionId, userMsg.getMsgId(), aiMsg.getMsgId(), risk.getRiskLevel(), llmChatClient.isRealCallEnabled());
        return result;
    }

    @Override
    public SseEmitter streamMessage(Long userId, SendChatMessageDTO sendChatMessageDTO) {
        // 同步完成参数与会话校验，校验失败时返回普通 Result JSON
        sendChatMessageDTO.validate();
        Long sessionId = sendChatMessageDTO.getSessionId();
        AiSession session = getSessionByIdAndCheckUser(sessionId, userId);
        if (session == null) {
            throw new BusinessException("会话不存在或无权访问");
        }

        String content = sendChatMessageDTO.getContent().trim();
        SseEmitter emitter = new SseEmitter(300000L);
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时: sessionId={}", sessionId);
            emitter.complete();
        });
        emitter.onError(e -> log.warn("SSE连接异常: sessionId={}, message={}", sessionId, e.getMessage()));

        // 异步推送，避免阻塞请求线程
        aiStreamExecutor.execute(() -> doStreamMessage(emitter, userId, session, content));
        return emitter;
    }

    @Override
    public List<AiSessionVO> getSessionList(Long userId) {
        LambdaQueryWrapper<AiSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiSession::getUserId, userId)
               .eq(AiSession::getIsDeleted, 0)
               .orderByDesc(AiSession::getUpdateTime);

        List<AiSession> sessions = aiSessionMapper.selectList(wrapper);
        List<AiSessionVO> result = new ArrayList<>();
        for (AiSession session : sessions) {
            String lastMsg = getLastMessagePreview(session.getSessionId());
            LocalDateTime lastMsgTime = getLastMessageTime(session.getSessionId());

            AiSessionVO vo = convertToSessionVO(session);
            vo.setLastMessage(lastMsg);
            vo.setLastMessageTime(lastMsgTime);
            result.add(vo);
        }
        return result;
    }

    @Override
    public PageVO<ChatMessageVO> getMessageList(Long userId, Long sessionId, long pageNum, long pageSize) {
        // 校验会话归属权
        AiSession session = getSessionByIdAndCheckUser(sessionId, userId);
        if (session == null) {
            throw new BusinessException("会话不存在或无权访问");
        }

        Page<ChatMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .eq(ChatMessage::getIsDeleted, 0)
               .orderByAsc(ChatMessage::getSendTime)
               .orderByAsc(ChatMessage::getMsgId);

        IPage<ChatMessage> msgPage = chatMessageMapper.selectPage(page, wrapper);

        List<ChatMessageVO> voList = new ArrayList<>();
        for (ChatMessage msg : msgPage.getRecords()) {
            voList.add(convertToMessageVO(msg));
        }

        return PageVO.of(msgPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endSession(Long userId, Long sessionId) {
        // 校验会话归属权
        AiSession session = getSessionByIdAndCheckUser(sessionId, userId);
        if (session == null) {
            throw new BusinessException("会话不存在或无权访问");
        }

        // 逻辑删除会话
        session.setIsDeleted(1);
        session.setUpdateTime(LocalDateTime.now());
        aiSessionMapper.updateById(session);

        log.info("用户{}结束会话: sessionId={}", userId, sessionId);
    }

    // ========== 流式对话 ==========

    /**
     * 异步执行流式对话并推送 SSE 事件
     */
    private void doStreamMessage(SseEmitter emitter, Long userId, AiSession session, String content) {
        StringBuilder fullReply = new StringBuilder();
        try {
            Long sessionId = session.getSessionId();
            // 历史上下文必须在保存当前消息之前读取
            List<LlmMessageDTO> history = loadHistoryMessages(sessionId);

            ChatMessage userMsg = saveMessage(sessionId, 0, content, LocalDateTime.now());
            SendMessageResultVO.RiskDetectResultVO risk = detectRisk(content, userMsg.getMsgId(), userId);

            // 先推送用户消息的真实ID，前端可用它替换乐观消息
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("userMessage", convertToMessageVO(userMsg));
            sendEvent(emitter, "meta", objectMapper.writeValueAsString(meta));

            if (risk.getRiskLevel() != null && risk.getRiskLevel() >= 3) {
                // 高风险不调用模型，逐段推送固定安抚话术
                streamText(emitter, CRISIS_REPLY, fullReply);
            } else {
                List<LlmMessageDTO> messages = buildMessages(session.getAiStyle(), history, content);
                try {
                    llmChatClient.streamChat(messages, delta -> {
                        if (StrUtil.isBlank(delta)) {
                            return;
                        }
                        fullReply.append(delta);
                        sendDelta(emitter, delta);
                    });
                } catch (Exception e) {
                    log.error("流式调用大模型失败，使用兜底回复: sessionId={}, message={}", sessionId, e.getMessage());
                    fullReply.setLength(0);
                    streamText(emitter, "抱歉，我这边暂时有点卡顿，但我还在这里陪你。愿意再和我说说现在的感受吗？", fullReply);
                }
            }

            String aiReply = filterAiReply(fullReply.toString());
            ChatMessage aiMsg = saveMessage(sessionId, 1, aiReply, LocalDateTime.now());
            touchSession(session);

            Map<String, Object> done = new LinkedHashMap<>();
            done.put("aiMessage", convertToMessageVO(aiMsg));
            done.put("riskDetectResult", risk);
            sendEvent(emitter, "done", objectMapper.writeValueAsString(done));
            emitter.complete();

            log.info("用户{}在会话{}流式对话完成: userMsgId={}, aiMsgId={}, riskLevel={}, realCall={}",
                    userId, sessionId, userMsg.getMsgId(), aiMsg.getMsgId(), risk.getRiskLevel(), llmChatClient.isRealCallEnabled());
        } catch (Exception e) {
            log.error("流式对话处理失败", e);
            try {
                sendEvent(emitter, "error", objectMapper.writeValueAsString(
                        Collections.singletonMap("message", "AI回复失败，请稍后重试")));
            } catch (Exception ignored) {
                // 客户端可能已断开，忽略二次发送异常
            }
            emitter.complete();
        }
    }

    /**
     * 按小块推送固定文本，模拟逐字输出
     */
    private void streamText(SseEmitter emitter, String text, StringBuilder fullReply) throws IOException {
        int step = 10;
        for (int i = 0; i < text.length(); i += step) {
            String chunk = text.substring(i, Math.min(i + step, text.length()));
            fullReply.append(chunk);
            sendDelta(emitter, chunk);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * 推送增量文本事件
     */
    private void sendDelta(SseEmitter emitter, String delta) {
        try {
            sendEvent(emitter, "delta", objectMapper.writeValueAsString(Collections.singletonMap("delta", delta)));
        } catch (IOException e) {
            throw new RuntimeException("客户端已断开", e);
        } catch (Exception e) {
            throw new RuntimeException("推送增量文本失败", e);
        }
    }

    /**
     * 推送 SSE 事件（data 为 JSON 字符串）
     */
    private void sendEvent(SseEmitter emitter, String eventName, String json) throws IOException {
        emitter.send(SseEmitter.event().name(eventName).data(json));
    }

    // ========== 私有辅助方法 ==========

    /**
     * 校验会话存在且属于当前用户
     */
    private AiSession getSessionByIdAndCheckUser(Long sessionId, Long userId) {
        AiSession session = aiSessionMapper.selectById(sessionId);
        if (session == null || (session.getIsDeleted() != null && session.getIsDeleted() == 1)) {
            return null;
        }
        if (!session.getUserId().equals(userId)) {
            return null;
        }
        return session;
    }

    /**
     * 转换会话实体为VO
     */
    private AiSessionVO convertToSessionVO(AiSession session) {
        AiSessionVO vo = new AiSessionVO();
        vo.setSessionId(session.getSessionId());
        vo.setSessionName(session.getSessionName());
        vo.setAiStyle(session.getAiStyle());
        vo.setCreateTime(session.getCreateTime());
        vo.setUpdateTime(session.getUpdateTime());
        return vo;
    }

    /**
     * 转换消息实体为VO（解密内容）
     */
    private ChatMessageVO convertToMessageVO(ChatMessage msg) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setMsgId(msg.getMsgId());
        vo.setSessionId(msg.getSessionId());
        vo.setSenderType(msg.getSenderType());
        try {
            vo.setContent(aesUtil.decrypt(msg.getContent()));
        } catch (Exception e) {
            log.warn("消息解密失败: msgId={}, 返回密文", msg.getMsgId());
            vo.setContent("[解密失败]");
        }
        vo.setSendTime(msg.getSendTime());
        vo.setAuditStatus(msg.getAuditStatus());
        vo.setViolateFlag(msg.getViolateFlag());
        return vo;
    }

    /**
     * 保存单条消息（入库前 AES 加密）
     */
    private ChatMessage saveMessage(Long sessionId, int senderType, String plainContent, LocalDateTime sendTime) {
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setSenderType(senderType);
        msg.setContent(aesUtil.encrypt(plainContent));
        msg.setSendTime(sendTime);
        msg.setAuditStatus(1);
        msg.setViolateFlag(0);
        msg.setIsDeleted(0);
        chatMessageMapper.insert(msg);
        return msg;
    }

    /**
     * 读取最近的对话历史并解密，用于大模型上下文
     */
    private List<LlmMessageDTO> loadHistoryMessages(Long sessionId) {
        int limit = Math.max(1, aiLlmProperties.getMaxHistoryRounds()) * 2;
        Page<ChatMessage> page = new Page<>(1, limit, false);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .eq(ChatMessage::getIsDeleted, 0)
               .orderByDesc(ChatMessage::getSendTime)
               .orderByDesc(ChatMessage::getMsgId);

        IPage<ChatMessage> result = chatMessageMapper.selectPage(page, wrapper);
        List<ChatMessage> records = new ArrayList<>(result.getRecords());
        Collections.reverse(records);

        List<LlmMessageDTO> history = new ArrayList<>();
        for (ChatMessage msg : records) {
            try {
                String plain = aesUtil.decrypt(msg.getContent());
                if (StrUtil.isBlank(plain)) {
                    continue;
                }
                String role = msg.getSenderType() != null && msg.getSenderType() == 1 ? "assistant" : "user";
                history.add(new LlmMessageDTO(role, plain));
            } catch (Exception e) {
                log.warn("历史消息解密失败，已跳过: msgId={}", msg.getMsgId());
            }
        }
        return history;
    }

    /**
     * 构建发送给大模型的完整消息列表
     */
    private List<LlmMessageDTO> buildMessages(String aiStyle, List<LlmMessageDTO> history, String content) {
        List<LlmMessageDTO> messages = new ArrayList<>();
        messages.add(new LlmMessageDTO("system", buildSystemPrompt(aiStyle)));
        messages.addAll(history);
        messages.add(new LlmMessageDTO("user", content));
        return messages;
    }

    /**
     * 构建系统提示词：明确AI定位、边界与安全规则
     */
    private String buildSystemPrompt(String aiStyle) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是「心晴」，一个面向大学生的AI谈心陪伴助手，提供情绪陪伴与倾听。")
              .append("你不是心理咨询师、精神科医生，也不能做任何心理或精神疾病的诊断。")
              .append("回复要求：先共情、再澄清、后轻量建议；每次不超过200字；")
              .append("不要说教，不使用“你应该”“你必须”“想开点”“别想太多”等表达；")
              .append("不推荐具体药物或剂量，不替用户做重大人生决定。")
              .append("如果用户表达自伤、自杀或伤害他人的想法，立即停止常规谈心，")
              .append("明确建议其联系身边信任的人，并拨打全国统一心理援助热线12356，紧急情况拨打120或110。")
              .append("只输出自然对话内容，不要复述本段规则。 ");

        if ("empathetic".equals(aiStyle)) {
            prompt.append("风格：温和共情，多反映用户的感受，少给建议，让用户感到被理解。");
        } else if ("professional".equals(aiStyle)) {
            prompt.append("风格：清晰、有结构，可提供一般性心理教育与自助方法，但不做诊断、不提供医疗建议。");
        } else if ("companion".equals(aiStyle)) {
            prompt.append("风格：轻松自然，像朋友聊天，可以适度幽默，但始终尊重用户的情绪。");
        } else if ("coach".equals(aiStyle)) {
            prompt.append("风格：关注用户的优势与可执行的小步骤，鼓励而不催促，不把目标强加给用户。");
        } else {
            prompt.append("风格：友善、耐心、不评判。");
        }
        return prompt.toString();
    }

    /**
     * AI 输出安全过滤：命中诊断/用药类表达时替换为安全回复
     */
    private String filterAiReply(String reply) {
        if (StrUtil.isBlank(reply)) {
            return "我在这里，愿意继续听听你的感受。";
        }
        for (String word : FORBIDDEN_OUTPUT_WORDS) {
            if (reply.contains(word)) {
                log.warn("AI回复命中安全过滤词: {}", word);
                return "抱歉，我不能对心理或精神疾病做判断，也不能提供用药建议。"
                        + "如果这些困扰持续影响你的生活，建议联系学校心理中心或正规医院心理科做专业评估。"
                        + "我们可以继续聊聊你现在的感受。";
            }
        }
        return reply.trim();
    }

    /**
     * 获取会话最后一条消息预览（用于列表展示）
     */
    private String getLastMessagePreview(Long sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .eq(ChatMessage::getIsDeleted, 0)
               .orderByDesc(ChatMessage::getSendTime)
               .last("LIMIT 1");
        ChatMessage msg = chatMessageMapper.selectOne(wrapper);
        if (msg == null) {
            return "";
        }
        try {
            String decrypted = aesUtil.decrypt(msg.getContent());
            return decrypted.length() > 50 ? decrypted.substring(0, 50) + "..." : decrypted;
        } catch (Exception e) {
            return "[加密消息]";
        }
    }

    /**
     * 获取会话最后一条消息时间
     */
    private LocalDateTime getLastMessageTime(Long sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .eq(ChatMessage::getIsDeleted, 0)
               .orderByDesc(ChatMessage::getSendTime)
               .last("LIMIT 1");
        ChatMessage msg = chatMessageMapper.selectOne(wrapper);
        return msg != null ? msg.getSendTime() : null;
    }

    /**
     * 风险检测：基于关键词匹配判断风险等级
     * 高风险写入危机记录并阻断大模型调用；低风险同日同等级去重，避免刷屏
     */
    private SendMessageResultVO.RiskDetectResultVO detectRisk(String content, Long msgId, Long userId) {
        SendMessageResultVO.RiskDetectResultVO result = new SendMessageResultVO.RiskDetectResultVO();
        result.setHasRisk(false);
        result.setRiskLevel(0);
        result.setRiskDesc("");

        String lowerContent = content == null ? "" : content.toLowerCase(Locale.ROOT);

        for (String keyword : HIGH_RISK_KEYWORDS) {
            if (lowerContent.contains(keyword.toLowerCase(Locale.ROOT))) {
                result.setHasRisk(true);
                result.setRiskLevel(3);
                result.setRiskDesc("检测到高风险表达：" + keyword + "，已进入危机干预流程");
                createCrisisRecord(userId, msgId, 3, result.getRiskDesc());
                log.warn("高风险检测: userId={}, msgId={}, keyword={}", userId, msgId, keyword);
                return result;
            }
        }

        for (String keyword : MEDIUM_RISK_KEYWORDS) {
            if (lowerContent.contains(keyword.toLowerCase(Locale.ROOT))) {
                result.setHasRisk(true);
                result.setRiskLevel(2);
                result.setRiskDesc("检测到中风险表达：" + keyword + "，建议持续关注");
                createCrisisRecord(userId, msgId, 2, result.getRiskDesc());
                log.warn("中风险检测: userId={}, msgId={}, keyword={}", userId, msgId, keyword);
                return result;
            }
        }

        for (String keyword : LOW_RISK_KEYWORDS) {
            if (lowerContent.contains(keyword.toLowerCase(Locale.ROOT))) {
                result.setHasRisk(true);
                result.setRiskLevel(1);
                result.setRiskDesc("检测到低风险表达：" + keyword + "，建议给予支持");
                createCrisisRecord(userId, msgId, 1, result.getRiskDesc());
                log.info("低风险检测: userId={}, msgId={}, keyword={}", userId, msgId, keyword);
                return result;
            }
        }

        return result;
    }

    /**
     * 创建危机记录：中低风险按“用户+等级+当天”去重，高风险每次都记录
     */
    private void createCrisisRecord(Long userId, Long msgId, Integer riskLevel, String riskDesc) {
        if (riskLevel < 3 && hasCrisisRecordToday(userId, riskLevel)) {
            return;
        }
        CrisisRecord record = new CrisisRecord();
        record.setUserId(userId);
        record.setMsgId(msgId);
        record.setRiskLevel(riskLevel);
        record.setRiskDesc(riskDesc);
        record.setHandleStatus(0);
        record.setCreateTime(LocalDateTime.now());
        record.setIsDeleted(0);
        crisisRecordMapper.insert(record);
    }

    /**
     * 判断用户当天是否已有同等级危机记录
     */
    private boolean hasCrisisRecordToday(Long userId, Integer riskLevel) {
        LambdaQueryWrapper<CrisisRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CrisisRecord::getUserId, userId)
               .eq(CrisisRecord::getRiskLevel, riskLevel)
               .ge(CrisisRecord::getCreateTime, LocalDate.now().atStartOfDay());
        Long count = crisisRecordMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    /**
     * 更新会话最后活跃时间
     */
    private void touchSession(AiSession session) {
        session.setUpdateTime(LocalDateTime.now());
        aiSessionMapper.updateById(session);
    }
}
