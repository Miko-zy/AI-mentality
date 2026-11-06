package com.aimental.service;

import com.aimental.vo.BadgeVO;

import java.util.List;

/**
 * 成就徽章服务接口
 * 负责徽章达成条件的自动判定与解锁、已解锁徽章查询
 *
 * 解锁条件格式约定（badge.obtain_condition，结构化 TYPE:VALUE）：
 * CHECKIN_STREAK:n   连续打卡满n天
 * CHECKIN_TOTAL:n    累计打卡满n天
 * COURSE_FINISH:n    完成n门训练课程
 * STUDY_MINUTES:n    累计学习满n分钟
 * 无法识别的条件（如早期录入的中文描述）安全跳过，不参与判定
 */
public interface BadgeService {

    /**
     * 查询当前用户已解锁的全部徽章
     *
     * @param userId 当前登录用户ID
     * @return 已解锁徽章列表（按解锁时间倒序）
     */
    List<BadgeVO> getUnlockedBadges(Long userId);

    /**
     * 根据用户最新统计自动判定并解锁达成条件的徽章（幂等：已解锁的不再重复插入）
     * 在打卡成功、学习进度更新后调用
     *
     * @param userId 当前登录用户ID
     * @return 本次新解锁的徽章列表（无新解锁则为空列表）
     */
    List<BadgeVO> checkAndUnlock(Long userId);
}
