package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by hasee on 2018/4/24.
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;

    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse) {
        ServiceResponse<User> response = iUserService.login(username,password);
        if(response.isSuccess()){
            CookieUtil.writeLoginToken(httpServletResponse,session.getId());
            RedisShardedPoolUtil.setEx(session.getId(), JsonUtil.obj2String(response.getData()), Const.redisCacheExTime.SESSION_EXPIRE_TIME);
        }
        return response;
    }

    /**
     * 退出登陆，如果获取的loginToken为空，则表示未登录，同样提示退出成功
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> logout(HttpServletRequest request,HttpServletResponse response){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isNotBlank(loginToken)) {
            RedisShardedPoolUtil.delete(loginToken);
            CookieUtil.deleteLoginToken(request,response);
        }
        return ServiceResponse.createBySuccessMessage("成功退出登陆");
    }

    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> register(User user){
        return iUserService.register(user);
    }

    /**
     * 在用户进行注册输入用户名和邮箱时通过ajax实时的验证唯一性
     * @param type
     * @param value
     * @return
     */
    @RequestMapping(value = "check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> checkValid(String type,String value){
        return iUserService.checkValid(type,value);
    }

    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<User> getUserInfo(HttpServletRequest request){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isNotBlank(loginToken)) {
            String jsonStr = RedisShardedPoolUtil.get(loginToken);
            User user = JsonUtil.string2Obj(jsonStr, User.class);
            if(user != null){
                return ServiceResponse.createBySuccess(user);
            }
        }
        return ServiceResponse.createByErrorMessage("用户当前未登录，无法获取详细信息");
    }

    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> forgetGetQuestion(String username){
        return iUserService.selectQuestion(username);
    }

    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> forgetCheckAnswer(String username,String question,String answer){
        return iUserService.checkAnswer(username,question,answer);
    }

    /**
     * 忘记密码情况下的修改密码
     * @param username
     * @param newPassword
     * @param token
     * @return
     */
    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> forgetResetPassword(String username,String newPassword,String token){
        return iUserService.forgetResetPassword(username,newPassword,token);
    }

    /**
     * 登陆状态下的修改密码
     * @param request
     * @param oldPassword
     * @param newPassword
     * @return
     */
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> resetPassword(HttpServletRequest request,String oldPassword,String newPassword){
//        使用session进行一个用户是否登陆的校验
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if(user == null){
            return ServiceResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(user,oldPassword,newPassword);
    }

    @RequestMapping(value = "update_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<User> updateUserInfo(HttpServletRequest request,User user){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User currentUser = JsonUtil.string2Obj(jsonStr, User.class);
        if(currentUser == null){
            return ServiceResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId());
        ServiceResponse response = iUserService.updateUserInfo(user);
//        更新信息成功后更新Redis中的信息
        if(response.isSuccess()){
            String newJsonStr = JsonUtil.obj2String(response.getData());
            RedisShardedPoolUtil.setEx(loginToken, newJsonStr, Const.redisCacheExTime.SESSION_EXPIRE_TIME);
        }
        return response;
    }

    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<User> getInformation(HttpServletRequest request){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        if(user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登陆，需要强制登陆status = 10");
        }
        return iUserService.getUserInfo(user.getId());
    }
}
