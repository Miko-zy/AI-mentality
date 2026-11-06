package com.aimental.mapper;

import com.aimental.entity.SysPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统权限Mapper接口
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 按权限编码物理删除逻辑删除残留行
     * 权限编码有唯一键：逻辑删除的残留行仍占用唯一键，
     * 删除同编码权限后再次新增会主键冲突，新增前需物理清理残留
     *
     * @param permCode 权限编码
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_permission WHERE perm_code = #{permCode}")
    int physicalDeleteByPermCode(@Param("permCode") String permCode);
}
