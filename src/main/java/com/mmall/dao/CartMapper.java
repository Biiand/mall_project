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

//    但传递多个参数时需要是用@Param注解为每一个参数起一个别名，因为java中方法中传入的参数是形参，
//    java保存形参是以arg0,arg1这样的名字来保存，这样传给sql时就没办法进行对应的绑定，会报bindingException
    Cart selectByUserIdAndProductId(@Param("userId") Integer userId,@Param("productId") Integer productId);

    List<Cart> selectCartByUserId(Integer userId);

    int selectProductCheckedStatusByUserId(Integer userId);

    int deleteByUserIdAndProductId(@Param("userId") Integer userId,@Param("productIdList") List<String> productIdList);

    int checkedOrUncheckedProduct(@Param("userId") Integer userId,@Param("checked") Integer checked,@Param("productId") Integer productId);

//    该方法对应的返回sum()函数的值，如果是null会报错，因为null不能赋值给基本类型，
//    有两种解决方式，1.将int换为Integer；2.在sql中对sum()函数使用ifnull()函数，设定为null时的返回值；
    int selectCartProductCount(Integer userId);

    List<Cart> selectSelectedItemsByUserId(Integer userId);
}