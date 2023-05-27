package com.mrs.xuecheng.content.feignclient;

import org.springframework.web.multipart.MultipartFile;

/**
 * description: MediaServiceClientFallback
 * date: 2023/5/26 19:19
 * author: MR.孙
 */
public class MediaServiceClientFallback implements MediaServiceClient{
    @Override
    public String upload(MultipartFile filedata, String objectName) {
        return null;
    }
}
