package com.aimental.controller;

import com.aimental.common.Result;
import com.aimental.dto.UpdateProgressDTO;
import com.aimental.security.SecurityUtil;
import com.aimental.service.TrainingCourseService;
import com.aimental.vo.CourseDetailVO;
import com.aimental.vo.CourseVO;
import com.aimental.vo.TaskVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 训练课程与学习进度接口（普通用户端）
 * 全部接口经JWT鉴权过滤器，userId从Token解析，用户只能操作本人训练任务
 *
 * 接口清单（context-path为/api）：
 * GET  /api/course                            获取上架训练课程列表
 * GET  /api/course/{courseId}                 获取课程详情及小节
 * POST /api/course/{courseId}/task            选择课程生成训练任务（幂等）
 * GET  /api/course/tasks                      查询本人全部训练任务
 * PUT  /api/course/progress/{progressId}      学习小节，更新学习进度
 */
@RestController
@RequestMapping("/course")
public class CourseController {

    /**
     * 训练课程服务
     */
    private final TrainingCourseService trainingCourseService;

    /**
     * 构造方法注入服务
     *
     * @param trainingCourseService 训练课程服务
     */
    public CourseController(TrainingCourseService trainingCourseService) {
        this.trainingCourseService = trainingCourseService;
    }

    /**
     * 获取全部上架训练课程列表
     *
     * @return 课程列表
     */
    @GetMapping
    public Result<List<CourseVO>> getOnlineCourses() {
        return Result.success(trainingCourseService.getOnlineCourses());
    }

    /**
     * 查询本人全部训练任务（学习进度列表）
     *
     * @return 训练任务列表
     */
    @GetMapping("/tasks")
    public Result<List<TaskVO>> getMyTasks() {
        return Result.success(trainingCourseService.getMyTasks(SecurityUtil.getCurrentUserId()));
    }

    /**
     * 获取课程详情及小节列表（仅上架课程可查看）
     *
     * @param courseId 课程ID
     * @return 课程详情（含小节）
     */
    @GetMapping("/{courseId}")
    public Result<CourseDetailVO> getCourseDetail(@PathVariable Long courseId) {
        return Result.success(trainingCourseService.getCourseDetail(courseId));
    }

    /**
     * 选择课程生成训练任务（幂等：已有任务直接返回）
     *
     * @param courseId 课程ID
     * @return 训练任务
     */
    @PostMapping("/{courseId}/task")
    public Result<TaskVO> createTask(@PathVariable Long courseId) {
        return Result.success(trainingCourseService.createTask(SecurityUtil.getCurrentUserId(), courseId));
    }

    /**
     * 学习小节，更新学习进度：
     * 当前停留小节、累计学习时长、完成率与课程完成状态
     *
     * @param progressId 进度ID（任务ID）
     * @param dto        本次学习的小节ID与学习时长
     * @return 更新后的训练任务（含本次新解锁徽章）
     */
    @PutMapping("/progress/{progressId}")
    public Result<TaskVO> updateProgress(@PathVariable Long progressId, @RequestBody UpdateProgressDTO dto) {
        return Result.success(trainingCourseService.updateProgress(
                SecurityUtil.getCurrentUserId(), progressId, dto));
    }
}
