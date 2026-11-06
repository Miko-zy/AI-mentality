package com.aimental.controller;

import com.aimental.common.Result;
import com.aimental.dto.ChangePwdDTO;
import com.aimental.dto.EmailCodeLoginDTO;
import com.aimental.dto.LoginDTO;
import com.aimental.dto.RegisterDTO;
import com.aimental.dto.SendEmailCodeDTO;
import com.aimental.dto.UpdateUserInfoDTO;
import com.aimental.security.SecurityUtil;
import com.aimental.service.UserService;
import com.aimental.vo.BadgeVO;
import com.aimental.vo.CheckinRecordVO;
import com.aimental.vo.LoginVO;
import com.aimental.vo.PageVO;
import com.aimental.vo.UserProfileVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户账号与个人中心控制器
 * 注册、登录、邮箱验证码、退出登录、修改密码、个人资料、个人档案、打卡、徽章
 */
@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 用户账号与个人中心服务
     */
    private final UserService userService;

    /**
     * 构造方法注入用户服务
     *
     * @param userService 用户服务
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册（无需鉴权）
     *
     * @param registerDTO 注册请求
     * @return 注册成功后的登录结果（含JWT Token）
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@RequestBody RegisterDTO registerDTO) {
        return Result.success("注册成功", userService.register(registerDTO));
    }

    /**
     * 账号密码登录（无需鉴权）
     *
     * @param loginDTO 登录请求
     * @return 登录结果（含JWT Token）
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        return Result.success("登录成功", userService.login(loginDTO));
    }

    /**
     * 邮箱验证码登录（无需鉴权，未注册时自动注册）
     *
     * @param emailCodeLoginDTO 邮箱验证码登录请求
     * @return 登录结果（含JWT Token）
     */
    @PostMapping("/login/email")
    public Result<LoginVO> loginByEmailCode(@RequestBody EmailCodeLoginDTO emailCodeLoginDTO) {
        return Result.success("登录成功", userService.loginByEmailCode(emailCodeLoginDTO));
    }

    /**
     * 发送邮箱验证码（无需鉴权）
     *
     * @param sendEmailCodeDTO 发送验证码请求
     * @return 发送结果
     */
    @PostMapping("/email-code")
    public Result<Void> sendEmailCode(@RequestBody SendEmailCodeDTO sendEmailCodeDTO) {
        userService.sendEmailCode(sendEmailCodeDTO.getEmail());
        return Result.success("验证码已发送，请在5分钟内完成验证", null);
    }

    /**
     * 退出登录（需要JWT鉴权）
     *
     * @return 退出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout(SecurityUtil.getCurrentUserId());
        return Result.success("退出登录成功", null);
    }

    /**
     * 修改密码（需要JWT鉴权）
     *
     * @param changePwdDTO 修改密码请求
     * @return 修改结果
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody ChangePwdDTO changePwdDTO) {
        userService.changePassword(SecurityUtil.getCurrentUserId(), changePwdDTO);
        return Result.success("密码修改成功", null);
    }

    /**
     * 获取个人档案（需要JWT鉴权）
     *
     * @return 个人档案
     */
    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile() {
        return Result.success(userService.getUserProfile(SecurityUtil.getCurrentUserId()));
    }

    /**
     * 编辑个人资料（需要JWT鉴权）
     *
     * @param updateUserInfoDTO 编辑个人资料请求
     * @return 编辑结果
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UpdateUserInfoDTO updateUserInfoDTO) {
        userService.updateUserInfo(SecurityUtil.getCurrentUserId(), updateUserInfoDTO);
        return Result.success("个人资料更新成功", null);
    }

    /**
     * 分页查看当前用户打卡记录（需要JWT鉴权）
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 打卡记录分页结果
     */
    @GetMapping("/checkins")
    public Result<PageVO<CheckinRecordVO>> getCheckinRecords(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(userService.getCheckinRecords(SecurityUtil.getCurrentUserId(), pageNum, pageSize));
    }

    /**
     * 分页查看当前用户已解锁徽章列表（需要JWT鉴权）
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 徽章列表分页结果
     */
    @GetMapping("/badges")
    public Result<PageVO<BadgeVO>> getMyBadges(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(userService.getMyBadges(SecurityUtil.getCurrentUserId(), pageNum, pageSize));
    }
}