package com.quizai.cos;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 腾讯云 COS 工具类（支持CDN）
 */
@Component
public class TengxunCosUtil {

    @Value("${cos.secret-id}")
    private String secretId;

    @Value("${cos.secret-key}")
    private String secretKey;

    @Value("${cos.region}")
    private String region;

    @Value("${cos.bucket-name}")
    private String bucketName;

    @Value("${cos.cdn-domain}")
    private String cdnDomain;

    // 文件上传
    public String upload(String fileName, InputStream inputStream) {
        // 1. 创建COS客户端
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            // 2. 生成文件路径
            String objectName = "Ossimages/" + new SimpleDateFormat("yyyy/MM/dd").format(new Date())
                    + "/" + System.currentTimeMillis() + fileName.substring(fileName.lastIndexOf("."));

            // 3. 设置内容类型
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getContentType(fileName.substring(fileName.lastIndexOf("."))));

            // 4. 上传
            cosClient.putObject(bucketName, objectName, inputStream, metadata);

            // 返回 CDN 地址
            return cdnDomain + "/" + objectName;
        } finally {
            cosClient.shutdown();
        }
    }

    // 文件后缀处理
    private String getContentType(String FilenameExtension) {
        if (FilenameExtension.equalsIgnoreCase(".bmp")) {
            return "image/bmp";
        }
        if (FilenameExtension.equalsIgnoreCase(".gif")) {
            return "image/gif";
        }
        if (FilenameExtension.equalsIgnoreCase(".jpeg") ||
                FilenameExtension.equalsIgnoreCase(".jpg") ||
                FilenameExtension.equalsIgnoreCase(".png")) {
            return "image/jpg";
        }
        return "image/jpg";
    }
}
