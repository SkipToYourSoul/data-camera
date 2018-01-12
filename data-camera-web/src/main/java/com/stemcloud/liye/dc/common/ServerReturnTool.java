package com.stemcloud.liye.dc.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Belongs to data-camera-web
 * Description:
 *  return status of server
 * @author liye on 2017/11/28
 */
public class ServerReturnTool {
    public static Map<String, Object> serverSuccess(Object obj){
        Map<String, Object> map = new HashMap<String, Object>(2);
        map.put("code", ReturnCode.SUCCESS.getCode());
        map.put("data", obj);
        return map;
    }

    public static Map<String, Object> serverFailure(String reason){
        Map<String, Object> map = new HashMap<String, Object>(1);
        map.put("code", ReturnCode.FAILURE.getCode());
        map.put("data", reason);
        return map;
    }

    private enum ReturnCode {
        /**
         * success, failure
         */
        SUCCESS("0000"), FAILURE("1111");
        private String code;

        ReturnCode(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
