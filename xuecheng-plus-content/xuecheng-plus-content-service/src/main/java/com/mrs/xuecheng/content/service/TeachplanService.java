package com.mrs.xuecheng.content.service;

import com.mrs.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.mrs.xuecheng.content.model.dto.SaveTeachplanDto;
import com.mrs.xuecheng.content.model.dto.TeachplanDto;
import com.mrs.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;


/**
 * <p>
 * 课程计划 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-04-12
 */
public interface TeachplanService {

    List<TeachplanDto> findTeachplayTree(long courseId);

    void saveTeachplan(SaveTeachplanDto dto);


    TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

}
