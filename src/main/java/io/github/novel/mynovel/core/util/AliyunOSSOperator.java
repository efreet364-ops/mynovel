package io.github.novel.mynovel.core.util;

import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.OSSClientBuilder;
import com.aliyun.sdk.service.oss2.PresignOptions;
import com.aliyun.sdk.service.oss2.credentials.CredentialsProvider;
import com.aliyun.sdk.service.oss2.credentials.EnvironmentVariableCredentialsProvider;
import com.aliyun.sdk.service.oss2.models.GetObjectRequest;
import com.aliyun.sdk.service.oss2.models.PresignResult;
import com.aliyun.sdk.service.oss2.models.PutObjectRequest;
import com.aliyun.sdk.service.oss2.transport.BinaryData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;

@Component
public class AliyunOSSOperator {

    @Value("${aliyun.oss.endpoint}")
    String endpoint;

    @Value("${aliyun.oss.region}")
    String region;

    @Value("${aliyun.oss.bucket-name}")
    String bucket;

    @Value("${aliyun.oss.agent-bucket-name}")
    String agentBucket;

    @Value("${aliyun.oss.agent-url-expire-days:7}")
    long agentUrlExpireDays;

    @Value("${aliyun.oss.agent-prefix:agent/pdf/}")
    String agentPrefix;

    public String upload(byte[] content, String originalFilename) throws Exception {
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        // 根据文件名生成完整路径
        String key = OssKeyUtils.genSimpleKey(originalFilename);

        // 创建OSSClient实例
        try (OSSClient client = newClient()) {
            // 上传文件
            ByteArrayInputStream data = new ByteArrayInputStream(content);

            client.putObject(PutObjectRequest.newBuilder()
                    .bucket(bucket)
                    .key(key)
                    .body(BinaryData.fromStream(data))
                    .build());
        }

        // 手动拼接图片访问url
        return endpoint.split("//")[0] + "//" + bucket + "." + endpoint.split("//")[1] + "/" + key;
    }

    public void uploadAgentPdf(Path filePath, String objectKey, String originalFilename) throws Exception {
        try (OSSClient client = newClient()) {
            client.putObjectFromFile(PutObjectRequest.newBuilder()
                    .bucket(agentBucket)
                    .key(objectKey)
                    .contentType("application/pdf")
                    .contentDisposition(buildAttachmentDisposition(originalFilename))
                    .objectAcl("private")
                    .build(), filePath);
        }
    }

    public String generateAgentPdfDownloadUrl(String objectKey, String originalFilename) throws Exception {
        GetObjectRequest request = GetObjectRequest.newBuilder()
                .bucket(agentBucket)
                .key(objectKey)
                .build();

        PresignOptions options = PresignOptions.newBuilder()
                .expiration(Duration.ofDays(agentUrlExpireDays))
                .build();

        try (OSSClient client = newClient()) {
            PresignResult result = client.presign(request, options);
            return result.url();
        }
    }

    public long getAgentUrlExpireDays() {
        return agentUrlExpireDays;
    }

    public String getAgentPrefix() {
        return agentPrefix;
    }

    private OSSClient newClient() {
        CredentialsProvider provider = new EnvironmentVariableCredentialsProvider();
        OSSClientBuilder clientBuilder = OSSClient.newBuilder()
                .credentialsProvider(provider)
                .region(region);
        if (endpoint != null && !endpoint.isBlank()) {
            clientBuilder.endpoint(endpoint);
        }
        return clientBuilder.build();
    }

    private String buildAttachmentDisposition(String originalFilename) {
        String safeFileName = OssKeyUtils.sanitizePdfFileName(originalFilename);
        String fallbackFileName = safeFileName.replaceAll("[^A-Za-z0-9._-]", "_");
        String encodedFileName = URLEncoder.encode(safeFileName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return "attachment; filename=\"" + fallbackFileName + "\"; filename*=UTF-8''" + encodedFileName;
    }
}
