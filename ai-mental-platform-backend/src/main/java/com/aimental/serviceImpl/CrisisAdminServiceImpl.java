package com.aimental.serviceImpl;

import com.aimental.dto.CrisisHandleDTO;
import com.aimental.entity.ChatMessage;
import com.aimental.entity.CrisisRecord;
import com.aimental.entity.User;
import com.aimental.exception.BusinessException;
import com.aimental.mapper.ChatMessageMapper;
import com.aimental.mapper.CrisisRecordMapper;
import com.aimental.mapper.UserMapper;
import com.aimental.service.CrisisAdminService;
import com.aimental.util.AesUtil;
import com.aimental.vo.CrisisVO;
import com.aimental.vo.PageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 安全危机事件管理服务实现类
 *
 * 事件查询：分页 + 处理状态筛选，批量补全用户昵称与关联风险消息内容；
 * 事件处理：修改handle_status并填写handle_remark，处理时刷新handle_time
 */
@Service
public class CrisisAdminServiceImpl implements CrisisAdminService {

    private static final Logger log = LoggerFactory.getLogger(CrisisAdminServiceImpl.class);

    /**
     * 危机记录Mapper
     */
    private final CrisisRecordMapper crisisRecordMapper;

    /**
     * 用户Mapper
     */
    private final UserMapper userMapper;

    /**
     * 对话消息Mapper
     */
    private final ChatMessageMapper chatMessageMapper;

    /**
     * AES加解密工具
     */
    private final AesUtil aesUtil;

    /**
     * 构造方法注入
     *
     * @param crisisRecordMapper 危机记录Mapper
     * @param userMapper         用户Mapper
     * @param chatMessageMapper  对话消息Mapper
     * @param aesUtil            AES加解密工具
     */
    public CrisisAdminServiceImpl(CrisisRecordMapper crisisRecordMapper,
                                  UserMapper userMapper,
                                  ChatMessageMapper chatMessageMapper,
                                  AesUtil aesUtil) {
        this.crisisRecordMapper = crisisRecordMapper;
        this.userMapper = userMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.aesUtil = aesUtil;
    }

    @Override
    public PageVO<CrisisVO> getCrisisRecords(long pageNum, long pageSize, Integer handleStatus) {
        Page<CrisisRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CrisisRecord> wrapper = new LambdaQueryWrapper<>();

        // 1. 处理状态筛选（0待处理/1已处理）
        if (handleStatus != null) {
            if (handleStatus != HANDLE_STATUS_PENDING && handleStatus != HANDLE_STATUS_DONE) {
                throw new IllegalArgumentException("处理状态只能为0待处理或1已处理");
            }
            wrapper.eq(CrisisRecord::getHandleStatus, handleStatus);
        }
        wrapper.orderByDesc(CrisisRecord::getCrisisId);
        Page<CrisisRecord> result = crisisRecordMapper.selectPage(page, wrapper);
        List<CrisisRecord> records = result.getRecords();
        if (records.isEmpty()) {
            return PageVO.of(result, new java.util.ArrayList<>());
        }

        // 2. 转VO
        List<CrisisVO> list = records.stream()
                .map(CrisisVO::of)
                .collect(Collectors.toList());

        // 3. 批量补全用户昵称（昵称为明文存储）
        Set<Long> userIds = records.stream()
                .map(CrisisRecord::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, String> nicknameMap = new HashMap<>();
            for (User u : users) {
                nicknameMap.put(u.getUserId(), u.getNickname());
            }
            for (CrisisVO vo : list) {
                vo.setUserNickname(nicknameMap.get(vo.getUserId()));
            }
        }

        // 4. 批量补全关联风险消息内容（AES解密）
        Set<Long> msgIds = records.stream()
                .map(CrisisRecord::getMsgId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!msgIds.isEmpty()) {
            List<ChatMessage> messages = chatMessageMapper.selectBatchIds(msgIds);
            Map<Long, String> contentMap = new HashMap<>();
            for (ChatMessage m : messages) {
                contentMap.put(m.getMsgId(), decryptQuietly(m.getContent()));
            }
            for (CrisisVO vo : list) {
                vo.setMsgContent(contentMap.get(vo.getMsgId()));
            }
        }
        return PageVO.of(result, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleCrisis(Long crisisId, CrisisHandleDTO dto) {
        // 1. 危机事件存在性校验
        if (crisisId == null) {
            throw new IllegalArgumentException("危机记录ID不能为空");
        }
        CrisisRecord crisis = crisisRecordMapper.selectById(crisisId);
        if (crisis == null) {
            throw new BusinessException(404, "危机事件不存在");
        }

        // 2. 参数校验
        if (dto == null || dto.getHandleStatus() == null) {
            throw new IllegalArgumentException("处理状态不能为空");
        }
        int status = dto.getHandleStatus();
        if (status != HANDLE_STATUS_PENDING && status != HANDLE_STATUS_DONE) {
            throw new IllegalArgumentException("处理状态只能为0待处理或1已处理");
        }
        if (dto.getHandleRemark() != null && dto.getHandleRemark().length() > 500) {
            throw new IllegalArgumentException("处理备注不能超过500字");
        }

        // 3. 更新处理状态、处理备注（未传备注时保留原值）与处理时间
        crisis.setHandleStatus(status);
        if (dto.getHandleRemark() != null) {
            crisis.setHandleRemark(dto.getHandleRemark());
        }
        crisis.setHandleTime(LocalDateTime.now());
        crisisRecordMapper.updateById(crisis);
        log.info("危机事件处理: crisisId={}, handleStatus={}", crisisId, status);
    }

    /**
     * 静默解密（失败返回null，不阻断列表展示）
     *
     * @param encrypted 密文
     * @return 明文或null
     */
    private String decryptQuietly(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) {
            return null;
        }
        try {
            return aesUtil.decrypt(encrypted);
        } catch (Exception e) {
            log.warn("消息内容AES解密失败: {}", e.getMessage());
            return null;
        }
    }
}
