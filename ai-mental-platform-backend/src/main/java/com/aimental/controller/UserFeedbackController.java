package com.aimental.controller;

import com.aimental.common.Result;
import com.aimental.dto.FeedbackSubmitDTO;
import com.aimental.security.SecurityUtil;
import com.aimental.service.UserFeedbackService;
import com.aimental.vo.FeedbackVO;
import com.aimental.vo.PageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户反馈控制器
 * 提交反馈、查看自己的反馈与管理员回复（需要JWT鉴权）
 */
@RestController
@RequestMapping("/feedback")
public class UserFeedbackController {

    /**
     * 用户反馈服务
     */
    private final UserFeedbackService userFeedbackService;

    /**
     * 构造方法注入用户反馈服务
     *
     * @param userFeedbackService 用户反馈服务
     */
    public UserFeedbackController(UserFeedbackService userFeedbackService) {
        this.userFeedbackService = userFeedbackService;
    }

    /**
     * 提交用户反馈（需要JWT鉴权）
     *
     * @param feedbackSubmitDTO 反馈提交请求
     * @return 提交结果
     */
    @PostMapping
    public Result<Void> submitFeedback(@RequestBody FeedbackSubmitDTO feedbackSubmitDTO) {
        userFeedbackService.submitFeedback(SecurityUtil.getCurrentUserId(), feedbackSubmitDTO.getContent());
        return Result.success("反馈提交成功，感谢您的反馈", null);
    }

    /**
     * 分页查看当前用户提交的反馈（含管理员回复，需要JWT鉴权）
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 反馈列表分页结果
     */
    @GetMapping
    public Result<PageVO<FeedbackVO>> listMyFeedback(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(userFeedbackService.listMyFeedback(SecurityUtil.getCurrentUserId(), pageNum, pageSize));
    }
}