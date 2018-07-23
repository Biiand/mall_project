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
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hasee on 2018/5/7.
 */
@Controller
@RequestMapping("/order/")
@Slf4j
public class OrderController {

    @Autowired
    private IOrderService iOrderService;

    @RequestMapping(value = "create.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse create(HttpServletRequest request, Integer shippingId) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.create(user.getId(), shippingId);
    }

    @RequestMapping(value = "cancel.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse cancel(HttpServletRequest request, Long orderNo) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancel(user.getId(), orderNo);
    }

    /**
     * 在购物车确认购买商品，进行创建订单的页面展示商品信息
     * @param request
     * @return
     */
    @RequestMapping(value = "get_product_info_in_order.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse getProductInfoInOrder(HttpServletRequest request) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getProductInfoInOrder(user.getId());
    }

    @RequestMapping(value = "get_order_detail.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse getOrderDetail(HttpServletRequest request, Long orderNo) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderDetail(user.getId(), orderNo);
    }

    @RequestMapping(value = "get_order_list.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<PageInfo> getOrderList(HttpServletRequest request,
                                                  @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderList(user.getId(), pageNum, pageSize);
    }

    /**
     * 使用支付宝当面付完成付款
     * @param request
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "pay.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse pay(HttpServletRequest request, Long orderNo) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
//        获取名为upload的文件夹的全路径用于接收生成的二维码
        String path = request.getServletContext().getRealPath("upload");
        return iOrderService.pay(user.getId(), orderNo, path);
    }

    /**
     * 支付宝回调的处理
     *
     * @param request 来自支付宝的请求
     * @return
     */
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request) {
        //支付宝的回调数据都在request里，先将数据取出来，getParameterMap()的返回类型是Map<String,String[]>
        Map requestMap = request.getParameterMap();
        Iterator iterator = requestMap.keySet().iterator();
        Map<String, String> params = new HashMap<>();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String[] values = (String[]) requestMap.get(key);
            StringBuilder valueStr = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                //拼接最后一个元素时就不再在后面添加“,”
                valueStr = (values.length - 1 == i) ? valueStr.append(values[i]) : valueStr.append(values[i]).append(",");
            }
            params.put(key, valueStr.toString());
        }
        log.info("支付宝回调参数：sign:{},trade_status:{},参数:{}", params.get("sign"), params.get("trade_status"), params.toString());

        //非常重要，使用公钥验证支付宝回调的正确性，确保回调是来自支付宝，还要避免重复通知
        //1.验证支付宝的签名
        //根据支付宝给的文档，验签前要除去sign和sign_type，在验签的方法中移除了sign，所以还需要手动移除sign_type
        params.remove("sign_type");
        try {
            //调用支付宝的验签方法
            boolean alipaySignCheck = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            if (!alipaySignCheck) {
                log.info("验签未通过：", !alipaySignCheck);
                return ServiceResponse.createByErrorMessage("验签未通过");
            }
        } catch (AlipayApiException e) {
            log.error("支付宝回调验签异常", e);
        }

        //验签通过后校验返回的数据的状态，做处理后按文档要求给支付宝返回success或其它字符串作为响应，
        // 必须响应，否则支付宝在25小时内发送8次回调直至超时
        ServiceResponse response = iOrderService.verifyParams(params);
        if (response.isSuccess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;//给支付宝返回"success"字符串表示交易成功
        } else {
            return Const.AlipayCallback.RESPONSE_FAILED;//返回非"success"字符串表示交易失败
        }
    }

    /**
     * 前端查询订单是否付款成功，只查询状态，不返回数据
     * @param request
     * @param orderNo
     * @return
     */
    @RequestMapping("query_pay_status.do")
    @ResponseBody
    public ServiceResponse queryPayStatus(HttpServletRequest request, Long orderNo) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iOrderService.queryPayStatus(user.getId(), orderNo).isSuccess()) {
            return ServiceResponse.createBySuccess(true);
        }
        return ServiceResponse.createBySuccess(false);
    }
}
