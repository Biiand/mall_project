package com.mmall.dto;

/**
 * 向前端暴露秒杀是否开始
 */
public class Exposer {
    private boolean exposed;

    private Integer secKillId;

    private String md5Token;

    //服务器当前时间（毫秒），返回前端后交给前端处理成时间格式
    private long currentTime;

    private long startTime;

    private long endTime;

    public Exposer(boolean exposed, Integer secKillId) {
        this.exposed = exposed;
        this.secKillId = secKillId;
    }

    public Exposer(boolean exposed, Integer secKillId, long currentTime, long startTime, long endTime) {
        this.exposed = exposed;
        this.secKillId = secKillId;
        this.currentTime = currentTime;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Exposer(boolean exposed, Integer secKillId, String md5Token) {
        this.exposed = exposed;
        this.secKillId = secKillId;
        this.md5Token = md5Token;
    }

    public boolean isExposed() {
        return exposed;
    }

    public void setExposed(boolean exposed) {
        this.exposed = exposed;
    }

    public Integer getSecKillId() {
        return secKillId;
    }

    public void setSecKillId(Integer secKillId) {
        this.secKillId = secKillId;
    }

    public String getMd5Token() {
        return md5Token;
    }

    public void setMd5Token(String md5Token) {
        this.md5Token = md5Token;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
