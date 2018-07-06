package com.mmall.util;

import com.mmall.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 将对象转化为json字符串
 */
@Slf4j
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    //    初始化ObjectMapper,非常重要，配置会影响json序列化的行为
    static {
//        setting defalt POJO property inclusion strategy for serialization.设置为对象的所有字段都进行序列化
//        四种策略：ALWAYS,序列化所有的属性；NON_NULL,序列化值非null的属性；NON_DEFAULT,序列化值不是在类加载时就默认设置的属性；
//        NON_EMPTY，序列化值非null且不为空的属性
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);

//        是否将Date类型和基于Date类型的其它类型的属性序列化为timestamps类型，默认为true；timestamps展示的是从1970.1.1到现在的毫秒数，看上去是一个很大的数
//          结合下面设置的DateFormat，按照指定格式输出，否则按Date的默认格式输出
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);

//        设置序列化和反序列化时间类型的数据时使用的格式，这里使用DateTimeUtil中定义的标准格式yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

//        序列化遇到空bean时不要抛出异常，序列化为一个空的对象，默认为true
//        empty bean:no accessors are found for a type ,不能访问到类的属性，比如没有给私有属性设置setter和getter，
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);

//        忽略反序列化时遇到的在json字符串中存在，但在java对象中不存在对应属性的情况，不要抛出异常
//        在实际开发中，自己的业务模块和别的业务模块有接口调用，通过http交互，拿到json进行反序列化，别的业务可能根据他们的需要，
//         在json中添加了新的字段，但这字段对我的业务没用，如果下面的设置为true，则我的模块会因为别人添加了我不需要的字段而出错，
//         所以如果存在这种情况，就要设置为false，忽略不匹配的属性进行反序列化。
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> String obj2String(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? String.valueOf(obj) : objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("parse object to String error", e);
            return null;
        }
    }

    /**
     * 调用writerWithDefaultPrettyPrinter方法将序列化后的json字符串格式化为标准的json格式输出，
     * 主要便于调试时查看，生产环境不建议使用，因为格式化后会占用更多的空间
     *
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> String obj2StringPretty(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? String.valueOf(obj) : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("parse object to String error", e);
            return null;
        }
    }

    /**
     * 将json字符串反序列化为java对象，使用泛型限制返回类型和传入的Class类型一致
     *
     * @param jsonStr
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T string2Obj(String jsonStr, Class<T> clazz) {
        if (StringUtils.isEmpty(jsonStr) || clazz == null) {
            return null;
        }
        try {
            return clazz.isInstance(String.class) ? (T) jsonStr : objectMapper.readValue(jsonStr, clazz);
        } catch (Exception e) {
            log.error("parse String to object error", e);
            return null;
        }
    }

    /**
     * 针对集合的反序列化的方法，因为集合除了有集合本身的类型外还有泛型，使用Class做参数无法同时指定
     * 如果传入List.class，反序列化的结果是集合中的类型被改为了默认的LinkedHashMap;
     * 使用Jackson中的TypeReference类加泛型来解决这个问题
     *
     * @param jsonStr
     * @param typeReference
     * @param <T>
     * @return
     */
    public static <T> T string2Obj(String jsonStr, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(jsonStr) || typeReference == null) {
            return null;
        }
        try {
            return typeReference.getType().equals(String.class) ? (T) jsonStr : objectMapper.readValue(jsonStr, typeReference);
        } catch (Exception e) {
            log.error("parse String to object error", e);
            return null;
        }
    }

    /**
     * 解决集合反序列化存在的问题的第二种方式，使用？来表示泛型是因为这里的类型不同，都不是T
     *  这个方法在处理其它复杂对象的时候也是可以用的，使用不定长参数使灵活性增强
     * @param jsonStr
     * @param collectionClass 集合的Class
     * @param elementClasses  集合中存放类型的Class
     * @param <T>
     * @return
     */
    public static <T> T string2Obj(String jsonStr, Class<?> collectionClass, Class<?>... elementClasses) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
        try {
            return objectMapper.readValue(jsonStr, javaType);
        } catch (Exception e) {
            log.error("parse String to object error", e);
            return null;
        }
    }

    //    测试
//    public static void main(String[] args) {
//        User u1 = new User();
//        u1.setId(1);
//        u1.setPassword("1111");
//        User u2 = new User();
//        u2.setId(2);
//        u2.setPassword("2222");
//
//        String u1Str = JsonUtil.obj2String(u1);
//        log.info(u1Str);
//
//        String u1StrPretty = JsonUtil.obj2StringPretty(u1);
//        log.info(u1StrPretty);
//
//        User u3 = JsonUtil.string2Obj(u1Str, User.class);
//
//        List<User> list = new ArrayList<>();
//        list.add(u1);
//        list.add(u2);
//
//        String listStr = JsonUtil.obj2StringPretty(list);
//        log.info(listStr);
//
////       传入Class作为参数在反序列化集合时无法指定集合中泛型的类型，只能使用jackson默认设置的类型
//        List<User> list1 = JsonUtil.string2Obj(listStr, List.class);
//
////        使用TypeReference来指定集合类型和集合中的泛型
////        TypeReference是一个接口，这里做成了一个空实现
//        List<User> list2 = JsonUtil.string2Obj(listStr, new TypeReference<List<User>>() {
//        });
//
//        List<User> list3 = JsonUtil.string2Obj(listStr, List.class, User.class);
//
//        System.out.println("end");
//
//    }

}
