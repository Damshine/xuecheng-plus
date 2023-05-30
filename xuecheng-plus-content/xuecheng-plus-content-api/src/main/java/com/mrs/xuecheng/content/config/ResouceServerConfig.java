package com.mrs.xuecheng.content.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * description: ResouceServerConfig
 * date: 2023/5/28 22:58
 * author: MR.孙
 */
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(securedEnabled = true,prePostEnabled = true)
public class ResouceServerConfig extends ResourceServerConfigurerAdapter {

    //资源服务标识
    public static final String RESOURCE_ID = "xuecheng-plus";

    @Autowired
    TokenStore tokenStore;



    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId(RESOURCE_ID)
                .tokenStore(tokenStore)
                .stateless(true);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                //需要认证的请求
                // .antMatchers("/r/**","/course/**").authenticated()//所有匹配的请求必须经过认证
                .anyRequest().permitAll()    //全部请求放行
        ;


    }
}
