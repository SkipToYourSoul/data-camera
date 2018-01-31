package com.stemcloud.liye.dc.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Service;

import javax.servlet.*;
import java.io.IOException;

/**
 * Belongs to data-camera-web
 * Description:
 *  judge user access before request
 * @author liye on 2018/1/27
 */
@Service
public class MySecurityFilterInterceptor extends AbstractSecurityInterceptor implements Filter {
    @Autowired
    private FilterInvocationSecurityMetadataSource securityMetadataSource;
    @Autowired
    public void setCustomAccessDecisionManager(AccessDecisionManagerImpl accessDecisionManager){
        super.setAccessDecisionManager(accessDecisionManager);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        FilterInvocation fi = new FilterInvocation( servletRequest, servletResponse, filterChain );
        InterceptorStatusToken token = super.beforeInvocation(fi);
        try{
            fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
        }finally{
            super.afterInvocation(token, null);
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public Class<?> getSecureObjectClass() {
        return FilterInvocation.class;
    }

    @Override
    public SecurityMetadataSource obtainSecurityMetadataSource() {
        return this.securityMetadataSource;
    }
}
