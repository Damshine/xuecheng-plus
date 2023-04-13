package com.mrs.xuecheng.content.api;

import com.mrs.xuecheng.base.model.PageParams;
import com.mrs.xuecheng.base.model.PageResult;
import com.mrs.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.mrs.xuecheng.content.model.po.CourseBase;
import com.mrs.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * description: 课程信息编辑接口
 * date: 2023/4/13 9:27
 * author: MR.孙
 */
@Api(value = "课程信息编辑接口", tags = "课程信息编辑接口")
@RestController
public class CourseBaseInfoController {


    @Autowired
    private CourseBaseInfoService courseBaseInfoService;


    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParamsDto) {
        return courseBaseInfoService.queryCourseBaseList(pageParams,queryCourseParamsDto);

    }


}
