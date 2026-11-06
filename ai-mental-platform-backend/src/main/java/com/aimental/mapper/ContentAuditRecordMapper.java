package com.aimental.mapper;

import com.aimental.entity.ContentAuditRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 内容审核记录Mapper接口
 */
@Mapper
public interface ContentAuditRecordMapper extends BaseMapper<ContentAuditRecord> {
}
