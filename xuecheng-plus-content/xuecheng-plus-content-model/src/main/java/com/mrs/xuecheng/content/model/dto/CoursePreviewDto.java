package com.mrs.xuecheng.content.model.dto;

import lombok.Data;

import java.util.List;

/**
 * description: CoursePreviewDto
 * date: 2023/5/23 21:11
 * author: MR.孙
 */
@Data
public class CoursePreviewDto {

    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;

    //课程计划信息
    List<TeachplanDto> teachplans;

    //师资信息暂时不加...


}
