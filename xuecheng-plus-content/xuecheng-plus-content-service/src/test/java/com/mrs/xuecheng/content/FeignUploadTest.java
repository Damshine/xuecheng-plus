package com.mrs.xuecheng.content;

import com.mrs.xuecheng.content.config.MultipartSupportConfig;
import com.mrs.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * description: FeignUploadTest
 * date: 2023/5/26 17:35
 * author: MR.孙
 */
@SpringBootTest
public class FeignUploadTest {

    @Autowired
    MediaServiceClient mediaServiceClient;

    //远程调用，上传文件
    @Test
    public void test() {
        //将file转成MultipartFile
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("D:\\下载文件\\118.html"));
        //远程调用得到返回值
        String upload = mediaServiceClient.upload(multipartFile, "course/118.html");
        if (upload == null) {
            System.out.println("走了降级逻辑");
        }

    }



}
