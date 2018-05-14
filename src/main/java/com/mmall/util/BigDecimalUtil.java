package com.mmall.util;

import java.math.BigDecimal;

/**
 * 为解决java中小数计算丢失精度的问题构建该工具类，商业上的计算都要求精确计算
 * 因此必须使用BigDecimal(String str)这个构造器来构建BigDecimal对象进行计算
 * Created by hasee on 2018/5/2.
 */
public class BigDecimalUtil {

//    使该工具类便不能在外部被实例化
    private BigDecimalUtil(){}

    public static BigDecimal add(double num1,double num2){
        BigDecimal b1 = new BigDecimal(Double.toString(num1));
        BigDecimal b2 = new BigDecimal(Double.toString(num2));
        return b1.add(b2);
    }

    public static BigDecimal sub(double num1,double num2){
        BigDecimal b1 = new BigDecimal(Double.toString(num1));
        BigDecimal b2 = new BigDecimal(Double.toString(num2));
        return b1.subtract(b2);
    }

    public static BigDecimal mul(double num1,double num2){
        BigDecimal b1 = new BigDecimal(Double.toString(num1));
        BigDecimal b2 = new BigDecimal(Double.toString(num2));
        return b1.multiply(b2);
    }

    public static BigDecimal div(double num1,double num2){
        BigDecimal b1 = new BigDecimal(Double.toString(num1));
        BigDecimal b2 = new BigDecimal(Double.toString(num2));
//        除法使用四舍五入保留两位小数
        return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP);
    }


}
