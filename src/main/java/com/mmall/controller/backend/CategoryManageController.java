package com.mmall.controller.backend;

import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
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

/**
 * Created by hasee on 2018/4/27.
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping(value = "add_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse addCategory(HttpServletRequest request, String categoryName,
                                       @RequestParam(value = "parentId",defaultValue = "0") Integer parentId){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            return iCategoryService.addCategory(categoryName,parentId);
        }
        return response;
    }

    @RequestMapping(value = "update_category_Name.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse updateCategoryName(HttpServletRequest request ,String categoryName,Integer categoryId){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            return iCategoryService.updateCategoryName(categoryName,categoryId);
        }
        return response;
    }

    @RequestMapping(value = "get_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse getChildrenParallelCategory(HttpServletRequest request,
                                                       @RequestParam(value = "parentId",defaultValue = "0") Integer parentId){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            return iCategoryService.getChildrenParallelCategory(parentId);
        }
        return response;
    }

    @RequestMapping(value = "get_all_category.do",method = RequestMethod.GET)
    @ResponseBody
    public ServiceResponse getAllChildrenCategory(HttpServletRequest request,
                                               @RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isBlank(loginToken)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String jsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(jsonStr, User.class);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            return iCategoryService.getAllChildrenCategory(categoryId);
        }
        return response;
    }
}
