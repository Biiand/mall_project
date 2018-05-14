package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by hasee on 2018/5/2.
 */
@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    IProductService iProductService;

    @RequestMapping(value = "detail.do")
    @ResponseBody
    public ServiceResponse<ProductDetailVo> getProductDetail(Integer productId){
//        用户查看商品详情在未登录状态下也可以进行，所以就不进行权限校验了
        return iProductService.getProductDetail(productId);
    }

    @RequestMapping(value = "list.do")
    @ResponseBody
    public ServiceResponse<PageInfo> getProductList(@RequestParam(value = "keyword",required = false) String keyword,
                                                    @RequestParam(value = "categoryId",required = false) Integer categoryId,
                                                    @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                                    @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize,
                                                    @RequestParam(value = "orderBy",defaultValue = "") String orderBy){

        return iProductService.getProductByKeywordOrCategoryId(keyword, categoryId, pageNum, pageSize, orderBy);
    }
}
