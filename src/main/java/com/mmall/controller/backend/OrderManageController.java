package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import com.mmall.vo.OrderVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by hasee on 2018/5/11.
 */
@Controller
@RequestMapping("/manage/order")
public class OrderManageController {


    @Autowired
    IOrderService iOrderService;

    @RequestMapping("get_order_list.do")
    @ResponseBody
    public ServiceResponse<PageInfo> getOrderList(@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                                  @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
//            管理员查询订单不需要用户id
        return iOrderService.manageOrderList(pageNum,pageSize);
    }

    @RequestMapping("get_order_detail.do")
    @ResponseBody
    public ServiceResponse<OrderVo> getOrderDetail(Long orderNo){

        return iOrderService.manageOrderDetail(orderNo);
    }

    @RequestMapping("search_order.do")
    @ResponseBody
    public ServiceResponse<PageInfo> searchOrder(Long orderNo,
                                                @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                                @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){

        return iOrderService.manageSearchOrder(orderNo,pageNum,pageSize);
    }

    @RequestMapping("send_out.do")
    @ResponseBody
    public ServiceResponse<OrderVo> sendOutGoods(Long orderNo){

        return iOrderService.manageSendOut(orderNo);
    }




}
