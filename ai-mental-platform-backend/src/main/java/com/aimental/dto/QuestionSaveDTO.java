package com.aimental.dto;

/**
 * 管理员新增/编辑量表题目请求DTO
 * optionJson：题目选项JSON数组；scoreRule：计分规则JSON（维度+分值映射）
 * 参数格式校验在Service层执行
 */
public class QuestionSaveDTO {

    /**
     * 题目内容（1-2000字符）
     */
    private String questionContent;

    /**
     * 选项JSON，格式示例：
     * [{"value":1,"label":"完全不符合"},{"value":2,"label":"不太符合"}, ...]
     */
    private String optionJson;

    /**
     * 计分规则JSON，格式示例：
     * {"dimension":"toughness","scoreMap":{"1":1,"2":2,"3":3,"4":4,"5":5}}
     * dimension：维度标识（toughness坚韧性/strength力量性/optimism乐观性）
     * scoreMap：选项值到分值的映射（可省略，省略时选项值即得分）
     */
    private String scoreRule;

    /**
     * 排序号（缺省为0）
     */
    private Integer sortNum;

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
