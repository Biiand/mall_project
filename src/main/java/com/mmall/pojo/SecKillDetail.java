package com.mmall.pojo;

import java.util.Date;

public class SecKillDetail {
    private Integer secKillId;

    private Integer userId;

    private Integer status;

    private Date createTime;

    public SecKillDetail(Integer secKillId, Integer userId) {
        this.secKillId = secKillId;
        this.userId = userId;
    }

    public SecKillDetail(Integer secKillId, Integer userId, Integer status, Date createTime) {
        this.secKillId = secKillId;
        this.userId = userId;
        this.status = status;
        this.createTime = createTime;
    }

    public Integer getSecKillId() {
        return secKillId;
    }

    public void setSecKillId(Integer secKillId) {
        this.secKillId = secKillId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
