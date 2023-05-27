package com.mrs.xuecheng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * description: ContentApplication
 * date: 2023/4/13 14:56
 * author: MR.孙
 */
@EnableFeignClients(basePackages={"com.mrs.xuecheng.content.feignclient"})
@SpringBootApplication
public class ContentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }

}
