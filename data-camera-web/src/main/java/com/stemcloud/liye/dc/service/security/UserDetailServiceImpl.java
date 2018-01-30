package com.stemcloud.liye.dc.service.security;

import com.stemcloud.liye.dc.dao.system.SysUserRepository;
import com.stemcloud.liye.dc.domain.system.SysUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Belongs to data-camera-web
 * Description:
 *  将用户权限交给spring security进行管控
 * @author liye on 2018/1/27
 */
@Service
public class UserDetailServiceImpl implements UserDetailsService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    SysUserRepository sysUserRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        SysUser user = sysUserRepository.findOne(userName);
        if (null == user){
            throw new UsernameNotFoundException("the user is not exist!");
        }
        return new SecurityUser(user);
    }
}
