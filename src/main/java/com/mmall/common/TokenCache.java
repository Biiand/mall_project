package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by hasee on 2018/4/26.
 * 使用GuavaCache实现本地缓存
 */
@Slf4j
public class TokenCache {

//    为存入缓存的数据的Key设定前缀
    public static final String TOKEN_PREFIX = "token_";

    public static final String SECKILL_TOKEN_PREFIX = "secKIllID_";

//    使用logBack提供的Logger，org.slf4j.Logger
//    private static Logger log = LoggerFactory.getLogger(TokenCache.class);

    /**
     * 使用本地缓存的原因：在系统中，有些数据，数据量小，但是访问十分频繁，针对这种场景，需要将数据搞到应用的本地缓存中，以提升系统的访问效率，
     * 减少无谓的数据库访问（数据库访问占用数据库连接，同时网络消耗比较大），但是有一点需要注意，就是缓存的占用空间以及缓存的失效策略。
     * 这里是引入guava的cacheBuilder来构建缓存，guava是google托管的一个开源的java类库，提供了缓存，集合，并发等一系列的实用方法；
     * 使用CacheBuilder.newBuilder()创建缓存实例，initialCapacity设置初始（最小）容量，单位是存储的数据的条目数，maximumSize设置最大容量,
     * 到达最大容量后底层是是使用LRU算法（最少使用算法）来移除缓存项,expireAfterAccess(12, TimeUnit.HOURS)设置缓存失效时间，单位设为小时；
     * 因为guava使用调用链模式，所以initialCapacity、expireAfterAccess和maximumSize的前后顺序可以调换，
     * 调用链模式是使用方法链来完成的，方法链是在一个类中的声明的多个方法时，每一个方法最后都返回本对象（this）,这样就能在一次对象的引用中连续调用多个方法，
     * 省去了重复引用对象名，在方法很多时可以减少代码量，因为顺序没有固定顺序，书写也更自然。但条件是每个方法的返回值都只能是本对象，详见百度百科 方法链；
     */
    private static LoadingCache<String,String> localCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000)
            .expireAfterAccess(12, TimeUnit.HOURS).build(new CacheLoader<String, String>() {
//                默认的缓存加载器，当使用getKey取得缓存数据时，如果没有这个key，就调用这个方法进行加载
                @Override
                public String load(String key) throws Exception {
//                    将null换位字符串"null"，避免使用equals方法的时候出现空指针
                    return "null";
                }
            });

//    localCache对象设置为私有，所以通过setKey来调用
    public static void setKey(String key,String value){
        localCache.put(key,value);
    }

//    根据key获取缓存数据，课程使用的方法名是getKey
    public static String getValue(String key){
        String value = null;
        try {
            value = localCache.get(key);
            if("null".equals(value)){
                return null;
            }
        } catch (ExecutionException e) {
            log.error("localCache getValue error",e);
            e.printStackTrace();
        }
        return value;
    }
}
