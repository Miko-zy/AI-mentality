package com.aimental.controller;

import com.aimental.common.Result;
import com.aimental.dto.CourseSaveDTO;
import com.aimental.dto.CourseStatusDTO;
import com.aimental.dto.SectionSaveDTO;
import com.aimental.service.TrainingCourseAdminService;
import com.aimental.vo.CourseVO;
import com.aimental.vo.SectionVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 训练课程管理接口（管理员端）
 * 全部接口要求管理员角色（JWT中role=admin，映射为ROLE_admin）
 *
 * 接口清单（context-path为/api）：
 * GET    /api/admin/course                          查询全部课程（含下架）
 * POST   /api/admin/course                          新增课程
 * PUT    /api/admin/course/{courseId}               编辑课程
 * PUT    /api/admin/course/{courseId}/status        课程上架/下架
 * GET    /api/admin/course/{courseId}/sections      查询课程全部小节
 * POST   /api/admin/course/{courseId}/section       新增课程小节
 * PUT    /api/admin/course/section/{sectionId}      编辑课程小节
 * DELETE /api/admin/course/section/{sectionId}      删除课程小节（逻辑删除）
 */
@RestController
@RequestMapping("/admin/course")
public class AdminCourseController {

    /**
     * 训练课程管理服务
     */
    private final TrainingCourseAdminService trainingCourseAdminService;

    /**
     * 构造方法注入服务
     *
     * @param trainingCourseAdminService 训练课程管理服务
     */
    public AdminCourseController(TrainingCourseAdminService trainingCourseAdminService) {
        this.trainingCourseAdminService = trainingCourseAdminService;
    }

    /**
     * 查询全部训练课程（含下架，管理端列表）
     *
     * @return 课程列表
     */
    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public Result<List<CourseVO>> getAllCourses() {
        return Result.success(trainingCourseAdminService.getAllCourses());
    }

    /**
     * 新增训练课程
     *
     * @param dto 课程信息
     * @return 新建的课程
     */
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<CourseVO> createCourse(@RequestBody CourseSaveDTO dto) {
        return Result.success(trainingCourseAdminService.createCourse(dto));
    }

    /**
     * 编辑训练课程（不改上下架状态）
     *
     * @param courseId 课程ID
     * @param dto      课程信息
     * @return 编辑后的课程
     */
    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('admin')")
    public Result<CourseVO> updateCourse(@PathVariable Long courseId, @RequestBody CourseSaveDTO dto) {
        return Result.success(trainingCourseAdminService.updateCourse(courseId, dto));
    }

    /**
     * 课程上架/下架
     *
     * @param courseId 课程ID
     * @param dto      目标状态（0下架，1正常）
     * @return 操作结果
     */
    @PutMapping("/{courseId}/status")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> updateCourseStatus(@PathVariable Long courseId, @RequestBody CourseStatusDTO dto) {
        trainingCourseAdminService.updateCourseStatus(courseId, dto);
        return Result.success();
    }

    /**
     * 查询指定课程的全部小节（按排序号升序）
     *
     * @param courseId 课程ID
     * @return 小节列表
     */
    @GetMapping("/{courseId}/sections")
    @PreAuthorize("hasRole('admin')")
    public Result<List<SectionVO>> getSections(@PathVariable Long courseId) {
        return Result.success(trainingCourseAdminService.getSections(courseId));
    }

    /**
     * 新增课程小节（自动同步课程小节总数）
     *
     * @param courseId 课程ID
     * @param dto      小节信息
     * @return 新建的小节
     */
    @PostMapping("/{courseId}/section")
    @PreAuthorize("hasRole('admin')")
    public Result<SectionVO> addSection(@PathVariable Long courseId, @RequestBody SectionSaveDTO dto) {
        return Result.success(trainingCourseAdminService.addSection(courseId, dto));
    }

    /**
     * 编辑课程小节（不改所属课程）
     *
     * @param sectionId 小节ID
     * @param dto       小节信息
     * @return 编辑后的小节
     */
    @PutMapping("/section/{sectionId}")
    @PreAuthorize("hasRole('admin')")
    public Result<SectionVO> updateSection(@PathVariable Long sectionId, @RequestBody SectionSaveDTO dto) {
        return Result.success(trainingCourseAdminService.updateSection(sectionId, dto));
    }

    /**
     * 逻辑删除课程小节（自动同步课程小节总数）
     *
     * @param sectionId 小节ID
     * @return 操作结果
     */
    @DeleteMapping("/section/{sectionId}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> deleteSection(@PathVariable Long sectionId) {
        trainingCourseAdminService.deleteSection(sectionId);
        return Result.success();
    }
}
