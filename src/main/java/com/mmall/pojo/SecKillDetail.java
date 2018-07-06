package com.mmall.pojo;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecKillDetail {
    private Integer secKillId;

    private Integer userId;

    private Integer status;

    private Date createTime;

    public SecKillDetail(Integer secKillId, Integer userId) {
        this.secKillId = secKillId;
        this.userId = userId;
    }

}
