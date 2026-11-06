package com.aimental.dto;

/**
 * 系统公告新增/编辑请求DTO
 */
public class NoticeSaveDTO {

    /**
     * 公告标题（必填，不超过100字）
     */
    private String noticeTitle;

    /**
     * 公告正文（必填，不超过5000字）
     */
    private String noticeContent;

    /**
     * 公告状态：0草稿，1已发布，2下架（仅新增时生效，可选缺省0；编辑时不修改状态）
     */
    private Integer status;

    public String getNoticeTitle() {
        return noticeTitle;
    }

    public void setNoticeTitle(String noticeTitle) {
        this.noticeTitle = noticeTitle;
    }

    public String getNoticeContent() {
        return noticeContent;
    }

    public void setNoticeContent(String noticeContent) {
        this.noticeContent = noticeContent;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
