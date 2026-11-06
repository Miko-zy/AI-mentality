package com.aimental.controller;

import com.aimental.common.Result;
import com.aimental.dto.CrisisHandleDTO;
import com.aimental.service.CrisisAdminService;
import com.aimental.vo.CrisisVO;
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
 * 管理端安全危机事件管理控制器
 *
 * 接口清单（全部要求管理员角色）：
 * GET /api/admin/crisis             分页查询危机事件（支持handleStatus筛选）
 * PUT /api/admin/crisis/{crisisId}  修改处理状态并填写处理备注
 */
@RestController
@RequestMapping("/admin/crisis")
public class AdminCrisisController {

    /**
     * 危机事件管理服务
     */
    private final CrisisAdminService crisisAdminService;

    /**
     * 构造方法注入
     *
     * @param crisisAdminService 危机事件管理服务
     */
    public AdminCrisisController(CrisisAdminService crisisAdminService) {
        this.crisisAdminService = crisisAdminService;
    }

    /**
     * 分页查询危机事件
     *
     * @param pageNum      页码（缺省1）
     * @param pageSize     每页条数（缺省10）
     * @param handleStatus 处理状态：0待处理/1已处理（可空）
     * @return 危机事件分页结果
     */
    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public Result<PageVO<CrisisVO>> getCrisisRecords(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer handleStatus) {
        return Result.success(crisisAdminService.getCrisisRecords(pageNum, pageSize, handleStatus));
    }

    /**
     * 处理危机事件（修改处理状态并填写处理备注）
     *
     * @param crisisId 危机记录ID
     * @param dto      处理信息
     * @return 操作结果
     */
    @PutMapping("/{crisisId}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> handleCrisis(@PathVariable Long crisisId, @RequestBody CrisisHandleDTO dto) {
        crisisAdminService.handleCrisis(crisisId, dto);
        return Result.success(null);
    }
}
