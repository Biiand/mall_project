package com.mmall.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 处理全局异常的类，实现HandlerExceptionResolver接口，
 * 该接口在org.springframework.web.servlet.handler.HandlerExceptionResolverComposite中被引用，
 * 当异常抛到Controller的handler时就会被这个接口的实现类处理；
 * 这个类的作用：将后台抛出的异常做处理后再给前端显示，如果不做处理，所有异常信息会暴露在用户面前，其中会包括项目的内部细节，
 * 这些信息泄露可能会让攻击者更轻易的攻破网站，非常危险；
 * 后台日志记录异常信息，定义发生异常时给前端显示的内容，在ModelAndView的构造器中传入使用Jackson将model内容转换为Json格式
 * 的MappingJacksonJsonView，最终将json序列化的数据返回给前端。
 * Spring MVC的DispatcherServlet的render方法会处理返回的ModelAndView,在这个方法中会调用View实例的render方法来实际完成对model的处理，
 * 经由MappingJacksonJsonView的父类的render方法，进入MappingJacksonJsonView的renderMergedOutputModel方法，再调用writeContent方法
 * 完成Json序列化和写入Response.
 *
 */
@Component
@Slf4j
public class ExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                         Object o, Exception e) {
//        将异常记录到日志，便于排查
        log.error("{} exception:",httpServletRequest.getRequestURL(),e);
//        使用ModelAndView(View view)这个构造器，MappingJacksonJsonView的父类实现了View接口，将数据序列化为json返回给前端
//          不使用MappingJackson2JsonView是因为这个类是Jackson 2.x才能使用，项目使用1.9
        ModelAndView modelAndView = new ModelAndView(new MappingJacksonJsonView());
        modelAndView.addObject("status", ResponseCode.ERROR.getCode());
        modelAndView.addObject("msg", "内部错误");
        modelAndView.addObject("data", e.toString());//返回异常的名称，也可以不返回
        return modelAndView;
    }
}
