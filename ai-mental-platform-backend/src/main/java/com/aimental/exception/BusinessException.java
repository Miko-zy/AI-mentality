package com.aimental.exception;

/**
 * 自定义业务异常类
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 异常码
     */
    private final Integer code;

    /**
     * 异常消息
     */
    private final String message;

    /**
     * 默认异常码
     */
    public static final int DEFAULT_ERROR_CODE = 500;

    /**
     * 构造方法（仅消息）
     *
     * @param message 异常消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = DEFAULT_ERROR_CODE;
        this.message = message;
    }

    /**
     * 构造方法（异常码和消息）
     *
     * @param code    异常码
     * @param message 异常消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造方法（消息和原因）
     *
     * @param message 异常消息
     * @param cause   原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = DEFAULT_ERROR_CODE;
        this.message = message;
    }

    /**
     * 构造方法（异常码、消息和原因）
     *
     * @param code    异常码
     * @param message 异常消息
     * @param cause   原因
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
