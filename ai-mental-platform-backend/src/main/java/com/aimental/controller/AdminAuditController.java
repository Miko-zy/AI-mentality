package com.aimental.controller;

import com.aimental.common.Result;
import com.aimental.dto.MessageAuditDTO;
import com.aimental.security.SecurityUtil;
import com.aimental.service.ContentAuditService;
import com.aimental.vo.AuditRecordVO;
import com.aimental.vo.MessageVO;
import com.aimental.vo.PageVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端内容审核控制器
 *
 * 接口清单（全部要求管理员角色）：
 * GET  /api/admin/audit/messages            分页查询对话消息（支持auditStatus筛选，0查待审）
 * POST /api/admin/audit/messages/{msgId}    审核对话消息（更新消息状态并生成审核记录）
 * GET  /api/admin/audit/records             分页查询内容审核记录（支持msgId筛选）
 */
@RestController
@RequestMapping("/admin/audit")
public class AdminAuditController {

    /**
     * 内容审核服务
     */
    private final ContentAuditService contentAuditService;

    /**
     * 构造方法注入
     *
     * @param contentAuditService 内容审核服务
     */
    public AdminAuditController(ContentAuditService contentAuditService) {
        this.contentAuditService = contentAuditService;
    }

    /**
     * 分页查询对话消息
     *
     * @param pageNum     页码（缺省1）
     * @param pageSize    每页条数（缺省10）
     * @param auditStatus 审核状态：0待审/1通过/2不通过（可空）
     * @return 消息分页结果
     */
    @GetMapping("/messages")
    @PreAuthorize("hasRole('admin')")
    public Result<PageVO<MessageVO>> getMessages(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.success(contentAuditService.getMessages(pageNum, pageSize, auditStatus));
    }

    /**
     * 审核单条对话消息（更新消息审核状态并生成审核留痕记录）
     *
     * @param msgId 消息ID
     * @param dto   审核结果
     * @return 生成的审核记录
     */
    @PostMapping("/messages/{msgId}")
    @PreAuthorize("hasRole('admin')")
    public Result<AuditRecordVO> auditMessage(@PathVariable Long msgId, @RequestBody MessageAuditDTO dto) {
        Long adminId = SecurityUtil.getCurrentUserId();
        return Result.success(contentAuditService.auditMessage(msgId, dto, adminId));
    }

    /**
     * 分页查询内容审核记录
     *
     * @param pageNum  页码（缺省1）
     * @param pageSize 每页条数（缺省10）
     * @param msgId    按消息ID筛选（可空）
     * @return 审核记录分页结果
     */
    @GetMapping("/records")
    @PreAuthorize("hasRole('admin')")
    public Result<PageVO<AuditRecordVO>> getAuditRecords(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long msgId) {
        return Result.success(contentAuditService.getAuditRecords(pageNum, pageSize, msgId));
    }
}
