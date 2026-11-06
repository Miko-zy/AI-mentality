package com.aimental.serviceImpl;

import com.aimental.dto.CheckinDTO;
import com.aimental.entity.DailyCheckin;
import com.aimental.entity.User;
import com.aimental.exception.BusinessException;
import com.aimental.mapper.DailyCheckinMapper;
import com.aimental.mapper.UserMapper;
import com.aimental.service.BadgeService;
import com.aimental.service.CheckinService;
import com.aimental.util.CheckinStatUtil;
import com.aimental.vo.BadgeVO;
import com.aimental.vo.CheckinResultVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 每日打卡服务实现类
 *
 * 打卡流程：校验今日未打卡 -> 插入打卡记录（uk_user_date唯一键兜底防重）->
 * 用户总积分累加 -> 统计连续/累计打卡天数 -> 自动判定成就徽章解锁
 */
@Service
public class CheckinServiceImpl implements CheckinService {

    private static final Logger log = LoggerFactory.getLogger(CheckinServiceImpl.class);

    /**
     * 打卡记录Mapper
     */
    private final DailyCheckinMapper dailyCheckinMapper;

    /**
     * 用户Mapper
     */
    private final UserMapper userMapper;

    /**
     * 成就徽章服务
     */
    private final BadgeService badgeService;

    /**
     * 构造方法注入
     *
     * @param dailyCheckinMapper 打卡记录Mapper
     * @param userMapper         用户Mapper
     * @param badgeService       成就徽章服务
     */
    public CheckinServiceImpl(DailyCheckinMapper dailyCheckinMapper,
                              UserMapper userMapper,
                              BadgeService badgeService) {
        this.dailyCheckinMapper = dailyCheckinMapper;
        this.userMapper = userMapper;
        this.badgeService = badgeService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckinResultVO checkin(Long userId, CheckinDTO dto) {
        LocalDate today = LocalDate.now();

        // 1. 心情描述校验（可选）
        String moodDesc = dto == null ? null : dto.getMoodDesc();
        if (moodDesc != null) {
            checkMaxLength(moodDesc, 200, "心情描述不能超过200字");
        }

        // 2. 校验今日是否已打卡
        Long exists = dailyCheckinMapper.selectCount(new LambdaQueryWrapper<DailyCheckin>()
                .eq(DailyCheckin::getUserId, userId)
                .eq(DailyCheckin::getCheckinDate, today));
        if (exists != null && exists > 0) {
            throw new BusinessException(400, "今日已打卡，请勿重复打卡");
        }

        // 3. 插入今日打卡记录（唯一键 uk_user_date 兜底防并发重复）
        DailyCheckin record = new DailyCheckin();
        record.setUserId(userId);
        record.setCheckinDate(today);
        record.setMoodDesc(moodDesc);
        try {
            dailyCheckinMapper.insert(record);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(400, "今日已打卡，请勿重复打卡");
        }

        // 4. 用户总积分累加
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        int pointsBefore = user.getTotalPoints() == null ? 0 : user.getTotalPoints();
        int pointsAfter = pointsBefore + CHECKIN_POINTS;
        user.setTotalPoints(pointsAfter);
        userMapper.updateById(user);
        log.info("打卡积分发放: userId={}, 奖励={}, 总积分={}", userId, CHECKIN_POINTS, pointsAfter);

        // 5. 统计连续/累计打卡天数
        List<LocalDate> dates = loadCheckinDates(userId);
        int streak = CheckinStatUtil.calcStreak(dates, today);

        // 6. 自动判定成就徽章解锁
        List<BadgeVO> newBadges = badgeService.checkAndUnlock(userId);

        // 7. 组装打卡结果
        CheckinResultVO vo = new CheckinResultVO();
        vo.setTodayChecked(true);
        vo.setMoodDesc(moodDesc);
        vo.setCheckinPoints(CHECKIN_POINTS);
        vo.setTotalPoints(pointsAfter);
        vo.setStreakDays(streak);
        vo.setTotalDays(dates.size());
        vo.setNewBadges(newBadges);
        return vo;
    }

    @Override
    public CheckinResultVO getStatus(Long userId) {
        LocalDate today = LocalDate.now();
        List<LocalDate> dates = loadCheckinDates(userId);
        boolean todayChecked = dates.contains(today);

        // 今日已打卡则回显心情描述
        String moodDesc = null;
        if (todayChecked) {
            DailyCheckin record = dailyCheckinMapper.selectOne(new LambdaQueryWrapper<DailyCheckin>()
                    .eq(DailyCheckin::getUserId, userId)
                    .eq(DailyCheckin::getCheckinDate, today)
                    .last("LIMIT 1"));
            if (record != null) {
                moodDesc = record.getMoodDesc();
            }
        }

        User user = userMapper.selectById(userId);
        int totalPoints = (user == null || user.getTotalPoints() == null) ? 0 : user.getTotalPoints();

        CheckinResultVO vo = new CheckinResultVO();
        vo.setTodayChecked(todayChecked);
        vo.setMoodDesc(moodDesc);
        vo.setCheckinPoints(0);
        vo.setTotalPoints(totalPoints);
        vo.setStreakDays(CheckinStatUtil.calcStreak(dates, today));
        vo.setTotalDays(dates.size());
        return vo;
    }

    /**
     * 校验字符串不超过最大长度（失败抛IllegalArgumentException，统一映射为400）
     *
     * @param value   待校验字符串
     * @param max     最大长度
     * @param message 失败提示
     */
    private static void checkMaxLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 查询用户全部打卡日期（倒序）
     *
     * @param userId 用户ID
     * @return 打卡日期列表
     */
    private List<LocalDate> loadCheckinDates(Long userId) {
        return dailyCheckinMapper.selectList(new LambdaQueryWrapper<DailyCheckin>()
                        .eq(DailyCheckin::getUserId, userId)
                        .orderByDesc(DailyCheckin::getCheckinDate))
                .stream()
                .map(DailyCheckin::getCheckinDate)
                .collect(Collectors.toList());
    }
}
