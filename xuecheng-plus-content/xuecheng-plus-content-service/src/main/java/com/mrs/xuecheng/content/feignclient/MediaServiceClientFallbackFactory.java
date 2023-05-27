package com.mrs.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * description: MediaServiceClientFallbackFactory
 * date: 2023/5/26 19:20
 * author: MR.孙
 */
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {

    //拿到了熔断的异常信息throwable
    @Override
    public MediaServiceClient create(Throwable throwable) {
        //发生熔断上传服务调用此方法执行降级逻辑

        return new MediaServiceClient() {
            @Override
            public String upload(MultipartFile filedata, String objectName) {
                log.debug("远程调用上传文件的接口发生熔断:{}",throwable.toString(),throwable);
                return null;
            }
        };
    }
}
