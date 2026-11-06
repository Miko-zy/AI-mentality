package com.aimental.service;

import com.aimental.dto.ChangePwdDTO;
import com.aimental.dto.EmailCodeLoginDTO;
import com.aimental.dto.LoginDTO;
import com.aimental.dto.RegisterDTO;
import com.aimental.dto.UpdateUserInfoDTO;
import com.aimental.vo.BadgeVO;
import com.aimental.vo.CheckinRecordVO;
import com.aimental.vo.LoginVO;
import com.aimental.vo.PageVO;
import com.aimental.vo.UserProfileVO;

/**
 * 用户账号与个人中心服务接口
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param registerDTO 注册请求
     * @return 注册成功后返回登录结果（含JWT Token）
     */
    LoginVO register(RegisterDTO registerDTO);

    /**
     * 账号密码登录
     *
     * @param loginDTO 登录请求
     * @return 登录结果（含JWT Token）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 邮箱验证码登录（未注册时自动注册）
     *
     * @param emailCodeLoginDTO 邮箱验证码登录请求
     * @return 登录结果（含JWT Token）
     */
    LoginVO loginByEmailCode(EmailCodeLoginDTO emailCodeLoginDTO);

    /**
     * 发送邮箱验证码（存入Redis，有效期5分钟）
     *
     * @param email 邮箱
     */
    void sendEmailCode(String email);

    /**
     * 退出登录（删除Redis中的登录凭证，使Token立即失效）
     *
     * @param userId 用户ID
     */
    void logout(Long userId);

    /**
     * 修改密码
     *
     * @param userId        用户ID
     * @param changePwdDTO 修改密码请求
     */
    void changePassword(Long userId, ChangePwdDTO changePwdDTO);

    /**
     * 编辑个人资料（昵称、头像、实名认证标记、手机号、邮箱）
     *
     * @param userId              用户ID
     * @param updateUserInfoDTO   编辑个人资料请求
     */
    void updateUserInfo(Long userId, UpdateUserInfoDTO updateUserInfoDTO);

    /**
     * 获取个人档案（含累计评估次数、累计训练完成数量等统计）
     *
     * @param userId 用户ID
     * @return 个人档案
     */
    UserProfileVO getUserProfile(Long userId);

    /**
     * 分页查看当前用户打卡记录
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 打卡记录分页结果
     */
    PageVO<CheckinRecordVO> getCheckinRecords(Long userId, long pageNum, long pageSize);

    /**
     * 分页查看当前用户已解锁徽章列表
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 徽章列表分页结果
     */
    PageVO<BadgeVO> getMyBadges(Long userId, long pageNum, long pageSize);
}