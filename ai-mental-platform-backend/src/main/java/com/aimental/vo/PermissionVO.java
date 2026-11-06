package com.aimental.vo;

import com.aimental.entity.SysPermission;

import java.time.LocalDateTime;

/**
 * 系统权限视图对象
 */
public class PermissionVO {

    /**
     * 权限ID
     */
    private Long permId;

    /**
     * 权限编码
     */
    private String permCode;

    /**
     * 权限名称
     */
    private String permName;

    /**
     * 权限类型：1菜单，2按钮/操作
     */
    private Integer permType;

    /**
     * 权限类型中文描述
     */
    private String permTypeText;

    /**
     * 权限说明
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 由权限实体转换为视图对象
     *
     * @param permission 权限实体
     * @return 权限视图对象
     */
    public static PermissionVO of(SysPermission permission) {
        PermissionVO vo = new PermissionVO();
        vo.setPermId(permission.getPermId());
        vo.setPermCode(permission.getPermCode());
        vo.setPermName(permission.getPermName());
        vo.setPermType(permission.getPermType());
        vo.setPermTypeText(permission.getPermType() != null && permission.getPermType() == 2 ? "按钮/操作" : "菜单");
        vo.setDescription(permission.getDescription());
        vo.setCreateTime(permission.getCreateTime());
        return vo;
    }

    public Long getPermId() {
        return permId;
    }

    public void setPermId(Long permId) {
        this.permId = permId;
    }

    public String getPermCode() {
        return permCode;
    }

    public void setPermCode(String permCode) {
        this.permCode = permCode;
    }

    public String getPermName() {
        return permName;
    }

    public void setPermName(String permName) {
        this.permName = permName;
    }

    public Integer getPermType() {
        return permType;
    }

    public void setPermType(Integer permType) {
        this.permType = permType;
    }

    public String getPermTypeText() {
        return permTypeText;
    }

    public void setPermTypeText(String permTypeText) {
        this.permTypeText = permTypeText;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
