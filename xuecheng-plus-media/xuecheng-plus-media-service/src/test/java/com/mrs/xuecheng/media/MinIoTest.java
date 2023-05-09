package com.mrs.xuecheng.media;

import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * description: MinIoTest
 * date: 2023/4/25 19:50
 * author: MR.孙
 */
public class MinIoTest {

    @Test
    void testUpload() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint("http://192.168.187.129:9000")
                        .credentials("minioadmin", "minioadmin")
                        .build();

        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket("test")
                        .object("商城项目.mp4")
                        .filename("D:\\下载文件\\商城项目.mp4")
                        .build());

    }


}
