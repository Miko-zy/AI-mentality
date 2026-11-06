package com.aimental.mapper;

import com.aimental.entity.DailyCheckin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日打卡Mapper接口
 */
@Mapper
public interface DailyCheckinMapper extends BaseMapper<DailyCheckin> {
}
