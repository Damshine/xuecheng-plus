package com.mrs.xuecheng.content.service.impl;

import com.mrs.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.mrs.xuecheng.content.model.dto.CoursePreviewDto;
import com.mrs.xuecheng.content.model.dto.TeachplanDto;
import com.mrs.xuecheng.content.service.CourseBaseInfoService;
import com.mrs.xuecheng.content.service.CoursePublishService;
import com.mrs.xuecheng.content.service.TeachplanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * description: CoursePublishServiceImpl
 * date: 2023/5/23 21:18
 * author: MR.孙
 */
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;


    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        //课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);

        //课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplayTree(courseId);
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;

    }


}
