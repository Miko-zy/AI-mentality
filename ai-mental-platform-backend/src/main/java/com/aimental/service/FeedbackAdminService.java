package com.aimental.service;

import com.aimental.dto.FeedbackHandleDTO;
import com.aimental.vo.AdminFeedbackVO;
import com.aimental.vo.PageVO;

/**
 * 用户反馈处理服务接口（管理员运营模块）
 * 提供反馈分页查询、回复与状态更新能力
 */
public interface FeedbackAdminService {

    /**
     * 反馈状态：待处理
     */
    Integer FEEDBACK_STATUS_PENDING = 0;

    /**
     * 反馈状态：已回复
     */
    Integer FEEDBACK_STATUS_REPLIED = 1;

    /**
     * 反馈状态：关闭
     */
    Integer FEEDBACK_STATUS_CLOSED = 2;

    /**
     * 分页查询用户反馈
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param status   状态筛选（可空）
     * @return 反馈分页结果
     */
    PageVO<AdminFeedbackVO> getFeedbacks(long pageNum, long pageSize, Integer status);

    /**
     * 处理用户反馈：填写管理员回复并更新处理状态
     *
     * @param feedbackId 反馈ID
     * @param dto        处理信息
     */
    void handleFeedback(Long feedbackId, FeedbackHandleDTO dto);
}
