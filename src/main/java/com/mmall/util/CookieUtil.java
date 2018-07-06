package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CookieUtil {

//    设置能够获得该cookie的域名
    /**
     * 关于域名domain,返回路径path和cookie可见性之间的关系；
     * domain X:".mall.com" //注意以"."开始
     *
     * domain A:"A.mall.com"   cookie:domain = "A.mall.com" ; path = "/"
     * domain B:"B.mall.com"   cookie:domain = "B.mall.com" ; path = "/"
     *
     * domain C:"A.mall.com/test"   cookie:domain = "A.mall.com" ; path = "test"
     * domain D:"A.mall.com/test/dd"   cookie:domain = "A.mall.com" ; path = "test/dd"
     * domain E:"A.mall.com/test/ee"   cookie:domain = "A.mall.com" ; path = "test/ee"
     *
     * X为一级域名，下面的二级域名A B C D E都能访问到 X 的cookie;
     * A B是不同的二级域名，互相访问不到对方的cookie;
     * C D E同属 A 的二级域名，只是返回路径比 A 窄，可以访问到 A 的cookie，无法访问 B 的cookie;
     * D E的返回路径在 C 之下，可以访问到 C 的cookie;
     * D E属于同级域名，同级返回路径，互相访问不到对方的cookie;
     *
     * 这样做主要是在项目服务化后将不同的功能模块（如用户模块，产品模块）抽取出来作为一个单独的服务，每个服务对应一个
     * 二级域名，这些二级域名下的服务也可以访问到一级域名下的cookie；
     */
    public static final String COOKIE_DOMAIN = ".mymall.com";
    public static final String COOKIE_NAME = "LOGIN_TOKEN";

    /**
     * 将sessionId写入cookie
     * @param response
     * @param token
     */
    public static void writeLoginToken(HttpServletResponse response,String token) {
        Cookie cookie = new Cookie(COOKIE_NAME,token);
//        设置cookie的域名和路径，这里设为一级域名下的根目录，简单通用
//        设置能获得该cookie的域名
        cookie.setDomain(COOKIE_DOMAIN);
//          设置客户端返回cookie的路径，在该路径下的文件夹都能访问到这个cookie，这里设置根目录
        cookie.setPath("/");

//        设置cookie的过期时间，正数表示cookie的存在时间，单位秒
//          负数表示cookie在客户端不会被持久化到硬盘上，关闭浏览器页面后cookie就会被删除，0表示从浏览器删除该cookie
        cookie.setMaxAge(60*60*24*365);

//          设置cookie只能被http请求访问，不能被脚本访问，这是为了防止脚本攻击，提高安全性
        cookie.setHttpOnly(true);
        log.info("write cookieName:{},cookieValue:{}",cookie.getName(),cookie.getValue());
        response.addCookie(cookie);
    }

    /**
     * 从请求中读取存放用户登录信息的session的sessionId
     * @param request
     * @return
     */
    public static String readLoginToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
//                  记录日志方便查找问题
                log.info("cookieName:{},cookieValue:{}",cookie.getName(),cookie.getValue());
                if (StringUtils.equals(cookie.getName(),COOKIE_NAME)) {
                    log.info("read cookieName={},cookieValue={}",cookie.getName(),cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 删除客户端的cookie
     * @param request
     * @param response
     */
    public static void deleteLoginToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (StringUtils.equals(cookie.getName(),COOKIE_NAME)) {
                    cookie.setDomain(COOKIE_DOMAIN);
                    cookie.setMaxAge(0);//设置过期时间为0就表示删除cookie
                    cookie.setPath("/");
                    log.info("delete cookieName:{},cookieValue:{}",cookie.getName(),cookie.getValue());
                    response.addCookie(cookie);
                    return;
                }
            }
        }
    }
}
