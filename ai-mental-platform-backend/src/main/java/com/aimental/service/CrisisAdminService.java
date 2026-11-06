package com.aimental.service;

import com.aimental.dto.CrisisHandleDTO;
import com.aimental.vo.CrisisVO;
import com.aimental.vo.PageVO;

/**
 * 安全危机事件管理服务接口（管理员运营模块）
 * 提供危机事件分页查询、修改处理状态与处理备注能力
 */
public interface CrisisAdminService {

    /**
     * 处理状态：待处理
     */
    Integer HANDLE_STATUS_PENDING = 0;

    /**
     * 处理状态：已处理
     */
    Integer HANDLE_STATUS_DONE = 1;

    /**
     * 分页查询危机事件
     *
     * @param pageNum     页码
     * @param pageSize    每页条数
     * @param handleStatus 处理状态筛选（可空）
     * @return 危机事件分页结果
     */
    PageVO<CrisisVO> getCrisisRecords(long pageNum, long pageSize, Integer handleStatus);

    /**
     * 处理危机事件：修改处理状态并填写处理备注
     *
     * @param crisisId 危机记录ID
     * @param dto      处理信息
     */
    void handleCrisis(Long crisisId, CrisisHandleDTO dto);
}
