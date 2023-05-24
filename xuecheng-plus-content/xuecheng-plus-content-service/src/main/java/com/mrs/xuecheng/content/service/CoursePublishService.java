package com.mrs.xuecheng.content.service;

import com.mrs.xuecheng.content.model.dto.CoursePreviewDto;

public interface CoursePublishService {

    /**
     * 获取课程预览信息
     * @param courseId 课程id
     * @return
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

}
