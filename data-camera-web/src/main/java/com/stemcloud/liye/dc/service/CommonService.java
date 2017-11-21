package com.stemcloud.liye.dc.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * Belongs to data-camera-web
 * Description:
 *  some common methods
 * @author liye on 2017/11/7
 */
@Service
public class CommonService {
    public String getCurrentLoginUser(HttpServletRequest request){
        /*SecurityContextImpl securityContextImpl = (SecurityContextImpl) request
                .getSession().getAttribute("SPRING_SECURITY_CONTEXT");
        if (securityContextImpl == null) {
            return null;
        }

        return securityContextImpl.getAuthentication().getName();*/
        return "root";
    }
}
