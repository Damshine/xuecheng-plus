package com.xuecheng.auth.controller;

import com.xuecheng.auth.service.WxAuthService;
import com.xuecheng.ucenter.model.po.XcUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * description: WxLoginController
 * date: 2023/5/30 15:56
 * author: MR.孙
 */
@Slf4j
@Controller
public class WxLoginController {

    @Autowired
    WxAuthService wxAuthService;

    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) {

        log.debug("微信扫码回调,code:{},state:{}", code, state);

        //请求微信申请令牌，拿到令牌查询用户信息，将用户信息写入本项目数据库
        XcUser xcUser = wxAuthService.wxAuth(code);


        // XcUser xcUser = new XcUser();
        // //暂时硬编写，目的是调试环境
        // xcUser.setUsername("t1");

        if(xcUser==null){
            return "redirect:http://www.51xuecheng.cn/error.html";
        }
        String username = xcUser.getUsername();
        return "redirect:http://www.51xuecheng.cn/sign.html?username=" + username + "&authType=wx";

    }


}
