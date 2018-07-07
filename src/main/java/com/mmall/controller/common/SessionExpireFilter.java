package com.mmall.controller.common;

import com.mmall.common.Const;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 将进行操作的用户在Redis中存储的登陆信息的过期时间进行重置，
 * 虽然起名叫session，但其实并不是存储在session中，只是使用了一个sessionId作为唯一标识
 */
public class SessionExpireFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isNotBlank(loginToken)) {
            String jsonStr = RedisShardedPoolUtil.get(loginToken);
            User user = JsonUtil.string2Obj(jsonStr, User.class);
            if (user != null) {
                RedisShardedPoolUtil.expire(loginToken, Const.redisCacheExTime.SESSION_EXPIRE_TIME);
            }
        }
        chain.doFilter(request,response);
    }

    @Override
    public void destroy() {

    }
}
