package com.mrs.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description: EditCourseDto
 * date: 2023/4/21 10:19
 * author: MR.孙
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="EditCourseDto", description="修改课程基本信息")
public class EditCourseDto extends AddCourseDto{

    @ApiModelProperty(value = "课程名称", required = true)
    private Long id;

}
