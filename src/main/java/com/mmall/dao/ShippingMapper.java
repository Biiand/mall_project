package com.mmall.dao;

import com.mmall.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    Shipping selectByPrimaryKeyAndUserId(@Param("userId") Integer userId,@Param("PrimaryId") Integer shippingId);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    int updateByPrimaryKeyAndUserId(Shipping record);

    int deleteByUserIdAndPrimaryKey(@Param("userId") Integer userId,@Param("primaryKey") Integer shippingId);

    List<Shipping> selectByUserId(Integer userId);
}