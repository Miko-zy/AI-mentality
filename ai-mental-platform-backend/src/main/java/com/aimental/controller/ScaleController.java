package com.aimental.controller;

import com.aimental.common.Result;
import com.aimental.dto.AnswerSubmitDTO;
import com.aimental.dto.CreateScaleRecordDTO;
import com.aimental.security.SecurityUtil;
import com.aimental.service.ScaleAssessmentService;
import com.aimental.vo.AssessmentProgressVO;
import com.aimental.vo.AssessmentRecordVO;
import com.aimental.vo.AssessmentReportVO;
import com.aimental.vo.ScaleDetailVO;
import com.aimental.vo.ScaleVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 心理韧性评估接口（普通用户端）
 * 全部接口经JWT鉴权过滤器，userId从Token解析，用户只能访问本人评估记录
 *
 * 接口清单（context-path为/api）：
 * GET    /api/scale                          获取全部可用量表列表
 * GET    /api/scale/{scaleId}                获取量表详情（题目+选项）
 * POST   /api/scale/record                   新建评估记录，开始测评
 * GET    /api/scale/records                  查询本人全部历史评估记录
 * GET    /api/scale/record/{recordId}        查询作答进度（暂停后恢复）
 * PUT    /api/scale/record/{recordId}/answers 保存作答（中途暂停）
 * POST   /api/scale/record/{recordId}/submit 提交答卷并生成评估报告
 * GET    /api/scale/record/{recordId}/report 查看历史评估报告
 */
@RestController
@RequestMapping("/scale")
public class ScaleController {

    /**
     * 评估服务
     */
    private final ScaleAssessmentService scaleAssessmentService;

    /**
     * 构造方法注入服务
     *
     * @param scaleAssessmentService 评估服务
     */
    public ScaleController(ScaleAssessmentService scaleAssessmentService) {
        this.scaleAssessmentService = scaleAssessmentService;
    }

    /**
     * 获取全部可用（启用中）评估量表列表
     *
     * @return 量表列表
     */
    @GetMapping
    public Result<List<ScaleVO>> getEnabledScales() {
        return Result.success(scaleAssessmentService.getEnabledScales());
    }

    /**
     * 获取量表详情（题目+选项）
     *
     * @param scaleId 量表ID
     * @return 量表详情
     */
    @GetMapping("/{scaleId}")
    public Result<ScaleDetailVO> getScaleDetail(@PathVariable Long scaleId) {
        return Result.success(scaleAssessmentService.getScaleDetail(scaleId));
    }

    /**
     * 新建评估记录，开始测评
     *
     * @param dto 创建请求（量表ID）
     * @return 评估进度（记录ID）
     */
    @PostMapping("/record")
    public Result<AssessmentProgressVO> createRecord(@RequestBody CreateScaleRecordDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(scaleAssessmentService.createRecord(userId, dto));
    }

    /**
     * 查询本人全部历史评估记录
     *
     * @return 评估记录列表（按记录ID倒序）
     */
    @GetMapping("/records")
    public Result<List<AssessmentRecordVO>> getMyRecords() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(scaleAssessmentService.getMyRecords(userId));
    }

    /**
     * 查询评估记录作答进度（暂停后恢复作答）
     *
     * @param recordId 评估记录ID
     * @return 评估进度（含已保存作答）
     */
    @GetMapping("/record/{recordId}")
    public Result<AssessmentProgressVO> getRecordProgress(@PathVariable Long recordId) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(scaleAssessmentService.getRecordProgress(userId, recordId));
    }

    /**
     * 保存作答进度（支持中途暂停）
     *
     * @param recordId 评估记录ID
     * @param dto      作答内容
     * @return 评估进度
     */
    @PutMapping("/record/{recordId}/answers")
    public Result<AssessmentProgressVO> saveAnswers(@PathVariable Long recordId,
                                                    @RequestBody AnswerSubmitDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(scaleAssessmentService.saveAnswers(userId, recordId, dto));
    }

    /**
     * 提交答卷并自动计分、生成评估报告
     *
     * @param recordId 评估记录ID
     * @param dto      全部作答内容
     * @return 评估结果报告
     */
    @PostMapping("/record/{recordId}/submit")
    public Result<AssessmentReportVO> submit(@PathVariable Long recordId,
                                             @RequestBody AnswerSubmitDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(scaleAssessmentService.submit(userId, recordId, dto));
    }

    /**
     * 查看历史评估报告
     *
     * @param recordId 评估记录ID
     * @return 评估结果报告
     */
    @GetMapping("/record/{recordId}/report")
    public Result<AssessmentReportVO> getReport(@PathVariable Long recordId) {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.success(scaleAssessmentService.getReport(userId, recordId));
    }
}
