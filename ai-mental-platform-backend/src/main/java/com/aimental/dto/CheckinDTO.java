package com.aimental.dto;

/**
 * 每日打卡请求DTO
 * 对应接口：POST /api/checkin
 */
public class CheckinDTO {

    /**
     * 心情描述（可选，最长200字）
     */
    private String moodDesc;

    public String getMoodDesc() {
        return moodDesc;
    }

    public void setMoodDesc(String moodDesc) {
        this.moodDesc = moodDesc;
    }
}
