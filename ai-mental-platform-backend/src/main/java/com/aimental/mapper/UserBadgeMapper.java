package com.aimental.mapper;

import com.aimental.entity.UserBadge;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户获得徽章关联Mapper接口
 */
@Mapper
public interface UserBadgeMapper extends BaseMapper<UserBadge> {
}
