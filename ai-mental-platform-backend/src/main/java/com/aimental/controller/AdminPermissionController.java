package com.aimental.controller;

import com.aimental.common.Result;
import com.aimental.dto.PermissionSaveDTO;
import com.aimental.service.RbacAdminService;
import com.aimental.vo.PermissionVO;
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
 * 管理端RBAC权限字典管理控制器
 *
 * 接口清单（全部要求管理员角色）：
 * GET    /api/admin/permissions        查询全部权限字典
 * POST   /api/admin/permissions        新增权限
 * PUT    /api/admin/permissions/{permId}   编辑权限（不修改权限编码）
 * DELETE /api/admin/permissions/{permId}   删除权限（级联清理角色分配）
 */
@RestController
@RequestMapping("/admin/permissions")
public class AdminPermissionController {

    /**
     * RBAC权限管理服务
     */
    private final RbacAdminService rbacAdminService;

    /**
     * 构造方法注入
     *
     * @param rbacAdminService RBAC权限管理服务
     */
    public AdminPermissionController(RbacAdminService rbacAdminService) {
        this.rbacAdminService = rbacAdminService;
    }

    /**
     * 查询全部权限字典
     *
     * @return 权限列表
     */
    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public Result<List<PermissionVO>> getPermissions() {
        return Result.success(rbacAdminService.getPermissions());
    }

    /**
     * 新增权限
     *
     * @param dto 权限新增信息
     * @return 新增后的权限
     */
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<PermissionVO> createPermission(@RequestBody PermissionSaveDTO dto) {
        return Result.success(rbacAdminService.createPermission(dto));
    }

    /**
     * 编辑权限（权限编码不允许修改）
     *
     * @param permId 权限ID
     * @param dto    权限编辑信息
     * @return 编辑后的权限
     */
    @PutMapping("/{permId}")
    @PreAuthorize("hasRole('admin')")
    public Result<PermissionVO> updatePermission(@PathVariable Long permId, @RequestBody PermissionSaveDTO dto) {
        return Result.success(rbacAdminService.updatePermission(permId, dto));
    }

    /**
     * 删除权限（逻辑删除并级联清理角色分配）
     *
     * @param permId 权限ID
     * @return 操作结果
     */
    @DeleteMapping("/{permId}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> deletePermission(@PathVariable Long permId) {
        rbacAdminService.deletePermission(permId);
        return Result.success(null);
    }
}
