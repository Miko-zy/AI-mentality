package com.aimental.serviceImpl;

import com.aimental.dto.FeedbackHandleDTO;
import com.aimental.entity.User;
import com.aimental.entity.UserFeedback;
import com.aimental.exception.BusinessException;
import com.aimental.mapper.UserFeedbackMapper;
import com.aimental.mapper.UserMapper;
import com.aimental.service.FeedbackAdminService;
import com.aimental.vo.AdminFeedbackVO;
import com.aimental.vo.PageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户反馈处理服务实现类
 *
 * 反馈查询：分页 + 状态筛选，批量补全反馈用户昵称；
 * 反馈处理：填写管理员回复并更新处理状态（状态置为已回复时回复内容必填）
 */
@Service
public class FeedbackAdminServiceImpl implements FeedbackAdminService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackAdminServiceImpl.class);

    /**
     * 回复内容最大长度
     */
    private static final int REPLY_MAX_LENGTH = 1000;

    /**
     * 用户反馈Mapper
     */
    private final UserFeedbackMapper userFeedbackMapper;

    /**
     * 用户Mapper
     */
    private final UserMapper userMapper;

    /**
     * 构造方法注入
     *
     * @param userFeedbackMapper 用户反馈Mapper
     * @param userMapper         用户Mapper
     */
    public FeedbackAdminServiceImpl(UserFeedbackMapper userFeedbackMapper, UserMapper userMapper) {
        this.userFeedbackMapper = userFeedbackMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PageVO<AdminFeedbackVO> getFeedbacks(long pageNum, long pageSize, Integer status) {
        Page<UserFeedback> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserFeedback> wrapper = new LambdaQueryWrapper<>();

        // 1. 状态筛选（0待处理/1已回复/2关闭）
        if (status != null) {
            if (status != FEEDBACK_STATUS_PENDING && status != FEEDBACK_STATUS_REPLIED
                    && status != FEEDBACK_STATUS_CLOSED) {
                throw new IllegalArgumentException("反馈状态只能为0待处理、1已回复或2关闭");
            }
            wrapper.eq(UserFeedback::getStatus, status);
        }
        wrapper.orderByDesc(UserFeedback::getFeedbackId);
        Page<UserFeedback> result = userFeedbackMapper.selectPage(page, wrapper);
        List<UserFeedback> records = result.getRecords();
        if (records.isEmpty()) {
            return PageVO.of(result, new java.util.ArrayList<>());
        }

        // 2. 转VO
        List<AdminFeedbackVO> list = records.stream()
                .map(AdminFeedbackVO::of)
                .collect(Collectors.toList());

        // 3. 批量补全反馈用户昵称（昵称为明文存储）
        Set<Long> userIds = records.stream()
                .map(UserFeedback::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, String> nicknameMap = new HashMap<>();
            for (User u : users) {
                nicknameMap.put(u.getUserId(), u.getNickname());
            }
            for (AdminFeedbackVO vo : list) {
                vo.setUserNickname(nicknameMap.get(vo.getUserId()));
            }
        }
        return PageVO.of(result, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleFeedback(Long feedbackId, FeedbackHandleDTO dto) {
        // 1. 反馈存在性校验
        if (feedbackId == null) {
            throw new IllegalArgumentException("反馈ID不能为空");
        }
        UserFeedback feedback = userFeedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new BusinessException(404, "反馈不存在");
        }

        // 2. 参数校验：状态必填且合法
        if (dto == null || dto.getStatus() == null) {
            throw new IllegalArgumentException("处理状态不能为空");
        }
        int status = dto.getStatus();
        if (status != FEEDBACK_STATUS_PENDING && status != FEEDBACK_STATUS_REPLIED
                && status != FEEDBACK_STATUS_CLOSED) {
            throw new IllegalArgumentException("反馈状态只能为0待处理、1已回复或2关闭");
        }

        // 3. 回复内容校验：置为已回复时必填，长度统一限制
        String replyContent = dto.getReplyContent();
        if (status == FEEDBACK_STATUS_REPLIED
                && (replyContent == null || replyContent.trim().isEmpty())) {
            throw new IllegalArgumentException("回复状态必须填写回复内容");
        }
        if (replyContent != null && replyContent.length() > REPLY_MAX_LENGTH) {
            throw new IllegalArgumentException("回复内容不能超过" + REPLY_MAX_LENGTH + "字");
        }

        // 4. 更新反馈：状态始终更新；回复内容仅在传入时更新（未传保留原回复）
        feedback.setStatus(status);
        if (replyContent != null) {
            feedback.setReplyContent(replyContent);
        }
        feedback.setUpdateTime(LocalDateTime.now());
        userFeedbackMapper.updateById(feedback);
        log.info("处理用户反馈: feedbackId={}, status={}", feedbackId, status);
    }
}
