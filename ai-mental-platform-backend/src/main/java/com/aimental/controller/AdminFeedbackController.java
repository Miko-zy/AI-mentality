package com.aimental.controller;

import com.aimental.common.Result;
import com.aimental.dto.FeedbackHandleDTO;
import com.aimental.service.FeedbackAdminService;
import com.aimental.vo.AdminFeedbackVO;
import com.aimental.vo.PageVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端用户反馈处理控制器
 *
 * 接口清单（全部要求管理员角色）：
 * GET /api/admin/feedback             分页查询用户反馈（支持status筛选）
 * PUT /api/admin/feedback/{feedbackId} 填写管理员回复并更新处理状态
 */
@RestController
@RequestMapping("/admin/feedback")
public class AdminFeedbackController {

    /**
     * 用户反馈处理服务
     */
    private final FeedbackAdminService feedbackAdminService;

    /**
     * 构造方法注入
     *
     * @param feedbackAdminService 用户反馈处理服务
     */
    public AdminFeedbackController(FeedbackAdminService feedbackAdminService) {
        this.feedbackAdminService = feedbackAdminService;
    }

    /**
     * 分页查询用户反馈
     *
     * @param pageNum  页码（缺省1）
     * @param pageSize 每页条数（缺省10）
     * @param status   状态：0待处理/1已回复/2关闭（可空）
     * @return 反馈分页结果
     */
    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public Result<PageVO<AdminFeedbackVO>> getFeedbacks(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(feedbackAdminService.getFeedbacks(pageNum, pageSize, status));
    }

    /**
     * 处理用户反馈（填写管理员回复并更新处理状态）
     *
     * @param feedbackId 反馈ID
     * @param dto        处理信息
     * @return 操作结果
     */
    @PutMapping("/{feedbackId}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> handleFeedback(@PathVariable Long feedbackId, @RequestBody FeedbackHandleDTO dto) {
        feedbackAdminService.handleFeedback(feedbackId, dto);
        return Result.success(null);
    }
}
