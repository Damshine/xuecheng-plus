package com.mrs.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages={"com.mrs.xuecheng.content.feignclient"})
@EnableSwagger2Doc
@SpringBootApplication
public class XuechengPlusContentApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(XuechengPlusContentApiApplication.class, args);
    }

}
