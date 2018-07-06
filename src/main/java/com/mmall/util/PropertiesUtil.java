package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by hasee
 */

@Slf4j
public class PropertiesUtil {

//    使用lombok的@Slf4j注解在解析.java时添加logger对象，默认的引用名是log
//    private static final Logger log = LoggerFactory.getLogger(PropertiesUtil.class);

    private static Properties props;

    /**
     * 为什么要使用类加载器获取inputStream？
     * classloader可以获取classes路径下的配置文件，现在的web项目，resources编译后就在classes目录下，
     * 通过classloader可以加载后可以执行获取里面的值。
     * 以前直接在input stream里传file参数，需要确定file的绝对路径，对于变动的环境(现在都是部署在linux机器上)来说不太适用
     */

    static{
        String fileName = "mmall.properties";
        props = new Properties();
        try {
            props.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName),"UTF-8"));
        } catch (IOException e) {
            log.error("加载mmall.properties文件出错",e);
        }
    }

    public static String getProperty(String key){
//        对key和value进行trim是为了避免受配置文件时添加的空格的影响
        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            return null;
        }
        return value.trim();
    }

    public static String getProperty(String key,String defaultValue){
        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            return defaultValue;
        }
        return value.trim();
    }
}
