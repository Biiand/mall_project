package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class RedissonManager {
    private Config config = new Config();
    private Redisson redisson = null;

    public Redisson getRedisson() {
        return redisson;
    }

    private static String redisIp1 = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redisPort1 = Integer.valueOf(PropertiesUtil.getProperty("redis1.port"));
//    private static String redisIp2 = PropertiesUtil.getProperty("redis2.ip");
//    private static Integer redisPort2 = Integer.valueOf(PropertiesUtil.getProperty("redis2.port"));

    /**
     * 初始化redisson的方法
     * 除了可以使用静态块的方式完成初始化任务外，还可以使用@PostConstruct注解
     * 注解的作用是定义标注的方法在进行依赖注入，完成类的初始化的时候必须被调用，
     * 在这个类被注入一个service之前，方法必须被执行，方法不能传入参数，该方法会在类的构造函数执行完后执行
     */
    @PostConstruct
    private void init() {
        try {
//          redisson不支持分布式redis的一致性算法，因此使用单服务器，setAddress参数要求格式：host:port
            config.useSingleServer().setAddress(new StringBuilder().append(redisIp1).append(":").append(redisPort1).toString());
//        无参的create方法内默认设置config的IP和port是127.0.0.1：6379，使用create获得redisson对象是因为它封装了校验config对象的方法
            redisson = (Redisson) Redisson.create(config);
        } catch (Exception e) {
            log.error("redisson init error", e);
        }
    }
}
