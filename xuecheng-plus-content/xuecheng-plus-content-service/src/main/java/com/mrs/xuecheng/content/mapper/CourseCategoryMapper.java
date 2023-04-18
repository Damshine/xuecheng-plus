package com.mrs.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mrs.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.mrs.xuecheng.content.model.po.CourseCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    //根据id查询课程分类树状结构
    List<CourseCategoryTreeDto> selectTreeNodes(@Param("id") String id);

}
