package com.aimental.controller;

import com.aimental.common.Result;
import com.aimental.security.SecurityUtil;
import com.aimental.service.BadgeService;
import com.aimental.vo.BadgeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 成就徽章接口（普通用户端）
 * 全部接口经JWT鉴权过滤器，userId从Token解析
 *
 * 接口清单（context-path为/api）：
 * GET /api/badge   查询当前用户已解锁的全部成就徽章
 */
@RestController
@RequestMapping("/badge")
public class BadgeController {

    /**
     * 成就徽章服务
     */
    private final BadgeService badgeService;

    /**
     * 构造方法注入服务
     *
     * @param badgeService 成就徽章服务
     */
    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    /**
     * 查询当前用户已解锁的全部成就徽章
     *
     * @return 已解锁徽章列表（按解锁时间倒序）
     */
    @GetMapping
    public Result<List<BadgeVO>> getUnlockedBadges() {
        return Result.success(badgeService.getUnlockedBadges(SecurityUtil.getCurrentUserId()));
    }
}
