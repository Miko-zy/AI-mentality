package com.aimental.mapper;

import com.aimental.entity.StudyProgress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户学习进度Mapper接口
 */
@Mapper
public interface StudyProgressMapper extends BaseMapper<StudyProgress> {
}
