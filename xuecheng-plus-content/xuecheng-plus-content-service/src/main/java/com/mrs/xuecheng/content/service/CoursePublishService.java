package com.mrs.xuecheng.content.service;

import com.mrs.xuecheng.content.model.dto.CoursePreviewDto;
import com.mrs.xuecheng.content.model.po.CoursePublish;

import java.io.File;

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

    void publish(Long companyId, Long courseId);

    /**
     * 课程静态化
     * @param courseId
     * @return
     */
    File generateCourseHtml(Long courseId);

    void  uploadCourseHtml(Long courseId,File file);

    CoursePublish getCoursePublish(Long courseId);
}
