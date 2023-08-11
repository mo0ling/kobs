package com.kob.backend.Service.impl.user.account;

import com.kob.backend.Service.impl.util.UserDetailsImpl;
import com.kob.backend.Service.user.account.LoginService;
import com.kob.backend.pojo.User;
import com.kob.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
/**
 * loginService系统将输入的用户名和密码放入，调用ProviderManager的authentication，
 * provider.authenticate(authentication);DaoAuthenticationProvider 的父类是 AbstractUserDetailsAuthenticationProvider，实际上调用的是父类的 authentication()
 *--子类的 retrieveUser()，里面调用了 UserDetailsService.loadUserByUsername() 进行身份查找，UserDetailsServiceImpl重新方法调用sql
 *--子类的 additionalAuthenticationChecks()，里面调用 passwordEncoder.matches() 进行密码匹配
 *--子类的 createSuccessAuthentication()，认证完成，往 SecurityContext中放一个认证过的 auth 对象
 * 通过用户名查询账号信息和角色信息，统一存储在UserDetails中
 * 校验成功就构造一个认证过的 UsernamePasswordAuthenticationToken 对象放入 SecurityContext.
 * 之后PasswordEncoder会将查询到的账号密码和authentication中密码进行比对，密码不一致，则抛出异常（认证失败），密码正确则开始执行登录的业务，
 * 使用jwt生成token，将UserDetails存储到redis中，然后将token和角色id集返回给客户端。
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public Map<String, String> getToken(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);//账号密码封装

        Authentication authenticate = authenticationManager.authenticate(authenticationToken);  // 登录失败，会自动处理 用配置中的暴露认证管理器认证，认证不通过抛出异常403
        UserDetailsImpl loginUser = (UserDetailsImpl) authenticate.getPrincipal();//经过认证后，springSecurity会把查询到的用户信息封装到UserDetailsImpl，并且放到authentication的主体字段Principal
        User user = loginUser.getUser();
        String jwt = JwtUtil.createJWT(user.getId().toString());

        Map<String, String> map = new HashMap<>();
        map.put("error_message", "success");
        map.put("token", jwt);

        return map;
    }

}
