package com.stemcloud.liye.dc.config;

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

/**
 * Belongs to data-camera-web
 * Description:
 *  config denied page, filter http status 403/404
 * @author liye on 2017/11/9
 */
@Configuration
public class DeniedPageConfig implements EmbeddedServletContainerCustomizer {
    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        // session timeout, 单位为s
        container.setSessionTimeout(60 * 60 * 2);

        container.addErrorPages(new ErrorPage(HttpStatus.FORBIDDEN, "/denied?error403"));
        container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/denied?error404"));
    }
}
