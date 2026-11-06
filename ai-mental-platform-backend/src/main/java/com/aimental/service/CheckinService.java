package com.aimental.service;

import com.aimental.dto.CheckinDTO;
import com.aimental.vo.CheckinResultVO;

/**
 * 每日打卡服务接口（普通用户端）
 * 打卡获得积分并统计连续打卡天数，打卡成功后自动判定成就徽章解锁
 */
public interface CheckinService {

    /**
     * 每次打卡获得的积分
     */
    int CHECKIN_POINTS = 10;

    /**
     * 每日打卡（同一用户同一天只能打卡一次）
     * 逻辑：插入今日打卡记录 -> 用户总积分加积分 -> 统计连续/累计天数 -> 自动判定徽章解锁
     *
     * @param userId 当前登录用户ID
     * @param dto    打卡信息（心情描述，可选）
     * @return 打卡结果（获得积分、总积分、连续天数、新解锁徽章）
     */
    CheckinResultVO checkin(Long userId, CheckinDTO dto);

    /**
     * 查询今日打卡状态与连续打卡统计
     *
     * @param userId 当前登录用户ID
     * @return 打卡状态（今日是否已打卡、连续/累计天数、总积分）
     */
    CheckinResultVO getStatus(Long userId);
}
