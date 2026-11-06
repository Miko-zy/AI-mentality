package com.aimental.controller;

import com.aimental.common.Result;
import com.aimental.dto.NoticeSaveDTO;
import com.aimental.dto.NoticeStatusDTO;
import com.aimental.security.SecurityUtil;
import com.aimental.service.NoticeAdminService;
import com.aimental.vo.NoticeVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端系统公告管理控制器
 *
 * 接口清单（全部要求管理员角色）：
 * GET  /api/admin/notices                查询全部公告
 * POST /api/admin/notices                新增公告（status可选：0草稿缺省/1直接发布/2下架）
 * PUT  /api/admin/notices/{noticeId}     编辑公告（仅修改标题与正文，不改状态）
 * PUT  /api/admin/notices/{noticeId}/status 修改公告状态（发布/停用，发布时刷新发布时间与发布人）
 */
@RestController
@RequestMapping("/admin/notices")
public class AdminNoticeController {

    /**
     * 系统公告管理服务
     */
    private final NoticeAdminService noticeAdminService;

    /**
     * 构造方法注入
     *
     * @param noticeAdminService 系统公告管理服务
     */
    public AdminNoticeController(NoticeAdminService noticeAdminService) {
        this.noticeAdminService = noticeAdminService;
    }

    /**
     * 查询全部公告列表
     *
     * @return 公告列表
     */
    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public Result<List<NoticeVO>> getNotices() {
        return Result.success(noticeAdminService.getNotices());
    }

    /**
     * 新增公告
     *
     * @param dto 公告信息
     * @return 新增后的公告
     */
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<NoticeVO> createNotice(@RequestBody NoticeSaveDTO dto) {
        Long adminId = SecurityUtil.getCurrentUserId();
        return Result.success(noticeAdminService.createNotice(dto, adminId));
    }

    /**
     * 编辑公告（仅修改标题与正文，不修改状态）
     *
     * @param noticeId 公告ID
     * @param dto      公告信息
     * @return 编辑后的公告
     */
    @PutMapping("/{noticeId}")
    @PreAuthorize("hasRole('admin')")
    public Result<NoticeVO> updateNotice(@PathVariable Long noticeId, @RequestBody NoticeSaveDTO dto) {
        return Result.success(noticeAdminService.updateNotice(noticeId, dto));
    }

    /**
     * 修改公告状态（0草稿/1已发布/2下架停用，发布时刷新发布时间与发布人）
     *
     * @param noticeId 公告ID
     * @param dto      状态信息
     * @return 操作结果
     */
    @PutMapping("/{noticeId}/status")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> updateNoticeStatus(@PathVariable Long noticeId, @RequestBody NoticeStatusDTO dto) {
        Long adminId = SecurityUtil.getCurrentUserId();
        noticeAdminService.updateNoticeStatus(noticeId, dto, adminId);
        return Result.success(null);
    }
}
