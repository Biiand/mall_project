package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.ISecKIllService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/secKill/")
public class SecKillController {

    @Autowired
    ISecKIllService iSecKIllService;

    /**
     * 获取秒杀产品列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "list.do", method = RequestMethod.GET)
    @ResponseBody
    public ServiceResponse list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", defaultValue = "5") int pageSize) {
        return iSecKIllService.getSecKillList(pageNum, pageSize);
    }

    /**
     * 获取秒杀的暴露地址
     * @param session
     * @param secKillId
     * @return
     */
    @RequestMapping(value = "exposer.do", method = RequestMethod.GET)
    @ResponseBody
    public ServiceResponse getExposer(HttpSession session, Integer secKillId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iSecKIllService.getExposer(secKillId, user.getId());
    }

    /**
     * 执行秒杀，验证秒杀条件
     * @param session
     * @param secKillId
     * @param MD5Token
     * @return
     */
    @RequestMapping(value = "execution.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse execution(HttpSession session, Integer secKillId, String MD5Token) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iSecKIllService.secKillExecution(secKillId, user.getId(), MD5Token);
    }
}
