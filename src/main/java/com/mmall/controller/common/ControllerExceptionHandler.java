package com.mmall.controller.common;

import com.mmall.common.ServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * ControllerAdvice用来强化Controller，因为并没有在springMVC的配置文件中添加自动生成aop代理的配置，但注解起作用了，所以推测并不是使用aop来实现的
 * 使用ControllerAdvice + ExceptionHandler来实现Controller的全局异常处理；
 * @ControllerAdvice 用来表明被注解的类是用来增强Controller的
 * @ResponseBody 注解在类上就会对类中的所有方法起作用
 * @ResponseStatus 用来返回http标准的异常码，根据不同的异常使用不同的异常码，reason指定默认的响应信息，
 *  也可以直接注解在自定义的异常类上；
 * @ExceptionHandler 用来指定处理的运行时异常类型，最终还是通过handlerExceptionResolver的实现类来处理异常，
 *  当Controller的handler抛出异常时，由匹配异常的方法进行处理，更细化的异常处理可以自定义运行时异常，并在业务中抛给handler，然后在这里处理
 *  ControllerAdvice被Component注解，要使该类起作用，需要在springMVC的配置文件中将该该类加入包扫描，被springMVC容器管理
 */
@ControllerAdvice
@ResponseBody
@Slf4j
public class ControllerExceptionHandler {

    //在前端或者对接方需要获得http状态码时添加ResponseStatus，添加后返回的json就不会显示了
    //@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "服务器内部错误")
    @ExceptionHandler(Exception.class)
    public ServiceResponse handlerException(HttpServletRequest request, Exception e) {
        log.error("{},exception",request.getRequestURI(),e);
        //添加了ResponseStatus注解后下面的返回值就不起作用，返回的是一个原始的状态码页面
        return ServiceResponse.createByErrorMessage("server inner error");
    }
}
