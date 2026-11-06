package com.aimental.serviceImpl;

import com.aimental.dto.QuestionSaveDTO;
import com.aimental.dto.ScaleSaveDTO;
import com.aimental.dto.ScaleStatusDTO;
import com.aimental.entity.MentalScale;
import com.aimental.entity.ScaleQuestion;
import com.aimental.exception.BusinessException;
import com.aimental.mapper.MentalScaleMapper;
import com.aimental.mapper.ScaleQuestionMapper;
import com.aimental.service.ScaleAdminService;
import com.aimental.vo.AdminQuestionVO;
import com.aimental.vo.ScaleVO;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 心理韧性评估管理服务实现类（管理员端）
 * 量表新增/编辑/启停 + 题目与选项维护 + 分值映射配置
 * 全部参数校验在本层手动完成（项目未引入validation依赖）
 */
@Service
public class ScaleAdminServiceImpl implements ScaleAdminService {

    private static final Logger log = LoggerFactory.getLogger(ScaleAdminServiceImpl.class);

    /**
     * 量表Mapper
     */
    private final MentalScaleMapper mentalScaleMapper;

    /**
     * 题目Mapper
     */
    private final ScaleQuestionMapper scaleQuestionMapper;

    /**
     * 构造方法注入Mapper
     *
     * @param mentalScaleMapper   量表Mapper
     * @param scaleQuestionMapper 题目Mapper
     */
    public ScaleAdminServiceImpl(MentalScaleMapper mentalScaleMapper,
                                 ScaleQuestionMapper scaleQuestionMapper) {
        this.mentalScaleMapper = mentalScaleMapper;
        this.scaleQuestionMapper = scaleQuestionMapper;
    }

    // ==================== 量表管理 ====================

    @Override
    public List<ScaleVO> getAllScales() {
        LambdaQueryWrapper<MentalScale> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(MentalScale::getCreateTime);
        List<MentalScale> scales = mentalScaleMapper.selectList(wrapper);
        List<ScaleVO> list = new ArrayList<>();
        for (MentalScale scale : scales) {
            list.add(ScaleVO.of(scale));
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScaleVO createScale(ScaleSaveDTO dto) {
        validateScaleSaveDTO(dto, null);
        // 校验量表编码全局唯一
        LambdaQueryWrapper<MentalScale> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MentalScale::getScaleCode, dto.getScaleCode());
        Long count = mentalScaleMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(400, "量表编码已存在: " + dto.getScaleCode());
        }
        MentalScale scale = new MentalScale();
        scale.setScaleName(dto.getScaleName().trim());
        scale.setScaleCode(dto.getScaleCode().trim());
        scale.setDimensionDesc(dto.getDimensionDesc());
        scale.setTotalQuestionNum(dto.getTotalQuestionNum() == null ? 0 : dto.getTotalQuestionNum());
        scale.setReliability(dto.getReliability());
        // 未传状态默认启用
        scale.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        scale.setIsDeleted(0);
        mentalScaleMapper.insert(scale);
        log.info("管理员新增量表: scaleId={}, scaleCode={}", scale.getScaleId(), scale.getScaleCode());
        return ScaleVO.of(scale);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScaleVO updateScale(Long scaleId, ScaleSaveDTO dto) {
        MentalScale scale = requireScale(scaleId);
        validateScaleSaveDTO(dto, scaleId);
        // 校验量表编码唯一（排除自身）
        LambdaQueryWrapper<MentalScale> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MentalScale::getScaleCode, dto.getScaleCode())
                .ne(MentalScale::getScaleId, scaleId);
        Long count = mentalScaleMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(400, "量表编码已存在: " + dto.getScaleCode());
        }
        scale.setScaleName(dto.getScaleName().trim());
        scale.setScaleCode(dto.getScaleCode().trim());
        scale.setDimensionDesc(dto.getDimensionDesc());
        if (dto.getTotalQuestionNum() != null) {
            scale.setTotalQuestionNum(dto.getTotalQuestionNum());
        }
        scale.setReliability(dto.getReliability());
        // 启用/停用由独立状态接口维护，此处不修改status
        mentalScaleMapper.updateById(scale);
        log.info("管理员编辑量表: scaleId={}", scaleId);
        return ScaleVO.of(scale);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScaleStatus(Long scaleId, ScaleStatusDTO dto) {
        MentalScale scale = requireScale(scaleId);
        if (dto == null || dto.getStatus() == null) {
            throw new BusinessException(400, "状态不能为空");
        }
        if (dto.getStatus() != 0 && dto.getStatus() != 1) {
            throw new BusinessException(400, "状态只能为0（停用）或1（启用）");
        }
        scale.setStatus(dto.getStatus());
        mentalScaleMapper.updateById(scale);
        log.info("管理员{}量表: scaleId={}", dto.getStatus() == 1 ? "启用" : "停用", scaleId);
    }

    // ==================== 题目管理 ====================

    @Override
    public List<AdminQuestionVO> getQuestions(Long scaleId) {
        requireScale(scaleId);
        List<ScaleQuestion> questions = getScaleQuestions(scaleId);
        List<AdminQuestionVO> list = new ArrayList<>();
        for (ScaleQuestion question : questions) {
            list.add(AdminQuestionVO.of(question));
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminQuestionVO addQuestion(Long scaleId, QuestionSaveDTO dto) {
        requireScale(scaleId);
        validateQuestionSaveDTO(dto);
        ScaleQuestion question = new ScaleQuestion();
        question.setScaleId(scaleId);
        question.setQuestionContent(dto.getQuestionContent().trim());
        question.setOptionJson(dto.getOptionJson().trim());
        question.setScoreRule(dto.getScoreRule().trim());
        question.setSortNum(dto.getSortNum() == null ? 0 : dto.getSortNum());
        question.setIsDeleted(0);
        scaleQuestionMapper.insert(question);
        // 题目数量变化后同步量表总题数
        recalcTotalQuestionNum(scaleId);
        log.info("管理员新增题目: scaleId={}, questionId={}", scaleId, question.getQuestionId());
        return AdminQuestionVO.of(question);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminQuestionVO updateQuestion(Long questionId, QuestionSaveDTO dto) {
        if (questionId == null) {
            throw new BusinessException(400, "题目ID不能为空");
        }
        ScaleQuestion question = scaleQuestionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(404, "题目不存在");
        }
        validateQuestionSaveDTO(dto);
        question.setQuestionContent(dto.getQuestionContent().trim());
        question.setOptionJson(dto.getOptionJson().trim());
        question.setScoreRule(dto.getScoreRule().trim());
        if (dto.getSortNum() != null) {
            question.setSortNum(dto.getSortNum());
        }
        scaleQuestionMapper.updateById(question);
        log.info("管理员编辑题目: questionId={}", questionId);
        return AdminQuestionVO.of(question);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Long questionId) {
        if (questionId == null) {
            throw new BusinessException(400, "题目ID不能为空");
        }
        ScaleQuestion question = scaleQuestionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(404, "题目不存在");
        }
        // 逻辑删除（实体带@TableLogic，deleteById执行is_deleted=1更新）
        scaleQuestionMapper.deleteById(questionId);
        // 题目数量变化后同步量表总题数
        recalcTotalQuestionNum(question.getScaleId());
        log.info("管理员删除题目: questionId={}, scaleId={}", questionId, question.getScaleId());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 校验量表保存参数
     *
     * @param dto     量表参数
     * @param selfId  编辑时的量表ID（新增传null）
     */
    private void validateScaleSaveDTO(ScaleSaveDTO dto, Long selfId) {
        if (dto == null) {
            throw new BusinessException(400, "请求参数不能为空");
        }
        if (dto.getScaleName() == null || dto.getScaleName().trim().isEmpty()) {
            throw new BusinessException(400, "量表名称不能为空");
        }
        if (dto.getScaleName().trim().length() > 100) {
            throw new BusinessException(400, "量表名称不能超过100个字符");
        }
        if (dto.getScaleCode() == null || dto.getScaleCode().trim().isEmpty()) {
            throw new BusinessException(400, "量表编码不能为空");
        }
        if (dto.getScaleCode().trim().length() > 50) {
            throw new BusinessException(400, "量表编码不能超过50个字符");
        }
        if (dto.getTotalQuestionNum() != null && dto.getTotalQuestionNum() < 0) {
            throw new BusinessException(400, "总题目数不能为负数");
        }
        BigDecimal reliability = dto.getReliability();
        if (reliability != null && (reliability.compareTo(BigDecimal.ZERO) < 0
                || reliability.compareTo(BigDecimal.ONE) > 0)) {
            throw new BusinessException(400, "信度系数必须在0到1之间");
        }
        if (dto.getStatus() != null && dto.getStatus() != 0 && dto.getStatus() != 1) {
            throw new BusinessException(400, "状态只能为0（停用）或1（启用）");
        }
    }

    /**
     * 校验题目保存参数：题目内容、选项JSON、计分规则JSON、分值映射数字
     */
    private void validateQuestionSaveDTO(QuestionSaveDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "请求参数不能为空");
        }
        if (dto.getQuestionContent() == null || dto.getQuestionContent().trim().isEmpty()) {
            throw new BusinessException(400, "题目内容不能为空");
        }
        if (dto.getQuestionContent().trim().length() > 2000) {
            throw new BusinessException(400, "题目内容不能超过2000个字符");
        }
        validateOptionJson(dto.getOptionJson());
        validateScoreRule(dto.getScoreRule());
    }

    /**
     * 校验选项JSON：必须为非空数组，每项包含value与label
     */
    private void validateOptionJson(String optionJson) {
        if (optionJson == null || optionJson.trim().isEmpty()) {
            throw new BusinessException(400, "选项JSON不能为空");
        }
        if (!JSONUtil.isTypeJSONArray(optionJson)) {
            throw new BusinessException(400, "选项必须为JSON数组，格式示例: [{\"value\":1,\"label\":\"完全不符合\"}]");
        }
        try {
            JSONArray array = JSONUtil.parseArray(optionJson);
            if (array.isEmpty()) {
                throw new BusinessException(400, "选项不能为空");
            }
            for (int i = 0; i < array.size(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (obj.get("value") == null || !(obj.get("value") instanceof Number)) {
                    throw new BusinessException(400, "第" + (i + 1) + "个选项缺少数字类型的value");
                }
                if (obj.get("label") == null) {
                    throw new BusinessException(400, "第" + (i + 1) + "个选项缺少label");
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(400, "选项JSON格式不正确");
        }
    }

    /**
     * 校验计分规则JSON：必须为对象且包含dimension维度标识，scoreMap若存在则值必须为数字
     */
    private void validateScoreRule(String scoreRule) {
        if (scoreRule == null || scoreRule.trim().isEmpty()) {
            throw new BusinessException(400, "计分规则不能为空");
        }
        if (!JSONUtil.isTypeJSONObject(scoreRule)) {
            throw new BusinessException(400, "计分规则必须为JSON对象，格式示例: {\"dimension\":\"toughness\",\"scoreMap\":{\"1\":1,\"2\":2,\"3\":3,\"4\":4,\"5\":5}}");
        }
        JSONObject rule;
        try {
            rule = JSONUtil.parseObj(scoreRule);
        } catch (Exception e) {
            throw new BusinessException(400, "计分规则JSON格式不正确");
        }
        String dimension = rule.getStr("dimension");
        if (dimension == null || dimension.trim().isEmpty()) {
            throw new BusinessException(400, "计分规则必须包含dimension维度标识（如toughness/strength/optimism）");
        }
        if (!rule.containsKey("scoreMap") || rule.get("scoreMap") == null) {
            // 分值映射可省略，省略时选项值即得分
            return;
        }
        JSONObject scoreMap;
        try {
            scoreMap = rule.getJSONObject("scoreMap");
        } catch (Exception e) {
            throw new BusinessException(400, "scoreMap必须为JSON对象");
        }
        if (scoreMap == null) {
            throw new BusinessException(400, "scoreMap必须为JSON对象");
        }
        for (String key : scoreMap.keySet()) {
            if (scoreMap.get(key) == null || !(scoreMap.get(key) instanceof Number)) {
                throw new BusinessException(400, "scoreMap中选项" + key + "对应的分值必须为数字");
            }
        }
    }

    /**
     * 校验量表存在
     */
    private MentalScale requireScale(Long scaleId) {
        if (scaleId == null) {
            throw new BusinessException(400, "量表ID不能为空");
        }
        MentalScale scale = mentalScaleMapper.selectById(scaleId);
        if (scale == null) {
            throw new BusinessException(404, "量表不存在");
        }
        return scale;
    }

    /**
     * 查询量表全部题目（按排序号升序）
     */
    private List<ScaleQuestion> getScaleQuestions(Long scaleId) {
        LambdaQueryWrapper<ScaleQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScaleQuestion::getScaleId, scaleId)
                .orderByAsc(ScaleQuestion::getSortNum)
                .orderByAsc(ScaleQuestion::getQuestionId);
        return scaleQuestionMapper.selectList(wrapper);
    }

    /**
     * 按实际题目数量重新统计并同步量表总题数
     */
    private void recalcTotalQuestionNum(Long scaleId) {
        if (scaleId == null) {
            return;
        }
        Long count = scaleQuestionMapper.selectCount(
                new LambdaQueryWrapper<ScaleQuestion>().eq(ScaleQuestion::getScaleId, scaleId));
        MentalScale scale = mentalScaleMapper.selectById(scaleId);
        if (scale != null) {
            scale.setTotalQuestionNum(count == null ? 0 : count.intValue());
            mentalScaleMapper.updateById(scale);
        }
    }
}
