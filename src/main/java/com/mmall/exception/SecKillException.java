package com.mmall.exception;

/**
 * 自定义异常继承运行时异常，用于在执行秒杀业务时，如果秒杀不成功，就抛出，使Spring的声明式事务对数据库进行回滚
 * 注意，重复秒杀异常和秒杀关闭异常继承了这个异常，此异常没有定义无参构造，所以子异常不能显性或隐性的调用super(),
 * 需要显性的调用定义的父类构造方法
 */
public class SecKillException extends RuntimeException {

    public SecKillException(String message) {
        super(message);
    }

    public SecKillException(String message, Throwable cause) {
        super(message, cause);
    }

}
