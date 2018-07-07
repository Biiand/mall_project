package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import com.mmall.vo.CartVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by hasee on 2018/5/2.
 */
@Controller
@RequestMapping("/cart/")//定义模块的url
public class CartController {

    @Autowired
    ICartService iCartService;


    @RequestMapping(value = "add.do")
//    添加ResponseBody就告诉SpringMVC将返回的数据转换为json格式，前提是在RequestMapping中添加produces = "application/json;charset = UTF-8"属性，
//    或者在本例中在springMVC的配置文件中添加转换json的converter
//    不添加的话就是传统的会将返回值当作jsp的文件名进行处理
    @ResponseBody
    public ServiceResponse<CartVo> add(HttpServletRequest request, Integer productId, Integer count){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.addToCart(user.getId(),productId,count);
    }

    @RequestMapping("update.do")
    @ResponseBody
    public ServiceResponse<CartVo> update(HttpServletRequest request, Integer productId, Integer count){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.update(user.getId(),productId,count);
    }

    @RequestMapping("delete.do")
    @ResponseBody
    public ServiceResponse<CartVo> delete(HttpServletRequest request,String productIds){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.delete(user.getId(),productIds);
    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServiceResponse<CartVo> list(HttpServletRequest request){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.list(user.getId());
    }

    /**
     * 全选
     * @param request
     * @return
     */
    @RequestMapping("select_all.do")
    @ResponseBody
    public ServiceResponse<CartVo> selectAll(HttpServletRequest request){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnselect(user.getId(),Const.CartConstant.CHECKED,null);
    }

    /**
     * 全不选
     * @param request
     * @return
     */
    @RequestMapping("un_select_all.do")
    @ResponseBody
    public ServiceResponse<CartVo> unSelectAll(HttpServletRequest request){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnselect(user.getId(),Const.CartConstant.UNCHECKED,null);
    }

    /**
     * 单独选
     * @param request
     * @param productId
     * @return
     */
    @RequestMapping("select.do")
    @ResponseBody
    public ServiceResponse<CartVo> select(HttpServletRequest request,Integer productId){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnselect(user.getId(),Const.CartConstant.CHECKED,productId);
    }

    /**
     * 单独不选
     * @param request
     * @param productId
     * @return
     */
    @RequestMapping("un_select.do")
    @ResponseBody
    public ServiceResponse<CartVo> unSelect(HttpServletRequest request,Integer productId){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnselect(user.getId(),Const.CartConstant.UNCHECKED,productId);
    }

    /**
     * 获取购物车中的商品总数
     * @param request
     * @return
     */
    @RequestMapping("get_sum.do")
    @ResponseBody
    public ServiceResponse<Integer> getSumOfProducts(HttpServletRequest request){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if(user == null){
            return ServiceResponse.createBySuccess(0);
        }
        return iCartService.getSumOfProductsInCart(user.getId());
    }

}
