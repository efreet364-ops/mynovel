package io.github.novel.mynovel.core.util;

import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.OSSClientBuilder;
import com.aliyun.sdk.service.oss2.credentials.CredentialsProvider;
import com.aliyun.sdk.service.oss2.credentials.EnvironmentVariableCredentialsProvider;
import com.aliyun.sdk.service.oss2.models.PutObjectRequest;
import com.aliyun.sdk.service.oss2.models.PutObjectResult;
import com.aliyun.sdk.service.oss2.transport.BinaryData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

@Component
public class AliyunOSSOperator {

    @Value("${aliyun.oss.endpoint}")
    String endpoint;

    @Value("${aliyun.oss.region}")
    String region;

    @Value("${aliyun.oss.bucket-name}")
    String bucket;

    public String upload(byte[] content, String originalFilename) throws Exception {
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        CredentialsProvider provider = new EnvironmentVariableCredentialsProvider();

        OSSClientBuilder clientBuilder = OSSClient.newBuilder()
                .credentialsProvider(provider)
                .region(region);

        // 根据文件名生成完整路径
        String key = OssKeyUtils.genSimpleKey(originalFilename);

        // 创建OSSClient实例
        OSSClient client = clientBuilder.build();

        // 上传文件
        ByteArrayInputStream data = new ByteArrayInputStream(content);

        PutObjectResult result = client.putObject(PutObjectRequest.newBuilder()
                .bucket(bucket)
                .key(key)
                .body(BinaryData.fromStream(data))
                .build());

        // 手动拼接图片访问url
        return endpoint.split("//")[0] + "//" + bucket + "." + endpoint.split("//")[1] + "/" + key;
    }
}
