package com.mrs.xuecheng.content.model.dto;

import com.mrs.xuecheng.content.model.po.CourseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * description: CourseCategoryTreeDto
 * date: 2023/4/14 16:40
 * author: MR.孙
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CourseCategoryTreeDto extends CourseCategory {

    List<CourseCategoryTreeDto> childrenTreeNodes;

}
