package com.aimental.dto;

/**
 * 角色新增/编辑请求DTO
 */
public class RoleSaveDTO {

    /**
     * 角色编码（英文标识，唯一，新增必填；编辑时不允许修改）
     */
    private String roleCode;

    /**
     * 角色名称（必填）
     */
    private String roleName;

    /**
     * 角色描述（可选）
     */
    private String description;

    /**
     * 状态：0停用，1启用（可选，缺省1；编辑时可修改）
     */
    private Integer status;

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
