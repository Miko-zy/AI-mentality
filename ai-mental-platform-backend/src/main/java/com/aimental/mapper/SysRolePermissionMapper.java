package com.aimental.mapper;

import com.aimental.entity.SysRolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色权限关联Mapper接口
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

    /**
     * 按角色ID物理删除该角色的全部权限分配
     * 关联表为纯中间表，必须物理删除：
     * 逻辑删除会保留uk_role_perm唯一键占用，导致同一(角色,权限)重新分配时主键冲突
     *
     * @param roleId 角色ID
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int physicalDeleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 按权限ID物理删除引用该权限的全部角色分配
     *
     * @param permId 权限ID
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_role_permission WHERE perm_id = #{permId}")
    int physicalDeleteByPermId(@Param("permId") Long permId);
}
