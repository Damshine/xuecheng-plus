package com.mrs.xuecheng.content.api;

import com.mrs.xuecheng.content.model.dto.CoursePreviewDto;
import com.mrs.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * description: CoursePublishController
 * date: 2023/5/23 18:29
 * author: MR.孙
 */
@Api(value = "课程发布相关接口",tags = "课程发布相关接口")
@Controller
public class CoursePublishController {

    @Autowired
    CoursePublishService coursePublishService;

    /**
     * 根据课程id提交审核
     * @param courseId
     */
    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId,courseId);
    }



    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){

        //获取课程预览信息
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);


        ModelAndView modelAndView = new ModelAndView();
        //指定模型
        modelAndView.addObject("model",coursePreviewInfo);
        //指定模板
        modelAndView.setViewName("course_template");//根据视图名称加“.ftl”找到模板，即course_template.ftl
        return modelAndView;
    }



    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping ("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        coursePublishService.publish(companyId,courseId);
    }


}
