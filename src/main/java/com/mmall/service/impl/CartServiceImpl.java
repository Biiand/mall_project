package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by hasee on 2018/5/2.
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    CartMapper cartMapper;

    @Autowired
    ProductMapper productMapper;

    @Override
    public ServiceResponse<CartVo> addToCart(Integer userId,Integer productId,Integer count){
        if(productId == null || count == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdAndProductId(userId,productId);
        if(cart == null){
            Cart cartItem = new Cart();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.CartConstant.CHECKED);
            cartMapper.insert(cartItem);
        }else{
            count += cart.getQuantity();
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKey(cart);
        }
        return this.list(userId);
    }

    @Override
    public ServiceResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        if(productId == null || count == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdAndProductId(userId,productId);
        if(cart != null){
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKey(cart);
        }
        return this.list(userId);
    }

    /**
     *
     * @param userId
     * @param productIds 用于同时删除多个商品时传递多个产品的id，和前端约定好格式为"id1,id2,..."
     * @return
     */
    @Override
    public ServiceResponse<CartVo> delete(Integer userId, String productIds) {
        if(StringUtils.isBlank(productIds)){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
//        使用Guaua提供的工具类Splitter对字符串进行分割并转换为集合
        List<String> productIdList = Splitter.on(",").splitToList(productIds);
        cartMapper.deleteByUserIdAndproductId(userId,productIdList);
        return this.list(userId);
    }

    @Override
    public ServiceResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServiceResponse.createBySuccess(cartVo);
    }

    @Override
    public ServiceResponse<CartVo> selectOrUnselect(Integer userId, Integer checked, Integer productId) {
        cartMapper.checkedOrUncheckedProduct(userId,checked,productId);
        return this.list(userId);
    }

    @Override
    public ServiceResponse<Integer> getSumOfProductsInCart(Integer userId) {
        return ServiceResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }

    /**
     * 获取前端需要展示的购物车相关的状态信息，产品信息，计算勾选的商品的总价，控制添加进购物车的产品数量不超过库存
     * @param userId
     * @return
     */
    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();

        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = new ArrayList<>();

        BigDecimal cartTotalPrice = new BigDecimal("0");

        if(CollectionUtils.isNotEmpty(cartList)){
            for(Cart cartItem : cartList){
                CartProductVo cartProductVo = new CartProductVo();

                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(cartItem.getUserId());
                cartProductVo.setProductId(cartItem.getProductId());

                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if(product != null){
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductStock(product.getStock());
//                    判断库存
                    int availableQuantity = 0;
                    if(product.getStock() >= cartItem.getQuantity()){
                        availableQuantity = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.CartConstant.LIMIT_NUM_SUCCESS);
                    }else{
                        availableQuantity = product.getStock();
                        cartProductVo.setLimitQuantity(Const.CartConstant.LIMIT_NUM_FAIL);
//                        购物车中的商品数量大于库存，此时需要更新数据库中购物车表中的商品数量
                        Cart cartForUpdateQuantity = new Cart();
                        cartForUpdateQuantity.setId(cartItem.getId());
                        cartForUpdateQuantity.setQuantity(availableQuantity);
                        cartForUpdateQuantity.setUpdateTime(new Date());
                        cartMapper.updateByPrimaryKeySelective(cartForUpdateQuantity);
                    }
                    cartProductVo.setQuantity(availableQuantity);
//                    使用BigDecimalUtil计算当前商品的总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),availableQuantity));
//                    可以理解为只有商品存在才进行勾选的状态判断
                    cartProductVo.setProductChecked(cartItem.getChecked());
//                    如果勾选，则将该商品的总价加入购物车的总价中
                    if(cartItem.getChecked() == Const.CartConstant.CHECKED){
                        cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                    }
                }
                cartProductVoList.add(cartProductVo);
            }
        }
//        组装CartVo
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;
    }

//    判断购物车中的商品是否全选，这工作在后端比较容易实现，前端根据返回的结果判断要不要把全选按钮勾选上，
//     这样就不用在前端进行遍历了
    private boolean getAllCheckedStatus(Integer userId){
        if(userId == null){
            return false;
        }
        return cartMapper.selectProductCheckedStatusByUserId(userId) == 0;
    }

}
