package com.aimental.dto;

/**
 * 修改密码请求DTO
 * 需校验旧密码，新密码bcrypt加密保存
 * 参数格式校验在Service层执行（见ParamValidator）
 */
public class ChangePwdDTO {

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}