package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hasee on 2018/5/7.
 */
@Controller
@RequestMapping("/order/")
public class OrderController {

    @Autowired
    private IOrderService iOrderService;

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @RequestMapping("create.do")
    @ResponseBody
    public ServiceResponse create(HttpSession session,Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.create(user.getId(),shippingId);
    }

    @RequestMapping("cancel.do")
    @ResponseBody
    public ServiceResponse cancel(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancel(user.getId(),orderNo);
    }

    /**
     * 在购物车确认购买商品，进行创建订单的页面展示商品信息
     * @param session
     * @return
     */
    @RequestMapping("get_product_info_in_order.do")
    @ResponseBody
    public ServiceResponse getProductInfoInOrder(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getProductInfoInOrder(user.getId());
    }

    @RequestMapping("get_order_detail.do")
    @ResponseBody
    public ServiceResponse getOrderDetail(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderDetail(user.getId(),orderNo);
    }

    @RequestMapping("get_order_list.do")
    @ResponseBody
    public ServiceResponse<PageInfo> getOrderList(HttpSession session,
                                                  @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                                  @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderList(user.getId(),pageNum,pageSize);
    }

    @RequestMapping("pay.do")
    @ResponseBody
    public ServiceResponse pay(HttpSession session, HttpServletRequest request,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
//        获取名为upload的文件夹的全路径用于接收生成的二维码
        String path = request.getServletContext().getRealPath("upload");
        return iOrderService.pay(user.getId(),orderNo,path);
    }

    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
//        支付宝的回调数据都在request里，先将数据取出来，getParameterMap()的返回类型是Map<String,String[]>
        Map requestMap = request.getParameterMap();
        Iterator iterator = requestMap.keySet().iterator();
        Map<String,String> params = new HashMap<>();
        while(iterator.hasNext()){
            String key = (String) iterator.next();
            String[] values = (String[]) requestMap.get(key);
            String valueStr = "";
            for(int i = 0 ; i < values.length ; i++){
//                拼接最后一个元素时就不再在后面添加“,”
                valueStr = (values.length - 1 == i) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(key,valueStr);
        }
        logger.info("支付宝回调参数：sign:{},trade_status:{},参数:{}",params.get("sign"),params.get("trade_status"),params.toString());

//        非常重要，验证支付宝回调的正确性，确保回调是来自支付宝，还要避免重复通知
//        1.验证支付宝的签名
//        根据支付宝给的文档，在返回的参数中要对除了sign和sign_type之外的值进行验签，在验签的方法中移除了sign，所以还需要手动移除sign_type
        params.remove("sign_type");
        try {
//            调用支付宝的验签方法
            boolean alipaySignCheck = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if(!alipaySignCheck){
                logger.info("验签未通过：",!alipaySignCheck);
                return ServiceResponse.createByErrorMessage("验签未通过");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝回调验签异常",e);
        }

//        验签通过后校验返回的数据的状态，做处理后按文档要求给支付宝返回success或其它字符串作为响应
        ServiceResponse response = iOrderService.verifyParams(params);
        if(response.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }else{
            return Const.AlipayCallback.RESPONSE_FAILED;
        }
    }

    /**
     * 前端查询订单是否付款成功，只查询状态，不返回数据
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("query_pay_status.do")
    @ResponseBody
    public ServiceResponse queryPayStatus(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(iOrderService.queryPayStatus(user.getId(),orderNo).isSuccess()){
            return ServiceResponse.createBySuccess(true);
        }
        return ServiceResponse.createBySuccess(false);
    }
}
