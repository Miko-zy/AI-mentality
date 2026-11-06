package com.aimental.dto;

import java.util.List;

/**
 * 评估作答提交请求DTO
 * 中途暂停保存（PUT answers）与最终提交计分（POST submit）共用此结构
 * answers：每项为"题目ID + 所选选项值"
 */
public class AnswerSubmitDTO {

    /**
     * 作答列表
     */
    private List<AnswerItem> answers;

    public List<AnswerItem> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerItem> answers) {
        this.answers = answers;
    }

    /**
     * 单题作答项
     */
    public static class AnswerItem {

        /**
         * 题目ID
         */
        private Long questionId;

        /**
         * 所选选项值（与题目option_json中value对应）
         */
        private Integer optionValue;

        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public Integer getOptionValue() {
            return optionValue;
        }

        public void setOptionValue(Integer optionValue) {
            this.optionValue = optionValue;
        }
    }
}
