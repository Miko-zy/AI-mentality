package com.aimental.util;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.aimental.exception.BusinessException;

/**
 * 参数校验工具类
 * 在Service层手动校验参数，避免依赖额外的JSR-303校验包
 */
public final class ParamValidator {

    /**
     * 手机号正则
     */
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    /**
     * 密码正则（6-20位，需包含字母和数字）
     */
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{6,20}$";

    /**
     * 6位数字验证码正则
     */
    private static final String SIX_DIGIT_REGEX = "^\\d{6}$";

    /**
     * 私有构造方法，工具类不允许实例化
     */
    private ParamValidator() {
    }

    /**
     * 校验字符串不能为空白
     *
     * @param value   待校验字符串
     * @param message 校验失败时提示消息
     */
    public static void notBlank(String value, String message) {
        if (StrUtil.isBlank(value)) {
            throw new BusinessException(message);
        }
    }

    /**
     * 校验字符串长度在指定范围内（包含边界）
     *
     * @param value   待校验字符串
     * @param min     最小长度
     * @param max     最大长度
     * @param message 校验失败时提示消息
     */
    public static void lengthBetween(String value, int min, int max, String message) {
        notBlank(value, message);
        if (value.length() < min || value.length() > max) {
            throw new BusinessException(message);
        }
    }

    /**
     * 校验字符串最大长度（可传null，null不校验）
     *
     * @param value   待校验字符串
     * @param max     最大长度
     * @param message 校验失败时提示消息
     */
    public static void maxLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new BusinessException(message);
        }
    }

    /**
     * 校验邮箱格式
     *
     * @param value   待校验邮箱
     * @param message 校验失败时提示消息
     */
    public static void isEmail(String value, String message) {
        notBlank(value, message);
        if (!Validator.isEmail(value)) {
            throw new BusinessException(message);
        }
    }

    /**
     * 校验手机号格式（中国大陆手机号）
     *
     * @param value   待校验手机号
     * @param message 校验失败时提示消息
     */
    public static void isPhone(String value, String message) {
        notBlank(value, message);
        if (!Validator.isMatchRegex(PHONE_REGEX, value)) {
            throw new BusinessException(message);
        }
    }

    /**
     * 校验密码格式（6-20位，需包含字母和数字）
     *
     * @param value   待校验密码
     * @param message 校验失败时提示消息
     */
    public static void isPassword(String value, String message) {
        notBlank(value, message);
        if (!Validator.isMatchRegex(PASSWORD_REGEX, value)) {
            throw new BusinessException(message);
        }
    }

    /**
     * 校验邮箱验证码格式（6位数字）
     *
     * @param value   待校验验证码
     * @param message 校验失败时提示消息
     */
    public static void isSixDigitCode(String value, String message) {
        notBlank(value, message);
        if (!Validator.isMatchRegex(SIX_DIGIT_REGEX, value)) {
            throw new BusinessException(message);
        }
    }
}