package com.aimental.vo;

import com.aimental.entity.ScaleQuestion;

/**
 * 管理端量表题目VO（返回optionJson与scoreRule原始配置，便于题目维护）
 */
public class AdminQuestionVO {

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 所属量表ID
     */
    private Long scaleId;

    /**
     * 题目内容
     */
    private String questionContent;

    /**
     * 选项JSON
     */
    private String optionJson;

    /**
     * 计分规则JSON（维度+分值映射）
     */
    private String scoreRule;

    /**
     * 排序号
     */
    private Integer sortNum;

    /**
     * 由题目实体转换为VO
     *
     * @param question 题目实体
     * @return 题目VO
     */
    public static AdminQuestionVO of(ScaleQuestion question) {
        AdminQuestionVO vo = new AdminQuestionVO();
        vo.setQuestionId(question.getQuestionId());
        vo.setScaleId(question.getScaleId());
        vo.setQuestionContent(question.getQuestionContent());
        vo.setOptionJson(question.getOptionJson());
        vo.setScoreRule(question.getScoreRule());
        vo.setSortNum(question.getSortNum());
        return vo;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getScaleId() {
        return scaleId;
    }

    public void setScaleId(Long scaleId) {
        this.scaleId = scaleId;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
    }

    public String getOptionJson() {
        return optionJson;
    }

    public void setOptionJson(String optionJson) {
        this.optionJson = optionJson;
    }

    public String getScoreRule() {
        return scoreRule;
    }

    public void setScoreRule(String scoreRule) {
        this.scoreRule = scoreRule;
    }

    public Integer getSortNum() {
        return sortNum;
    }

    public void setSortNum(Integer sortNum) {
        this.sortNum = sortNum;
    }
}
