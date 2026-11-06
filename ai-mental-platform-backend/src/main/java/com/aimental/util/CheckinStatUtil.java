package com.aimental.util;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 打卡统计工具类
 */
public final class CheckinStatUtil {

    /**
     * 私有构造，禁止实例化
     */
    private CheckinStatUtil() {
    }

    /**
     * 计算连续打卡天数
     * 规则：
     * 1. 今日已打卡：从今日起往回逐日匹配，直到断签，得到连续天数
     * 2. 今日未打卡但昨日有打卡：从昨日起往回匹配（断签前的连续天数仍然显示）
     * 3. 今日与昨日都未打卡：连续天数为0
     *
     * @param checkinDates 用户全部打卡日期（顺序无关，内部按日期集合匹配）
     * @param today        当前日期
     * @return 连续打卡天数
     */
    public static int calcStreak(List<LocalDate> checkinDates, LocalDate today) {
        if (checkinDates == null || checkinDates.isEmpty()) {
            return 0;
        }
        Set<LocalDate> dateSet = new HashSet<>(checkinDates);
        LocalDate cursor;
        if (dateSet.contains(today)) {
            cursor = today;
        } else if (dateSet.contains(today.minusDays(1))) {
            cursor = today.minusDays(1);
        } else {
            return 0;
        }
        int streak = 0;
        while (dateSet.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
