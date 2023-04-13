package com.mrs.xuecheng.content.service;

import com.mrs.xuecheng.base.model.PageParams;
import com.mrs.xuecheng.base.model.PageResult;
import com.mrs.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.mrs.xuecheng.content.model.po.CourseBase;

/**
 * description: CourseBaseInfoService
 * date: 2023/4/13 16:26
 * author: MR.孙
 */
public interface CourseBaseInfoService {


    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

}
