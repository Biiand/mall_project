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
    public ServiceResponse<CartVo> addToCart(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if (cart == null) {
            Cart cartItem = new Cart();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.CartConstant.CHECKED);
            cartMapper.insert(cartItem);
        } else {
            count += cart.getQuantity();
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKey(cart);
        }
        return this.list(userId);
    }

    @Override
    public ServiceResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if (cart != null) {
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKey(cart);
        }
        return this.list(userId);
    }

    /**
     * @param userId
     * @param productIds 用于同时删除多个商品时传递多个产品的id，和前端约定好格式为"id1,id2,..."
     * @return
     */
    @Override
    public ServiceResponse<CartVo> delete(Integer userId, String productIds) {
        if (StringUtils.isBlank(productIds)) {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
//        使用Guaua提供的工具类Splitter对字符串进行分割并转换为集合
        List<String> productIdList = Splitter.on(",").splitToList(productIds);
        cartMapper.deleteByUserIdAndProductId(userId, productIdList);
        return this.list(userId);
    }

    @Override
    public ServiceResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServiceResponse.createBySuccess(cartVo);
    }

    @Override
    public ServiceResponse<CartVo> selectOrUnselect(Integer userId, Integer checked, Integer productId) {
        cartMapper.checkedOrUncheckedProduct(userId, checked, productId);
        return this.list(userId);
    }

    @Override
    public ServiceResponse<Integer> getSumOfProductsInCart(Integer userId) {
        return ServiceResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }

    /**
     * 获取前端需要展示的购物车相关的状态信息，产品信息，计算勾选的商品的总价，控制添加进购物车的产品数量不超过库存
     *
     * @param userId
     * @return
     */
    private CartVo getCartVoLimit(Integer userId) {
        CartVo cartVo = new CartVo();

        List<Cart> cartList = cartMapper.selectCartByUserId(userId);

        List<CartProductVo> cartProductVoList = new ArrayList<>();
        BigDecimal cartTotalPrice = new BigDecimal("0");

        int checked = 0;//用于统计购物车中勾选了的商品，减少一次数据库查询
        if (CollectionUtils.isNotEmpty(cartList)) {
            //将购物车中的产品id放入集合，sql中使用in一次性将产品查出来，避免在循环中不断的连接数据库；
            List<Integer> productIdList = new ArrayList<>();
            for (Cart cartItem : cartList) {
                productIdList.add(cartItem.getProductId());
            }
            List<Product> productList = productMapper.selectById(productIdList);

            int index = 0;//用于获取对应的产品的下标
            for (Cart cartItem : cartList) {
                CartProductVo cartProductVo = new CartProductVo();

                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(cartItem.getUserId());
                cartProductVo.setProductId(cartItem.getProductId());
                //从集合中获取产品而不是在这里连接数据库查询
                Product product = productList.get(index);
                index++;
                if (product != null) {
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductStock(product.getStock());
//                    判断库存
                    int availableQuantity = 0;
                    if (product.getStock() >= cartItem.getQuantity()) {
                        availableQuantity = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.CartConstant.LIMIT_NUM_SUCCESS);
                    } else {
                        availableQuantity = product.getStock();
                        cartProductVo.setLimitQuantity(Const.CartConstant.LIMIT_NUM_FAIL);
//                        购物车中的商品数量大于库存，此时需要更新数据库中购物车表中的商品数量
                        Cart cartForUpdateQuantity = new Cart();
                        cartForUpdateQuantity.setId(cartItem.getId());
                        cartForUpdateQuantity.setQuantity(availableQuantity);
                        cartForUpdateQuantity.setUpdateTime(new Date());
                        //这里因为需要进行判断后才能决定是否对数据库进行操作，所以只能放在循环内，
                        // 但因为这种购物车商品大于库存的情况不多，所以影响可控
                        cartMapper.updateByPrimaryKeySelective(cartForUpdateQuantity);
                    }
                    cartProductVo.setQuantity(availableQuantity);
//                    使用BigDecimalUtil计算当前商品的总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), availableQuantity));
//                    可以理解为只有商品存在才进行勾选的状态判断
                    cartProductVo.setProductChecked(cartItem.getChecked());
//                    如果勾选，则将该商品的总价加入购物车的总价中
                    if (cartItem.getChecked() == Const.CartConstant.CHECKED) {
                        cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
                        checked++;//有勾选了的商品就加1
                    }
                }
                cartProductVoList.add(cartProductVo);
            }
        }
//        组装CartVo
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setCartTotalPrice(cartTotalPrice);
        //通过比较勾选的数量和购物车的中的商品种类数来判断是否全选，这样就避免了直接从数据库查询
        //cartVo.setAllChecked(this.getAllCheckedStatus(userId));不好的方式
        if (checked != 0 && checked == cartList.size()) {
            cartVo.setAllChecked(true);
        }else {
            cartVo.setAllChecked(false);
        }
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;
    }

//    能通过业务逻辑判断得出购物车是否全选，就不要使用这种需要连接数据库的方式了
//    判断购物车中的商品是否全选，这工作在后端比较容易实现，前端根据返回的结果判断要不要把全选按钮勾选上，
//     这样就不用在前端进行遍历了
//    private boolean getAllCheckedStatus(Integer userId) {
//        if (userId == null) {
//            return false;
//        }
//        return cartMapper.selectProductCheckedStatusByUserId(userId) == 0;
//    }

}
