package com.aimental.service;

import com.aimental.dto.QuestionSaveDTO;
import com.aimental.dto.ScaleSaveDTO;
import com.aimental.dto.ScaleStatusDTO;
import com.aimental.vo.AdminQuestionVO;
import com.aimental.vo.ScaleVO;

import java.util.List;

/**
 * 心理韧性评估管理服务接口（管理员端）
 * 量表新增/编辑/启停，题目与选项维护、分值映射配置
 */
public interface ScaleAdminService {

    /**
     * 查询全部量表（含停用，供管理端列表使用）
     *
     * @return 量表列表
     */
    List<ScaleVO> getAllScales();

    /**
     * 新增评估量表
     *
     * @param dto 量表信息
     * @return 新建的量表VO
     */
    ScaleVO createScale(ScaleSaveDTO dto);

    /**
     * 编辑评估量表（名称、编码、维度说明、题数、信度）
     *
     * @param scaleId 量表ID
     * @param dto     量表信息
     * @return 编辑后的量表VO
     */
    ScaleVO updateScale(Long scaleId, ScaleSaveDTO dto);

    /**
     * 启用/停用评估量表
     *
     * @param scaleId 量表ID
     * @param dto     状态（0停用，1启用）
     */
    void updateScaleStatus(Long scaleId, ScaleStatusDTO dto);

    /**
     * 查询指定量表的全部题目（含选项JSON与计分规则）
     *
     * @param scaleId 量表ID
     * @return 题目列表
     */
    List<AdminQuestionVO> getQuestions(Long scaleId);

    /**
     * 新增量表题目（含选项与分值映射），并同步量表总题数
     *
     * @param scaleId 量表ID
     * @param dto     题目信息
     * @return 新建的题目VO
     */
    AdminQuestionVO addQuestion(Long scaleId, QuestionSaveDTO dto);

    /**
     * 编辑量表题目（内容、选项、计分规则、排序）
     *
     * @param questionId 题目ID
     * @param dto        题目信息
     * @return 编辑后的题目VO
     */
    AdminQuestionVO updateQuestion(Long questionId, QuestionSaveDTO dto);

    /**
     * 删除量表题目（逻辑删除），并同步量表总题数
     *
     * @param questionId 题目ID
     */
    void deleteQuestion(Long questionId);
}
