package com.aimental.service;

import com.aimental.vo.FeedbackVO;
import com.aimental.vo.PageVO;

/**
 * 用户反馈服务接口
 */
public interface UserFeedbackService {

    /**
     * 提交用户反馈
     *
     * @param userId    用户ID
     * @param content   反馈内容
     */
    void submitFeedback(Long userId, String content);

    /**
     * 分页查看当前用户提交的反馈（含管理员回复）
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 反馈列表分页结果
     */
    PageVO<FeedbackVO> listMyFeedback(Long userId, long pageNum, long pageSize);
}