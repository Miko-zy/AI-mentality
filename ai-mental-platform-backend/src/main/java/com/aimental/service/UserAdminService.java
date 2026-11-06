package com.aimental.service;

import com.aimental.dto.UserStatusDTO;
import com.aimental.vo.AdminUserVO;
import com.aimental.vo.PageVO;

/**
 * 普通用户账号管理服务接口（管理员运营模块）
 * 提供用户分页查询与账号启用/禁用能力
 */
public interface UserAdminService {

    /**
     * 账号状态：禁用
     */
    Integer ACCOUNT_STATUS_DISABLE = 0;

    /**
     * 账号状态：正常
     */
    Integer ACCOUNT_STATUS_NORMAL = 1;

    /**
     * 分页查询普通用户
     *
     * @param pageNum       页码
     * @param pageSize      每页条数
     * @param keyword       关键字（模糊匹配昵称或精确匹配邮箱）
     * @param accountStatus 账号状态筛选（可空）
     * @return 用户分页结果
     */
    PageVO<AdminUserVO> getUsers(long pageNum, long pageSize, String keyword, Integer accountStatus);

    /**
     * 修改用户账号状态（禁用时同步吊销该用户登录Token）
     *
     * @param userId 用户ID
     * @param dto    状态信息
     */
    void updateUserStatus(Long userId, UserStatusDTO dto);
}
