package com.stemcloud.liye.dc.config;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.Assert.*;

/**
 * Belongs to data-camera-web
 * Description:
 *
 * @author liye on 2018/1/27
 */
public class WebSecurityConfigTest {
    @Test
    public void passwordGenerator(){
        String name = "user";
        String password = "user";

        BCryptPasswordEncoder bc=new BCryptPasswordEncoder(4);
        System.out.println(bc.encode(password));
    }
}