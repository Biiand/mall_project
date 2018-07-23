package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.*;
import com.mmall.exception.SecKillException;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by hasee on 2018/5/7.
 */
@Service("iOrderService")
@Slf4j
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;

    @Autowired
    SecKillProductMapper secKillProductMapper;


    /**
     * 创建普通商品的订单
     *
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServiceResponse create(Integer userId, Integer shippingId) {
        //将购物车勾选的商品取出来
        List<Cart> cartList = cartMapper.selectSelectedItemsByUserId(userId);
        //获取商品明细
        ServiceResponse response = this.getOrderItems(userId, cartList);
        if (!response.isSuccess()) {
            return response;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();
        if (CollectionUtils.isEmpty(orderItemList)) {
            return ServiceResponse.createByErrorMessage("生成订单明细错误");
        }
        //计算订单总价
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);

        //生成订单对象并存入数据库
        Order order = this.assembleOrder(userId, shippingId, payment);
        if (order == null) {
            return ServiceResponse.createByErrorMessage("生成订单错误");
        }

        //为每一个订单明细补齐订单号
        for (OrderItem item : orderItemList) {
            item.setOrderNo(order.getOrderNo());
        }

        //使用mybatis的批量插入将明细的集合中的数据添加到order_item表中中
        orderItemMapper.batchInsert(orderItemList);

        //更新商品库存
        this.updateProductStock(orderItemList, Const.UpdateProductStockEnum.REDUCE.getCode());
        //订单创建成功后清空购物车，这样若取消订单，再次购买需要重新添加到购物车
        this.cleanCart(cartList);

        //组装返回前端的信息
        return ServiceResponse.createBySuccess(this.assembleOrderVo(order, orderItemList));
    }

    /**
     * 创建秒杀商品的订单
     *
     * @param userId
     * @param SecKillId
     * @param shippingId
     * @return
     */
    @Transactional
    public ServiceResponse createSecKillOrder(Integer userId, Integer SecKillId, Integer shippingId) {
        SecKillProduct secKillProduct = secKillProductMapper.selectById(SecKillId);
        Product product = productMapper.selectByPrimaryKey(secKillProduct.getProductId());
        //生成订单明细
        OrderItem orderItem = new OrderItem();
        orderItem.setUserId(userId);
        orderItem.setProductId(product.getId());
        orderItem.setProductName(product.getName());
        orderItem.setCurrentUnitPrice(secKillProduct.getPrice());
        orderItem.setProductImage(product.getMainImage());
        orderItem.setQuantity(1);
        orderItem.setTotalPrice(secKillProduct.getPrice());
        //订单总价
        BigDecimal payment = secKillProduct.getPrice();

        //生成订单对象并存入数据库
        Order order = this.assembleOrder(userId, shippingId, payment);
        if (order == null) {
            return ServiceResponse.createByErrorMessage("生成订单错误");
        }

        orderItem.setOrderNo(order.getOrderNo());
        //保存订单明细
        orderItemMapper.insert(orderItem);

        int resultCount = secKillProductMapper.reduceStockById(SecKillId, new Date());
        if (resultCount <= 0) {
            //抛出运行时异常，回滚数据
            throw new SecKillException("秒杀失败");
        }
        List<OrderItem> orderItemList = new ArrayList<>();
        orderItemList.add(orderItem);

        return ServiceResponse.createBySuccess(this.assembleOrderVo(order, orderItemList));
    }

    /**
     * 获取订单商品明细
     *
     * @param userId
     * @param cartList
     * @return
     */
    private ServiceResponse getOrderItems(Integer userId, List<Cart> cartList) {
        if (CollectionUtils.isEmpty(cartList)) {
            return ServiceResponse.createByErrorMessage("未选中任何购物车中的商品");
        }

        List<Integer> productIdList = new ArrayList<>();
        for (Cart cartItem : cartList) {
            productIdList.add(cartItem.getProductId());
        }
        List<Product> productList = productMapper.selectById(productIdList);

        int index = 0;
        List<OrderItem> orderItemList = new ArrayList<>();
        for (Cart cartItem : cartList) {
            Product product = productList.get(index);
            index++;
            //检验商品的状态和数量
            if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
                return ServiceResponse.createByErrorMessage(product.getName() + "已下架");
            }
            if (cartItem.getQuantity() > product.getStock()) {
                return ServiceResponse.createByErrorMessage(product.getName() + "库存不足");
            }

            OrderItem orderItem = new OrderItem();

            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartItem.getQuantity()));

            orderItemList.add(orderItem);
        }
        return ServiceResponse.createBySuccess(orderItemList);
    }

    //计算订单总价
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        BigDecimal totalPrice = new BigDecimal(0);
        for (OrderItem item : orderItemList) {
            totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(), item.getTotalPrice().doubleValue());
        }
        return totalPrice;
    }

    //组装Order对象
    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment) {
        Order order = new Order();

        order.setOrderNo(this.generateOrderNo());
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setPayment(payment);
        //设置付款方式，项目只使用了一种付款方式以进行简化，真实场景下用户会从前端选择方式后传递给后台
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        //设置邮费，实际中邮费需要根据情况进行计算，这里就简化了
        order.setPostage(0);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());

        int rowCount = orderMapper.insert(order);
        if (rowCount > 0) {
            return order;
        }
        return null;
    }

    //生成订单号,这里使用的算法很简单，实际生成中需要考虑的情况更多
    private Long generateOrderNo() {
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random(100).nextInt();
        //下面的方式很容易得到相同的值，会导致并发时出现因为订单号重复而下单失败，因此改为上面的方式，为每一个时间点设置一个数值范围
        //return currentTime + currentTime % 10;
    }

    //更新商品的库存
    private void updateProductStock(List<OrderItem> orderItemList, int addOrReduce) {
        List<Integer> productIdList = new ArrayList<>();
        for (OrderItem cartItem : orderItemList) {
            productIdList.add(cartItem.getProductId());
        }
        List<Product> productList = productMapper.selectById(productIdList);

        int index = 0;
        for (OrderItem Item : orderItemList) {
            Product product = productList.get(index);
            index++;
            switch (addOrReduce) {
                case 1:
                    product.setStock(product.getStock() + Item.getQuantity());
                    break;
                case 2:
                    product.setStock(product.getStock() - Item.getQuantity());
                    break;
                default:
                    throw new RuntimeException("addOrReduce参数错误");
            }
            Product productForUpdate = new Product();
            productForUpdate.setId(product.getId());
            productForUpdate.setStock(product.getStock());
            productMapper.updateByPrimaryKeySelective(productForUpdate);
        }
    }

    //将创建订单后的商品从购物车移除
    private void cleanCart(List<Cart> cartList) {
        for (Cart cartItem : cartList) {
            cartMapper.deleteByPrimaryKey(cartItem.getId());
        }
    }

    //组装orderVo
    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();

        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        orderVo.setOrderItemVoList(this.assembleOrderItemVo(orderItemList));

        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVo.setShippingId(order.getShippingId());
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(this.assembleShippingVo(shipping));
        }

        return orderVo;
    }

    private List<OrderItemVo> assembleOrderItemVo(List<OrderItem> orderItemList) {
        List<OrderItemVo> orderItemVoList = new ArrayList<>();

        for (OrderItem item : orderItemList) {
            OrderItemVo orderItemVo = new OrderItemVo();

            orderItemVo.setOrderNo(item.getOrderNo());
            orderItemVo.setProductId(item.getProductId());
            orderItemVo.setProductName(item.getProductName());
            orderItemVo.setProductImage(item.getProductImage());
            orderItemVo.setCurrentUnitPrice(item.getCurrentUnitPrice());
            orderItemVo.setQuantity(item.getQuantity());
            orderItemVo.setTotalPrice(item.getTotalPrice());
            orderItemVo.setCreateTime(DateTimeUtil.dateToStr(item.getCreateTime()));

            orderItemVoList.add(orderItemVo);
        }
        return orderItemVoList;
    }

    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();

        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverZip(shipping.getReceiverZip());

        return shippingVo;
    }

    /**
     * 调用支付宝当面付来连接支付宝完成支付
     *
     * @param userId
     * @param orderNo
     * @param path    保存支付二维码图片的路径
     * @return
     */
    @Override
    public ServiceResponse pay(Integer userId, Long orderNo, String path) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            ServiceResponse.createByErrorMessage("该订单号不存在");
        }
        //map存放返回给前端的信息，包括订单号和支付二维码图片的访问路径
        Map<String, String> resultMap = new HashMap<>();
        //还可以用String.valueOf(order.getOrderNo())得到String类型的orderNo,valueOf方法也只是调用了包装类的toString()
        resultMap.put("orderNo", order.getOrderNo().toString());

        //以下是支付宝当面付的集成部分
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("myMall订单扫码支付，订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为30分钟
        String timeoutExpress = "30m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        //根据订单号和用户id查出商品信息
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId, order.getOrderNo());

        for (OrderItem item : orderItemList) {
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods = GoodsDetail.newInstance(item.getProductId().toString()
                    , item.getProductName()
                    , item.getCurrentUnitPrice().longValue()
                    , item.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        Configs.init("zfbinfo.properties");
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                //获取支付宝预下单的返回结果response
                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);//将响应结果记录到日志

                //以下是对支付宝预下单相应结果的处理
                //如果path不存在则创建
                File folder = new File(path);
                if (!folder.exists()) {
                    folder.setWritable(true);
                    folder.mkdirs();
                }
                //组装二维码图片的存储路径，%s是占位符，代表response.getOutTradeNo()的值，这里就是订单号
                String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                log.info("qrPath", qrPath);

                //使用二维码生成工具将qrCode转化为图片并存放进qrPath指定的路径下，width属性指定生成的图片的宽度
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                //将二维码图片存储到FTP服务器上
                File targetFile = new File(qrPath);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    log.error("上传二维码图片到FTP服务器异常", e);
                }

                //组装二维码图片的url
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName();
                resultMap.put("qrUrl", qrUrl);

                return ServiceResponse.createBySuccess(resultMap);

            case FAILED:
                log.error("支付宝预下单失败!!!");
                return ServiceResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return ServiceResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return ServiceResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }


    @Override
    public ServiceResponse verifyParams(Map<String, String> params) {
        //从回调参数中取得商户订单号
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        //取得支付宝交易号
        String tradeNo = params.get("trade_no");
        //取得交易状态
        String tradeStatus = params.get("trade_status");

        //校验订单号是否商户系统创建的，是否存在
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServiceResponse.createByErrorMessage("验证回调参数异常，回调订单号不是本系统的订单号");
        }
        //对支付宝重复回调进行判断过滤
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            //返回success是为了在Controller中将该状态和上面回调参数异常的状态区分开
            return ServiceResponse.createBySuccessMessage("支付宝重复回调");
        }

        //用户付款成功后更新订单状态和付款时间
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            //支付宝回调的日期字符串的格式是通用格式，yyyy-MM-dd HH:mm:ss，和DateTimeUtil设置的默认格式相同
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            orderMapper.updateByPrimaryKeySelective(order);
        }

        //保存支付状态详情
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);
        payInfoMapper.insert(payInfo);

        return ServiceResponse.createBySuccess();
    }

    /**
     * 数据库查询订单状态
     *
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServiceResponse queryPayStatus(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServiceResponse.createByErrorMessage("未找到该订单");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServiceResponse.createBySuccess();
        }
        return ServiceResponse.createByError();
    }

    /**
     * 取消订单
     *
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServiceResponse cancel(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServiceResponse.createByErrorMessage("该用户没有订单号为" + orderNo + "的订单");
        }
        if (order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()) {
            return ServiceResponse.createByErrorMessage("该订单不可取消");
        }
        Order orderForUpdate = new Order();
        orderForUpdate.setId(order.getId());
        orderForUpdate.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        orderForUpdate.setUpdateTime(new Date());
        int rowCount = orderMapper.updateByPrimaryKeySelective(orderForUpdate);
        if (rowCount > 0) {
            //没有删除被取消的订单的商品明细是因为被取消的订单也会保存在用户的记录里面，以后可能需要查询明细
            List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId, orderNo);
            //更新商品库存，没对orderItemList做校验是因为创建订单时就确保了订单和明细都要创建成功
            this.updateProductStock(orderItemList, Const.UpdateProductStockEnum.ADD.getCode());
            return ServiceResponse.createBySuccessMessage("取消订单成功");
        }
        return ServiceResponse.createByErrorMessage("取消订单失败");
    }

    @Override
    public ServiceResponse getProductInfoInOrder(Integer userId) {
        List<Cart> cartList = cartMapper.selectSelectedItemsByUserId(userId);
        ServiceResponse response = this.getOrderItems(userId, cartList);
        if (!response.isSuccess()) {
            return response;
        }

        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();

        List<OrderItemVo> orderItemVoList = this.assembleOrderItemVo(orderItemList);

        BigDecimal totalPrice = this.getOrderTotalPrice(orderItemList);

        OrderProductVo orderProductVo = new OrderProductVo();
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setTotalPrice(totalPrice);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return ServiceResponse.createBySuccess(orderProductVo);
    }

    @Override
    public ServiceResponse getOrderDetail(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServiceResponse.createByErrorMessage("该用户没有订单号为：" + orderNo + "的订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId, orderNo);

        OrderVo orderVo = assembleOrderVo(order, orderItemList);

        return ServiceResponse.createBySuccess(orderVo);
    }

    @Override
    public ServiceResponse<PageInfo> getOrderList(Integer userId, int pageNum, int pageSize) {
        //初步理解：开始分页，起到设置参数的作用，放在数据库查询之前，因为插件后对查询sql按分页的要求进行处理
        PageHelper.startPage(pageNum, pageSize);

        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList, userId);

        //mybatisPageHelper的原理，它是用aop做的切面。
        //所以必须和之前的dao层有请求才会添加分页相关信息，如果直接放分页的信息就没有了
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServiceResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServiceResponse<PageInfo> manageOrderList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, null);

        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServiceResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServiceResponse<OrderVo> manageOrderDetail(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServiceResponse.createBySuccess(orderVo);
    }

    /**
     * 后台查询订单，目前还是精确匹配，和manageOrderDetail方法的区别是为了后期的模糊查询和多条件查询扩展，添加了分页
     *
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServiceResponse<PageInfo> manageSearchOrder(Long orderNo, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        Order order = orderMapper.selectByOrderNo(orderNo);
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);

        PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
        pageInfo.setList(Lists.newArrayList(orderVo));

        return ServiceResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServiceResponse manageSendOut(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServiceResponse.createByErrorMessage("未找到该订单");
        }
        if (order.getStatus() != Const.OrderStatusEnum.PAID.getCode()) {
            return ServiceResponse.createByErrorMessage("该订单未付款或已发货");
        }
        Order orderForUpdate = new Order();
        orderForUpdate.setId(order.getId());
        orderForUpdate.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
        Date sendTime = new Date();
        orderForUpdate.setSendTime(sendTime);
        orderForUpdate.setUpdateTime(sendTime);
        int rowCount = orderMapper.updateByPrimaryKeySelective(orderForUpdate);
        if (rowCount > 0) {
            return ServiceResponse.createBySuccessMessage("订单" + orderNo + "发货成功");
        }
        return ServiceResponse.createByErrorMessage("订单" + orderNo + "发货失败");
    }

    /**
     * 定时关单
     *
     * @param hour
     */
    @Override
    public void closeOrder(int hour) {
        //定义关闭订单的基准时间，如果未支付订单的创建时间小于基准时间，则关闭该订单
        Date closeDateTime = DateUtils.addHours(new Date(), -hour);

        List<Order> overTimeOrder = orderMapper.selectOverTimeOrders(Const.OrderStatusEnum.NO_PAY.getCode(), DateTimeUtil.dateToStr(closeDateTime));

        List<Long> orderNoList = new ArrayList<>();
        for (Order order : overTimeOrder) {
            orderNoList.add(order.getOrderNo());
        }
        //使用in集中查询，避免在循环中不断的连接数据库
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNos(orderNoList);

        //将超时订单中的商品id和数量合并集中到一个map中，尽可能减少连接数据库和修改表的次数
        Map<Integer, Integer> productMap = new HashMap<>();
        for (OrderItem orderItem : orderItemList) {
            Integer key = orderItem.getProductId();
            Integer value;
            if ((value = productMap.get(key)) != null) {
                value += orderItem.getQuantity();
                productMap.put(key, value);
            }
            productMap.put(key, orderItem.getQuantity());
        }

        //循环更新商品库存
        Set<Integer> keySet = productMap.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext()) {
            Integer productId = (Integer) it.next();
            int resultCount = productMapper.updateStockById(productId, productMap.get(productId));
            if (resultCount < 1) {
                log.info("关闭超时订单，更新商品库存失败，产品id = {}，更新数量 = {}", productId, productMap.get(productId));
            }
            log.info("关闭超时订单，更新商品库存成功，产品id = {}，更新数量 = {}", productId, productMap.get(productId));
        }

        //将超时订单状态置为取消
        int resultCount = orderMapper.closeOrderByIds(orderNoList);
        log.info("应关闭超时订单数量：{}，实际关闭数量：{}", orderNoList.size(), resultCount);

//          旧的方式，嵌套循环内产生大量数据库连接，对性能影响十分严重
//        for (Order order : overTimeOrder) {
//            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
//            for (OrderItem item : orderItemList) {
//                Integer productStock = productMapper.selectStockById(item.getProductId());
//                //考虑到商品可能被“删除”或下架处理而查不到数据的情况，直接跳过
//                if (productStock == null) {
//                    continue;
//                }
//                Product product = new Product();
//                product.setId(item.getProductId());
//                product.setStock(productStock + item.getQuantity());
//                productMapper.updateByPrimaryKeySelective(product);
//            }
//            orderMapper.closeOrderById(order.getId());
//            log.info("关闭超时订单，orderNo = {}", order.getOrderNo());
//        }
    }

    /**
     * 获取订单列表时用于组装List<OrderVo>
     *
     * @param orderList
     * @param userId
     * @return
     */
    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId) {
        List<OrderVo> orderVoList = new ArrayList<>();
        //将创建接收返回值用的集合对象放在循环外，避免重复创造对象
        List<OrderItem> orderItemList = null;
        for (Order order : orderList) {
            if (userId == null) {
                //管理员查询订单
                orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
            } else {
                //用户查询订单
                orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId, order.getOrderNo());
            }

            OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    //支付宝打印响应的方法
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }

}
