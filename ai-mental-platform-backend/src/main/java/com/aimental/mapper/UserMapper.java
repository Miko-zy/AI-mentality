package com.aimental.mapper;

import com.aimental.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 普通用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
