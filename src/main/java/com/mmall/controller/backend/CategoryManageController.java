package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

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
    public ServiceResponse addCategory(HttpSession session, String categoryName,
                                       @RequestParam(value = "parentId",defaultValue = "0") Integer parentId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            return iCategoryService.addCategory(categoryName,parentId);
        }
        return response;
    }

    @RequestMapping(value = "update_category_Name.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse updateCategoryName(HttpSession session,String categoryName,Integer categoryId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            return iCategoryService.updateCategoryName(categoryName,categoryId);
        }
        return response;
    }

    @RequestMapping(value = "get_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse getChildrenParallelCategory(HttpSession session,
                                                       @RequestParam(value = "parentId",defaultValue = "0") Integer parentId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            return iCategoryService.getChildrenParallelCategory(parentId);
        }
        return response;
    }

    @RequestMapping(value = "get_all_category.do",method = RequestMethod.GET)
    @ResponseBody
    public ServiceResponse getAllChildrenCategory(HttpSession session,
                                               @RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        ServiceResponse response = iUserService.checkAdminBeforeOperate(user);
        if(response.isSuccess()){
            return iCategoryService.getAllChildrenCategory(categoryId);
        }
        return response;
    }
}
