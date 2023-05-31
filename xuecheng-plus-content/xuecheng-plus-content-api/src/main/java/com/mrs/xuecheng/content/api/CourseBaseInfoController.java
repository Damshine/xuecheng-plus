package com.mrs.xuecheng.content.api;

import com.mrs.xuecheng.base.exception.ValidationGroups;
import com.mrs.xuecheng.base.model.PageParams;
import com.mrs.xuecheng.base.model.PageResult;
import com.mrs.xuecheng.content.model.dto.AddCourseDto;
import com.mrs.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.mrs.xuecheng.content.model.dto.EditCourseDto;
import com.mrs.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.mrs.xuecheng.content.model.po.CourseBase;
import com.mrs.xuecheng.content.service.CourseBaseInfoService;
import com.mrs.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 课程查询接口
     * @param pageParams
     * @param queryCourseParamsDto
     * @return
     */
    @ApiOperation("课程查询接口")
    @PreAuthorize("hasAnyAuthority('xc_teachmanager_course_list')")  // 拥有课程列表查询的接口方可查询
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParamsDto) {

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        //用户所属机构id
        Long companyId = null;

        if (StringUtils.isNotEmpty(user.getCompanyId())) {
            companyId = Long.parseLong(user.getCompanyId());
        }


        return courseBaseInfoService.queryCourseBaseList(companyId, pageParams,queryCourseParamsDto);

    }

    /**
     *新增课程基本信息
     * @return
     */
    @ApiOperation("新增课程基本信息")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated({ValidationGroups.Insert.class}) AddCourseDto addCourseDto) {
        //TODO 机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 22L;
        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }

    /**
     * 根据课程id查询课程基础信息
     * @param courseId
     * @return
     */
    @ApiOperation("根据课程id查询课程基础信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println(user);
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    /**
     * 修改课程信息
     * @param editCourseDto
     * @return
     */
    @ApiOperation("修改课程基础信息")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated EditCourseDto editCourseDto) {
        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
    }




}
