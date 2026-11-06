package com.aimental.service;

import com.aimental.dto.UpdateProgressDTO;
import com.aimental.vo.CourseDetailVO;
import com.aimental.vo.CourseVO;
import com.aimental.vo.TaskVO;

import java.util.List;

/**
 * 训练课程与学习进度服务接口（普通用户端）
 * 全部方法基于当前登录用户（userId由Token解析），只能操作本人训练任务
 */
public interface TrainingCourseService {

    /**
     * 获取全部上架（status=1）训练课程列表
     *
     * @return 课程列表（按创建时间倒序）
     */
    List<CourseVO> getOnlineCourses();

    /**
     * 获取课程详情及小节列表（仅上架课程可查看）
     *
     * @param courseId 课程ID
     * @return 课程详情（含小节）
     */
    CourseDetailVO getCourseDetail(Long courseId);

    /**
     * 用户选择课程生成训练任务（幂等：已有任务则直接返回，不重复创建）
     *
     * @param userId   当前登录用户ID
     * @param courseId 课程ID（须为上架课程）
     * @return 训练任务（学习进度）
     */
    TaskVO createTask(Long userId, Long courseId);

    /**
     * 查询本人全部训练任务（学习进度列表）
     *
     * @param userId 当前登录用户ID
     * @return 训练任务列表（按最后学习时间倒序，未学过的排前面）
     */
    List<TaskVO> getMyTasks(Long userId);

    /**
     * 学习小节，更新学习进度：
     * 当前停留小节（只进不退）、累计学习时长累加、完成率与课程完成状态重算；
     * 课程学完时自动判定成就徽章解锁
     *
     * @param userId     当前登录用户ID
     * @param progressId 进度ID（任务ID，须为本人任务）
     * @param dto        本次学习的小节ID与学习时长
     * @return 更新后的训练任务（含本次新解锁徽章）
     */
    TaskVO updateProgress(Long userId, Long progressId, UpdateProgressDTO dto);
}
