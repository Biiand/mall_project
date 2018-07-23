package com.mmall.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestDemo {

    private static Integer test1() {
        Integer a = 1;
        try {
            System.out.println("try...");
            return a += 10;
        } catch (Exception e) {
            System.out.println("catch...");
        }finally {
            a++;
            System.out.println("finally..." + a);
        }
        return a;
    }

    /**
     * 测试使用正则表达式
     * @param phone
     */
    private static void test2(String phone) {
        //Pattern是正则表达式的抽象封装类
        Pattern pattern = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$");
        //Matcher是执行匹配操作的类
        Matcher matcher = pattern.matcher(phone);
        boolean isMatch = matcher.matches();
        System.out.println(isMatch);
    }


    public static void main(String[] args) {
        String str = "18623419360";
        test2(str);
//        System.out.println("test1的结果，a = " + test1());
    }
}
