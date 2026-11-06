package com.aimental.service;

import com.aimental.dto.AnswerSubmitDTO;
import com.aimental.dto.CreateScaleRecordDTO;
import com.aimental.vo.AssessmentProgressVO;
import com.aimental.vo.AssessmentRecordVO;
import com.aimental.vo.AssessmentReportVO;
import com.aimental.vo.ScaleDetailVO;
import com.aimental.vo.ScaleVO;

import java.util.List;

/**
 * 心理韧性评估服务接口（普通用户端）
 * 全部方法基于当前登录用户（userId由Token解析），只能访问本人评估记录
 */
public interface ScaleAssessmentService {

    /**
     * 评估状态：进行中
     */
    int STATUS_IN_PROGRESS = 1;

    /**
     * 评估状态：已暂停（中途保存）
     */
    int STATUS_PAUSED = 2;

    /**
     * 评估状态：已提交（完成计分）
     */
    int STATUS_SUBMITTED = 3;

    /**
     * 获取全部可用（启用中）评估量表列表
     *
     * @return 量表列表
     */
    List<ScaleVO> getEnabledScales();

    /**
     * 获取量表详情（题目+选项）
     *
     * @param scaleId 量表ID
     * @return 量表详情（含题目与选项）
     */
    ScaleDetailVO getScaleDetail(Long scaleId);

    /**
     * 新建评估记录，开始测评
     *
     * @param userId 当前登录用户ID
     * @param dto    创建请求（量表ID）
     * @return 评估进度（记录ID+初始作答状态）
     */
    AssessmentProgressVO createRecord(Long userId, CreateScaleRecordDTO dto);

    /**
     * 查询评估记录作答进度（暂停后恢复作答）
     *
     * @param userId   当前登录用户ID
     * @param recordId 评估记录ID
     * @return 评估进度（含已保存作答）
     */
    AssessmentProgressVO getRecordProgress(Long userId, Long recordId);

    /**
     * 保存作答进度（支持中途暂停）
     *
     * @param userId   当前登录用户ID
     * @param recordId 评估记录ID
     * @param dto      作答内容
     * @return 评估进度
     */
    AssessmentProgressVO saveAnswers(Long userId, Long recordId, AnswerSubmitDTO dto);

    /**
     * 提交答卷并自动计算得分、生成评估报告
     *
     * @param userId   当前登录用户ID
     * @param recordId 评估记录ID
     * @param dto      全部作答内容
     * @return 评估结果报告
     */
    AssessmentReportVO submit(Long userId, Long recordId, AnswerSubmitDTO dto);

    /**
     * 查询本人全部历史评估记录
     *
     * @param userId 当前登录用户ID
     * @return 评估记录列表（按记录ID倒序）
     */
    List<AssessmentRecordVO> getMyRecords(Long userId);

    /**
     * 查看历史评估报告
     *
     * @param userId   当前登录用户ID
     * @param recordId 评估记录ID
     * @return 评估结果报告
     */
    AssessmentReportVO getReport(Long userId, Long recordId);
}
