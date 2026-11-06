package com.aimental.serviceImpl;

import com.aimental.dto.ChangePwdDTO;
import com.aimental.dto.EmailCodeLoginDTO;
import com.aimental.dto.LoginDTO;
import com.aimental.dto.RegisterDTO;
import com.aimental.dto.UpdateUserInfoDTO;
import com.aimental.entity.Badge;
import com.aimental.entity.DailyCheckin;
import com.aimental.entity.StudyProgress;
import com.aimental.entity.User;
import com.aimental.entity.UserBadge;
import com.aimental.entity.UserScaleRecord;
import com.aimental.exception.BusinessException;
import com.aimental.mapper.BadgeMapper;
import com.aimental.mapper.DailyCheckinMapper;
import com.aimental.mapper.StudyProgressMapper;
import com.aimental.mapper.UserBadgeMapper;
import com.aimental.mapper.UserMapper;
import com.aimental.mapper.UserScaleRecordMapper;
import com.aimental.security.SecurityConstants;
import com.aimental.service.UserService;
import com.aimental.util.AesUtil;
import com.aimental.util.BcryptUtil;
import com.aimental.util.JwtUtil;
import com.aimental.util.ParamValidator;
import com.aimental.util.RedisUtil;
import com.aimental.vo.BadgeVO;
import com.aimental.vo.CheckinRecordVO;
import com.aimental.vo.LoginVO;
import com.aimental.vo.PageVO;
import com.aimental.vo.UserProfileVO;
import com.aimental.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户账号与个人中心服务实现
 * 负责注册、登录、退出登录、修改密码、编辑资料、个人档案、打卡记录、徽章列表等业务
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * 用户Mapper
     */
    private final UserMapper userMapper;

    /**
     * 用户心理评估记录Mapper（用于统计累计评估次数）
     */
    private final UserScaleRecordMapper userScaleRecordMapper;

    /**
     * 用户学习进度Mapper（用于统计累计训练完成数量）
     */
    private final StudyProgressMapper studyProgressMapper;

    /**
     * 每日打卡Mapper（用于统计累计打卡次数）
     */
    private final DailyCheckinMapper dailyCheckinMapper;

    /**
     * 用户徽章关联Mapper（用于查询已解锁徽章）
     */
    private final UserBadgeMapper userBadgeMapper;

    /**
     * 徽章Mapper（用于查询徽章详情）
     */
    private final BadgeMapper badgeMapper;

    /**
     * JWT工具类
     */
    private final JwtUtil jwtUtil;

    /**
     * AES-256加密工具类（手机号、邮箱加密）
     */
    private final AesUtil aesUtil;

    /**
     * Bcrypt密码加密工具类
     */
    private final BcryptUtil bcryptUtil;

    /**
     * Redis工具类（验证码、登录凭证）
     */
    private final RedisUtil redisUtil;

    /**
     * JWT过期时间（毫秒）
     */
    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * 构造方法注入依赖
     */
    public UserServiceImpl(UserMapper userMapper, UserScaleRecordMapper userScaleRecordMapper,
                           StudyProgressMapper studyProgressMapper, DailyCheckinMapper dailyCheckinMapper,
                           UserBadgeMapper userBadgeMapper, BadgeMapper badgeMapper,
                           JwtUtil jwtUtil, AesUtil aesUtil, BcryptUtil bcryptUtil, RedisUtil redisUtil) {
        this.userMapper = userMapper;
        this.userScaleRecordMapper = userScaleRecordMapper;
        this.studyProgressMapper = studyProgressMapper;
        this.dailyCheckinMapper = dailyCheckinMapper;
        this.userBadgeMapper = userBadgeMapper;
        this.badgeMapper = badgeMapper;
        this.jwtUtil = jwtUtil;
        this.aesUtil = aesUtil;
        this.bcryptUtil = bcryptUtil;
        this.redisUtil = redisUtil;
    }

    /**
     * 用户注册
     * 校验邮箱验证码 -> 校验用户名、邮箱唯一 -> bcrypt加密密码 -> 生成JWT Token返回
     *
     * @param registerDTO 注册请求
     * @return 登录结果（含JWT Token）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO registerDTO) {
        // 0.参数校验
        ParamValidator.lengthBetween(registerDTO.getUsername(), 2, 20, "用户名长度需在2-20个字符之间");
        ParamValidator.isEmail(registerDTO.getEmail(), "邮箱格式不正确");
        ParamValidator.isPassword(registerDTO.getPassword(), "密码长度需在6-20位之间，且需包含字母和数字");
        // 联调阶段暂时注释验证码校验，便于直接测试注册入库；上线前恢复
        // ParamValidator.isSixDigitCode(registerDTO.getCode(), "验证码为6位数字");

        String username = registerDTO.getUsername().trim();
        String email = normalizeEmail(registerDTO.getEmail());

        // 1.校验邮箱验证码（联调阶段已注释，恢复验证码校验时放开）
        // verifyEmailCode(email, registerDTO.getCode());

        // 2.校验用户名唯一（用户名对应user表的nickname字段）
        Long nicknameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getNickname, username));
        if (nicknameCount != null && nicknameCount > 0) {
            throw new BusinessException("用户名已被注册，请更换用户名");
        }

        // 3.校验邮箱唯一（邮箱AES加密后按密文比对）
        String encryptedEmail = aesUtil.encrypt(email);
        Long emailCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, encryptedEmail));
        if (emailCount != null && emailCount > 0) {
            throw new BusinessException("该邮箱已被注册，请直接登录");
        }

        // 4.构建用户并保存
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setNickname(username);
        user.setEmail(encryptedEmail);
        user.setPassword(bcryptUtil.encode(registerDTO.getPassword()));
        user.setRegisterTime(now);
        user.setAccountStatus(1);
        user.setRealAuthFlag(0);
        user.setTotalPoints(0);
        user.setUserLevel(1);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setIsDeleted(0);
        userMapper.insert(user);

        // 5.验证码一次性使用，注册成功后删除（联调阶段已注释，需连Redis时放开）
        // redisUtil.delete(SecurityConstants.emailCodeKey(email));
        log.info("用户注册成功: userId={}, email={}", user.getUserId(), email);

        // 6.生成Token并返回登录结果
        return buildLoginVO(user);
    }

    /**
     * 账号密码登录
     *
     * @param loginDTO 登录请求
     * @return 登录结果（含JWT Token）
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 参数校验
        ParamValidator.isEmail(loginDTO.getEmail(), "邮箱格式不正确");
        ParamValidator.notBlank(loginDTO.getPassword(), "密码不能为空");
        String email = normalizeEmail(loginDTO.getEmail());
        // 邮箱按AES加密后查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, aesUtil.encrypt(email)));
        if (user == null || !bcryptUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            // 统一提示，避免暴露账号是否存在
            throw new BusinessException("账号或密码错误");
        }
        checkAccountStatus(user);
        updateLastLoginTime(user);
        log.info("用户账号密码登录成功: userId={}, email={}", user.getUserId(), email);
        return buildLoginVO(user);
    }

    /**
     * 邮箱验证码登录
     * 未注册的邮箱首次验证码登录时自动完成注册（一键登录注册）
     *
     * @param emailCodeLoginDTO 邮箱验证码登录请求
     * @return 登录结果（含JWT Token）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO loginByEmailCode(EmailCodeLoginDTO emailCodeLoginDTO) {
        // 参数校验
        ParamValidator.isEmail(emailCodeLoginDTO.getEmail(), "邮箱格式不正确");
        ParamValidator.isSixDigitCode(emailCodeLoginDTO.getCode(), "验证码为6位数字");
        String email = normalizeEmail(emailCodeLoginDTO.getEmail());
        // 1.校验邮箱验证码
        verifyEmailCode(email, emailCodeLoginDTO.getCode());

        // 2.按加密邮箱查询用户，不存在则自动注册
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, aesUtil.encrypt(email)));
        if (user == null) {
            user = buildAutoRegisterUser(email);
            userMapper.insert(user);
            log.info("邮箱验证码登录自动注册: userId={}, email={}", user.getUserId(), email);
        }
        // 3.验证码一次性使用，登录成功后删除
        redisUtil.delete(SecurityConstants.emailCodeKey(email));

        checkAccountStatus(user);
        updateLastLoginTime(user);
        return buildLoginVO(user);
    }

    /**
     * 发送邮箱验证码（存入Redis，有效期5分钟）
     * 未接入SMTP邮件服务时验证码打印在控制台日志中，便于本地联调
     *
     * @param email 邮箱
     */
    @Override
    public void sendEmailCode(String email) {
        // 参数校验
        ParamValidator.isEmail(email, "邮箱格式不正确");
        String normalizedEmail = normalizeEmail(email);
        // 生成6位随机数字验证码
        String code = RandomUtil.randomNumbers(6);
        redisUtil.set(SecurityConstants.emailCodeKey(normalizedEmail), code,
                SecurityConstants.EMAIL_CODE_TTL_MINUTES, TimeUnit.MINUTES);
        // TODO 可在此接入spring-boot-starter-mail发送邮件，当前打印日志便于调试
        log.info("【邮箱验证码】发送至 {} 的验证码为: {}，有效期5分钟", normalizedEmail, code);
    }

    /**
     * 退出登录
     * 删除Redis中的登录Token，使当前Token立即失效
     *
     * @param userId 用户ID
     */
    @Override
    public void logout(Long userId) {
        redisUtil.delete(SecurityConstants.tokenKey(SecurityConstants.ROLE_USER, userId));
        log.info("用户退出登录: userId={}", userId);
    }

    /**
     * 修改密码
     * 校验旧密码 -> 新密码bcrypt加密保存
     *
     * @param userId       用户ID
     * @param changePwdDTO 修改密码请求
     */
    @Override
    public void changePassword(Long userId, ChangePwdDTO changePwdDTO) {
        // 参数校验
        ParamValidator.notBlank(changePwdDTO.getOldPassword(), "旧密码不能为空");
        ParamValidator.isPassword(changePwdDTO.getNewPassword(), "密码长度需在6-20位之间，且需包含字母和数字");
        User user = getUserById(userId);
        // 校验旧密码
        if (!bcryptUtil.matches(changePwdDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        if (changePwdDTO.getOldPassword().equals(changePwdDTO.getNewPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }
        // 新密码bcrypt加密保存
        user.setPassword(bcryptUtil.encode(changePwdDTO.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("用户修改密码成功: userId={}", userId);
    }

    /**
     * 编辑个人资料
     * 昵称、头像、实名认证标记直接更新；手机号、邮箱业务层AES加密后保存
     *
     * @param userId            用户ID
     * @param updateUserInfoDTO 编辑个人资料请求
     */
    @Override
    public void updateUserInfo(Long userId, UpdateUserInfoDTO updateUserInfoDTO) {
        // 参数校验（可选字段仅在非空时校验）
        if (StrUtil.isNotBlank(updateUserInfoDTO.getNickname())) {
            ParamValidator.lengthBetween(updateUserInfoDTO.getNickname(), 2, 20, "昵称长度需在2-20个字符之间");
        }
        if (updateUserInfoDTO.getAvatarUrl() != null) {
            ParamValidator.maxLength(updateUserInfoDTO.getAvatarUrl(), 255, "头像地址长度不能超过255个字符");
        }
        if (StrUtil.isNotBlank(updateUserInfoDTO.getPhone())) {
            ParamValidator.isPhone(updateUserInfoDTO.getPhone(), "手机号格式不正确");
        }
        if (StrUtil.isNotBlank(updateUserInfoDTO.getEmail())) {
            ParamValidator.isEmail(updateUserInfoDTO.getEmail(), "邮箱格式不正确");
        }
        User user = getUserById(userId);

        // 昵称（非空且变化时校验唯一，排除自身）
        if (StrUtil.isNotBlank(updateUserInfoDTO.getNickname())) {
            String nickname = updateUserInfoDTO.getNickname().trim();
            if (!nickname.equals(user.getNickname())) {
                Long count = userMapper.selectCount(
                        new LambdaQueryWrapper<User>().eq(User::getNickname, nickname));
                if (count != null && count > 0) {
                    throw new BusinessException("昵称已被占用，请更换昵称");
                }
                user.setNickname(nickname);
            }
        }

        // 头像URL
        if (updateUserInfoDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(updateUserInfoDTO.getAvatarUrl());
        }

        // 实名认证标记（仅允许0/1）
        if (updateUserInfoDTO.getRealAuthFlag() != null) {
            if (updateUserInfoDTO.getRealAuthFlag() != 0 && updateUserInfoDTO.getRealAuthFlag() != 1) {
                throw new BusinessException("实名认证标记只能为0或1");
            }
            user.setRealAuthFlag(updateUserInfoDTO.getRealAuthFlag());
        }

        // 手机号（AES加密后保存）
        if (StrUtil.isNotBlank(updateUserInfoDTO.getPhone())) {
            user.setPhone(aesUtil.encrypt(updateUserInfoDTO.getPhone().trim()));
        }

        // 邮箱（AES加密后保存，变化时校验唯一，排除自身）
        if (StrUtil.isNotBlank(updateUserInfoDTO.getEmail())) {
            String email = normalizeEmail(updateUserInfoDTO.getEmail());
            String encryptedEmail = aesUtil.encrypt(email);
            if (!encryptedEmail.equals(user.getEmail())) {
                Long count = userMapper.selectCount(
                        new LambdaQueryWrapper<User>().eq(User::getEmail, encryptedEmail));
                if (count != null && count > 0) {
                    throw new BusinessException("该邮箱已被其他账号使用");
                }
                user.setEmail(encryptedEmail);
            }
        }

        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("用户编辑个人资料成功: userId={}", userId);
    }

    /**
     * 获取个人档案
     * 基础信息（邮箱、手机号解密返回）+ 成长统计
     *
     * @param userId 用户ID
     * @return 个人档案
     */
    @Override
    public UserProfileVO getUserProfile(Long userId) {
        User user = getUserById(userId);
        UserProfileVO vo = new UserProfileVO();
        fillBaseInfo(vo, user);

        // 累计评估次数
        Long assessCount = userScaleRecordMapper.selectCount(
                new LambdaQueryWrapper<UserScaleRecord>().eq(UserScaleRecord::getUserId, userId));
        // 累计训练完成数量（完成率>=100%的课程数）
        Long courseFinishCount = studyProgressMapper.selectCount(
                new LambdaQueryWrapper<StudyProgress>()
                        .eq(StudyProgress::getUserId, userId)
                        .ge(StudyProgress::getFinishRate, new BigDecimal("100")));
        // 累计打卡次数
        Long checkinCount = dailyCheckinMapper.selectCount(
                new LambdaQueryWrapper<DailyCheckin>().eq(DailyCheckin::getUserId, userId));
        // 已解锁徽章数量
        Long badgeCount = userBadgeMapper.selectCount(
                new LambdaQueryWrapper<UserBadge>().eq(UserBadge::getUserId, userId));

        vo.setAssessCount(assessCount == null ? 0L : assessCount);
        vo.setCourseFinishCount(courseFinishCount == null ? 0L : courseFinishCount);
        vo.setCheckinCount(checkinCount == null ? 0L : checkinCount);
        vo.setBadgeCount(badgeCount == null ? 0L : badgeCount);
        return vo;
    }

    /**
     * 分页查看当前用户打卡记录
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 打卡记录分页结果
     */
    @Override
    public PageVO<CheckinRecordVO> getCheckinRecords(Long userId, long pageNum, long pageSize) {
        Page<DailyCheckin> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DailyCheckin> wrapper = new LambdaQueryWrapper<DailyCheckin>()
                .eq(DailyCheckin::getUserId, userId)
                .orderByDesc(DailyCheckin::getCheckinDate);
        Page<DailyCheckin> result = dailyCheckinMapper.selectPage(page, wrapper);

        List<CheckinRecordVO> list = result.getRecords().stream().map(record -> {
            CheckinRecordVO vo = new CheckinRecordVO();
            vo.setCheckinId(record.getCheckinId());
            vo.setCheckinDate(record.getCheckinDate());
            vo.setMoodDesc(record.getMoodDesc());
            vo.setCreateTime(record.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
        return PageVO.of(result, list);
    }

    /**
     * 分页查看当前用户已解锁徽章列表
     * 查询用户徽章关联记录后，批量查询徽章详情组装返回
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 徽章列表分页结果
     */
    @Override
    public PageVO<BadgeVO> getMyBadges(Long userId, long pageNum, long pageSize) {
        Page<UserBadge> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UserBadge> wrapper = new LambdaQueryWrapper<UserBadge>()
                .eq(UserBadge::getUserId, userId)
                .orderByDesc(UserBadge::getObtainTime);
        Page<UserBadge> ubPage = userBadgeMapper.selectPage(page, wrapper);

        if (ubPage.getRecords().isEmpty()) {
            return PageVO.of(ubPage, Collections.emptyList());
        }

        // 批量查询徽章详情，组装成 userId -> Badge 映射
        List<Long> badgeIds = ubPage.getRecords().stream()
                .map(UserBadge::getBadgeId)
                .distinct()
                .collect(Collectors.toList());
        List<Badge> badges = badgeMapper.selectBatchIds(badgeIds);
        Map<Long, Badge> badgeMap = badges.stream()
                .collect(Collectors.toMap(Badge::getBadgeId, badge -> badge, (oldValue, newValue) -> oldValue));

        List<BadgeVO> list = new ArrayList<>();
        for (UserBadge ub : ubPage.getRecords()) {
            Badge badge = badgeMap.get(ub.getBadgeId());
            if (badge == null) {
                continue;
            }
            BadgeVO vo = new BadgeVO();
            vo.setBadgeId(badge.getBadgeId());
            vo.setBadgeName(badge.getBadgeName());
            vo.setBadgeIcon(badge.getBadgeIcon());
            vo.setDescription(badge.getDescription());
            vo.setObtainTime(ub.getObtainTime());
            list.add(vo);
        }
        return PageVO.of(ubPage, list);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据用户ID查询用户，不存在时抛出业务异常
     *
     * @param userId 用户ID
     * @return 用户实体
     */
    private User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    /**
     * 校验账号状态，禁用账号禁止登录
     *
     * @param user 用户实体
     */
    private void checkAccountStatus(User user) {
        if (user.getAccountStatus() == null || user.getAccountStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
    }

    /**
     * 更新用户最后登录时间
     *
     * @param user 用户实体
     */
    private void updateLastLoginTime(User user) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 生成登录结果：JWT Token + 用户信息，并将Token写入Redis登录凭证
     *
     * @param user 用户实体
     * @return 登录结果
     */
    private LoginVO buildLoginVO(User user) {
        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), SecurityConstants.ROLE_USER);
        // Token写入Redis，退出登录时删除即可使Token失效（有效期与JWT过期时间一致）
        long ttlSeconds = jwtExpiration / 1000;
        redisUtil.set(SecurityConstants.tokenKey(SecurityConstants.ROLE_USER, user.getUserId()),
                token, ttlSeconds, TimeUnit.SECONDS);

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(buildUserVO(user));
        return loginVO;
    }

    /**
     * 构建用户信息视图对象（邮箱、手机号AES解密后返回）
     *
     * @param user 用户实体
     * @return 用户信息视图对象
     */
    private UserVO buildUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setUserId(user.getUserId());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setEmail(aesUtil.decrypt(user.getEmail()));
        vo.setPhone(aesUtil.decrypt(user.getPhone()));
        vo.setRealAuthFlag(user.getRealAuthFlag());
        vo.setAccountStatus(user.getAccountStatus());
        vo.setTotalPoints(user.getTotalPoints());
        vo.setUserLevel(user.getUserLevel());
        vo.setRole(SecurityConstants.ROLE_USER);
        return vo;
    }

    /**
     * 填充个人档案基础信息（邮箱、手机号AES解密后返回）
     *
     * @param profile 个人档案视图对象
     * @param user    用户实体
     */
    private void fillBaseInfo(UserProfileVO profile, User user) {
        profile.setUserId(user.getUserId());
        profile.setNickname(user.getNickname());
        profile.setAvatarUrl(user.getAvatarUrl());
        profile.setEmail(aesUtil.decrypt(user.getEmail()));
        profile.setPhone(aesUtil.decrypt(user.getPhone()));
        profile.setRealAuthFlag(user.getRealAuthFlag());
        profile.setAccountStatus(user.getAccountStatus());
        profile.setTotalPoints(user.getTotalPoints());
        profile.setUserLevel(user.getUserLevel());
        profile.setRegisterTime(user.getRegisterTime());
        profile.setRole(SecurityConstants.ROLE_USER);
    }

    /**
     * 邮箱验证码登录时自动注册用户
     * 昵称取邮箱@前缀，生成随机密码（仅邮箱验证码登录场景使用）
     *
     * @param email 邮箱
     * @return 自动注册的用户实体（未执行insert）
     */
    private User buildAutoRegisterUser(String email) {
        LocalDateTime now = LocalDateTime.now();
        // 昵称取邮箱@前缀，超长截断；与已有用户冲突时追加随机数字后缀
        String nickname = email.substring(0, email.indexOf('@'));
        if (nickname.length() > 20) {
            nickname = nickname.substring(0, 20);
        }
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getNickname, nickname));
        if (count != null && count > 0) {
            String suffix = RandomUtil.randomNumbers(4);
            String newNickname = nickname + suffix;
            nickname = newNickname.length() > 20 ? newNickname.substring(0, 20) : newNickname;
        }

        User user = new User();
        user.setNickname(nickname);
        user.setEmail(aesUtil.encrypt(email));
        // 自动注册生成随机密码（用户后续可通过找我忘记密码重置，此处保证密码字段非空）
        user.setPassword(bcryptUtil.encode(RandomUtil.randomString(16)));
        user.setRegisterTime(now);
        user.setAccountStatus(1);
        user.setRealAuthFlag(0);
        user.setTotalPoints(0);
        user.setUserLevel(1);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setIsDeleted(0);
        return user;
    }

    /**
     * 校验邮箱验证码
     *
     * @param email     邮箱
     * @param inputCode 用户输入的验证码
     */
    private void verifyEmailCode(String email, String inputCode) {
        String code = redisUtil.get(SecurityConstants.emailCodeKey(email));
        if (StrUtil.isBlank(code)) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!code.equals(inputCode)) {
            throw new BusinessException("验证码错误");
        }
    }

    /**
     * 邮箱规范化：去除首尾空格并转小写
     *
     * @param email 原始邮箱
     * @return 规范化后的邮箱
     */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}