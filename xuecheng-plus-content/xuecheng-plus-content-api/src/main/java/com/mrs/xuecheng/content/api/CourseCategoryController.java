package com.mrs.xuecheng.content.api;

import com.mrs.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.mrs.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * description: CourseCategoryController
 * date: 2023/4/14 16:43
 * author: MR.孙
 */
@RestController
@Api(value = "课程分类接口", tags = "课程分类接口")
public class CourseCategoryController {

    @Autowired
    private CourseCategoryService courseCategoryService;

    /**
     * 获取课程分类
     * @return
     */
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        return courseCategoryService.queryTreeNodes("1");
    }


}
