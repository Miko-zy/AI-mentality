package com.aimental.serviceImpl;

import com.aimental.entity.Badge;
import com.aimental.entity.StudyProgress;
import com.aimental.entity.UserBadge;
import com.aimental.mapper.BadgeMapper;
import com.aimental.mapper.DailyCheckinMapper;
import com.aimental.mapper.StudyProgressMapper;
import com.aimental.mapper.UserBadgeMapper;
import com.aimental.service.BadgeService;
import com.aimental.util.CheckinStatUtil;
import com.aimental.vo.BadgeVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 成就徽章服务实现类
 *
 * 解锁条件格式约定（badge.obtain_condition，结构化 TYPE:VALUE）：
 * CHECKIN_STREAK:n   连续打卡满n天
 * CHECKIN_TOTAL:n    累计打卡满n天
 * COURSE_FINISH:n    完成n门训练课程
 * STUDY_MINUTES:n    累计学习满n分钟
 * 无法识别的条件（如早期录入的中文自然语言描述）安全跳过，不参与判定
 */
@Service
public class BadgeServiceImpl implements BadgeService {

    private static final Logger log = LoggerFactory.getLogger(BadgeServiceImpl.class);

    /**
     * 条件类型：连续打卡满n天
     */
    private static final String TYPE_CHECKIN_STREAK = "CHECKIN_STREAK";

    /**
     * 条件类型：累计打卡满n天
     */
    private static final String TYPE_CHECKIN_TOTAL = "CHECKIN_TOTAL";

    /**
     * 条件类型：完成n门训练课程
     */
    private static final String TYPE_COURSE_FINISH = "COURSE_FINISH";

    /**
     * 条件类型：累计学习满n分钟
     */
    private static final String TYPE_STUDY_MINUTES = "STUDY_MINUTES";

    /**
     * 徽章Mapper
     */
    private final BadgeMapper badgeMapper;

    /**
     * 用户徽章Mapper
     */
    private final UserBadgeMapper userBadgeMapper;

    /**
     * 打卡记录Mapper
     */
    private final DailyCheckinMapper dailyCheckinMapper;

    /**
     * 学习进度Mapper
     */
    private final StudyProgressMapper studyProgressMapper;

    /**
     * 构造方法注入Mapper
     *
     * @param badgeMapper        徽章Mapper
     * @param userBadgeMapper    用户徽章Mapper
     * @param dailyCheckinMapper 打卡记录Mapper
     * @param studyProgressMapper 学习进度Mapper
     */
    public BadgeServiceImpl(BadgeMapper badgeMapper,
                            UserBadgeMapper userBadgeMapper,
                            DailyCheckinMapper dailyCheckinMapper,
                            StudyProgressMapper studyProgressMapper) {
        this.badgeMapper = badgeMapper;
        this.userBadgeMapper = userBadgeMapper;
        this.dailyCheckinMapper = dailyCheckinMapper;
        this.studyProgressMapper = studyProgressMapper;
    }

    @Override
    public List<BadgeVO> getUnlockedBadges(Long userId) {
        List<BadgeVO> result = new ArrayList<>();
        List<UserBadge> userBadges = userBadgeMapper.selectList(new LambdaQueryWrapper<UserBadge>()
                .eq(UserBadge::getUserId, userId)
                .orderByDesc(UserBadge::getObtainTime));
        if (userBadges.isEmpty()) {
            return result;
        }
        Set<Long> badgeIds = new HashSet<>();
        for (UserBadge userBadge : userBadges) {
            badgeIds.add(userBadge.getBadgeId());
        }
        List<Badge> badges = badgeMapper.selectBatchIds(badgeIds);
        Map<Long, Badge> badgeMap = badges.stream()
                .collect(Collectors.toMap(Badge::getBadgeId, Function.identity()));
        for (UserBadge userBadge : userBadges) {
            Badge badge = badgeMap.get(userBadge.getBadgeId());
            if (badge != null) {
                result.add(BadgeVO.ofUnlocked(badge, userBadge.getObtainTime()));
            }
        }
        return result;
    }

    @Override
    public List<BadgeVO> checkAndUnlock(Long userId) {
        List<BadgeVO> unlockedNow = new ArrayList<>();
        List<Badge> allBadges = badgeMapper.selectList(new LambdaQueryWrapper<Badge>()
                .orderByAsc(Badge::getBadgeId));
        if (allBadges.isEmpty()) {
            return unlockedNow;
        }
        // 已解锁徽章ID集合（幂等基础）
        Set<Long> ownedBadgeIds = userBadgeMapper.selectList(new LambdaQueryWrapper<UserBadge>()
                        .eq(UserBadge::getUserId, userId))
                .stream()
                .map(UserBadge::getBadgeId)
                .collect(Collectors.toSet());
        // 待判定徽章：全部徽章去掉已解锁的
        List<Badge> pending = new ArrayList<>();
        for (Badge badge : allBadges) {
            if (!ownedBadgeIds.contains(badge.getBadgeId())) {
                pending.add(badge);
            }
        }
        if (pending.isEmpty()) {
            return unlockedNow;
        }

        LocalDate today = LocalDate.now();
        // 统计值懒加载：只有存在对应类型条件时才查询
        List<LocalDate> checkinDates = null;
        Integer finishedCourseCount = null;
        Integer studyMinutes = null;

        for (Badge badge : pending) {
            String condition = badge.getObtainCondition();
            if (condition == null || !condition.contains(":")) {
                // 非结构化条件（如中文描述），安全跳过
                continue;
            }
            String[] parts = condition.split(":", 2);
            String type = parts[0].trim();
            int threshold;
            try {
                threshold = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                log.warn("徽章条件阈值无法解析，跳过判定: badgeId={}, condition={}", badge.getBadgeId(), condition);
                continue;
            }
            if (threshold <= 0) {
                continue;
            }
            boolean met = false;
            if (TYPE_CHECKIN_STREAK.equals(type)) {
                if (checkinDates == null) {
                    checkinDates = loadCheckinDates(userId);
                }
                met = CheckinStatUtil.calcStreak(checkinDates, today) >= threshold;
            } else if (TYPE_CHECKIN_TOTAL.equals(type)) {
                if (checkinDates == null) {
                    checkinDates = loadCheckinDates(userId);
                }
                met = checkinDates.size() >= threshold;
            } else if (TYPE_COURSE_FINISH.equals(type)) {
                if (finishedCourseCount == null) {
                    finishedCourseCount = countFinishedCourses(userId);
                }
                met = finishedCourseCount >= threshold;
            } else if (TYPE_STUDY_MINUTES.equals(type)) {
                if (studyMinutes == null) {
                    studyMinutes = computeStudyMinutes(userId);
                }
                met = studyMinutes >= threshold;
            } else {
                // 未知条件类型，安全跳过
                continue;
            }

            if (met) {
                UserBadge userBadge = new UserBadge();
                userBadge.setUserId(userId);
                userBadge.setBadgeId(badge.getBadgeId());
                userBadge.setObtainTime(LocalDateTime.now());
                try {
                    userBadgeMapper.insert(userBadge);
                } catch (DuplicateKeyException e) {
                    // 并发下已被解锁，跳过即可
                    log.info("徽章已存在，跳过重复解锁: userId={}, badgeId={}", userId, badge.getBadgeId());
                    continue;
                }
                log.info("成就徽章自动解锁: userId={}, badgeId={}, badgeName={}",
                        userId, badge.getBadgeId(), badge.getBadgeName());
                unlockedNow.add(BadgeVO.ofUnlocked(badge, userBadge.getObtainTime()));
            }
        }
        return unlockedNow;
    }

    /**
     * 查询用户全部打卡日期（倒序）
     *
     * @param userId 用户ID
     * @return 打卡日期列表
     */
    private List<LocalDate> loadCheckinDates(Long userId) {
        return dailyCheckinMapper.selectList(new LambdaQueryWrapper<com.aimental.entity.DailyCheckin>()
                        .eq(com.aimental.entity.DailyCheckin::getUserId, userId)
                        .orderByDesc(com.aimental.entity.DailyCheckin::getCheckinDate))
                .stream()
                .map(com.aimental.entity.DailyCheckin::getCheckinDate)
                .collect(Collectors.toList());
    }

    /**
     * 统计用户已完成（完成率100%）的课程数
     *
     * @param userId 用户ID
     * @return 已完成课程数
     */
    private int countFinishedCourses(Long userId) {
        Long count = studyProgressMapper.selectCount(new LambdaQueryWrapper<StudyProgress>()
                .eq(StudyProgress::getUserId, userId)
                .ge(StudyProgress::getFinishRate, new BigDecimal("100")));
        return count == null ? 0 : count.intValue();
    }

    /**
     * 统计用户累计学习分钟数（秒数换算，向下取整）
     *
     * @param userId 用户ID
     * @return 累计学习分钟数
     */
    private int computeStudyMinutes(Long userId) {
        List<StudyProgress> progresses = studyProgressMapper.selectList(new LambdaQueryWrapper<StudyProgress>()
                .eq(StudyProgress::getUserId, userId));
        long totalSeconds = 0L;
        for (StudyProgress progress : progresses) {
            if (progress.getStudyDuration() != null) {
                totalSeconds += progress.getStudyDuration();
            }
        }
        return (int) (totalSeconds / 60);
    }
}
