package com.aimental.dto;

/**
 * 权限新增/编辑请求DTO
 */
public class PermissionSaveDTO {

    /**
     * 权限编码（如user:list，唯一，新增必填；编辑时不允许修改）
     */
    private String permCode;

    /**
     * 权限名称（必填）
     */
    private String permName;

    /**
     * 权限类型：1菜单，2按钮/操作（可选，缺省1）
     */
    private Integer permType;

    /**
     * 权限说明（可选）
     */
    private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
