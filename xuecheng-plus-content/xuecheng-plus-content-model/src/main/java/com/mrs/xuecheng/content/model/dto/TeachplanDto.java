package com.mrs.xuecheng.content.model.dto;

import com.mrs.xuecheng.content.model.po.Teachplan;
import com.mrs.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

/**
 * description: TeachplanDto
 * date: 2023/4/23 21:41
 * author: MR.孙
 */
@Data
public class TeachplanDto extends Teachplan {

    //课程计划关联的媒资信息
    TeachplanMedia teachplanMedia;
    //子结点
    List<TeachplanDto> teachPlanTreeNodes;

}
