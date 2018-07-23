package com.mmall.controller.backend;

import com.mmall.common.ServiceResponse;
import com.mmall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Created by hasee on 2018/4/27.
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping(value = "add_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse addCategory(String categoryName,
                                       @RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
//        使用AuthorityInterceptor完成了登陆校验和权限校验，重构代码
//        String loginToken = CookieUtil.readLoginToken(request);
//        if (StringUtils.isBlank(loginToken)) {
//            return ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
//        }
//        String jsonStr = RedisShardedPoolUtil.get(loginToken);
//        User user = JsonUtil.string2Obj(jsonStr, User.class);
//        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
//        if(response.isSuccess()){
//            return iCategoryService.addCategory(categoryName,parentId);
//        }
//        return response;

        return iCategoryService.addCategory(categoryName, parentId);
    }

    @RequestMapping(value = "update_category_Name.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse updateCategoryName(String categoryName, Integer categoryId) {

        return iCategoryService.updateCategoryName(categoryName, categoryId);
    }

    @RequestMapping(value = "get_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse getChildrenParallelCategory(@RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {

        return iCategoryService.getChildrenParallelCategory(parentId);
    }

    @RequestMapping(value = "get_all_category.do", method = RequestMethod.GET)
    @ResponseBody
    public ServiceResponse getAllChildrenCategory(@RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {

        return iCategoryService.getAllChildrenCategory(categoryId);
    }
}
