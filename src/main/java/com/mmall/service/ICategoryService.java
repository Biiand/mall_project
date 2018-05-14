package com.mmall.service;


import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Category;

import java.util.List;


/**
 * Created by hasee on 2018/4/27.
 */
public interface ICategoryService {
    ServiceResponse addCategory(String categoryName,Integer parentId);

    ServiceResponse updateCategoryName(String categoryName,Integer id);

    ServiceResponse<List<Category>> getChildrenParallelCategory(Integer parentId);

    ServiceResponse<List<Integer>> getAllChildrenCategory(Integer parentId);
}
