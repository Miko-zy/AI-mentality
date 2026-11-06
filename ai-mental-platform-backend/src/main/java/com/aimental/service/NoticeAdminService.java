package com.aimental.service;

import com.aimental.dto.NoticeSaveDTO;
import com.aimental.dto.NoticeStatusDTO;
import com.aimental.vo.NoticeVO;

import java.util.List;

/**
 * 系统公告管理服务接口（管理员运营模块）
 * 提供公告新增、编辑、发布/停用能力
 */
public interface NoticeAdminService {

    /**
     * 公告状态：草稿
     */
    Integer NOTICE_STATUS_DRAFT = 0;

    /**
     * 公告状态：已发布
     */
    Integer NOTICE_STATUS_PUBLISHED = 1;

    /**
     * 公告状态：下架（停用）
     */
    Integer NOTICE_STATUS_OFFLINE = 2;

    /**
     * 查询全部公告列表
     *
     * @return 公告列表
     */
    List<NoticeVO> getNotices();

    /**
     * 新增公告
     *
     * @param dto     公告信息
     * @param adminId 当前登录管理员ID
     * @return 新增后的公告
     */
    NoticeVO createNotice(NoticeSaveDTO dto, Long adminId);

    /**
     * 编辑公告（仅修改标题与正文，不修改状态）
     *
     * @param noticeId 公告ID
     * @param dto      公告信息
     * @return 编辑后的公告
     */
    NoticeVO updateNotice(Long noticeId, NoticeSaveDTO dto);

    /**
     * 修改公告状态（发布时刷新发布时间与发布人）
     *
     * @param noticeId 公告ID
     * @param dto      状态信息
     * @param adminId  当前登录管理员ID
     */
    void updateNoticeStatus(Long noticeId, NoticeStatusDTO dto, Long adminId);
}
