package com.aimental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户获得徽章关联实体类（多对多）
 */
@TableName("user_badge")
public class UserBadge implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联记录ID
     */
    @TableId(value = "ub_id", type = IdType.AUTO)
    private Long ubId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 徽章ID
     */
    @TableField("badge_id")
    private Long badgeId;

    /**
     * 获得时间
     */
    @TableField("obtain_time")
    private LocalDateTime obtainTime;

    /**
     * 逻辑删除标记：0未删，1已删
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    public Long getUbId() {
        return ubId;
    }

    public void setUbId(Long ubId) {
        this.ubId = ubId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(Long badgeId) {
        this.badgeId = badgeId;
    }

    public LocalDateTime getObtainTime() {
        return obtainTime;
    }

    public void setObtainTime(LocalDateTime obtainTime) {
        this.obtainTime = obtainTime;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}
