package com.aimental.serviceImpl;

import com.aimental.entity.UserFeedback;
import com.aimental.exception.BusinessException;
import com.aimental.mapper.UserFeedbackMapper;
import com.aimental.service.UserFeedbackService;
import com.aimental.vo.FeedbackVO;
import com.aimental.vo.PageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户反馈服务实现
 * 负责提交反馈、查看自己的反馈与管理员回复
 */
@Service
public class UserFeedbackServiceImpl implements UserFeedbackService {

    private static final Logger log = LoggerFactory.getLogger(UserFeedbackServiceImpl.class);

    /**
     * 用户反馈Mapper
     */
    private final UserFeedbackMapper userFeedbackMapper;

    /**
     * 构造方法注入用户反馈Mapper
     *
     * @param userFeedbackMapper 用户反馈Mapper
     */
    public UserFeedbackServiceImpl(UserFeedbackMapper userFeedbackMapper) {
        this.userFeedbackMapper = userFeedbackMapper;
    }

    /**
     * 提交用户反馈
     * 反馈初始状态为待处理（0），等待管理员回复
     *
     * @param userId  用户ID
     * @param content 反馈内容
     */
    @Override
    public void submitFeedback(Long userId, String content) {
        String feedbackContent = content.trim();
        if (StrUtil.isBlank(feedbackContent)) {
            throw new BusinessException("反馈内容不能为空");
        }
        if (feedbackContent.length() > 500) {
            throw new BusinessException("反馈内容不能超过500个字符");
        }

        LocalDateTime now = LocalDateTime.now();
        UserFeedback feedback = new UserFeedback();
        feedback.setUserId(userId);
        feedback.setFeedbackContent(feedbackContent);
        feedback.setStatus(0);
        feedback.setCreateTime(now);
        feedback.setUpdateTime(now);
        feedback.setIsDeleted(0);
        userFeedbackMapper.insert(feedback);
        log.info("用户提交反馈成功: userId={}, feedbackId={}", userId, feedback.getFeedbackId());
    }

    /**
     * 分页查看当前用户提交的反馈（含管理员回复）
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 反馈列表分页结果
     */
    @Override
    public PageVO<FeedbackVO> listMyFeedback(Long userId, long pageNum, long pageSize) {
        Page<UserFeedback> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserFeedback> wrapper = new LambdaQueryWrapper<UserFeedback>()
                .eq(UserFeedback::getUserId, userId)
                .orderByDesc(UserFeedback::getCreateTime);
        Page<UserFeedback> result = userFeedbackMapper.selectPage(page, wrapper);

        List<FeedbackVO> list = result.getRecords().stream().map(feedback -> {
            FeedbackVO vo = new FeedbackVO();
            vo.setFeedbackId(feedback.getFeedbackId());
            vo.setFeedbackContent(feedback.getFeedbackContent());
            vo.setStatus(feedback.getStatus());
            vo.setReplyContent(feedback.getReplyContent());
            vo.setCreateTime(feedback.getCreateTime());
            vo.setUpdateTime(feedback.getUpdateTime());
            return vo;
        }).collect(Collectors.toList());
        return PageVO.of(result, list);
    }
}