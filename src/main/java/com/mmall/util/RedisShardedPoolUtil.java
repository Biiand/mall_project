package com.mmall.util;

import com.mmall.common.RedisShardedPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.ShardedJedis;

@Slf4j
public class RedisShardedPoolUtil {

    public static String set(String key, String value) {
        ShardedJedis jedis = null;
        String result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.set(key, value);
        } catch (Exception e) {
            log.error("set key{},value{},error", key, value, e); //在日志记录异常的详细信息用于查找问题，使用e.getMessage只会记录异常的名称
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    /**
     * 存入键值的同时设置超时时间，单位是秒
     *
     * @param key
     * @param value
     * @param expireTime
     * @return
     */
    public static String setex(String key, String value, int expireTime) {
        ShardedJedis jedis = null;
        String result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.setex(key, expireTime, value);
        } catch (Exception e) {
            log.error("setex key:{},value:{},error", key, value, e); //在日志记录异常的详细信息用于查找问题，使用e.getMessage只会记录异常的名称
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static Long setnx(String key, String value) {
        ShardedJedis jedis = null;
        Long result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.setnx(key, value);
        } catch (Exception e) {
            log.error("setnx key:{},value:{},error", key, value, e);
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static String getset(String key, String value) {
        ShardedJedis jedis = null;
        String result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.getSet(key, value);
        } catch (Exception e) {
            log.error("getset key:{},value:{},error", key, value, e);
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }


    public static String get(String key) {
        ShardedJedis jedis = null;
        String result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("key{},error", key, e);
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    /**
     * 设置键值的超时时间，Jedis的expire方法如果键值设置了超时时间，超时后返回1，没有设置或键值不存在返回0
     *
     * @param key
     * @param expireTime
     * @return
     */
    public static Long expire(String key, int expireTime) {
        ShardedJedis jedis = null;
        Long result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.expire(key, expireTime);
        } catch (Exception e) {
            log.error("key{},expireTime{},error", key, expireTime, e);
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static Long delete(String key) {
        ShardedJedis jedis = null;
        Long result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("delete key{},error", key, e);
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

//      测试
//    public static void main(String[] args) {
//        ShardedJedis jedis = RedisShardedPool.getJedis();
//
//        set("testKey","testValue");
//
//        String value = get("testKey");
//        System.out.println(value);
//
//        setex("testKeyEx","testValueEx",60*2);
//
//        expire("testKey",60*5);
//
//        delete("testKey");
//
//        System.out.println("end");
//
//
//    }
}
