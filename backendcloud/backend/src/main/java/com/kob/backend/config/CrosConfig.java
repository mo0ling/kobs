package com.kob.backend.config;

import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
/**
 * 跨域过滤器
 */
@Configuration
public class CrosConfig implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        // 获取客户端域名 和 携带的请求头
        String origin = request.getHeader("Origin");
        if(origin!=null) {
            response.setHeader("Access-Control-Allow-Origin", origin);// 设置响应头，此处ip地址为需要访问服务器的ip及端口号 也可以设置为*表示所有域都可以通过；
        }

        String headers = request.getHeader("Access-Control-Request-Headers");
        if(headers!=null) {
            response.setHeader("Access-Control-Allow-Headers", headers);//表示的是允许跨域请求包含content-type头；
            response.setHeader("Access-Control-Expose-Headers", headers);
        }

        response.setHeader("Access-Control-Allow-Methods", "*");//表示的是允许跨域的请求方法；
        response.setHeader("Access-Control-Max-Age", "3600");// 设置预检请求的缓存时长，单位“秒”，表示的是在3600秒内，不需要再发送预检验请求，可以缓存该结果，一般默认。
        response.setHeader("Access-Control-Allow-Credentials", "true");//表示的是跨域的ajax中是否可以携带cookie，此时第一项设置不能为*，需指定域名；

        chain.doFilter(request, response);// 放行调用下一个过滤器。调用Controller2前
    }

    @Override
    public void init(FilterConfig filterConfig) {

    }//容器初始化过滤器时会被调用，且只会被调用一次。

    @Override
    public void destroy() {
    }//容器初始化过滤器时会被调用，且只会被调用一次。
}
