package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectByUserIdAndProductId(@Param("userId") Integer userId,@Param("productId") Integer productId);

    List<Cart> selectCartByUserId(Integer userId);

    int selectProductCheckedStatusByUserId(Integer userId);

    int deleteByUserIdAndproductId(@Param("userId") Integer userId,@Param("productIdList") List<String> productIdList);

    int checkedOrUncheckedProduct(@Param("userId") Integer userId,@Param("checked") Integer checked,@Param("productId") Integer productId);

//    该方法对应的返回sum()函数的值，如果是null会报错，因为null不能赋值给基本类型，
//    有两种解决方式，1.将int换为Integer；2.在sql中对sum()函数使用ifnull()函数，设定为null时的返回值；
    int selectCartProductCount(Integer userId);

    List<Cart> selectSelectedItemsByUserId(Integer userId);
}