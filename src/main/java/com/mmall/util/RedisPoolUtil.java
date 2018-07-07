package com.mmall.util;

import com.mmall.common.RedisSingletonPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

/**
 * 单个Redis实例时使用的工具类，现在项目使用分布式Redis,使用RedisShartedPoolUtil,这个类闲置
 */
@Slf4j
public class RedisPoolUtil {
    public static String set(String key, String value) {
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisSingletonPool.getJedis();
            result = jedis.set(key, value);
        } catch (Exception e) {
            log.error("key{},value{},error", key, value, e); //在日志记录异常的详细信息用于查找问题，使用e.getMessage只会记录异常的名称
            RedisSingletonPool.returnBrokenResource(jedis);
            return result;
        }
        RedisSingletonPool.returnResource(jedis);
        return result;
    }

    /**
     * 存入键值的同时设置超时时间，单位是秒
     * @param key
     * @param value
     * @param expireTime
     * @return
     */
    public static String setEx(String key, String value, int expireTime) {
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisSingletonPool.getJedis();
            result = jedis.setex(key, expireTime, value);
        } catch (Exception e) {
            log.error("key{},value{},error", key, value, e); //在日志记录异常的详细信息用于查找问题，使用e.getMessage只会记录异常的名称
            RedisSingletonPool.returnBrokenResource(jedis);
            return result;
        }
        RedisSingletonPool.returnResource(jedis);
        return result;
    }


    public static String get(String key) {
        Jedis jedis = null;
        String result = null;
        try {
            jedis = RedisSingletonPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("key{},error", key, e);
            RedisSingletonPool.returnBrokenResource(jedis);
            return result;
        }
        RedisSingletonPool.returnResource(jedis);
        return result;
    }

    /**
     * 设置键值的超时时间，Jedis的expire方法如果键值设置了超时时间，超时后返回1，没有设置或键值不存在返回0
     * @param key
     * @param expireTime
     * @return
     */
    public static Long expire(String key, int expireTime) {
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisSingletonPool.getJedis();
            result = jedis.expire(key, expireTime);
        } catch (Exception e) {
            log.error("key{},expireTime{},error", key, expireTime, e);
            RedisSingletonPool.returnBrokenResource(jedis);
            return result;
        }
        RedisSingletonPool.returnResource(jedis);
        return result;
    }

    public static Long delete(String key){
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = RedisSingletonPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("delete key{},error",key,e);
            RedisSingletonPool.returnBrokenResource(jedis);
            return result;
        }
        RedisSingletonPool.returnResource(jedis);
        return result;
    }
}
