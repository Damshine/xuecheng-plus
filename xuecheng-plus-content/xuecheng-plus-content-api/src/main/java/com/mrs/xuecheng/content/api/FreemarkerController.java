package com.mrs.xuecheng.content.api;

import io.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * description: FreemarkerController
 * date: 2023/5/23 11:15
 * author: MR.孙
 */
@Api(value = "测试FreeMarker",tags = "测试FreeMarker")
@Controller
public class FreemarkerController {


    @GetMapping("/testfreemarker")
    public ModelAndView test() {

        ModelAndView modelAndView = new ModelAndView();
        //设置模型数据
        modelAndView.addObject("name","小明");
        //设置模板名称
        modelAndView.setViewName("test");
        return modelAndView;


    }


}
