package com.mmall.dao;

import com.mmall.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    List<Product> selectList();

    List<Product> selectProductByNameOrId(@Param("productName") String productName,@Param("productId") Integer productId);

    List<Product> selectByNameAndCategoryIds(@Param("productName") String keyword,@Param("categoryIdList") List<Integer> categoryIdList);

    List<Product> selectById(List<Integer> list);

//    返回值使用包装类，防止未查询到记录时返回null时出现类型不匹配而报错
    Integer selectStockById(int productId);

    int updateStockById(@Param("productId")Integer productId,@Param("quantity") Integer quantity);


}