package com.github.unclecatmyself.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by MySelf on 2018/11/28.
 */
@Slf4j
//@Component
public class MyAsyncHandlerInterceptor implements AsyncHandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        log.info(Thread.currentThread().getName() + "服务端调用完成，返回结果给客户端");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        if (null != ex){
            System.out.println("发生异常：" + ex.getMessage());
        }
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //拦截后，重新写回数据，将原来的hello world换成如下字符
        String resp = "This is handler";
        response.setContentLength(resp.length());
        response.getOutputStream().write(resp.getBytes());

        log.info(Thread.currentThread().getName() + " 进入afterConcurrentHandlingStarted方法");
    }
}
