package com.aimental.mapper;

import com.aimental.entity.AiSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI对话会话Mapper接口
 */
@Mapper
public interface AiSessionMapper extends BaseMapper<AiSession> {
}
