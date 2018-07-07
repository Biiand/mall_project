package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 单个Redis实例时使用的jedis池，现在项目使用分布式Redis,使用RedisShartedPool,这个类闲置,在同样被闲置的RedisPoolUtil中被引用
 */
public class RedisSingletonPool {

    //    设为static是为了在类加载时就完成JedisPool的初始化
    private static JedisPool jedisPool;

    private static String redisIp = PropertiesUtil.getProperty("redis.ip");
    private static Integer redisPort = Integer.valueOf(PropertiesUtil.getProperty("redis.port"));

    private static Integer maxTotal = Integer.valueOf(PropertiesUtil.getProperty("redis.max.total", "20"));
    private static Integer maxIdle = Integer.valueOf(PropertiesUtil.getProperty("redis.max.idle", "10"));
    private static Integer minIdle = Integer.valueOf(PropertiesUtil.getProperty("redis.min.idle", "2"));

    private static boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));
    private static boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return", "false"));

    private static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();

        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        //默认为true，单独写出来是为了强调，当所有的redis实例都被使用时，阻塞新的请求等待有实例被释放，直到过了超时时间，设为false会直接抛出异常
        config.setBlockWhenExhausted(true);

        jedisPool = new JedisPool(config, redisIp, redisPort, 1000 * 2);
    }

    static {
        initPool();
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }

    public static void returnResource(Jedis jedis) {
        jedisPool.returnResource(jedis); // 方法内对jedis进行了非空校验，所以这里就不用进行检验了
    }

    public static void returnBrokenResource(Jedis jedis) {
        jedisPool.returnBrokenResource(jedis);
    }

//      测试redis连接
//    public static void main(String[] args) {
//        Jedis jedis = jedisPool.getResource();
//        jedis.set("testKey","testValue");
//        jedisPool.returnResource(jedis);
//        jedisPool.destroy();
//        System.out.println("program is end");
//    }
}

