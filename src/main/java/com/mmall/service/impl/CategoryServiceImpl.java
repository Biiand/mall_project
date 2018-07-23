package com.mmall.service.impl;

import com.mmall.common.ServiceResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hasee on 2018/4/27.
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public ServiceResponse addCategory(String categoryName, Integer parentId) {
        if (StringUtils.isBlank(categoryName) || parentId == null) {
            return ServiceResponse.createByErrorMessage("添加品类参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        int resultCount = categoryMapper.insert(category);
        if (resultCount > 0) {
            return ServiceResponse.createByErrorMessage("添加成功");
        }
        return ServiceResponse.createByErrorMessage("添加失败");
    }

    @Override
    public ServiceResponse updateCategoryName(String categoryName, Integer categoryId) {
        if (StringUtils.isBlank(categoryName) || categoryId == null) {
            return ServiceResponse.createByErrorMessage("修改品类名称参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setId(categoryId);
        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount > 0) {
            return ServiceResponse.createBySuccessMessage("修改品类名称成功");
        }
        return ServiceResponse.createByErrorMessage("修改品类名称失败");
    }

    @Override
    public ServiceResponse<List<Category>> getChildrenParallelCategory(Integer parentId) {
        List<Category> categoryList = categoryMapper.selectChildrenCategoryByParentId(parentId);
        if (categoryList != null && categoryList.size() > 0) {
            return ServiceResponse.createBySuccess(categoryList);
        }
        return ServiceResponse.createByErrorMessage("该分类下没有子分类");
    }

    /**
     * 递归查询子节点
     * ServiceResponse没有指定泛型是因为在不确定返回的泛型类型或返回的泛型有多种时时就可以不指定，通常只有在确定返回值的泛型类型时才指定
     *
     * @param categoryId
     * @return
     */
    @Override
    public ServiceResponse<List<Integer>> getAllChildrenCategory(Integer categoryId) {
        Set<Category> categorySet = new HashSet<>();//用于存储所有子品类的集合
        findChildrenCategory(categorySet, categoryId);

        //这里最后只返回了一个id的集合，所以查询的时候也可以只将id查出来，
        //只返回id是因为这个方法实在商品查询的时候被调用，只需要使用id
        List<Integer> categoryIdList = new ArrayList<>();
        if (categorySet != null) {
            for (Category category : categorySet) {
                categoryIdList.add(category.getId());
            }
        }
        return ServiceResponse.createBySuccess(categoryIdList);
    }

    /**
     * 递归算法获取一个商品分类下的所有子分类
     * 结束递归的条件：当categoryList为null的时候执行return结束该层递归
     * @param categorySet
     * @param categoryId
     * @return
     */
    private Set<Category> findChildrenCategory(Set<Category> categorySet, Integer categoryId) {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            categorySet.add(category);
        }
        //查找子节点
        List<Category> categoryList = categoryMapper.selectChildrenCategoryByParentId(categoryId);
        //categoryList == null时结束递归
        for (Category categoryItem : categoryList) {
            findChildrenCategory(categorySet, categoryItem.getId());
        }
        return categorySet;
    }
}
