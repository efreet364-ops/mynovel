package io.github.novel.mynovel;

import cn.hutool.core.io.resource.ClassPathResource;
import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.OSSClientBuilder;
import com.aliyun.sdk.service.oss2.credentials.CredentialsProvider;
import com.aliyun.sdk.service.oss2.credentials.EnvironmentVariableCredentialsProvider;
import com.aliyun.sdk.service.oss2.exceptions.ServiceException;
import com.aliyun.sdk.service.oss2.models.*;
import com.aliyun.sdk.service.oss2.paginator.ListBucketsIterable;
import com.aliyun.sdk.service.oss2.transport.BinaryData;
import io.github.novel.mynovel.core.util.OssKeyUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;


import java.io.*;
import java.nio.file.Files;

//@SpringBootTest
public class OSSTest {

    @Value("${aliyun.oss.endpoint}")
    String endpoint;

    @Value("${aliyun.oss.region}")
    String region;

    @Value("${aliyun.oss.bucket-name}")
    String bucket;

    String key;


    @Test
    public void osstest1() {

        CredentialsProvider provider = new EnvironmentVariableCredentialsProvider();
        OSSClientBuilder clientBuilder = OSSClient.newBuilder()
                .credentialsProvider(provider)
                .region(region);

        try (OSSClient client = clientBuilder.build()) {

            ListBucketsIterable paginator = client.listBucketsPaginator(
                    ListBucketsRequest.newBuilder()
                            .build());

            for (ListBucketsResult result : paginator) {
                for (BucketSummary info : result.buckets()) {
                    System.out.printf("bucket: name:%s, region:%s, storageClass:%s\n", info.name(), info.region(), info.storageClass());
                }
            }

        } catch (Exception e) {
            ServiceException se = ServiceException.asCause(e);
            if (se != null) {
                System.out.printf("ServiceException: requestId:%s, errorCode:%s\n", se.requestId(), se.errorCode());
            }
            System.out.printf("error:\n%s", e);
        }
    }

    @Test
    void execute() {

        key = OssKeyUtils.genSimpleKey("test.jpg");

        CredentialsProvider provider = new EnvironmentVariableCredentialsProvider();
        OSSClientBuilder clientBuilder = OSSClient.newBuilder()
                .credentialsProvider(provider)
                .region(region);

        if (endpoint != null) {
            clientBuilder.endpoint(endpoint);
        }

        try (OSSClient client = clientBuilder.build()) {

            File file = new File("/Users/efreet233/Downloads/cat.jpg");
            byte[] content = Files.readAllBytes(file.toPath());

            ByteArrayInputStream data = new ByteArrayInputStream(content);

            PutObjectResult result = client.putObject(PutObjectRequest.newBuilder()
                    .bucket(bucket)
                    .key(key)
                    .body(BinaryData.fromStream(data))
                    .build());

            System.out.printf("status code:%d, request id:%s, eTag:%s\n",
                    result.statusCode(), result.requestId(), result.eTag());

        } catch (Exception e) {
            //If the exception is caused by ServiceException, detailed information can be obtained in this way.
            // ServiceException se = ServiceException.asCause(e);
            // if (se != null) {
            //    System.out.printf("ServiceException: requestId:%s, errorCode:%s\n", se.requestId(), se.errorCode());
            //}
            System.out.printf("error:\n%s", e);
        }
    }
}
