package com.aimental.serviceImpl;

import com.aimental.dto.UpdateProgressDTO;
import com.aimental.entity.CourseSection;
import com.aimental.entity.StudyCourse;
import com.aimental.entity.StudyProgress;
import com.aimental.exception.BusinessException;
import com.aimental.mapper.CourseSectionMapper;
import com.aimental.mapper.StudyCourseMapper;
import com.aimental.mapper.StudyProgressMapper;
import com.aimental.service.BadgeService;
import com.aimental.service.TrainingCourseService;
import com.aimental.vo.BadgeVO;
import com.aimental.vo.CourseDetailVO;
import com.aimental.vo.CourseVO;
import com.aimental.vo.SectionVO;
import com.aimental.vo.TaskVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 训练课程与学习进度服务实现类（普通用户端）
 *
 * 业务设计：
 * 1. 训练任务 = study_progress 记录，表上 uk_user_course 唯一键保证一人一课仅一条，
 *    “选择课程生成训练任务”为幂等操作，重复选择直接返回已有任务
 * 2. 学习小节更新进度：当前停留小节按小节排序位置推进（只进不退）、
 *    累计学习时长累加、完成率=停留小节/课程小节总数*100（上限100）、最后学习时间刷新
 * 3. 课程完成状态由完成率推导（>=100 为已完成），不单独落库
 * 4. 每次进度更新后自动判定成就徽章（完成课程、累计学习时长类徽章）
 */
@Service
public class TrainingCourseServiceImpl implements TrainingCourseService {

    private static final Logger log = LoggerFactory.getLogger(TrainingCourseServiceImpl.class);

    /**
     * 课程完成状态：学习中
     */
    private static final int FINISH_STATUS_STUDYING = 0;

    /**
     * 课程完成状态：已完成
     */
    private static final int FINISH_STATUS_DONE = 1;

    /**
     * 单次学习时长上限（秒），防止脏数据
     */
    private static final int MAX_SINGLE_DURATION = 7200;

    /**
     * 完成率100的比较基准
     */
    private static final BigDecimal RATE_HUNDRED = new BigDecimal("100");

    /**
     * 课程Mapper
     */
    private final StudyCourseMapper studyCourseMapper;

    /**
     * 课程小节Mapper
     */
    private final CourseSectionMapper courseSectionMapper;

    /**
     * 学习进度Mapper
     */
    private final StudyProgressMapper studyProgressMapper;

    /**
     * 成就徽章服务
     */
    private final BadgeService badgeService;

    /**
     * 构造方法注入
     *
     * @param studyCourseMapper   课程Mapper
     * @param courseSectionMapper 小节Mapper
     * @param studyProgressMapper 学习进度Mapper
     * @param badgeService        成就徽章服务
     */
    public TrainingCourseServiceImpl(StudyCourseMapper studyCourseMapper,
                                     CourseSectionMapper courseSectionMapper,
                                     StudyProgressMapper studyProgressMapper,
                                     BadgeService badgeService) {
        this.studyCourseMapper = studyCourseMapper;
        this.courseSectionMapper = courseSectionMapper;
        this.studyProgressMapper = studyProgressMapper;
        this.badgeService = badgeService;
    }

    @Override
    public List<CourseVO> getOnlineCourses() {
        List<StudyCourse> courses = studyCourseMapper.selectList(new LambdaQueryWrapper<StudyCourse>()
                .eq(StudyCourse::getStatus, 1)
                .orderByDesc(StudyCourse::getCreateTime));
        List<CourseVO> result = new ArrayList<>();
        for (StudyCourse course : courses) {
            result.add(CourseVO.of(course));
        }
        return result;
    }

    @Override
    public CourseDetailVO getCourseDetail(Long courseId) {
        StudyCourse course = requireCourse(courseId);
        if (course.getStatus() == null || course.getStatus() != 1) {
            throw new BusinessException(400, "该课程已下架，暂不可查看");
        }
        List<CourseSection> sections = loadSections(courseId);
        List<SectionVO> sectionVOs = new ArrayList<>();
        for (CourseSection section : sections) {
            sectionVOs.add(SectionVO.of(section));
        }
        return CourseDetailVO.of(course, sectionVOs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskVO createTask(Long userId, Long courseId) {
        StudyCourse course = requireCourse(courseId);
        if (course.getStatus() == null || course.getStatus() != 1) {
            throw new BusinessException(400, "该课程已下架，无法生成训练任务");
        }
        // 幂等：一人一课仅一条任务（uk_user_course唯一键兜底），已存在直接返回，不重置进度
        StudyProgress exist = studyProgressMapper.selectOne(new LambdaQueryWrapper<StudyProgress>()
                .eq(StudyProgress::getUserId, userId)
                .eq(StudyProgress::getCourseId, courseId)
                .last("LIMIT 1"));
        if (exist != null) {
            return toTaskVO(exist, course, new ArrayList<BadgeVO>());
        }
        StudyProgress progress = new StudyProgress();
        progress.setUserId(userId);
        progress.setCourseId(courseId);
        progress.setFinishedSection(0);
        progress.setFinishRate(BigDecimal.ZERO);
        progress.setStudyDuration(0);
        studyProgressMapper.insert(progress);
        log.info("生成训练任务: userId={}, courseId={}, progressId={}", userId, courseId, progress.getProgressId());
        return toTaskVO(progress, course, new ArrayList<BadgeVO>());
    }

    @Override
    public List<TaskVO> getMyTasks(Long userId) {
        // 未学习过的任务 last_study_time 为NULL，在MySQL降序排序中排在最后
        List<StudyProgress> progresses = studyProgressMapper.selectList(new LambdaQueryWrapper<StudyProgress>()
                .eq(StudyProgress::getUserId, userId)
                .orderByDesc(StudyProgress::getLastStudyTime));
        List<TaskVO> result = new ArrayList<>();
        if (progresses.isEmpty()) {
            return result;
        }
        Set<Long> courseIds = new HashSet<>();
        for (StudyProgress progress : progresses) {
            courseIds.add(progress.getCourseId());
        }
        Map<Long, StudyCourse> courseMap = studyCourseMapper.selectBatchIds(courseIds)
                .stream()
                .collect(Collectors.toMap(StudyCourse::getCourseId, Function.identity()));
        for (StudyProgress progress : progresses) {
            StudyCourse course = courseMap.get(progress.getCourseId());
            if (course == null) {
                // 课程数据异常（物理不存在）时跳过，避免空指针
                continue;
            }
            result.add(toTaskVO(progress, course, new ArrayList<BadgeVO>()));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskVO updateProgress(Long userId, Long progressId, UpdateProgressDTO dto) {
        // 1. 任务存在性与归属校验（他人任务403）
        if (progressId == null) {
            throw new IllegalArgumentException("进度ID不能为空");
        }
        StudyProgress progress = studyProgressMapper.selectById(progressId);
        if (progress == null) {
            throw new BusinessException(404, "训练任务不存在");
        }
        if (!userId.equals(progress.getUserId())) {
            throw new BusinessException(403, "无权操作他人的训练任务");
        }

        // 2. 入参校验（Service层手动校验，无validation依赖）
        if (dto == null || dto.getSectionId() == null) {
            throw new IllegalArgumentException("学习小节ID不能为空");
        }
        int duration = dto.getStudyDuration() == null ? 0 : dto.getStudyDuration();
        if (duration < 0 || duration > MAX_SINGLE_DURATION) {
            throw new IllegalArgumentException("单次学习时长应在0-" + MAX_SINGLE_DURATION + "秒之间");
        }

        // 3. 小节与课程校验
        CourseSection section = courseSectionMapper.selectById(dto.getSectionId());
        if (section == null) {
            throw new BusinessException(404, "课程小节不存在");
        }
        if (!section.getCourseId().equals(progress.getCourseId())) {
            throw new IllegalArgumentException("该小节不属于当前课程");
        }
        StudyCourse course = studyCourseMapper.selectById(progress.getCourseId());
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        // 4. 计算当前停留小节：小节按排序位置（1-based），只进不退
        List<CourseSection> sections = loadSections(course.getCourseId());
        int position = 0;
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).getSectionId().equals(section.getSectionId())) {
                position = i + 1;
                break;
            }
        }
        int oldFinished = progress.getFinishedSection() == null ? 0 : progress.getFinishedSection();
        int finished = Math.max(oldFinished, position);

        // 5. 重算完成率：分母为课程小节总数，结果保留2位小数，上限100
        int total = course.getTotalSection() == null ? 0 : course.getTotalSection();
        BigDecimal rate;
        if (total <= 0) {
            rate = BigDecimal.ZERO;
        } else {
            rate = new BigDecimal(finished * 100)
                    .divide(new BigDecimal(total), 2, RoundingMode.HALF_UP);
            if (rate.compareTo(RATE_HUNDRED) > 0) {
                rate = new BigDecimal("100.00");
            }
        }

        // 6. 累计学习时长累加、刷新最后学习时间
        int oldDuration = progress.getStudyDuration() == null ? 0 : progress.getStudyDuration();
        progress.setFinishedSection(finished);
        progress.setFinishRate(rate);
        progress.setStudyDuration(oldDuration + duration);
        progress.setLastStudyTime(LocalDateTime.now());
        studyProgressMapper.updateById(progress);
        log.info("学习进度更新: userId={}, progressId={}, 停留小节={}/{}, 完成率={}%, 本次时长={}秒",
                userId, progressId, finished, total, rate, duration);

        // 7. 自动判定成就徽章解锁（完成课程、累计学习时长类）
        List<BadgeVO> newBadges = badgeService.checkAndUnlock(userId);
        return toTaskVO(progress, course, newBadges);
    }

    /**
     * 学习进度实体转换为训练任务VO
     *
     * @param progress  学习进度实体
     * @param course    所属课程实体
     * @param newBadges 本次操作新解锁的徽章
     * @return 训练任务VO
     */
    private TaskVO toTaskVO(StudyProgress progress, StudyCourse course, List<BadgeVO> newBadges) {
        TaskVO vo = new TaskVO();
        vo.setProgressId(progress.getProgressId());
        vo.setCourseId(course.getCourseId());
        vo.setCourseName(course.getCourseName());
        vo.setCoverUrl(course.getCoverUrl());
        vo.setTotalSection(course.getTotalSection() == null ? 0 : course.getTotalSection());
        vo.setFinishedSection(progress.getFinishedSection() == null ? 0 : progress.getFinishedSection());
        BigDecimal rate = progress.getFinishRate() == null ? BigDecimal.ZERO : progress.getFinishRate();
        vo.setFinishRate(rate);
        boolean done = rate.compareTo(RATE_HUNDRED) >= 0;
        vo.setFinishStatus(done ? FINISH_STATUS_DONE : FINISH_STATUS_STUDYING);
        vo.setFinishStatusText(done ? "已完成" : "学习中");
        vo.setStudyDuration(progress.getStudyDuration() == null ? 0 : progress.getStudyDuration());
        vo.setLastStudyTime(progress.getLastStudyTime());
        vo.setNewBadges(newBadges);
        return vo;
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
}
