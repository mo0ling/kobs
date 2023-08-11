package com.kob.backend.config.filter;

import com.kob.backend.Service.impl.util.UserDetailsImpl;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;

import com.kob.backend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
/**
 * token认证过滤器,OncePerRequestFilter确保在一次请求中只通过一次filter，而不会重复执行，进入Controller前
 * 用来验证 jwt token ，如果验证成功，则将 User 信息注入上下文中。
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private UserMapper userMapper;

    //controllerq前 1
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");//获取token

        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {// 判断token是否存在并以Bearer开头
            filterChain.doFilter(request, response);
            return;
        }
        //登录未退出，获取token解码，得到id查询

        token = token.substring(7);// 去掉token前缀

        String userid;
        try {
            Claims claims = JwtUtil.parseJWT(token);// 解析JWT
            userid = claims.getSubject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        User user = userMapper.selectById(Integer.parseInt(userid));

        if (user == null) {
            throw new RuntimeException("用户名未登录");
        }

        UserDetailsImpl loginUser = new UserDetailsImpl(user);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, null);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }
}