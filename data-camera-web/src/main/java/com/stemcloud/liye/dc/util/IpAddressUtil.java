package com.stemcloud.liye.dc.util;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author liye at 2017/7/11
 */
public class IpAddressUtil {

    public static String getClientIpAddress(HttpServletRequest request){
        String ip = request.getHeader("X-Forwarded-For");
        if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip) || !ipFormat(ip)){
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip) || !ipFormat(ip)){
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip) || !ipFormat(ip)){
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip) || !ipFormat(ip)){
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip) || !ipFormat(ip)){
            ip = request.getHeader("X-Real-IP");
        }
        if (ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip) || !ipFormat(ip)){
            ip = request.getRemoteAddr();
        }

        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    private static boolean ipFormat(String ip){
        if (ip == null) {
            return false;
        }
        String ipRex = "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))";
        Pattern pattern = Pattern.compile(ipRex);
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }
}
