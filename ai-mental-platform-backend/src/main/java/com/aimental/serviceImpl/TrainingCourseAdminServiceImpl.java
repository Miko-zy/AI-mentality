package com.aimental.serviceImpl;

import com.aimental.dto.CourseSaveDTO;
import com.aimental.dto.CourseStatusDTO;
import com.aimental.dto.SectionSaveDTO;
import com.aimental.entity.CourseSection;
import com.aimental.entity.StudyCourse;
import com.aimental.exception.BusinessException;
import com.aimental.mapper.CourseSectionMapper;
import com.aimental.mapper.StudyCourseMapper;
import com.aimental.service.TrainingCourseAdminService;
import com.aimental.vo.CourseVO;
import com.aimental.vo.SectionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 训练课程与小节维护服务实现类（管理员端）
 * 负责课程新增、编辑、上下架，以及课程小节的增删改查；
 * 每次增删小节后自动重算课程的 total_section，保证课程与小节数一致
 */
@Service
public class TrainingCourseAdminServiceImpl implements TrainingCourseAdminService {

    private static final Logger log = LoggerFactory.getLogger(TrainingCourseAdminServiceImpl.class);

    /**
     * 课程表状态：下架
     */
    private static final int COURSE_STATUS_OFF = 0;

    /**
     * 课程表状态：上架正常
     */
    private static final int COURSE_STATUS_ON = 1;

    /**
     * 小节默认建议学习时长（分钟）
     */
    private static final int DEFAULT_STUDY_MINUTES = 5;

    /**
     * 小节默认排序号
     */
    private static final int DEFAULT_SORT_NUM = 0;

    /**
     * 课程Mapper
     */
    private final StudyCourseMapper studyCourseMapper;

    /**
     * 课程小节Mapper
     */
    private final CourseSectionMapper courseSectionMapper;

    /**
     * 构造方法注入Mapper
     *
     * @param studyCourseMapper  课程Mapper
     * @param courseSectionMapper 小节Mapper
     */
    public TrainingCourseAdminServiceImpl(StudyCourseMapper studyCourseMapper,
                                          CourseSectionMapper courseSectionMapper) {
        this.studyCourseMapper = studyCourseMapper;
        this.courseSectionMapper = courseSectionMapper;
    }

    @Override
    public List<CourseVO> getAllCourses() {
        List<StudyCourse> courses = studyCourseMapper.selectList(new LambdaQueryWrapper<StudyCourse>()
                .orderByDesc(StudyCourse::getCreateTime));
        List<CourseVO> result = new ArrayList<>();
        for (StudyCourse course : courses) {
            result.add(CourseVO.of(course));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseVO createCourse(CourseSaveDTO dto) {
        validateCourse(dto);
        StudyCourse course = new StudyCourse();
        course.setCourseName(dto.getCourseName().trim());
        course.setCourseDesc(dto.getCourseDesc());
        course.setCoverUrl(dto.getCoverUrl());
        course.setTotalSection(dto.getTotalSection() == null ? 0 : dto.getTotalSection());
        course.setStatus(dto.getStatus() == null ? COURSE_STATUS_ON : dto.getStatus());
        studyCourseMapper.insert(course);
        log.info("管理员新增训练课程: courseId={}, courseName={}", course.getCourseId(), course.getCourseName());
        return CourseVO.of(course);
    }

    @Override
    public CourseVO updateCourse(Long courseId, CourseSaveDTO dto) {
        StudyCourse course = requireCourse(courseId);
        validateCourse(dto);
        // 编辑不改上下架状态（状态变更走独立接口）
        course.setCourseName(dto.getCourseName().trim());
        course.setCourseDesc(dto.getCourseDesc());
        course.setCoverUrl(dto.getCoverUrl());
        if (dto.getTotalSection() != null) {
            course.setTotalSection(dto.getTotalSection());
        }
        studyCourseMapper.updateById(course);
        log.info("管理员编辑训练课程: courseId={}, courseName={}", courseId, course.getCourseName());
        return CourseVO.of(course);
    }

    @Override
    public void updateCourseStatus(Long courseId, CourseStatusDTO dto) {
        StudyCourse course = requireCourse(courseId);
        if (dto == null || dto.getStatus() == null) {
            throw new IllegalArgumentException("课程状态不能为空");
        }
        int status = dto.getStatus();
        if (status != COURSE_STATUS_OFF && status != COURSE_STATUS_ON) {
            throw new IllegalArgumentException("课程状态只能为0（下架）或1（上架）");
        }
        course.setStatus(status);
        studyCourseMapper.updateById(course);
        log.info("管理员{}训练课程: courseId={}", status == COURSE_STATUS_ON ? "上架" : "下架", courseId);
    }

    @Override
    public List<SectionVO> getSections(Long courseId) {
        requireCourse(courseId);
        List<CourseSection> sections = loadSections(courseId);
        List<SectionVO> result = new ArrayList<>();
        for (CourseSection section : sections) {
            result.add(SectionVO.of(section));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SectionVO addSection(Long courseId, SectionSaveDTO dto) {
        requireCourse(courseId);
        validateSection(dto);
        CourseSection section = new CourseSection();
        section.setCourseId(courseId);
        applySectionFields(section, dto);
        courseSectionMapper.insert(section);
        // 同步课程小节总数
        recalcTotalSection(courseId);
        log.info("管理员新增课程小节: courseId={}, sectionId={}, title={}", courseId, section.getSectionId(), section.getSectionTitle());
        return SectionVO.of(section);
    }

    @Override
    public SectionVO updateSection(Long sectionId, SectionSaveDTO dto) {
        CourseSection section = requireSection(sectionId);
        validateSection(dto);
        // 编辑不改所属课程
        applySectionFields(section, dto);
        courseSectionMapper.updateById(section);
        log.info("管理员编辑课程小节: sectionId={}, title={}", sectionId, section.getSectionTitle());
        return SectionVO.of(section);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSection(Long sectionId) {
        CourseSection section = requireSection(sectionId);
        courseSectionMapper.deleteById(sectionId);
        // 同步课程小节总数
        recalcTotalSection(section.getCourseId());
        log.info("管理员删除课程小节: sectionId={}, courseId={}", sectionId, section.getCourseId());
    }

    /**
     * 校验课程信息
     *
     * @param dto 课程信息
     */
    private void validateCourse(CourseSaveDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        checkNotBlank(dto.getCourseName(), "课程名称不能为空");
        checkMaxLength(dto.getCourseName(), 100, "课程名称不能超过100字");
        if (dto.getCourseDesc() != null) {
            checkMaxLength(dto.getCourseDesc(), 10000, "课程简介不能超过10000字");
        }
        if (dto.getCoverUrl() != null) {
            checkMaxLength(dto.getCoverUrl(), 255, "封面图地址不能超过255字");
        }
        if (dto.getTotalSection() != null && dto.getTotalSection() < 0) {
            throw new IllegalArgumentException("小节总数不能为负数");
        }
        if (dto.getStatus() != null && dto.getStatus() != COURSE_STATUS_OFF && dto.getStatus() != COURSE_STATUS_ON) {
            throw new IllegalArgumentException("课程状态只能为0（下架）或1（上架）");
        }
    }

    /**
     * 校验小节信息
     *
     * @param dto 小节信息
     */
    private void validateSection(SectionSaveDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        checkNotBlank(dto.getSectionTitle(), "小节标题不能为空");
        checkMaxLength(dto.getSectionTitle(), 200, "小节标题不能超过200字");
        if (dto.getSectionContent() != null) {
            checkMaxLength(dto.getSectionContent(), 10000, "小节内容不能超过10000字");
        }
        if (dto.getStudyMinutes() != null && (dto.getStudyMinutes() < 1 || dto.getStudyMinutes() > 600)) {
            throw new IllegalArgumentException("建议学习时长应在1-600分钟之间");
        }
        if (dto.getSortNum() != null && dto.getSortNum() < 0) {
            throw new IllegalArgumentException("排序号不能为负数");
        }
    }

    /**
     * 校验字符串非空白（失败抛IllegalArgumentException，统一映射为400）
     *
     * @param value   待校验字符串
     * @param message 失败提示
     */
    private static void checkNotBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 校验字符串不超过最大长度（失败抛IllegalArgumentException，统一映射为400）
     *
     * @param value   待校验字符串
     * @param max     最大长度
     * @param message 失败提示
     */
    private static void checkMaxLength(String value, int max, String message) {
        if (value != null && value.length() > max) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 将DTO字段应用到小节实体
     *
     * @param section 小节实体
     * @param dto     小节信息
     */
    private void applySectionFields(CourseSection section, SectionSaveDTO dto) {
        section.setSectionTitle(dto.getSectionTitle().trim());
        section.setSectionContent(dto.getSectionContent());
        section.setStudyMinutes(dto.getStudyMinutes() == null ? DEFAULT_STUDY_MINUTES : dto.getStudyMinutes());
        section.setSortNum(dto.getSortNum() == null ? DEFAULT_SORT_NUM : dto.getSortNum());
    }

    /**
     * 重算课程小节总数并回写课程表
     *
     * @param courseId 课程ID
     */
    private void recalcTotalSection(Long courseId) {
        Long count = courseSectionMapper.selectCount(new LambdaQueryWrapper<CourseSection>()
                .eq(CourseSection::getCourseId, courseId));
        StudyCourse course = studyCourseMapper.selectById(courseId);
        if (course != null) {
            course.setTotalSection(count == null ? 0 : count.intValue());
            studyCourseMapper.updateById(course);
        }
    }

    /**
     * 查询课程全部小节（排序号升序，同序按ID升序）
     *
     * @param courseId 课程ID
     * @return 小节列表
     */
    private List<CourseSection> loadSections(Long courseId) {
        return courseSectionMapper.selectList(new LambdaQueryWrapper<CourseSection>()
                .eq(CourseSection::getCourseId, courseId)
                .orderByAsc(CourseSection::getSortNum)
                .orderByAsc(CourseSection::getSectionId));
    }

    /**
     * 查询课程，不存在则抛404
     *
     * @param courseId 课程ID
     * @return 课程实体
     */
    private StudyCourse requireCourse(Long courseId) {
        if (courseId == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        StudyCourse course = studyCourseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        return course;
    }

    /**
     * 查询小节，不存在则抛404
     *
     * @param sectionId 小节ID
     * @return 小节实体
     */
    private CourseSection requireSection(Long sectionId) {
        if (sectionId == null) {
            throw new IllegalArgumentException("小节ID不能为空");
        }
        CourseSection section = courseSectionMapper.selectById(sectionId);
        if (section == null) {
            throw new BusinessException(404, "课程小节不存在");
        }
        return section;
    }
}
