package com.stemcloud.liye.dc.service.security;

import com.stemcloud.liye.dc.domain.system.SysRole;
import com.stemcloud.liye.dc.domain.system.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Belongs to data-camera-web
 * Description:
 *  spring security 管理的 user 类
 * @author liye on 2018/1/27
 */
public class SecurityUser extends SysUser implements UserDetails {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public SecurityUser(SysUser sysUser) {
        if (sysUser != null){
            this.setUser(sysUser.getUser());
            this.setEmail(sysUser.getEmail());
            this.setPassword(sysUser.getPassword());
            this.setCreateTime(sysUser.getCreateTime());
            this.setRole(sysUser.getRole());
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        SysRole role = this.getRole();
        if (role != null){
            GrantedAuthority authority = new SimpleGrantedAuthority(role.getName());
            authorities.add(authority);
        }
        return authorities;
    }

    @Override
    public String getUsername() {
        return super.getUser();
    }

    @Override
    public String getPassword() {
        return super.getPassword();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
