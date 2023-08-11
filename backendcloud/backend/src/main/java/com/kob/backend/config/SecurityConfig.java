package com.kob.backend.config;

import com.kob.backend.config.filter.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//创建后端api之前要对数据库进行修改,放行登录、注册等接口
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    /**
     * token认证过滤器
     */
    @Autowired
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    /**
     * 强散列哈希加密实现
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 解决 无法直接注入 AuthenticationManager
     *
     * @return
     * @throws Exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();//2
    }
    /**
     * 用于拦截请求，并对请求进行处理
     * @param http
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {//4
        http.csrf().disable()//禁用跨域请求伪造的攻击，关闭csrf防护关闭Session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()//获得所有认证请求，任何请求都需要进行验证
                .antMatchers("/api/user/account/token/", "/api/user/account/register/").permitAll()//匹配指定的路径//permitAll()无需认证即可访问
                .antMatchers("/pk/start/game/", "/pk/receive/bot/move/").hasIpAddress("127.0.0.1")
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .anyRequest().authenticated();//获得任意请求//必须认证才允许访问

        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);//在UsernamePasswordAuthenticationFilter前添加认证过滤器
    }
    /***
     * 核心过滤器配置方法
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {//5
        web.ignoring().antMatchers("/websocket/**");//用来忽略URL地址，被忽略的URL不会被Security拦截，解决静态资源被拦截的问题
    }
}
