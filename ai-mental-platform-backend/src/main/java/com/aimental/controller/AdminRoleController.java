package com.aimental.controller;

import com.aimental.common.Result;
import com.aimental.dto.RolePermAssignDTO;
import com.aimental.dto.RoleSaveDTO;
import com.aimental.service.RbacAdminService;
import com.aimental.vo.PermissionVO;
import com.aimental.vo.RoleVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端RBAC角色管理控制器
 *
 * 接口清单（全部要求管理员角色）：
 * GET    /api/admin/roles                     查询全部角色
 * POST   /api/admin/roles                     新增角色
 * PUT    /api/admin/roles/{roleId}            编辑角色（不修改角色编码）
 * DELETE /api/admin/roles/{roleId}            删除角色（级联清理权限分配，内置角色不可删）
 * GET    /api/admin/roles/{roleId}/permissions 查询角色已分配权限
 * PUT    /api/admin/roles/{roleId}/permissions 给角色分配权限（全量覆盖）
 */
@RestController
@RequestMapping("/admin/roles")
public class AdminRoleController {

    /**
     * RBAC权限管理服务
     */
    private final RbacAdminService rbacAdminService;

    /**
     * 构造方法注入
     *
     * @param rbacAdminService RBAC权限管理服务
     */
    public AdminRoleController(RbacAdminService rbacAdminService) {
        this.rbacAdminService = rbacAdminService;
    }

    /**
     * 查询全部角色列表
     *
     * @return 角色列表
     */
    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public Result<List<RoleVO>> getRoles() {
        return Result.success(rbacAdminService.getRoles());
    }

    /**
     * 新增角色
     *
     * @param dto 角色新增信息
     * @return 新增后的角色
     */
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<RoleVO> createRole(@RequestBody RoleSaveDTO dto) {
        return Result.success(rbacAdminService.createRole(dto));
    }

    /**
     * 编辑角色（角色编码不允许修改）
     *
     * @param roleId 角色ID
     * @param dto    角色编辑信息
     * @return 编辑后的角色
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasRole('admin')")
    public Result<RoleVO> updateRole(@PathVariable Long roleId, @RequestBody RoleSaveDTO dto) {
        return Result.success(rbacAdminService.updateRole(roleId, dto));
    }

    /**
     * 删除角色（逻辑删除并级联清理权限分配，内置角色不允许删除）
     *
     * @param roleId 角色ID
     * @return 操作结果
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> deleteRole(@PathVariable Long roleId) {
        rbacAdminService.deleteRole(roleId);
        return Result.success(null);
    }

    /**
     * 查询指定角色已分配的权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('admin')")
    public Result<List<PermissionVO>> getRolePermissions(@PathVariable Long roleId) {
        return Result.success(rbacAdminService.getRolePermissions(roleId));
    }

    /**
     * 给角色分配权限（全量覆盖：以请求permIds为准，传空数组表示清空）
     *
     * @param roleId 角色ID
     * @param dto    权限ID列表
     * @return 操作结果
     */
    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> assignPermissions(@PathVariable Long roleId, @RequestBody RolePermAssignDTO dto) {
        rbacAdminService.assignPermissions(roleId, dto);
        return Result.success(null);
    }
}
