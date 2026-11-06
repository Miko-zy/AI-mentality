package com.aimental.controller;

import com.aimental.common.Result;
import com.aimental.dto.UserStatusDTO;
import com.aimental.service.UserAdminService;
import com.aimental.vo.AdminUserVO;
import com.aimental.vo.PageVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端普通用户账号管理控制器
 *
 * 接口清单（全部要求管理员角色）：
 * GET /api/admin/users                    分页查询用户（支持keyword、accountStatus筛选）
 * PUT /api/admin/users/{userId}/status    启用/禁用用户账号（禁用时同步吊销登录Token）
 */
@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    /**
     * 用户账号管理服务
     */
    private final UserAdminService userAdminService;

    /**
     * 构造方法注入
     *
     * @param userAdminService 用户账号管理服务
     */
    public AdminUserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    /**
     * 分页查询普通用户
     *
     * @param pageNum       页码（缺省1）
     * @param pageSize      每页条数（缺省10）
     * @param keyword       关键字：昵称模糊或邮箱精确（可空）
     * @param accountStatus 账号状态：0禁用/1正常（可空）
     * @return 用户分页结果
     */
    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public Result<PageVO<AdminUserVO>> getUsers(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer accountStatus) {
        return Result.success(userAdminService.getUsers(pageNum, pageSize, keyword, accountStatus));
    }

    /**
     * 修改用户账号状态（禁用时同步吊销该用户登录Token）
     *
     * @param userId 用户ID
     * @param dto    状态信息
     * @return 操作结果
     */
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @RequestBody UserStatusDTO dto) {
        userAdminService.updateUserStatus(userId, dto);
        return Result.success(null);
    }
}
