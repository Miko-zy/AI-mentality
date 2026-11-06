package com.aimental.controller;

import com.aimental.common.Result;
import com.aimental.dto.CheckinDTO;
import com.aimental.security.SecurityUtil;
import com.aimental.service.CheckinService;
import com.aimental.vo.CheckinResultVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 每日打卡接口（普通用户端）
 * 全部接口经JWT鉴权过滤器，userId从Token解析
 *
 * 接口清单（context-path为/api）：
 * POST /api/checkin   每日打卡签到，获得积分并统计连续打卡天数
 * GET  /api/checkin   查询今日打卡状态与连续/累计打卡统计
 */
@RestController
@RequestMapping("/checkin")
public class CheckinController {

    /**
     * 打卡服务
     */
    private final CheckinService checkinService;

    /**
     * 构造方法注入服务
     *
     * @param checkinService 打卡服务
     */
    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    /**
     * 每日打卡签到（同一用户同一天只能打卡一次）
     *
     * @param dto 打卡信息（心情描述，可选）
     * @return 打卡结果（获得积分、总积分、连续天数、新解锁徽章）
     */
    @PostMapping
    public Result<CheckinResultVO> checkin(@RequestBody(required = false) CheckinDTO dto) {
        return Result.success(checkinService.checkin(SecurityUtil.getCurrentUserId(), dto));
    }

    /**
     * 查询今日打卡状态与连续/累计打卡统计
     *
     * @return 打卡状态
     */
    @GetMapping
    public Result<CheckinResultVO> getStatus() {
        return Result.success(checkinService.getStatus(SecurityUtil.getCurrentUserId()));
    }
}
