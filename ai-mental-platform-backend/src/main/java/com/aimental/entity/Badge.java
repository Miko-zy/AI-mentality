package com.aimental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * 成就徽章实体类
 */
@TableName("badge")
public class Badge implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 徽章ID
     */
    @TableId(value = "badge_id", type = IdType.AUTO)
    private Long badgeId;

    /**
     * 徽章名称
     */
    @TableField("badge_name")
    private String badgeName;

    /**
     * 图标地址
     */
    @TableField("badge_icon")
    private String badgeIcon;

    /**
     * 解锁条件
     */
    @TableField("obtain_condition")
    private String obtainCondition;

    /**
     * 徽章说明
     */
    @TableField("description")
    private String description;

    /**
     * 逻辑删除标记：0未删，1已删
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

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

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}
