package com.kob.backend.Service.impl.util;

import com.kob.backend.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }//获得权限集合

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }//判断账户是否过期(有效)

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }//判断账户是否被冻结

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }//凭证是否过期

    @Override
    public boolean isEnabled() {
        return true;
    }//账户是否启用
}
