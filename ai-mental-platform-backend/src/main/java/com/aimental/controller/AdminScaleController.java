package com.aimental.controller;

import com.aimental.common.Result;
import com.aimental.dto.QuestionSaveDTO;
import com.aimental.dto.ScaleSaveDTO;
import com.aimental.dto.ScaleStatusDTO;
import com.aimental.service.ScaleAdminService;
import com.aimental.vo.AdminQuestionVO;
import com.aimental.vo.ScaleVO;
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
 * 心理韧性评估管理接口（管理员端）
 * 全部接口要求管理员角色（JWT中role=admin，映射为ROLE_admin）
 *
 * 接口清单（context-path为/api）：
 * GET    /api/admin/scale                       查询全部量表（含停用）
 * POST   /api/admin/scale                       新增量表
 * PUT    /api/admin/scale/{scaleId}             编辑量表
 * PUT    /api/admin/scale/{scaleId}/status      启用/停用量表
 * GET    /api/admin/scale/{scaleId}/questions   查询量表全部题目
 * POST   /api/admin/scale/{scaleId}/question    新增题目（含选项与分值映射）
 * PUT    /api/admin/question/{questionId}       编辑题目
 * DELETE /api/admin/question/{questionId}       删除题目（逻辑删除）
 */
@RestController
@RequestMapping("/admin/scale")
public class AdminScaleController {

    /**
     * 评估管理服务
     */
    private final ScaleAdminService scaleAdminService;

    /**
     * 构造方法注入服务
     *
     * @param scaleAdminService 评估管理服务
     */
    public AdminScaleController(ScaleAdminService scaleAdminService) {
        this.scaleAdminService = scaleAdminService;
    }

    /**
     * 查询全部量表（含停用，管理端列表）
     *
     * @return 量表列表
     */
    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public Result<List<ScaleVO>> getAllScales() {
        return Result.success(scaleAdminService.getAllScales());
    }

    /**
     * 新增评估量表
     *
     * @param dto 量表信息
     * @return 新建的量表VO
     */
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<ScaleVO> createScale(@RequestBody ScaleSaveDTO dto) {
        return Result.success(scaleAdminService.createScale(dto));
    }

    /**
     * 编辑评估量表（名称、编码、维度说明、题数、信度）
     *
     * @param scaleId 量表ID
     * @param dto     量表信息
     * @return 编辑后的量表VO
     */
    @PutMapping("/{scaleId}")
    @PreAuthorize("hasRole('admin')")
    public Result<ScaleVO> updateScale(@PathVariable Long scaleId, @RequestBody ScaleSaveDTO dto) {
        return Result.success(scaleAdminService.updateScale(scaleId, dto));
    }

    /**
     * 启用/停用评估量表
     *
     * @param scaleId 量表ID
     * @param dto     状态（0停用，1启用）
     * @return 操作结果
     */
    @PutMapping("/{scaleId}/status")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> updateScaleStatus(@PathVariable Long scaleId, @RequestBody ScaleStatusDTO dto) {
        scaleAdminService.updateScaleStatus(scaleId, dto);
        return Result.success();
    }

    /**
     * 查询指定量表的全部题目（含选项JSON与计分规则）
     *
     * @param scaleId 量表ID
     * @return 题目列表
     */
    @GetMapping("/{scaleId}/questions")
    @PreAuthorize("hasRole('admin')")
    public Result<List<AdminQuestionVO>> getQuestions(@PathVariable Long scaleId) {
        return Result.success(scaleAdminService.getQuestions(scaleId));
    }

    /**
     * 新增量表题目（含选项与分值映射配置）
     *
     * @param scaleId 量表ID
     * @param dto     题目信息
     * @return 新建的题目VO
     */
    @PostMapping("/{scaleId}/question")
    @PreAuthorize("hasRole('admin')")
    public Result<AdminQuestionVO> addQuestion(@PathVariable Long scaleId, @RequestBody QuestionSaveDTO dto) {
        return Result.success(scaleAdminService.addQuestion(scaleId, dto));
    }

    /**
     * 编辑量表题目（内容、选项、计分规则、排序）
     *
     * @param questionId 题目ID
     * @param dto        题目信息
     * @return 编辑后的题目VO
     */
    @PutMapping("/question/{questionId}")
    @PreAuthorize("hasRole('admin')")
    public Result<AdminQuestionVO> updateQuestion(@PathVariable Long questionId,
                                                  @RequestBody QuestionSaveDTO dto) {
        return Result.success(scaleAdminService.updateQuestion(questionId, dto));
    }

    /**
     * 删除量表题目（逻辑删除）
     *
     * @param questionId 题目ID
     * @return 操作结果
     */
    @DeleteMapping("/question/{questionId}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> deleteQuestion(@PathVariable Long questionId) {
        scaleAdminService.deleteQuestion(questionId);
        return Result.success();
    }
}
