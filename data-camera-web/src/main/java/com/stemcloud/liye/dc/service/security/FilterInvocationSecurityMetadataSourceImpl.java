package com.stemcloud.liye.dc.service.security;

import com.google.gson.Gson;
import com.stemcloud.liye.dc.dao.system.SysRoleRepository;
import com.stemcloud.liye.dc.domain.system.SysResource;
import com.stemcloud.liye.dc.domain.system.SysRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Belongs to data-camera-web
 * Description:
 *  根据user -> role -> resource的关系定义权限map
 *  url pattern请求进来时，查看哪些role有权限访问
 * @author liye on 2018/1/27
 */
@Service
public class FilterInvocationSecurityMetadataSourceImpl implements FilterInvocationSecurityMetadataSource {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SysRoleRepository sysRoleRepository;
    private static Map<String, Collection<ConfigAttribute>> resourceMap = null;
    /**
     * 初始化权限配置
     */
    @PostConstruct
    private void initResourceDefine(){
        resourceMap = new HashMap<String, Collection<ConfigAttribute>>(16);
        for (SysRole role : sysRoleRepository.findAll()) {
            String auth = role.getName();
            ConfigAttribute ca = new SecurityConfig(auth);
            Set<SysResource> resources = role.getSysResources();
            for (SysResource resource : resources) {
                String key = resource.getResourceUrl();
                Collection<ConfigAttribute> value = new ArrayList<ConfigAttribute>();
                if (resourceMap.containsKey(key)) {
                    value = resourceMap.get(key);
                }
                value.add(ca);
                resourceMap.put(key, value);
            }
        }
        logger.info("[filter invocation security meta data source] resourceMap: {}", new Gson().toJson(resourceMap));
    }

    /**
     * 返回结果给CustomAccessDecisionManager的decide方法
     * @param o
     * @return
     * @throws IllegalArgumentException
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object o) throws IllegalArgumentException {
        FilterInvocation filterInvocation = (FilterInvocation) o;
        RequestMatcher requestMatcher;
        for (String resURL : resourceMap.keySet()) {
            requestMatcher = new AntPathRequestMatcher(resURL);
            if (requestMatcher.matches(filterInvocation.getHttpRequest())) {
                return resourceMap.get(resURL);
            }
        }
        return null;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
