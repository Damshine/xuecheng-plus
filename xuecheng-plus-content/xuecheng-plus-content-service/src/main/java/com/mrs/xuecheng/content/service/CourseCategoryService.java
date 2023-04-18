package com.mrs.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrs.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.mrs.xuecheng.content.model.po.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-04-12
 */
public interface CourseCategoryService extends IService<CourseCategory> {

    /**
     * 课程分类树形结构查询
     * @param id
     * @return
     */
    List<CourseCategoryTreeDto> queryTreeNodes(String id);

}
