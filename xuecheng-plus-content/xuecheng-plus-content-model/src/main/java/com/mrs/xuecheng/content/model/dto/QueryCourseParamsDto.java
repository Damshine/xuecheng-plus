package com.mrs.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description: QueryCourseParamsDto
 * date: 2023/4/12 23:16
 * author: MR.孙
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class QueryCourseParamsDto {

    /**
     * 审核状态
     */
    @ApiModelProperty("审核状态")
    private String auditStatus;
    /**
     * 课程名称
     */
    @ApiModelProperty("课程名称")
    private String courseName;
    /**
     * 发布状态
     */
    @ApiModelProperty("发布状态")
    private String publishStatus;

}
