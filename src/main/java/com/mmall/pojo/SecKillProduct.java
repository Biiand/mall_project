package com.mmall.pojo;

import java.math.BigDecimal;
import java.util.Date;

public class SecKillProduct {
    private Integer id;

    private Integer productId;

    private BigDecimal price;

    private Integer stock;

    private Date startTime;

    private Date endTime;

    private Date createTime;

    private Date updateTime;

    public SecKillProduct(Integer id, Integer productId, BigDecimal price, Integer stock, Date startTime, Date endTime, Date createTime, Date updateTime) {
        this.id = id;
        this.productId = productId;
        this.price = price;
        this.stock = stock;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
