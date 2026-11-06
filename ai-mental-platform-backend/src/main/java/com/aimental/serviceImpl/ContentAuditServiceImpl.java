package com.aimental.serviceImpl;

import com.aimental.dto.MessageAuditDTO;
import com.aimental.entity.Admin;
import com.aimental.entity.AiSession;
import com.aimental.entity.ChatMessage;
import com.aimental.entity.ContentAuditRecord;
import com.aimental.exception.BusinessException;
import com.aimental.mapper.AdminMapper;
import com.aimental.mapper.AiSessionMapper;
import com.aimental.mapper.ChatMessageMapper;
import com.aimental.mapper.ContentAuditRecordMapper;
import com.aimental.service.ContentAuditService;
import com.aimental.util.AesUtil;
import com.aimental.vo.AuditRecordVO;
import com.aimental.vo.MessageVO;
import com.aimental.vo.PageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 内容审核服务实现类
 *
 * 消息查询：分页 + 审核状态筛选，批量补全会话归属用户、解密消息内容；
 * 执行审核：更新chat_message的audit_status/violate_flag，并插入content_audit_record留痕（允许重复审核，历史记录保留）；
 * 记录查询：分页 + 按消息ID筛选，批量补全被审消息内容与审核人账号
 */
@Service
public class ContentAuditServiceImpl implements ContentAuditService {

    private static final Logger log = LoggerFactory.getLogger(ContentAuditServiceImpl.class);

    /**
     * 对话消息Mapper
     */
    private final ChatMessageMapper chatMessageMapper;

    /**
     * AI会话Mapper
     */
    private final AiSessionMapper aiSessionMapper;

    /**
     * 内容审核记录Mapper
     */
    private final ContentAuditRecordMapper contentAuditRecordMapper;

    /**
     * 管理员Mapper
     */
    private final AdminMapper adminMapper;

    /**
     * AES加解密工具
     */
    private final AesUtil aesUtil;

    /**
     * 构造方法注入
     *
     * @param chatMessageMapper        对话消息Mapper
     * @param aiSessionMapper          AI会话Mapper
     * @param contentAuditRecordMapper 内容审核记录Mapper
     * @param adminMapper              管理员Mapper
     * @param aesUtil                  AES加解密工具
     */
    public ContentAuditServiceImpl(ChatMessageMapper chatMessageMapper,
                                   AiSessionMapper aiSessionMapper,
                                   ContentAuditRecordMapper contentAuditRecordMapper,
                                   AdminMapper adminMapper,
                                   AesUtil aesUtil) {
        this.chatMessageMapper = chatMessageMapper;
        this.aiSessionMapper = aiSessionMapper;
        this.contentAuditRecordMapper = contentAuditRecordMapper;
        this.adminMapper = adminMapper;
        this.aesUtil = aesUtil;
    }

    @Override
    public PageVO<MessageVO> getMessages(long pageNum, long pageSize, Integer auditStatus) {
        Page<ChatMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();

        // 1. 审核状态筛选（0待审/1通过/2不通过）
        if (auditStatus != null) {
            if (auditStatus != AUDIT_STATUS_PENDING && auditStatus != AUDIT_STATUS_PASS
                    && auditStatus != AUDIT_STATUS_REJECT) {
                throw new IllegalArgumentException("审核状态只能为0待审、1通过或2不通过");
            }
            wrapper.eq(ChatMessage::getAuditStatus, auditStatus);
        }
        wrapper.orderByDesc(ChatMessage::getMsgId);
        Page<ChatMessage> result = chatMessageMapper.selectPage(page, wrapper);

        // 2. 转VO并解密消息内容
        List<MessageVO> list = result.getRecords().stream().map(message -> {
            MessageVO vo = MessageVO.of(message);
            vo.setContent(decryptQuietly(message.getContent()));
            return vo;
        }).collect(Collectors.toList());

        // 3. 批量补全消息归属用户ID（session_id -> user_id）
        fillSessionUser(list);
        return PageVO.of(result, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditRecordVO auditMessage(Long msgId, MessageAuditDTO dto, Long adminId) {
        // 1. 参数校验
        if (msgId == null) {
            throw new IllegalArgumentException("消息ID不能为空");
        }
        if (dto == null || dto.getAuditResult() == null) {
            throw new IllegalArgumentException("审核结果不能为空");
        }
        int result = dto.getAuditResult();
        if (result != AUDIT_STATUS_PASS && result != AUDIT_STATUS_REJECT) {
            throw new IllegalArgumentException("审核结果只能为1通过或2不通过");
        }
        // 违规标记：未传时按审核结果推导（不通过记1违规，通过记0正常）
        int violateFlag;
        if (dto.getViolateFlag() == null) {
            violateFlag = result == AUDIT_STATUS_REJECT ? VIOLATE_YES : VIOLATE_NO;
        } else {
            violateFlag = dto.getViolateFlag();
            if (violateFlag != VIOLATE_NO && violateFlag != VIOLATE_YES) {
                throw new IllegalArgumentException("违规标记只能为0正常或1违规");
            }
        }
        if (dto.getAuditRemark() != null && dto.getAuditRemark().length() > 500) {
            throw new IllegalArgumentException("审核备注不能超过500字");
        }

        // 2. 消息存在性校验
        ChatMessage message = chatMessageMapper.selectById(msgId);
        if (message == null) {
            throw new BusinessException(404, "消息不存在");
        }

        // 3. 更新消息审核状态与违规标记
        LambdaUpdateWrapper<ChatMessage> updateWrapper = new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getMsgId, msgId)
                .set(ChatMessage::getAuditStatus, result)
                .set(ChatMessage::getViolateFlag, violateFlag);
        chatMessageMapper.update(null, updateWrapper);

        // 4. 生成内容审核留痕记录（允许重复审核，每次审核保留一条历史）
        ContentAuditRecord record = new ContentAuditRecord();
        record.setMsgId(msgId);
        record.setSessionId(message.getSessionId());
        record.setAuditResult(result);
        record.setViolateFlag(violateFlag);
        record.setAuditRemark(dto.getAuditRemark());
        record.setAuditorId(adminId);
        record.setAuditTime(LocalDateTime.now());
        contentAuditRecordMapper.insert(record);
        log.info("内容审核完成: msgId={}, result={}, violateFlag={}, auditId={}",
                msgId, result, violateFlag, record.getAuditId());

        // 5. 组装返回（补全被审消息内容与审核人账号）
        AuditRecordVO vo = AuditRecordVO.of(record);
        vo.setMsgContent(decryptQuietly(message.getContent()));
        vo.setMsgSendTime(message.getSendTime());
        vo.setAuditorName(loadAdminName(adminId));
        return vo;
    }

    @Override
    public PageVO<AuditRecordVO> getAuditRecords(long pageNum, long pageSize, Long msgId) {
        Page<ContentAuditRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ContentAuditRecord> wrapper = new LambdaQueryWrapper<>();

        // 1. 按消息ID筛选（可选）
        if (msgId != null) {
            wrapper.eq(ContentAuditRecord::getMsgId, msgId);
        }
        wrapper.orderByDesc(ContentAuditRecord::getAuditId);
        Page<ContentAuditRecord> result = contentAuditRecordMapper.selectPage(page, wrapper);
        List<ContentAuditRecord> records = result.getRecords();
        if (records.isEmpty()) {
            return PageVO.of(result, new java.util.ArrayList<>());
        }

        // 2. 转VO
        List<AuditRecordVO> list = records.stream()
                .map(AuditRecordVO::of)
                .collect(Collectors.toList());

        // 3. 批量补全被审消息内容与发送时间
        Set<Long> msgIds = records.stream()
                .map(ContentAuditRecord::getMsgId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (!msgIds.isEmpty()) {
            List<ChatMessage> messages = chatMessageMapper.selectBatchIds(msgIds);
            Map<Long, ChatMessage> msgMap = new HashMap<>();
            for (ChatMessage m : messages) {
                msgMap.put(m.getMsgId(), m);
            }
            for (AuditRecordVO vo : list) {
                ChatMessage m = msgMap.get(vo.getMsgId());
                if (m != null) {
                    vo.setMsgContent(decryptQuietly(m.getContent()));
                    vo.setMsgSendTime(m.getSendTime());
                }
            }
        }

        // 4. 批量补全审核人账号
        Set<Long> auditorIds = records.stream()
                .map(ContentAuditRecord::getAuditorId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (!auditorIds.isEmpty()) {
            List<Admin> admins = adminMapper.selectBatchIds(auditorIds);
            Map<Long, String> adminMap = new HashMap<>();
            for (Admin a : admins) {
                adminMap.put(a.getAdminId(), a.getUsername());
            }
            for (AuditRecordVO vo : list) {
                vo.setAuditorName(adminMap.get(vo.getAuditorId()));
            }
        }
        return PageVO.of(result, list);
    }

    /**
     * 批量补全消息归属用户ID
     *
     * @param list 消息VO列表
     */
    private void fillSessionUser(List<MessageVO> list) {
        Set<Long> sessionIds = list.stream()
                .map(MessageVO::getSessionId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (sessionIds.isEmpty()) {
            return;
        }
        List<AiSession> sessions = aiSessionMapper.selectBatchIds(sessionIds);
        Map<Long, Long> sessionUserMap = new HashMap<>();
        for (AiSession s : sessions) {
            sessionUserMap.put(s.getSessionId(), s.getUserId());
        }
        for (MessageVO vo : list) {
            vo.setUserId(sessionUserMap.get(vo.getSessionId()));
        }
    }

    /**
     * 查询审核人账号名
     *
     * @param adminId 管理员ID
     * @return 管理员账号（不存在返回null）
     */
    private String loadAdminName(Long adminId) {
        if (adminId == null) {
            return null;
        }
        Admin admin = adminMapper.selectById(adminId);
        return admin == null ? null : admin.getUsername();
    }

    /**
     * 静默解密（失败返回null，不阻断列表展示）
     *
     * @param encrypted 密文
     * @return 明文或null
     */
    private String decryptQuietly(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) {
            return null;
        }
        try {
            return aesUtil.decrypt(encrypted);
        } catch (Exception e) {
            log.warn("消息内容AES解密失败: {}", e.getMessage());
            return null;
        }
    }
}
