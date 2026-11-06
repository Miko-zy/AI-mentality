package com.aimental.service;

import com.aimental.dto.MessageAuditDTO;
import com.aimental.vo.AuditRecordVO;
import com.aimental.vo.MessageVO;
import com.aimental.vo.PageVO;

/**
 * 内容审核服务接口（管理员运营模块）
 * 提供对话消息查询、执行审核（生成审核记录）、审核记录查询能力
 */
public interface ContentAuditService {

    /**
     * 审核状态：待审
     */
    Integer AUDIT_STATUS_PENDING = 0;

    /**
     * 审核状态：通过
     */
    Integer AUDIT_STATUS_PASS = 1;

    /**
     * 审核状态：不通过
     */
    Integer AUDIT_STATUS_REJECT = 2;

    /**
     * 违规标记：正常
     */
    Integer VIOLATE_NO = 0;

    /**
     * 违规标记：违规
     */
    Integer VIOLATE_YES = 1;

    /**
     * 分页查询对话消息
     *
     * @param pageNum     页码
     * @param pageSize    每页条数
     * @param auditStatus 审核状态筛选（可空，传0查待审消息）
     * @return 消息分页结果
     */
    PageVO<MessageVO> getMessages(long pageNum, long pageSize, Integer auditStatus);

    /**
     * 审核单条对话消息：更新消息审核状态并生成审核留痕记录
     *
     * @param msgId   消息ID
     * @param dto     审核结果
     * @param adminId 审核人（当前登录管理员ID）
     * @return 生成的审核记录
     */
    AuditRecordVO auditMessage(Long msgId, MessageAuditDTO dto, Long adminId);

    /**
     * 分页查询内容审核记录
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param msgId    按消息ID筛选（可空）
     * @return 审核记录分页结果
     */
    PageVO<AuditRecordVO> getAuditRecords(long pageNum, long pageSize, Long msgId);
}
