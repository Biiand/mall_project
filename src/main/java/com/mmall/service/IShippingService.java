package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Shipping;

/**
 * Created by hasee on 2018/5/3.
 */
public interface IShippingService {

    ServiceResponse add(Integer userId, Shipping shipping);

    ServiceResponse delete(Integer userId,Integer shippingId);

    ServiceResponse update(Integer userId, Shipping shipping);

    ServiceResponse getDetail(Integer userId,Integer shippingId);

    ServiceResponse<PageInfo> getList(Integer userId, Integer pageNum, Integer pageSize);
}
