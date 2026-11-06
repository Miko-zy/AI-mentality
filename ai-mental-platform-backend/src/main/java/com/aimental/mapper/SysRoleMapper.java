package com.aimental.mapper;

import com.aimental.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统角色Mapper接口
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 按角色编码物理删除逻辑删除残留行
     * 角色编码有唯一键：逻辑删除的残留行仍占用唯一键，
     * 删除同编码角色后再次新增会主键冲突，新增前需物理清理残留
     *
     * @param roleCode 角色编码
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_role WHERE role_code = #{roleCode}")
    int physicalDeleteByRoleCode(@Param("roleCode") String roleCode);
}
