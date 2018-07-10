package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.vo.OrderVo;

import java.util.Map;

/**
 * Created by hasee on 2018/5/7.
 */
public interface IOrderService {

    ServiceResponse create(Integer userId,Integer shippingId);

    ServiceResponse pay(Integer userId,Long orderNo,String path);

    /**
     * 校验支付宝回调的参数
     * @param params
     * @return
     */
    ServiceResponse verifyParams(Map<String,String> params);

    /**
     * 查询订单支付状态
     * @param userId
     * @param orderNo
     * @return
     */
    ServiceResponse queryPayStatus(Integer userId,Long orderNo);

    ServiceResponse cancel(Integer userId,Long orderNo);

    ServiceResponse getProductInfoInOrder(Integer userId);

    ServiceResponse getOrderDetail(Integer userId,Long orderNo);

    /**
     * 获取用户的订单列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    ServiceResponse<PageInfo> getOrderList(Integer userId,int pageNum,int pageSize);

//    以下是后台管理员使用的方法
    ServiceResponse<PageInfo> manageOrderList(int pageNum,int pageSize);

    ServiceResponse<OrderVo> manageOrderDetail(Long orderNo);

    ServiceResponse<PageInfo> manageSearchOrder(Long orderNo,int pageNum,int pageSize);

    ServiceResponse manageSendOut(Long orderNo);

//    定时关闭超时未支付的订单的方法
    void closeOrder(int hour);

}
