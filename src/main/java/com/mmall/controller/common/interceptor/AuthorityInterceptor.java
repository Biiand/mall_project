package com.mmall.controller.common.interceptor;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;

/**
 *
 */
@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("preHandle");
//        debug显示，handler的类型是HandlerMethod，所以可以强转，
//          HandlerMethod类封装了url请求映射的handler的信息，使用HandlerMethod实例来获取执行的方法和参数信息；
//        获取请求的类，方法，参数，记录到日志
        HandlerMethod handlerMethod = (HandlerMethod) handler;
//        获取方法名
        String methodName = handlerMethod.getMethod().getName();
//        获取类名，不包括包名
        String beanName = handlerMethod.getBean().getClass().getSimpleName();
//        获取请求参数
        String parameters = getParameters(request);

//        根据业务需要将一些不需要拦截器判断的方法排除出去，
//          方法一：如下所示，通过代码实现的，更灵活；
//          方法二：在springMVC的配置中，在mvc:interceptor中通过mvc:exclude-mapping配置需要排除的请求的路径；
        //当遇到登陆请求时放过，否则因为未登录时取得的user == null，会陷入无法登陆的死循环。
        if (StringUtils.equals(beanName, "UserManageController") && StringUtils.equals(methodName, "login")) {
            log.info("拦截器放过的方法，类名：{}，方法名：{}", beanName, methodName);
            return true;
        }

//        在过滤登陆方法后记录日志是为了避免把账号密码的信息计入日志，这样很危险
        log.info("拦截的方法，类名：{}，方法名：{},请求参数：{}", beanName, methodName, parameters);

        User user = null;

        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isNotBlank(loginToken)) {
            String jsonStr = RedisShardedPoolUtil.get(loginToken);
            user = JsonUtil.string2Obj(jsonStr, User.class);
        }

        if (user == null || (user.getRole() != Const.Role.ROLE_ADMIN)) {
//       使用拦截器在验证未通过的情况下通过拦截器直接返回信息到前端，不会进入handler执行，所以不能通过handler的response进行返回；
//          又由于interceptor的方法返回值是boolean，所以需要手动的接管HttpServletResponse实例，向前端写信息；

//          首先清空response实例中存在的数据和信息，然后重新设置，不然会因为冲突出错
            response.reset();
//            设置响应的编码，防止返回的数据乱码
            response.setCharacterEncoding("UTF-8");
//            设置返回的内容类型
            response.setContentType("application/json;charset=UTF-8");

            PrintWriter out = response.getWriter();
            if (user == null) {
//                富文本上传的方法因为使用了simditor，对返回格式有特殊要求，所以单独判断处理
                if (StringUtils.equals(beanName, "ProductManageController") && StringUtils.equals(methodName, "richTextImgUpload")) {
                    Map resultMap = new HashMap();
                    resultMap.put("success", false);
                    resultMap.put("msg", "拦截器拦截，未登录");
                    out.write(JsonUtil.obj2String(resultMap));
                } else {
                    out.write(JsonUtil.obj2String(ServiceResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc())));
                }
            } else {
                if (StringUtils.equals(beanName, "ProductManageController") && StringUtils.equals(methodName, "richTextImgUpload")) {
                    Map resultMap = new HashMap();
                    resultMap.put("success", false);
                    resultMap.put("msg", "拦截器拦截，无管理员权限");
                    out.write(JsonUtil.obj2String(resultMap));
                } else {
                    out.write(JsonUtil.obj2String(ServiceResponse.createByErrorMessage("拦截器拦截，无管理员权限")));
                }
            }
            out.flush();
            out.close();

            return false;
        }
        return true;
    }

    private String getParameters(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<Map.Entry<String, String[]>> entries = parameterMap.entrySet();
        Iterator it = entries.iterator();
//        使用StringBuffer来拼接参数
        StringBuffer parameters = new StringBuffer();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            //避免把密码明文记录进日志
            if (StringUtils.equals(key, "password")) {
                continue;
            }
            Object value = entry.getValue();
            if (value instanceof String[]) {
                String[] values = (String[]) value;
                parameters.append(key).append("=").append(Arrays.toString(values));
            }
        }
        return parameters.toString();
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("afterCompletion");
    }
}
