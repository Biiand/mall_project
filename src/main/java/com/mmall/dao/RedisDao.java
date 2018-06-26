package com.mmall.dao;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.mmall.pojo.SecKillProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisDao.class);

    private static final String KEY_PRIFEX = "secKill:";

//    生成序列化对象的类的模式或框架，记录类的信息
    private RuntimeSchema<SecKillProduct> schema = RuntimeSchema.createFrom(SecKillProduct.class);

//    jedis连接池，用来获取jedis对象，作用类似于数据库连接池，使用final修饰但未赋值，在构造函数内进行初始化，所以不会报错
    private final JedisPool jedisPool;

//    在spring中配置RedisDao时如果要使用给属性赋值<property>完成初始化的方式就添加下面需要的属性和对应的getter,setter,无参构造方法；
//    使用构造方法传递初始化参数的话在spring中使用<constructor-arg>传传参
//   private String ip;
//    private int port;

    public RedisDao(String ip, int port){
//        初始化JedisPool，使用默认设置
        jedisPool = new JedisPool(ip,port);
    }

    public SecKillProduct get(int secKillId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = KEY_PRIFEX + secKillId;
//            有两个重载的get方法，将key转换为字节数据才能取出字节数组，传入String只能取出String类型的值，其他类型的值会报错
            byte[] bytes = jedis.get(key.getBytes());
            if(bytes != null){
//                创建一个空对象用于接收数据，也可以new一个对象
                SecKillProduct product = schema.newMessage();
//                根据类的模板schema，将对象中的数据合并到空对象中完成反序列化
                ProtostuffIOUtil.mergeFrom(bytes,product,schema);
                return product;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public String set(SecKillProduct product){
        try(Jedis jedis = jedisPool.getResource()) {
            String key = KEY_PRIFEX + product.getId();
            byte[] bytes = ProtostuffIOUtil.toByteArray(product, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
            int timeout = 60 * 60; //设置过期时间，单位：秒
            String result = jedis.setex(key.getBytes(), timeout, bytes);
            return result;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
