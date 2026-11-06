package com.aimental.serviceImpl;

import com.aimental.dto.AnswerSubmitDTO;
import com.aimental.dto.CreateScaleRecordDTO;
import com.aimental.entity.MentalScale;
import com.aimental.entity.ScaleQuestion;
import com.aimental.entity.UserScaleRecord;
import com.aimental.exception.BusinessException;
import com.aimental.mapper.MentalScaleMapper;
import com.aimental.mapper.ScaleQuestionMapper;
import com.aimental.mapper.UserScaleRecordMapper;
import com.aimental.service.ScaleAssessmentService;
import com.aimental.vo.AssessmentProgressVO;
import com.aimental.vo.AssessmentRecordVO;
import com.aimental.vo.AssessmentReportVO;
import com.aimental.vo.ScaleDetailVO;
import com.aimental.vo.ScaleVO;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 心理韧性评估服务实现类（普通用户端）
 *
 * 存储设计（不改动既有表结构）：
 * 1. 作答进度与评估状态存于 answer_json：
 *    {"status":1,"answers":{"题目ID":选项值,...}}，status：1进行中 2已暂停 3已提交
 * 2. 评估报告（等级、解读、成长建议、三维度得分）存于 evaluate_result（JSON文本）
 * 3. 总分存于 total_score；finish_time 由数据库默认值填充，展示层按 status 过滤，
 *    仅已完成记录返回完成时间，避免进行中记录显示误导性时间
 * 4. 计分依据 scale_question.score_rule：
 *    {"dimension":"toughness","scoreMap":{"1":1,...,"5":5}}
 *    dimension 决定维度归属（toughness坚韧性/strength力量性/optimism乐观性），
 *    scoreMap 决定选项值到分值的映射（缺省时选项值即得分）
 */
@Service
public class ScaleAssessmentServiceImpl implements ScaleAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(ScaleAssessmentServiceImpl.class);

    /**
     * 量表Mapper
     */
    private final MentalScaleMapper mentalScaleMapper;

    /**
     * 题目Mapper
     */
    private final ScaleQuestionMapper scaleQuestionMapper;

    /**
     * 评估记录Mapper
     */
    private final UserScaleRecordMapper userScaleRecordMapper;

    /**
     * 维度标识到中文名的映射
     */
    private static final Map<String, String> DIMENSION_NAME_MAP = new LinkedHashMap<>();

    static {
        DIMENSION_NAME_MAP.put("toughness", "坚韧性");
        DIMENSION_NAME_MAP.put("strength", "力量性");
        DIMENSION_NAME_MAP.put("optimism", "乐观性");
    }

    /**
     * 构造方法注入Mapper
     *
     * @param mentalScaleMapper    量表Mapper
     * @param scaleQuestionMapper  题目Mapper
     * @param userScaleRecordMapper 评估记录Mapper
     */
    public ScaleAssessmentServiceImpl(MentalScaleMapper mentalScaleMapper,
                                      ScaleQuestionMapper scaleQuestionMapper,
                                      UserScaleRecordMapper userScaleRecordMapper) {
        this.mentalScaleMapper = mentalScaleMapper;
        this.scaleQuestionMapper = scaleQuestionMapper;
        this.userScaleRecordMapper = userScaleRecordMapper;
    }

    // ==================== 量表查询 ====================

    @Override
    public List<ScaleVO> getEnabledScales() {
        LambdaQueryWrapper<MentalScale> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MentalScale::getStatus, 1)
                .orderByDesc(MentalScale::getCreateTime);
        List<MentalScale> scales = mentalScaleMapper.selectList(wrapper);
        List<ScaleVO> list = new ArrayList<>();
        for (MentalScale scale : scales) {
            list.add(ScaleVO.of(scale));
        }
        return list;
    }

    @Override
    public ScaleDetailVO getScaleDetail(Long scaleId) {
        MentalScale scale = requireScale(scaleId);
        if (scale.getStatus() == null || scale.getStatus() != 1) {
            throw new BusinessException(400, "该量表已停用，暂不可用");
        }
        List<ScaleQuestion> questions = getScaleQuestions(scale.getScaleId());
        if (questions.isEmpty()) {
            throw new BusinessException(400, "该量表暂无题目");
        }
        ScaleDetailVO vo = new ScaleDetailVO();
        vo.setScaleId(scale.getScaleId());
        vo.setScaleName(scale.getScaleName());
        vo.setScaleCode(scale.getScaleCode());
        vo.setDimensionDesc(scale.getDimensionDesc());
        vo.setReliability(scale.getReliability());
        vo.setTotalQuestionNum(questions.size());
        List<ScaleDetailVO.QuestionItemVO> questionItems = new ArrayList<>();
        for (ScaleQuestion question : questions) {
            ScaleDetailVO.QuestionItemVO item = new ScaleDetailVO.QuestionItemVO();
            item.setQuestionId(question.getQuestionId());
            item.setQuestionContent(question.getQuestionContent());
            item.setSortNum(question.getSortNum());
            item.setOptions(parseOptions(question));
            questionItems.add(item);
        }
        vo.setQuestions(questionItems);
        return vo;
    }

    // ==================== 新建评估记录 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssessmentProgressVO createRecord(Long userId, CreateScaleRecordDTO dto) {
        if (dto == null) {
            throw new BusinessException(400, "请求参数不能为空");
        }
        if (dto.getScaleId() == null) {
            throw new BusinessException(400, "量表ID不能为空");
        }
        MentalScale scale = requireScale(dto.getScaleId());
        if (scale.getStatus() == null || scale.getStatus() != 1) {
            throw new BusinessException(400, "该量表已停用，暂不可用");
        }
        List<ScaleQuestion> questions = getScaleQuestions(scale.getScaleId());
        if (questions.isEmpty()) {
            throw new BusinessException(400, "该量表暂无题目，无法开始评估");
        }
        UserScaleRecord record = new UserScaleRecord();
        record.setUserId(userId);
        record.setScaleId(scale.getScaleId());
        record.setAnswerJson(buildAnswerJson(STATUS_IN_PROGRESS, new LinkedHashMap<>()));
        record.setTotalScore(0);
        record.setIsDeleted(0);
        userScaleRecordMapper.insert(record);
        log.info("用户[{}]创建评估记录: recordId={}, scaleId={}", userId, record.getRecordId(), scale.getScaleId());
        return toProgressVO(record, scale, questions.size());
    }

    // ==================== 作答进度（暂停恢复） ====================

    @Override
    public AssessmentProgressVO getRecordProgress(Long userId, Long recordId) {
        UserScaleRecord record = requireOwnedRecord(userId, recordId);
        MentalScale scale = requireScale(record.getScaleId());
        int questionCount = getScaleQuestions(record.getScaleId()).size();
        return toProgressVO(record, scale, questionCount);
    }

    // ==================== 中途暂停保存 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssessmentProgressVO saveAnswers(Long userId, Long recordId, AnswerSubmitDTO dto) {
        UserScaleRecord record = requireOwnedRecord(userId, recordId);
        if (parseStatus(record.getAnswerJson()) == STATUS_SUBMITTED) {
            throw new BusinessException(400, "该评估已提交，不能继续作答");
        }
        MentalScale scale = requireScale(record.getScaleId());
        List<ScaleQuestion> questions = getScaleQuestions(record.getScaleId());
        if (questions.isEmpty()) {
            throw new BusinessException(400, "该量表暂无题目");
        }
        Map<Long, Integer> answers = validateAnswers(dto, questions);
        record.setAnswerJson(buildAnswerJson(STATUS_PAUSED, answers));
        userScaleRecordMapper.updateById(record);
        log.info("用户[{}]暂停保存评估: recordId={}, 已答{}题", userId, recordId, answers.size());
        return toProgressVO(record, scale, questions.size());
    }

    // ==================== 提交答卷、自动计分、生成报告 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssessmentReportVO submit(Long userId, Long recordId, AnswerSubmitDTO dto) {
        UserScaleRecord record = requireOwnedRecord(userId, recordId);
        if (parseStatus(record.getAnswerJson()) == STATUS_SUBMITTED) {
            throw new BusinessException(400, "该评估已提交，请勿重复提交");
        }
        MentalScale scale = requireScale(record.getScaleId());
        List<ScaleQuestion> questions = getScaleQuestions(record.getScaleId());
        if (questions.isEmpty()) {
            throw new BusinessException(400, "该量表暂无题目，无法计分");
        }
        Map<Long, Integer> answers = validateAnswers(dto, questions);
        // 完整性校验：必须覆盖量表全部题目才能计分
        Set<Long> questionIds = questions.stream()
                .map(ScaleQuestion::getQuestionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!answers.keySet().equals(questionIds)) {
            throw new BusinessException(400, "答卷不完整，请完成全部" + questions.size() + "道题目后提交");
        }
        // 自动计算：总分 + 各维度得分 + 满分
        ScoreResult result = calculateScore(answers, questions);
        // 按得分率生成评估等级、解读文字、成长建议
        int percent = result.max > 0 ? (int) Math.round(result.total * 100.0 / result.max) : 0;
        String level = resolveLevel(percent);
        String interpretation = resolveInterpretation(level);
        String advice = resolveAdvice(level);
        // 持久化：状态、总分、报告JSON、完成时间
        record.setAnswerJson(buildAnswerJson(STATUS_SUBMITTED, answers));
        record.setTotalScore(result.total);
        record.setEvaluateResult(buildReportJson(level, interpretation, advice, percent, result));
        record.setFinishTime(LocalDateTime.now());
        userScaleRecordMapper.updateById(record);
        log.info("用户[{}]提交评估: recordId={}, totalScore={}, level={}", userId, recordId, result.total, level);
        return toReportVO(record, scale);
    }

    // ==================== 历史记录与报告查询 ====================

    @Override
    public List<AssessmentRecordVO> getMyRecords(Long userId) {
        LambdaQueryWrapper<UserScaleRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserScaleRecord::getUserId, userId)
                .orderByDesc(UserScaleRecord::getRecordId);
        List<UserScaleRecord> records = userScaleRecordMapper.selectList(wrapper);
        // 批量查询量表名称，避免循环查库
        Set<Long> scaleIds = records.stream()
                .map(UserScaleRecord::getScaleId)
                .collect(Collectors.toSet());
        Map<Long, MentalScale> scaleMap = new LinkedHashMap<>();
        if (!scaleIds.isEmpty()) {
            for (MentalScale scale : mentalScaleMapper.selectBatchIds(scaleIds)) {
                scaleMap.put(scale.getScaleId(), scale);
            }
        }
        List<AssessmentRecordVO> list = new ArrayList<>();
        for (UserScaleRecord record : records) {
            AssessmentRecordVO vo = new AssessmentRecordVO();
            vo.setRecordId(record.getRecordId());
            vo.setScaleId(record.getScaleId());
            MentalScale scale = scaleMap.get(record.getScaleId());
            vo.setScaleName(scale != null ? scale.getScaleName() : "未知量表");
            int status = parseStatus(record.getAnswerJson());
            vo.setStatus(status);
            vo.setStatusText(statusText(status));
            // 未完成记录不返回分数与完成时间，避免误导
            vo.setTotalScore(status == STATUS_SUBMITTED ? record.getTotalScore() : null);
            vo.setFinishTime(status == STATUS_SUBMITTED ? record.getFinishTime() : null);
            list.add(vo);
        }
        return list;
    }

    @Override
    public AssessmentReportVO getReport(Long userId, Long recordId) {
        UserScaleRecord record = requireOwnedRecord(userId, recordId);
        if (parseStatus(record.getAnswerJson()) != STATUS_SUBMITTED) {
            throw new BusinessException(400, "该评估尚未完成，暂无评估报告");
        }
        MentalScale scale = requireScale(record.getScaleId());
        return toReportVO(record, scale);
    }

    // ==================== 私有辅助方法 ====================

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
     * 校验评估记录存在且属于当前用户
     */
    private UserScaleRecord requireOwnedRecord(Long userId, Long recordId) {
        if (recordId == null) {
            throw new BusinessException(400, "评估记录ID不能为空");
        }
        UserScaleRecord record = userScaleRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(404, "评估记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权访问他人的评估记录");
        }
        return record;
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
     * 解析题目选项JSON为选项列表
     */
    private List<ScaleDetailVO.OptionItemVO> parseOptions(ScaleQuestion question) {
        List<ScaleDetailVO.OptionItemVO> options = new ArrayList<>();
        String optionJson = question.getOptionJson();
        if (optionJson == null || optionJson.trim().isEmpty()) {
            return options;
        }
        if (!JSONUtil.isTypeJSONArray(optionJson)) {
            throw new BusinessException(400, "题目选项配置有误，请联系管理员");
        }
        try {
            JSONArray array = JSONUtil.parseArray(optionJson);
            for (int i = 0; i < array.size(); i++) {
                JSONObject obj = array.getJSONObject(i);
                ScaleDetailVO.OptionItemVO option = new ScaleDetailVO.OptionItemVO();
                option.setValue(obj.getInt("value"));
                option.setLabel(obj.getStr("label"));
                options.add(option);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(400, "题目选项配置有误，请联系管理员");
        }
        return options;
    }

    /**
     * 校验作答内容：格式正确、题目属于该量表、选项值在题目选项范围内
     *
     * @return 题目ID到选项值的映射
     */
    private Map<Long, Integer> validateAnswers(AnswerSubmitDTO dto, List<ScaleQuestion> questions) {
        if (dto == null || dto.getAnswers() == null) {
            throw new BusinessException(400, "作答内容不能为空");
        }
        // 题目ID与选项值范围索引
        Map<Long, Set<Integer>> optionValueIndex = new LinkedHashMap<>();
        for (ScaleQuestion question : questions) {
            Set<Integer> values = new LinkedHashSet<>();
            String optionJson = question.getOptionJson();
            if (optionJson != null && JSONUtil.isTypeJSONArray(optionJson)) {
                JSONArray array = JSONUtil.parseArray(optionJson);
                for (int i = 0; i < array.size(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    if (obj.getInt("value") != null) {
                        values.add(obj.getInt("value"));
                    }
                }
            }
            optionValueIndex.put(question.getQuestionId(), values);
        }
        Map<Long, Integer> answers = new LinkedHashMap<>();
        for (AnswerSubmitDTO.AnswerItem item : dto.getAnswers()) {
            if (item == null || item.getQuestionId() == null || item.getOptionValue() == null) {
                throw new BusinessException(400, "作答内容格式不正确");
            }
            Set<Integer> allowed = optionValueIndex.get(item.getQuestionId());
            if (allowed == null) {
                throw new BusinessException(400, "存在不属于该量表的题目ID: " + item.getQuestionId());
            }
            if (item.getOptionValue() < 0) {
                throw new BusinessException(400, "选项值不合法");
            }
            // 题目配置了选项时，选项值必须在可选范围内
            if (!allowed.isEmpty() && !allowed.contains(item.getOptionValue())) {
                throw new BusinessException(400, "题目" + item.getQuestionId() + "的选项值" + item.getOptionValue() + "不在可选范围内");
            }
            answers.put(item.getQuestionId(), item.getOptionValue());
        }
        return answers;
    }

    /**
     * 按 score_rule 自动计分：总分、各维度得分、各维度满分
     */
    private ScoreResult calculateScore(Map<Long, Integer> answers, List<ScaleQuestion> questions) {
        ScoreResult result = new ScoreResult();
        for (ScaleQuestion question : questions) {
            Integer optionValue = answers.get(question.getQuestionId());
            if (optionValue == null) {
                continue;
            }
            // 解析计分规则：维度标识 + 分值映射
            String dimension = "other";
            JSONObject scoreMap = null;
            String scoreRule = question.getScoreRule();
            if (scoreRule != null && !scoreRule.trim().isEmpty()) {
                if (!JSONUtil.isTypeJSONObject(scoreRule)) {
                    throw new BusinessException(400, "题目" + question.getQuestionId() + "的计分规则配置有误");
                }
                JSONObject rule = JSONUtil.parseObj(scoreRule);
                if (rule.getStr("dimension") != null && !rule.getStr("dimension").trim().isEmpty()) {
                    dimension = rule.getStr("dimension");
                }
                scoreMap = rule.getJSONObject("scoreMap");
            }
            // 取分：优先分值映射，缺省时选项值即得分
            int score;
            if (scoreMap != null && scoreMap.containsKey(String.valueOf(optionValue))) {
                Integer mapped = scoreMap.getInt(String.valueOf(optionValue));
                if (mapped == null) {
                    throw new BusinessException(400, "题目" + question.getQuestionId() + "的分值映射必须为数字");
                }
                score = mapped;
            } else {
                score = optionValue;
            }
            int questionMax = resolveQuestionMaxScore(question, scoreMap);
            result.total += score;
            result.max += questionMax;
            result.dimScore.merge(dimension, score, Integer::sum);
            result.dimMax.merge(dimension, questionMax, Integer::sum);
        }
        return result;
    }

    /**
     * 计算单题满分：优先取分值映射的最大值，否则取选项值最大值，兜底为1
     */
    private int resolveQuestionMaxScore(ScaleQuestion question, JSONObject scoreMap) {
        if (scoreMap != null && !scoreMap.isEmpty()) {
            int max = Integer.MIN_VALUE;
            for (String key : scoreMap.keySet()) {
                Integer value = scoreMap.getInt(key);
                if (value != null && value > max) {
                    max = value;
                }
            }
            if (max != Integer.MIN_VALUE) {
                return max;
            }
        }
        int max = 1;
        String optionJson = question.getOptionJson();
        if (optionJson != null && JSONUtil.isTypeJSONArray(optionJson)) {
            JSONArray array = JSONUtil.parseArray(optionJson);
            for (int i = 0; i < array.size(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Integer value = obj.getInt("value");
                if (value != null && value > max) {
                    max = value;
                }
            }
        }
        return max;
    }

    /**
     * 按得分率划分评估等级
     * 分档依据（每题0-4分、共25题、总分0-100时得分率与总分数值一致）：
     * 70-100 高韧性（优秀）；50-69 中等韧性；0-49 低韧性（偏弱）
     */
    private String resolveLevel(int percent) {
        if (percent >= 70) {
            return "高韧性";
        }
        if (percent >= 50) {
            return "中等韧性";
        }
        return "低韧性";
    }

    /**
     * 评估等级对应的解读文字
     * 三档文案与测评报告页的分级参考保持一致
     */
    private String resolveInterpretation(String level) {
        if ("高韧性".equals(level)) {
            return "您的心理韧性处于优秀水平（总分70-100）。您善于在压力、挫折中自我调节、复原，面对逆境时通常能快速调整状态、调动资源积极应对。";
        }
        if ("中等韧性".equals(level)) {
            return "您的心理韧性处于中等水平（总分50-69）。大部分压力可以应对，具备一定的抗压与恢复能力，但在重大打击或持续压力下容易受到影响。";
        }
        return "您的心理韧性偏弱（总分0-49）。遇到挫折容易持续内耗、难以恢复，韧性资源相对不足，建议配合平台的韧性训练课程进行系统练习。";
    }

    /**
     * 评估等级对应的成长建议
     */
    private String resolveAdvice(String level) {
        if ("高韧性".equals(level)) {
            return "建议您继续保持现有的压力调节与情绪管理习惯，例如规律运动、保持社会联结与目标拆解；同时可将您的应对经验分享给身边的人，在助人中进一步巩固心理韧性。";
        }
        if ("中等韧性".equals(level)) {
            return "建议您从三方面提升：①坚韧性——通过正念冥想、情绪日记增强压力下的自我觉察与冷静应对；②力量性——主动构建社会支持网络，遇到困难时及时求助；③乐观性——练习记录每日积极事件，培养成长型思维，把挫折视为可学习的经验。";
        }
        return "建议您循序渐进地开展韧性训练：①建立稳定的生活节律（睡眠、运动、饮食）；②学习基础情绪调节技术，如腹式呼吸、放松训练；③设定小而可达成的目标，在一次次完成中积累掌控感与自信；④必要时寻求学校心理中心或专业人士的帮助。";
    }

    /**
     * 构建 answer_json 文本
     */
    private String buildAnswerJson(int status, Map<Long, Integer> answers) {
        JSONObject root = new JSONObject();
        root.set("status", status);
        JSONObject answerObj = new JSONObject();
        for (Map.Entry<Long, Integer> entry : answers.entrySet()) {
            answerObj.set(String.valueOf(entry.getKey()), entry.getValue());
        }
        root.set("answers", answerObj);
        return root.toString();
    }

    /**
     * 解析 answer_json 中的状态，缺省为进行中
     */
    private int parseStatus(String answerJson) {
        if (answerJson == null || answerJson.trim().isEmpty()) {
            return STATUS_IN_PROGRESS;
        }
        try {
            JSONObject root = JSONUtil.parseObj(answerJson);
            Integer status = root.getInt("status");
            return status == null ? STATUS_IN_PROGRESS : status;
        } catch (Exception e) {
            return STATUS_IN_PROGRESS;
        }
    }

    /**
     * 解析 answer_json 中的作答映射
     */
    private Map<Long, Integer> parseAnswers(String answerJson) {
        Map<Long, Integer> answers = new LinkedHashMap<>();
        if (answerJson == null || answerJson.trim().isEmpty()) {
            return answers;
        }
        try {
            JSONObject root = JSONUtil.parseObj(answerJson);
            JSONObject answerObj = root.getJSONObject("answers");
            if (answerObj != null) {
                for (String key : answerObj.keySet()) {
                    Integer value = answerObj.getInt(key);
                    if (value != null) {
                        answers.put(Long.valueOf(key), value);
                    }
                }
            }
        } catch (Exception e) {
            return answers;
        }
        return answers;
    }

    /**
     * 状态转文字
     */
    private String statusText(int status) {
        if (status == STATUS_SUBMITTED) {
            return "已完成";
        }
        if (status == STATUS_PAUSED) {
            return "已暂停";
        }
        return "进行中";
    }

    /**
     * 构建评估进度VO（未完成记录 finishTime 返回null）
     */
    private AssessmentProgressVO toProgressVO(UserScaleRecord record, MentalScale scale, int questionCount) {
        AssessmentProgressVO vo = new AssessmentProgressVO();
        vo.setRecordId(record.getRecordId());
        vo.setScaleId(record.getScaleId());
        vo.setScaleName(scale.getScaleName());
        int status = parseStatus(record.getAnswerJson());
        vo.setStatus(status);
        vo.setStatusText(statusText(status));
        vo.setTotalQuestionNum(questionCount);
        List<AssessmentProgressVO.AnswerItemVO> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : parseAnswers(record.getAnswerJson()).entrySet()) {
            AssessmentProgressVO.AnswerItemVO item = new AssessmentProgressVO.AnswerItemVO();
            item.setQuestionId(entry.getKey());
            item.setOptionValue(entry.getValue());
            items.add(item);
        }
        vo.setAnswers(items);
        vo.setFinishTime(status == STATUS_SUBMITTED ? record.getFinishTime() : null);
        return vo;
    }

    /**
     * 由记录中的总分与 evaluate_result 报告JSON 构建报告VO
     */
    private AssessmentReportVO toReportVO(UserScaleRecord record, MentalScale scale) {
        AssessmentReportVO vo = new AssessmentReportVO();
        vo.setRecordId(record.getRecordId());
        vo.setScaleId(record.getScaleId());
        vo.setScaleName(scale.getScaleName());
        vo.setStatus(STATUS_SUBMITTED);
        vo.setStatusText(statusText(STATUS_SUBMITTED));
        vo.setTotalScore(record.getTotalScore());
        vo.setFinishTime(record.getFinishTime());
        String reportJson = record.getEvaluateResult();
        if (reportJson == null || reportJson.trim().isEmpty()) {
            throw new BusinessException(500, "评估报告数据缺失，请联系管理员");
        }
        JSONObject report;
        try {
            report = JSONUtil.parseObj(reportJson);
        } catch (Exception e) {
            throw new BusinessException(500, "评估报告数据格式异常，请联系管理员");
        }
        vo.setMaxScore(report.getInt("maxScore"));
        vo.setScorePercent(report.getInt("percent"));
        vo.setLevel(report.getStr("level"));
        vo.setInterpretation(report.getStr("interpretation"));
        vo.setAdvice(report.getStr("advice"));
        List<AssessmentReportVO.DimensionScoreVO> dimensions = new ArrayList<>();
        JSONArray dimArray = report.getJSONArray("dimensions");
        if (dimArray != null) {
            for (int i = 0; i < dimArray.size(); i++) {
                JSONObject dimObj = dimArray.getJSONObject(i);
                AssessmentReportVO.DimensionScoreVO dim = new AssessmentReportVO.DimensionScoreVO();
                dim.setDimensionCode(dimObj.getStr("dimensionCode"));
                dim.setDimensionName(dimObj.getStr("dimensionName"));
                dim.setScore(dimObj.getInt("score"));
                dim.setMaxScore(dimObj.getInt("maxScore"));
                dimensions.add(dim);
            }
        }
        vo.setDimensions(dimensions);
        return vo;
    }

    /**
     * 构建 evaluate_result 报告JSON（等级、解读、建议、三维度得分）
     */
    private String buildReportJson(String level, String interpretation, String advice,
                                   int percent, ScoreResult result) {
        JSONObject report = new JSONObject();
        report.set("level", level);
        report.set("interpretation", interpretation);
        report.set("advice", advice);
        report.set("percent", percent);
        report.set("totalScore", result.total);
        report.set("maxScore", result.max);
        JSONArray dimArray = new JSONArray();
        for (Map.Entry<String, Integer> entry : result.dimScore.entrySet()) {
            String code = entry.getKey();
            JSONObject dimObj = new JSONObject();
            dimObj.set("dimensionCode", code);
            dimObj.set("dimensionName", DIMENSION_NAME_MAP.getOrDefault(code, code));
            dimObj.set("score", entry.getValue());
            dimObj.set("maxScore", result.dimMax.getOrDefault(code, 0));
            dimArray.add(dimObj);
        }
        report.set("dimensions", dimArray);
        return report.toString();
    }

    /**
     * 计分结果内部持有类
     */
    private static class ScoreResult {

        /**
         * 总分
         */
        private int total;

        /**
         * 满分
         */
        private int max;

        /**
         * 各维度得分（按计分规则中出现的顺序）
         */
        private Map<String, Integer> dimScore = new LinkedHashMap<>();

        /**
         * 各维度满分
         */
        private Map<String, Integer> dimMax = new LinkedHashMap<>();
    }
}
