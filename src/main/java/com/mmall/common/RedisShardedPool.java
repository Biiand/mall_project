package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

public class RedisShardedPool {

    private static ShardedJedisPool pool;

    //这里是采用分别读取配置的ip和端口，供initPool方法中初始化JedisShardInfo时分别调用的方式来连接Redis，
    // 如果想增减Redis节点，需要修改代码和配置文件并重新编译部署，很麻烦。更好的方式是将所有配置的ip和port对拼接成一个字符串，
    //读取后在initPool方法中分割成ip和port,然后循环的初始化JedisShardInfo,这样只用重启tomcat配置就能生效。
    private static String redisIp1 = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redisPort1 = Integer.valueOf(PropertiesUtil.getProperty("redis1.port"));
    private static String redisIp2 = PropertiesUtil.getProperty("redis2.ip");
    private static Integer redisPort2 = Integer.valueOf(PropertiesUtil.getProperty("redis2.port"));

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

        JedisShardInfo info1 = new JedisShardInfo(redisIp1, redisPort1,2*1000);
        JedisShardInfo info2 = new JedisShardInfo(redisIp2, redisPort2,2*1000);

//        编程中注意，能事先确定大小的集合就设定好初始容量，这是个好习惯
        List<JedisShardInfo> list = new ArrayList<>(2);
        list.add(info1);
        list.add(info2);

//        使用MURMUR_HASH作为Redis的一致性hash算法中生成hash的类，DEFAULT_KEY_TAG_PATTERN用来抽取key标签的模板
        pool = new ShardedJedisPool(config, list, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);
    }

    static {
        initPool();
    }

    public static ShardedJedis getJedis() {
        return pool.getResource();
    }

    public static void returnResource(ShardedJedis jedis) {
        pool.returnResource(jedis); // 方法内对jedis进行了非空校验，所以这里就不用进行检验了
    }

    public static void returnBrokenResource(ShardedJedis jedis) {
        pool.returnBrokenResource(jedis);
    }

//      测试redis连接，需要开启设置的两个Redis
//    public static void main(String[] args) {
//        ShardedJedis jedis = pool.getResource();
//        for (int i = 0; i < 10; i++) {
//            jedis.set("key" + i, "value" + i);
//        }
//        pool.returnResource(jedis);
//        System.out.println("program is end");
//    }
}
