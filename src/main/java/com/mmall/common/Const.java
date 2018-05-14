package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by hasee on 2018/4/25.
 */
public class Const {
    public static final String CURRENT_USER = "currentUser";

    public static final String USERNAME = "username";

    public static final String EMAIL = "email";


    /**
     * 用户和管理员是一个角色组，因为只有两种角色，专门使用一个枚举类的话显得过于繁重，
       因此在常量类中新建一个内部接口来实现这个功能，是一个开发的小技巧，接口内声明的变量都是常量
     */
    public interface Role{
        int ROLE_CUSTOMER = 0;
        int ROLE_ADMIN = 1;
    }

    /**
     * 对用户进行产品查询得到的产品集合进行排序的方式进行分组，这里使用一个set集合来存储排序方式，因为
     * 在service中使用该分类的时候会使用到集合的 contain()方法，Set的时间复杂度是O（1），List的时间复杂度是O（n），
     * 虽然这里包含的元素少，但从效率上考虑使用Set
     * 排序方式的书写格式要和前端约定好，这里是“_”作为分隔符，之前是排序使用的字段，之后是升序还是降序，
     * 这里只提供了按价格的升序和降序，还可以根据需要使用其它的字段进行排序
     */
    public interface ProductListOrderBy{
        Set<String> PRICE_DESC_ASC = Sets.newHashSet("price_desc","price_asc");
    }

    /**
     * 对加入购物车中的商品是否勾选进行分组
     */
    public interface CartConstant{
        int CHECKED = 1;
        int UNCHECKED = 0;

        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
    }

//    商品在售状态枚举
    public enum ProductStatusEnum{
        ON_SALE(1,"在售"),
        SOLD_OUT(2,"下架"),
        DELETED(3,"删除");

        private Integer code;
        private String value;

        ProductStatusEnum(Integer code,String value){
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public enum OrderStatusEnum{
        CANCELED(0,"已取消"),
        NO_PAY(10,"未付款"),
        PAID(20,"已支付"),
        SHIPPED(40,"已发货"),
        FINISH(50,"交易完成"),
        CLOSED(60,"交易关闭")
        ;

        private int code;
        private String value;

        OrderStatusEnum(int code,String value){
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public static OrderStatusEnum codeOf(int code){
            for(OrderStatusEnum orderStatus : OrderStatusEnum.values()){
                if(code == orderStatus.getCode()){
                    return orderStatus;
                }
            }
            throw new RuntimeException("没有该code对应的枚举");
        }
    }



    public interface AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";
        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    public enum PaymentTypeEnum{
        ONLINE_PAY(1,"在线支付"),
        CASH_ON_DILIVERY(2,"货到付款")
        ;

        private int code;
        private String value;

        PaymentTypeEnum(int code,String value){
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        //   根据code获取枚举
        public static PaymentTypeEnum codeOf(int code){
            for(PaymentTypeEnum paymentType : PaymentTypeEnum.values()){
                if(code == paymentType.getCode()){
                    return paymentType;
                }
            }
            throw new RuntimeException("没有该code对应的枚举");
        }
    }



    public enum PayPlatformEnum{
        ALIPAY(1,"支付宝"),
        WECHAT(2,"微信支付")
        ;

        private int code;
        private String value;

        PayPlatformEnum(int code,String value){
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public enum UpdateProductStockEnum{
        ADD(1,"增加库存"),
        REDUCE(2,"减少库存")
        ;

        private int code;
        private String value;

        UpdateProductStockEnum(int code,String value){
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
