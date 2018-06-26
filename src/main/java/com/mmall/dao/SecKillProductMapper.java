package com.mmall.dao;

import com.mmall.pojo.SecKillProduct;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SecKillProductMapper {

    List<SecKillProduct> selectList();

    SecKillProduct selectById(Integer secKillId);

    int reduceStockById(@Param("secKillId") Integer secKillId, @Param("currentTime")Date currentTime);

    int updateByPrimaryKeySelective(SecKillProduct secKillProduct);

    int insert(SecKillProduct secKillProduct);
}
