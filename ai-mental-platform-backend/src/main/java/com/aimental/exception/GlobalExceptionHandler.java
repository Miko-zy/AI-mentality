package com.aimental.exception;

import com.aimental.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义业务异常
     *
     * @param e       业务异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常 - 请求地址: {}, 异常码: {}, 异常信息: {}",
                request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     *
     * @param e       参数校验异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMsg = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验异常 - 请求地址: {}, 异常信息: {}", request.getRequestURI(), errorMsg);
        return Result.fail(400, errorMsg);
    }

    /**
     * 处理绑定异常
     *
     * @param e       绑定异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e, HttpServletRequest request) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMsg = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数绑定异常 - 请求地址: {}, 异常信息: {}", request.getRequestURI(), errorMsg);
        return Result.fail(400, errorMsg);
    }

    /**
     * 处理认证异常
     *
     * @param e       认证异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.error("认证异常 - 请求地址: {}, 异常信息: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(401, "未认证或认证已过期，请重新登录");
    }

    /**
     * 处理权限异常
     *
     * @param e       权限异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.error("权限异常 - 请求地址: {}, 异常信息: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(403, "没有访问权限");
    }

    /**
     * 处理非法参数异常
     *
     * @param e       非法参数异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.error("非法参数异常 - 请求地址: {}, 异常信息: {}", request.getRequestURI(), e.getMessage());
        return Result.fail(400, e.getMessage());
    }

    /**
     * 处理系统异常
     *
     * @param e       系统异常
     * @param request 请求对象
     * @return 统一返回结果
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 - 请求地址: {}", request.getRequestURI(), e);
        return Result.fail(500, "系统内部错误，请联系管理员");
    }
}
