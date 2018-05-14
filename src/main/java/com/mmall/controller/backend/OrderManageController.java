package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by hasee on 2018/5/11.
 */
@Controller
@RequestMapping("/manage/order")
public class OrderManageController {

    @Autowired
    IUserService iUserService;

    @Autowired
    IOrderService iOrderService;

    @RequestMapping("get_order_list.do")
    @ResponseBody
    public ServiceResponse<PageInfo> getOrderList(HttpSession session,
                                                  @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                                  @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
//            管理员查询订单不需要用户id
            return iOrderService.manageOrderList(pageNum,pageSize);
        }
        return response;
    }

    @RequestMapping("get_order_detail.do")
    @ResponseBody
    public ServiceResponse<OrderVo> getOrderDetail(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            return iOrderService.manageOrderDetail(orderNo);
        }
        return response;
    }

    @RequestMapping("search_order.do")
    @ResponseBody
    public ServiceResponse<PageInfo> searchOrder(HttpSession session, Long orderNo,
                                                @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                                @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
//            管理员查询订单不需要用户id
            return iOrderService.manageSearchOrder(orderNo,pageNum,pageSize);
        }
        return response;
    }

    @RequestMapping("send_out.do")
    @ResponseBody
    public ServiceResponse<OrderVo> sendOutGoods(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            return iOrderService.manageSendOut(orderNo);
        }
        return response;
    }




}
