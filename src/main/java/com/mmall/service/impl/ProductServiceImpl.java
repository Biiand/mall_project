package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Order;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by hasee on 2018/4/29.
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    //    平级调用，service调service
    @Autowired
    private ICategoryService iCategoryService;

    @Override
    public ServiceResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
//            上传图片的第一张设为主图
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String[] subImagesArray = product.getSubImages().split(",");
                if (subImagesArray.length > 0) {
                    product.setMainImage(subImagesArray[0]);
                }
            }

            int rowCount;
            if (product.getId() != null) {
                rowCount = productMapper.updateByPrimaryKeySelective(product);
                if (rowCount > 0) {
                    return ServiceResponse.createBySuccessMessage("修改商品信息成功");
                }
                return ServiceResponse.createByErrorMessage("修改商品信息失败");
            } else {
                rowCount = productMapper.insert(product);
                if (rowCount > 0) {
                    return ServiceResponse.createBySuccessMessage("新增商品信息成功");
                }
                return ServiceResponse.createByErrorMessage("新增商品信息失败");
            }
        }
        return ServiceResponse.createByErrorMessage("新增或修改商品信息参数错误");
    }

    @Override
    public ServiceResponse<String> setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
//        为了在修改状态时也记录下修改时间，将updateTime设为非空
        product.setUpdateTime(new Date());
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if (rowCount > 0) {
            return ServiceResponse.createBySuccessMessage("修改商品销售状态成功");
        }
        return ServiceResponse.createByErrorMessage("修改商品销售状态失败");
    }

    @Override
    public ServiceResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServiceResponse.createByErrorMessage("未找到此商品");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServiceResponse.createBySuccess(productDetailVo);
    }


    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();

        productDetailVo.setId(product.getId());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setName(product.getName());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null) {
            productDetailVo.setParentCategoryId(0);//默认父节点为0
        } else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }

    @Override
    public ServiceResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.selectList();

        List<ProductListVo> productListVoList = new ArrayList<>();
        for (Product productItem : productList) {
            productListVoList.add(assembleProductListVo(productItem));
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServiceResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServiceResponse<PageInfo> searchProduct(String productName, Integer productId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuilder("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectProductByNameOrId(productName, productId);
        List<ProductListVo> productListVoList = new ArrayList<>();
        for (Product productItem : productList) {
            productListVoList.add(assembleProductListVo(productItem));
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServiceResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServiceResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if (productId == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServiceResponse.createByErrorMessage("未找到此商品");
        }
        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServiceResponse.createByErrorMessage("该商品已下架");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServiceResponse.createBySuccess(productDetailVo);
    }

    @Override
    public ServiceResponse<PageInfo> getProductByKeywordOrCategoryId(String keyword, Integer categoryId,
                                                                     Integer pageNum, Integer pageSize, String orderBy) {
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        List<Integer> categoryIdList = new ArrayList<>();

        if (categoryId != null) {
//            判断是否有该产品分类
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
//            在没找到产品分类和没有关键字的时候返回一个空的结果集，为了和前端的展示逻辑匹配，也要进行分页
//          根据条件没查到任何数据时返回一个空的PageInfo给前端，不返回错误信息可以认为这种情况不是错误，只是没命中数据，注意也要按照分页的要求来进行返回，
            if (category == null && StringUtils.isBlank(keyword)) {
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVo> productListVoList = new ArrayList<>();
                PageInfo pageInfo = new PageInfo(productListVoList);
                return ServiceResponse.createBySuccess(pageInfo);
            }

            categoryIdList = iCategoryService.getAllChildrenCategory(categoryId).getData();
        }

        if (StringUtils.isNotBlank(keyword)) {
            keyword = new StringBuilder("%").append(keyword).append("%").toString();
        }

//        开始分页处理
        PageHelper.startPage(pageNum, pageSize);

//        排序处理,先判断客户端传来的排序方式是否为空和是否是约定好的排序方式，
//            使用PageHelper插件进行排序就不需要再在sql上动态的添加排序的值
        if (StringUtils.isNotBlank(orderBy) && Const.ProductListOrderBy.PRICE_DESC_ASC.contains(orderBy)) {
//            设置的排序常量的格式为“price_desc”
            String[] orderByArray = orderBy.split("_");
//            在PageHelper中传入排序的值，orderBy方法中参数的格式："字段名 升降序"
            PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
        }

//        获取产品的集合，keyword传入之前进行非空校验，因为上面对字符串进行拼接的情况是针对非空进行的，所以传入之前还要进行判断，如果是空字符串就传入null
//        ，否则拼接出的sql会查不出数据；categoryIdList是在category != null的条件下才能有数据，所以也要进行判断,isEmpty()即判断size是否为0
//        商品查询的核心语句
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword) ? null : keyword,
                categoryIdList.isEmpty() ? null : categoryIdList);

        List<ProductListVo> productListVoList = new ArrayList<>();
        for (Product productItem : productList) {
            productListVoList.add(assembleProductListVo(productItem));
        }

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServiceResponse.createBySuccess(pageInfo);
    }


    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();

        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setStatus(product.getStatus());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.happymmall.com/"));
        return productListVo;
    }
}
