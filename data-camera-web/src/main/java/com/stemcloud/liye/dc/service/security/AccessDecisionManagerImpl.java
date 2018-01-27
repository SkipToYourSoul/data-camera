package com.stemcloud.liye.dc.service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * Belongs to data-camera-web
 * Description:
 *  access decision manager, 用于判断当前用户访问当前url是否有权限
 * @author liye on 2018/1/27
 */
@Service
public class AccessDecisionManagerImpl implements AccessDecisionManager {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void decide(Authentication authentication, Object o, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {
        if (null == configAttributes || configAttributes.size() < 0){
            return;
        }
        HttpServletRequest request = ((FilterInvocation) o).getHttpRequest();
        logger.info("[access decision manager] Request url: {}, method: {}", request.getRequestURI(), request.getMethod());

        for (ConfigAttribute ca : configAttributes) {
            String needRole = ca.getAttribute();
            for (GrantedAuthority ga: authentication.getAuthorities()){
                logger.info("[access decision manager] Need role is {}, current role is {}", needRole, ga.getAuthority().trim());
                if (needRole.trim().equals(ga.getAuthority().trim())) {
                    logger.info("[access decision manager] Authentication passed!");
                    return;
                }
            }
        }
        throw new AccessDeniedException("Authentication denied!");
    }

    @Override
    public boolean supports(ConfigAttribute configAttribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
