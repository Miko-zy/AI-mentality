package com.aimental.service;

import com.aimental.dto.PermissionSaveDTO;
import com.aimental.dto.RolePermAssignDTO;
import com.aimental.dto.RoleSaveDTO;
import com.aimental.vo.PermissionVO;
import com.aimental.vo.RoleVO;

import java.util.List;

/**
 * RBAC权限管理服务接口（管理员运营模块）
 * 提供角色维护、权限字典维护、角色权限分配能力
 */
public interface RbacAdminService {

    /**
     * 角色状态：停用
     */
    Integer ROLE_STATUS_DISABLE = 0;

    /**
     * 角色状态：启用
     */
    Integer ROLE_STATUS_ENABLE = 1;

    /**
     * 权限类型：菜单
     */
    Integer PERM_TYPE_MENU = 1;

    /**
     * 权限类型：按钮/操作
     */
    Integer PERM_TYPE_BUTTON = 2;

    /**
     * 内置角色编码（不允许删除）
     */
    String BUILTIN_ROLE_CODE = "super_admin";

    /**
     * 查询全部角色列表
     *
     * @return 角色列表
     */
    List<RoleVO> getRoles();

    /**
     * 新增角色
     *
     * @param dto 角色新增信息
     * @return 新增后的角色
     */
    RoleVO createRole(RoleSaveDTO dto);

    /**
     * 编辑角色（不允许修改角色编码）
     *
     * @param roleId 角色ID
     * @param dto    角色编辑信息
     * @return 编辑后的角色
     */
    RoleVO updateRole(Long roleId, RoleSaveDTO dto);

    /**
     * 删除角色（逻辑删除，并级联清理该角色的权限分配）
     *
     * @param roleId 角色ID
     */
    void deleteRole(Long roleId);

    /**
     * 查询全部权限字典
     *
     * @return 权限列表
     */
    List<PermissionVO> getPermissions();

    /**
     * 新增权限
     *
     * @param dto 权限新增信息
     * @return 新增后的权限
     */
    PermissionVO createPermission(PermissionSaveDTO dto);

    /**
     * 编辑权限（不允许修改权限编码）
     *
     * @param permId 权限ID
     * @param dto    权限编辑信息
     * @return 编辑后的权限
     */
    PermissionVO updatePermission(Long permId, PermissionSaveDTO dto);

    /**
     * 删除权限（逻辑删除，并级联清理该权限的角色分配）
     *
     * @param permId 权限ID
     */
    void deletePermission(Long permId);

    /**
     * 查询指定角色已分配的权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<PermissionVO> getRolePermissions(Long roleId);

    /**
     * 给角色分配权限（全量覆盖：以请求中的permIds为准）
     *
     * @param roleId 角色ID
     * @param dto    权限ID列表
     */
    void assignPermissions(Long roleId, RolePermAssignDTO dto);
}
