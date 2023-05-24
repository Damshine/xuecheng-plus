package com.mrs.xuecheng.content.service;

import com.mrs.xuecheng.content.model.dto.CoursePreviewDto;

public interface CoursePublishService {

    /**
     * 获取课程预览信息
     * @param courseId 课程id
     * @return
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 根据机构id和课程id提交审核
     * @param companyId
     * @param courseId
     */
    void commitAudit(Long companyId, Long courseId);
}
