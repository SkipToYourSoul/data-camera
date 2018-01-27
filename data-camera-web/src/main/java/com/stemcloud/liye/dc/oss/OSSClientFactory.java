package com.stemcloud.liye.dc.oss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliyun.oss.OSSClient;

public class OSSClientFactory {
	
	private static Logger logger = LoggerFactory.getLogger(OSSClientFactory.class);
	private static OSSClient client = null;
	
	/**
	 * 新建OSSClient 
	 * 
	 * @return
	 */
	public static OSSClient createOSSClient(){
		if ( null == client){
			client = new OSSClient(OSSClientProperties.ossCliendEndPoint, OSSClientProperties.key, OSSClientProperties.secret);
			logger.info("Initializing CreateOSSClient success.");
		}
		return client;
	}

}
