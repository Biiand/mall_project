package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CookieUtil {

//    设置能够获得该cookie的域名
    public static final String COOKIE_DOMAIN = ".mymall.com";
    public static final String COOKIE_NAME = "LOGIN_TOKEN";

    /**
     * 将sessionId写入cookie
     * @param response
     * @param token
     */
    public static void writeLoginToken(HttpServletResponse response,String token) {
        Cookie cookie = new Cookie(COOKIE_NAME,token);
//        设置能获得该cookie的域名
        cookie.setDomain(COOKIE_DOMAIN);
//        设置cookie的过期时间，正数表示cookie的存在时间，单位秒
//          负数表示cookie在客户端不会被持久化到硬盘上，关闭浏览器页面后cookie就会被删除，0表示从浏览器删除该cookie
        cookie.setMaxAge(60*60*24*365);
//          设置客户端返回cookie的路径，在该路径下的文件夹都能访问到这个cookie，这里设置根目录
        cookie.setPath("/");
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
