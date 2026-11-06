package com.aimental.dto;

import java.util.List;

/**
 * 角色权限分配请求DTO（全量覆盖式）
 */
public class RolePermAssignDTO {

    /**
     * 要分配给角色的权限ID列表（全量覆盖，传空列表表示清空该角色全部权限）
     */
    private List<Long> permIds;

    public List<Long> getPermIds() {
        return permIds;
    }

    public void setPermIds(List<Long> permIds) {
        this.permIds = permIds;
    }
}
