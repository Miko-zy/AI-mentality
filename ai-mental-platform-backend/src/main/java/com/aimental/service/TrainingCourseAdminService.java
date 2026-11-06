package com.aimental.service;

import com.aimental.dto.CourseSaveDTO;
import com.aimental.dto.CourseStatusDTO;
import com.aimental.dto.SectionSaveDTO;
import com.aimental.vo.CourseVO;
import com.aimental.vo.SectionVO;

import java.util.List;

/**
 * 训练课程与小节维护服务接口（管理员端）
 * 负责课程的新增、编辑、上下架，以及课程小节信息维护
 */
public interface TrainingCourseAdminService {

    /**
     * 查询全部训练课程（含已下架）
     *
     * @return 课程列表（按创建时间倒序）
     */
    List<CourseVO> getAllCourses();

    /**
     * 新增训练课程
     *
     * @param dto 课程信息
     * @return 新增后的课程
     */
    CourseVO createCourse(CourseSaveDTO dto);

    /**
     * 编辑训练课程（不改上下架状态，状态走独立接口）
     *
     * @param courseId 课程ID
     * @param dto      课程信息
     * @return 编辑后的课程
     */
    CourseVO updateCourse(Long courseId, CourseSaveDTO dto);

    /**
     * 课程上架/下架
     *
     * @param courseId 课程ID
     * @param dto      目标状态（0下架，1正常）
     */
    void updateCourseStatus(Long courseId, CourseStatusDTO dto);

    /**
     * 查询指定课程的全部小节（按排序号升序）
     *
     * @param courseId 课程ID
     * @return 小节列表
     */
    List<SectionVO> getSections(Long courseId);

    /**
     * 新增课程小节（自动同步课程小节总数）
     *
     * @param courseId 课程ID
     * @param dto      小节信息
     * @return 新增后的小节
     */
    SectionVO addSection(Long courseId, SectionSaveDTO dto);

    /**
     * 编辑课程小节（不改所属课程）
     *
     * @param sectionId 小节ID
     * @param dto       小节信息
     * @return 编辑后的小节
     */
    SectionVO updateSection(Long sectionId, SectionSaveDTO dto);

    /**
     * 逻辑删除课程小节（自动同步课程小节总数）
     *
     * @param sectionId 小节ID
     */
    void deleteSection(Long sectionId);
}
