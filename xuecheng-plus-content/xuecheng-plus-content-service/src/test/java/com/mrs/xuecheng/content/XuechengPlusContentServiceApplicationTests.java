package com.mrs.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mrs.xuecheng.base.model.PageResult;
import com.mrs.xuecheng.content.mapper.CourseBaseMapper;
import com.mrs.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.mrs.xuecheng.content.model.po.CourseBase;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class XuechengPlusContentServiceApplicationTests {


    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Test
    void contextLoads() {

        CourseBase courseBase = courseBaseMapper.selectById(22);
        Assertions.assertNotNull(courseBase);

        QueryCourseParamsDto courseParamsDto = QueryCourseParamsDto.builder()
                .courseName("java").build();

        LambdaQueryWrapper<CourseBase> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()), CourseBase::getName, courseParamsDto.getCourseName());
        Page<CourseBase> page = new Page<>(1, 2);

        courseBaseMapper.selectPage(page, lambdaQueryWrapper);

        List<CourseBase> records = page.getRecords();

        long total = page.getTotal();

        PageResult<CourseBase> pageResult = new PageResult<CourseBase>(records, total, 1L, 2L);
        System.out.println(pageResult);

    }

}
