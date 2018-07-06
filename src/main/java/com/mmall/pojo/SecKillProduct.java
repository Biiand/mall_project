package com.mmall.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecKillProduct {
    private Integer id;

    private Integer productId;

    private BigDecimal price;

    private Integer stock;

    private Date startTime;

    private Date endTime;

    private Date createTime;

    private Date updateTime;

}
