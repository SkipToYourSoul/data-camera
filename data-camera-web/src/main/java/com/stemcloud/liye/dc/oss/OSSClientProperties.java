package com.stemcloud.liye.dc.oss;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSSClientProperties {

	private static final Logger LOGGER = LoggerFactory.getLogger(OSSClientProperties.class);
	
	private static Properties OSSKeyProperties = new Properties();
	// 阿里云是否启用配置
	public static boolean useStatus = true;
	public static String bucketName = "stemcloud";
	public static String key = "5HcA7p0YhjZSYEqy";
	public static String secret = "OKwE2JMYr61vSRADpcti7gPfTVWkkw";
	public static boolean autoCreateBucket = false;
	
	public static String ossCliendEndPoint = "http://oss-cn-shanghai.aliyuncs.com";
	public static String ossEndPoint = "http://stemcloud.oss-cn-shanghai.aliyuncs.com";
	public static boolean useCDN = false;
	public static String cdnEndPoint = "http://cdn.xiexianbin.cn.w.kunlunar.com/";
	
	public static boolean useLocalStorager = false;
	public static String uploadBasePath = "upload";
	public static boolean useAsynUploader = false;
	
	public static String uploadsDir = "";
}