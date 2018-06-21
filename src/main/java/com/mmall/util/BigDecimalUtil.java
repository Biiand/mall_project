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

    /**
     * 关于为什么要使用double类型的形参，并将形参转换为String后传入BigDecimal构造函数创建BigDecimal对象：
     * 根据BigDecimal的源码的文档描述，使用直接传入浮点数的构造函数创建的对象可能会产生不可预知的值，而使用String作为参数的
     * 构造函数得到的值不会产生这样的问题，完全可以预测，而且BigDecimal兼容通过浮点数转换String来创建对象，所以使用BigDecimal
     * 进行精确计算时最普遍的做法就是下面方法这样。
     * @param num1
     * @param num2
     * @return
     */
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
