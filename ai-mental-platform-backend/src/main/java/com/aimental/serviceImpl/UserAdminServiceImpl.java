package com.aimental.serviceImpl;

import com.aimental.dto.UserStatusDTO;
import com.aimental.entity.User;
import com.aimental.exception.BusinessException;
import com.aimental.mapper.UserMapper;
import com.aimental.security.SecurityConstants;
import com.aimental.service.UserAdminService;
import com.aimental.util.AesUtil;
import com.aimental.util.RedisUtil;
import com.aimental.vo.AdminUserVO;
import com.aimental.vo.PageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 普通用户账号管理服务实现类
 *
 * 用户查询：分页 + 关键字（昵称模糊/邮箱精确，邮箱按AES密文匹配）+ 账号状态筛选；
 * 启用/禁用：更新account_status，禁用时同步删除Redis登录Token使该用户在线会话立即失效
 */
@Service
public class UserAdminServiceImpl implements UserAdminService {

    private static final Logger log = LoggerFactory.getLogger(UserAdminServiceImpl.class);

    /**
     * 用户Mapper
     */
    private final UserMapper userMapper;

    /**
     * AES加解密工具
     */
    private final AesUtil aesUtil;

    /**
     * Redis工具
     */
    private final RedisUtil redisUtil;

    /**
     * 构造方法注入
     *
     * @param userMapper 用户Mapper
     * @param aesUtil    AES加解密工具
     * @param redisUtil  Redis工具
     */
    public UserAdminServiceImpl(UserMapper userMapper, AesUtil aesUtil, RedisUtil redisUtil) {
        this.userMapper = userMapper;
        this.aesUtil = aesUtil;
        this.redisUtil = redisUtil;
    }

    @Override
    public PageVO<AdminUserVO> getUsers(long pageNum, long pageSize, String keyword, Integer accountStatus) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 1. 关键字：昵称模糊匹配 或 邮箱精确匹配（邮箱为AES密文存储，先加密关键字再匹配）
        String kw = keyword == null ? null : keyword.trim();
        if (kw != null && !kw.isEmpty()) {
            final String encryptedEmail = encryptQuietly(kw);
            wrapper.and(w -> {
                w.like(User::getNickname, kw);
                if (encryptedEmail != null && !encryptedEmail.isEmpty()) {
                    w.or().eq(User::getEmail, encryptedEmail);
                }
            });
        }

        // 2. 账号状态筛选
        if (accountStatus != null) {
            if (accountStatus != ACCOUNT_STATUS_DISABLE && accountStatus != ACCOUNT_STATUS_NORMAL) {
                throw new IllegalArgumentException("账号状态只能为0禁用或1正常");
            }
            wrapper.eq(User::getAccountStatus, accountStatus);
        }

        // 3. 分页查询（按用户ID倒序）
        wrapper.orderByDesc(User::getUserId);
        Page<User> result = userMapper.selectPage(page, wrapper);

        // 4. 转VO并解密邮箱、手机号
        List<AdminUserVO> list = result.getRecords().stream().map(user -> {
            AdminUserVO vo = AdminUserVO.of(user);
            vo.setEmail(decryptQuietly(user.getEmail()));
            vo.setPhone(decryptQuietly(user.getPhone()));
            return vo;
        }).collect(Collectors.toList());
        return PageVO.of(result, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, UserStatusDTO dto) {
        // 1. 用户存在性校验
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 2. 状态参数校验
        if (dto == null || dto.getAccountStatus() == null) {
            throw new IllegalArgumentException("账号状态不能为空");
        }
        int status = dto.getAccountStatus();
        if (status != ACCOUNT_STATUS_DISABLE && status != ACCOUNT_STATUS_NORMAL) {
            throw new IllegalArgumentException("账号状态只能为0禁用或1正常");
        }

        // 3. 更新账号状态
        user.setAccountStatus(status);
        userMapper.updateById(user);
        log.info("修改用户账号状态: userId={}, accountStatus={}", userId, status);

        // 4. 禁用时同步吊销该用户登录Token（Redis白名单删除，JWT立即失效）
        if (status == ACCOUNT_STATUS_DISABLE) {
            redisUtil.delete(SecurityConstants.tokenKey(SecurityConstants.ROLE_USER, userId));
            log.info("禁用用户并吊销登录Token: userId={}", userId);
        }
    }

    /**
     * 静默加密（失败返回null，不阻断查询）
     *
     * @param plain 明文
     * @return 密文或null
     */
    private String encryptQuietly(String plain) {
        try {
            return aesUtil.encrypt(plain);
        } catch (Exception e) {
            log.warn("关键字AES加密失败: {}", e.getMessage());
            return null;
        }
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
            log.warn("字段AES解密失败: {}", e.getMessage());
            return null;
        }
    }
}
