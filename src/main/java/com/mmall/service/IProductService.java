package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

/**
 * Created by hasee on 2018/4/29.
 */
public interface IProductService {

    ServiceResponse saveOrUpdateProduct(Product product);

    ServiceResponse<String> setSaleStatus(Integer productId,Integer status);

    ServiceResponse<ProductDetailVo> manageProductDetail(Integer productId);

    ServiceResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize);

    ServiceResponse<PageInfo> searchProduct(String productName,Integer productId,Integer pageNum, Integer pageSize);

    ServiceResponse<ProductDetailVo> getProductDetail(Integer productId);

    ServiceResponse<PageInfo> getProductByKeywordOrCategoryId(String keyword,Integer categoryId,Integer pageNum,Integer pageSize,String orderBy);


}
