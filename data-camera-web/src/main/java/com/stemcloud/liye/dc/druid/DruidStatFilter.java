package com.stemcloud.liye.dc.druid;

import com.alibaba.druid.support.http.WebStatFilter;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

/**
 * Created by liye on 3/10/17
 * Email: liye@qiyi.com
 * Description:
 * @author liye
 */

@WebFilter(filterName="druidWebStatFilter",urlPatterns="/*",
    initParams={
        @WebInitParam(name="exclusions",value="*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico,/druid/*")
    }
)

public class DruidStatFilter extends WebStatFilter {
}
