package com.mmall.controller.backend;

import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.SecKillProduct;
import com.mmall.pojo.User;
import com.mmall.service.ISecKIllService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/manage/secKill/")
public class SecKillManageController {

    @Autowired
    IUserService userService;

    @Autowired
    ISecKIllService secKIllService;

    /**
     * 添加或修改秒杀商品
     * @param request
     * @param secKillProduct
     * @return
     */
    @RequestMapping(value = "save.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse add(HttpServletRequest request, SecKillProduct secKillProduct){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        ServiceResponse response = userService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            try {
                return secKIllService.saveOrUpdate(secKillProduct);
            } catch (Exception e) {
                return ServiceResponse.createByErrorMessage(e.getMessage());
            }
        }
        return response;
    }

    @RequestMapping(value = "list.do", method = RequestMethod.GET)
    @ResponseBody
    public ServiceResponse list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", defaultValue = "5") int pageSize) {
        return secKIllService.getSecKillList(pageNum, pageSize);
    }


}
