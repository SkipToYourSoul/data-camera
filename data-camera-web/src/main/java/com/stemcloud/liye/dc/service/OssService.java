package com.stemcloud.liye.dc.service;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectResult;
import com.stemcloud.liye.dc.oss.OSSClientFactory;
import com.stemcloud.liye.dc.oss.OSSClientProperties;
import com.stemcloud.liye.dc.oss.ObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Map;

/**
 * Belongs to data-camera-web
 * Description:
 *  upload or download file of oss
 * @author liye on 2018/1/24
 */
@Service
public class OssService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${oss.server.path}")
    private String baseServerUploadPath;

    @Value("${oss.cloud.path}")
    private String baseCloudUploadPath;

    @Value("${stem.server.path}")
    private String stemServerPath;

    private OSSClient client = OSSClientFactory.createOSSClient();

    public String uploadFileToOss(HttpServletRequest request, String from){
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = null;
        Map map =multipartRequest.getFileMap();
        for (Object obj : map.keySet()) {
            multipartFile = (MultipartFile) map.get(obj);
        }

        // 将文件先存到本地
        String fileName = multipartFile.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = "/share-" + from + "-" + System.currentTimeMillis() + suffix;
        String serverFilePath = baseServerUploadPath + newFileName;
        try {
            InputStream is = multipartFile.getInputStream();
            FileOutputStream fos = new FileOutputStream(new File(serverFilePath));
            byte [] b = new byte[1024];
            while (is.read(b)!=-1) {
                fos.write(b);
            }
            fos.close();
            is.close();
            logger.info("Save file at local server, fileName={} serverFilePath={}", fileName, serverFilePath);
        } catch (IOException e) {
            logger.error("An exception occured when save local file, original file={}", serverFilePath, e);
        }

        // 将文件上传至OSS
        String cloudFilePath = baseCloudUploadPath + newFileName;
        // OSS key cannot start with "/"
        String key = cloudFilePath.replaceFirst("/", "");
        try {
            FileInputStream fileInputStream = new FileInputStream(serverFilePath);
            PutObjectResult result = ObjectService.putObject(client, OSSClientProperties.bucketName, key, fileInputStream);
            fileInputStream.close();
            logger.info("upload file to aliyun OSS object server success. ETag: {}, key={}", result.getETag(), key);
        } catch (IOException e) {
            logger.error("An exception occured when copying file, original file={}", serverFilePath, e);
        }

        return stemServerPath + key;
    }
}
