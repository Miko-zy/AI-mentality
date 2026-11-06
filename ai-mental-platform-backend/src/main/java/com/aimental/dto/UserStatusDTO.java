package com.aimental.dto;

/**
 * 用户账号状态修改请求DTO
 */
public class UserStatusDTO {

    /**
     * 账号状态：0禁用，1正常（必填）
     */
    private Integer accountStatus;

    public Integer getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(Integer accountStatus) {
        this.accountStatus = accountStatus;
    }
}
