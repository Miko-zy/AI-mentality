package com.aimental.vo;

import com.aimental.entity.Badge;
import com.aimental.entity.UserBadge;

import java.time.LocalDateTime;

/**
 * 成就徽章VO（含用户解锁状态）
 */
public class BadgeVO {

    /**
     * 徽章ID
     */
    private Long badgeId;

    /**
     * 徽章名称
     */
    private String badgeName;

    /**
     * 图标地址
     */
    private String badgeIcon;

    /**
     * 解锁条件（结构化格式 TYPE:VALUE，如 CHECKIN_STREAK:3）
     */
    private String obtainCondition;

    /**
     * 徽章说明
     */
    private String description;

    /**
     * 是否已解锁：true已解锁，false未解锁
     */
    private Boolean unlocked;

    /**
     * 解锁时间（未解锁为null）
     */
    private LocalDateTime obtainTime;

    /**
     * 未解锁状态转换
     *
     * @param badge 徽章实体
     * @return 未解锁徽章VO
     */
    public static BadgeVO of(Badge badge) {
        BadgeVO vo = new BadgeVO();
        vo.setBadgeId(badge.getBadgeId());
        vo.setBadgeName(badge.getBadgeName());
        vo.setBadgeIcon(badge.getBadgeIcon());
        vo.setObtainCondition(badge.getObtainCondition());
        vo.setDescription(badge.getDescription());
        vo.setUnlocked(false);
        vo.setObtainTime(null);
        return vo;
    }

    /**
     * 已解锁状态转换
     *
     * @param badge     徽章实体
     * @param obtainTime 解锁时间
     * @return 已解锁徽章VO
     */
    public static BadgeVO ofUnlocked(Badge badge, LocalDateTime obtainTime) {
        BadgeVO vo = of(badge);
        vo.setUnlocked(true);
        vo.setObtainTime(obtainTime);
        return vo;
    }

    public Long getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(Long badgeId) {
        this.badgeId = badgeId;
    }

    public String getBadgeName() {
        return badgeName;
    }

    public void setBadgeName(String badgeName) {
        this.badgeName = badgeName;
    }

    public String getBadgeIcon() {
        return badgeIcon;
    }

    public void setBadgeIcon(String badgeIcon) {
        this.badgeIcon = badgeIcon;
    }

    public String getObtainCondition() {
        return obtainCondition;
    }

    public void setObtainCondition(String obtainCondition) {
        this.obtainCondition = obtainCondition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getUnlocked() {
        return unlocked;
    }

    public void setUnlocked(Boolean unlocked) {
        this.unlocked = unlocked;
    }

    public LocalDateTime getObtainTime() {
        return obtainTime;
    }

    public void setObtainTime(LocalDateTime obtainTime) {
        this.obtainTime = obtainTime;
    }
}
