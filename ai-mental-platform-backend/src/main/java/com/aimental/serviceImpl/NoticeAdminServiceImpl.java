package com.aimental.serviceImpl;

import com.aimental.dto.NoticeSaveDTO;
import com.aimental.dto.NoticeStatusDTO;
import com.aimental.entity.Admin;
import com.aimental.entity.SystemNotice;
import com.aimental.exception.BusinessException;
import com.aimental.mapper.AdminMapper;
import com.aimental.mapper.SystemNoticeMapper;
import com.aimental.service.NoticeAdminService;
import com.aimental.vo.NoticeVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统公告管理服务实现类
 *
 * 新增：标题/正文必填，状态可选（0草稿缺省，1直接发布时记录发布时间与发布人）；
 * 编辑：仅修改标题与正文，不改变公告状态；
 * 状态管理：0草稿/1已发布/2下架（停用），发布时刷新发布时间与发布管理员
 */
@Service
public class NoticeAdminServiceImpl implements NoticeAdminService {

    private static final Logger log = LoggerFactory.getLogger(NoticeAdminServiceImpl.class);

    /**
     * 公告标题最大长度
     */
    private static final int TITLE_MAX_LENGTH = 100;

    /**
     * 公告正文最大长度
     */
    private static final int CONTENT_MAX_LENGTH = 5000;

    /**
     * 系统公告Mapper
     */
    private final SystemNoticeMapper systemNoticeMapper;

    /**
     * 管理员Mapper
     */
    private final AdminMapper adminMapper;

    /**
     * 构造方法注入
     *
     * @param systemNoticeMapper 系统公告Mapper
     * @param adminMapper        管理员Mapper
     */
    public NoticeAdminServiceImpl(SystemNoticeMapper systemNoticeMapper, AdminMapper adminMapper) {
        this.systemNoticeMapper = systemNoticeMapper;
        this.adminMapper = adminMapper;
    }

    @Override
    public List<NoticeVO> getNotices() {
        List<SystemNotice> notices = systemNoticeMapper.selectList(new LambdaQueryWrapper<SystemNotice>()
                .orderByDesc(SystemNotice::getNoticeId));
        if (notices.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        // 转VO并批量补全发布管理员账号
        List<NoticeVO> list = notices.stream()
                .map(NoticeVO::of)
                .collect(Collectors.toList());
        Set<Long> adminIds = notices.stream()
                .map(SystemNotice::getAdminId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!adminIds.isEmpty()) {
            List<Admin> admins = adminMapper.selectBatchIds(adminIds);
            Map<Long, String> adminMap = new HashMap<>();
            for (Admin a : admins) {
                adminMap.put(a.getAdminId(), a.getUsername());
            }
            for (NoticeVO vo : list) {
                vo.setAdminName(adminMap.get(vo.getAdminId()));
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoticeVO createNotice(NoticeSaveDTO dto, Long adminId) {
        // 1. 参数校验
        if (dto == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        checkNoticeContent(dto);
        int status = dto.getStatus() == null ? NOTICE_STATUS_DRAFT : dto.getStatus();
        if (status != NOTICE_STATUS_DRAFT && status != NOTICE_STATUS_PUBLISHED
                && status != NOTICE_STATUS_OFFLINE) {
            throw new IllegalArgumentException("公告状态只能为0草稿、1已发布或2下架");
        }

        // 2. 新增公告；直接发布时记录发布时间与发布管理员
        SystemNotice notice = new SystemNotice();
        notice.setNoticeTitle(dto.getNoticeTitle().trim());
        notice.setNoticeContent(dto.getNoticeContent());
        notice.setStatus(status);
        if (status == NOTICE_STATUS_PUBLISHED) {
            notice.setPublishTime(LocalDateTime.now());
            notice.setAdminId(adminId);
        }
        systemNoticeMapper.insert(notice);
        log.info("新增公告: noticeId={}, status={}, adminId={}", notice.getNoticeId(), status, adminId);

        // 3. 组装返回并补全发布管理员账号
        NoticeVO vo = NoticeVO.of(notice);
        vo.setAdminName(loadAdminName(adminId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoticeVO updateNotice(Long noticeId, NoticeSaveDTO dto) {
        // 1. 公告存在性校验
        if (noticeId == null) {
            throw new IllegalArgumentException("公告ID不能为空");
        }
        SystemNotice notice = systemNoticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException(404, "公告不存在");
        }

        // 2. 参数校验（编辑仅修改标题与正文，状态不随本接口变化）
        if (dto == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        checkNoticeContent(dto);
        notice.setNoticeTitle(dto.getNoticeTitle().trim());
        notice.setNoticeContent(dto.getNoticeContent());
        systemNoticeMapper.updateById(notice);
        log.info("编辑公告: noticeId={}", noticeId);

        // 3. 组装返回并补全发布管理员账号
        NoticeVO vo = NoticeVO.of(notice);
        vo.setAdminName(loadAdminName(notice.getAdminId()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNoticeStatus(Long noticeId, NoticeStatusDTO dto, Long adminId) {
        // 1. 公告存在性校验
        if (noticeId == null) {
            throw new IllegalArgumentException("公告ID不能为空");
        }
        SystemNotice notice = systemNoticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException(404, "公告不存在");
        }

        // 2. 状态参数校验
        if (dto == null || dto.getStatus() == null) {
            throw new IllegalArgumentException("公告状态不能为空");
        }
        int status = dto.getStatus();
        if (status != NOTICE_STATUS_DRAFT && status != NOTICE_STATUS_PUBLISHED
                && status != NOTICE_STATUS_OFFLINE) {
            throw new IllegalArgumentException("公告状态只能为0草稿、1已发布或2下架");
        }

        // 3. 更新状态；发布时刷新发布时间与发布管理员
        notice.setStatus(status);
        if (status == NOTICE_STATUS_PUBLISHED) {
            notice.setPublishTime(LocalDateTime.now());
            notice.setAdminId(adminId);
        }
        systemNoticeMapper.updateById(notice);
        log.info("修改公告状态: noticeId={}, status={}, adminId={}", noticeId, status, adminId);
    }

    /**
     * 校验公告标题与正文
     *
     * @param dto 公告信息
     */
    private void checkNoticeContent(NoticeSaveDTO dto) {
        if (dto.getNoticeTitle() == null || dto.getNoticeTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("公告标题不能为空");
        }
        if (dto.getNoticeTitle().length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("公告标题不能超过" + TITLE_MAX_LENGTH + "字");
        }
        if (dto.getNoticeContent() == null || dto.getNoticeContent().trim().isEmpty()) {
            throw new IllegalArgumentException("公告正文不能为空");
        }
        if (dto.getNoticeContent().length() > CONTENT_MAX_LENGTH) {
            throw new IllegalArgumentException("公告正文不能超过" + CONTENT_MAX_LENGTH + "字");
        }
    }

    /**
     * 查询发布管理员账号名
     *
     * @param adminId 管理员ID
     * @return 管理员账号（不存在返回null）
     */
    private String loadAdminName(Long adminId) {
        if (adminId == null) {
            return null;
        }
        Admin admin = adminMapper.selectById(adminId);
        return admin == null ? null : admin.getUsername();
    }
}
