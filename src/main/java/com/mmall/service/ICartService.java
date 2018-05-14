package com.mmall.service;

import com.mmall.common.ServiceResponse;
import com.mmall.vo.CartVo;

/**
 * Created by hasee on 2018/5/2.
 */
public interface ICartService {

    ServiceResponse<CartVo> addToCart(Integer userId, Integer productId, Integer count);

    ServiceResponse<CartVo> update(Integer userId, Integer productId, Integer count);

    ServiceResponse<CartVo> delete(Integer userId, String productIds);

    ServiceResponse<CartVo> list(Integer userId);

    ServiceResponse<CartVo> selectOrUnselect(Integer userId,Integer checked,Integer productId);

    ServiceResponse<Integer> getSumOfProductsInCart(Integer userId);
}
