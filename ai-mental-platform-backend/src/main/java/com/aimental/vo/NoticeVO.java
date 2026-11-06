package com.aimental.vo;

import com.aimental.entity.SystemNotice;

import java.time.LocalDateTime;

/**
 * 系统公告视图对象
 */
public class NoticeVO {

    /**
     * 公告ID
     */
    private Long noticeId;

    /**
     * 公告标题
     */
    private String noticeTitle;

    /**
     * 公告正文
     */
    private String noticeContent;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 发布管理员ID
     */
    private Long adminId;

    /**
     * 发布管理员账号
     */
    private String adminName;

    /**
     * 状态：0草稿，1已发布，2下架
     */
    private Integer status;

    /**
     * 状态中文描述
     */
    private String statusText;

    /**
     * 由公告实体转换为视图对象（管理员账号由业务层补充）
     *
     * @param notice 公告实体
     * @return 公告视图对象
     */
    public static NoticeVO of(SystemNotice notice) {
        NoticeVO vo = new NoticeVO();
        vo.setNoticeId(notice.getNoticeId());
        vo.setNoticeTitle(notice.getNoticeTitle());
        vo.setNoticeContent(notice.getNoticeContent());
        vo.setPublishTime(notice.getPublishTime());
        vo.setAdminId(notice.getAdminId());
        vo.setStatus(notice.getStatus());
        vo.setStatusText(statusText(notice.getStatus()));
        return vo;
    }

    /**
     * 公告状态转中文
     *
     * @param status 状态
     * @return 中文描述
     */
    public static String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        if (status == 0) {
            return "草稿";
        }
        if (status == 1) {
            return "已发布";
        }
        if (status == 2) {
            return "下架";
        }
        return "未知";
    }

    public Long getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(Long noticeId) {
        this.noticeId = noticeId;
    }

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

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
}
