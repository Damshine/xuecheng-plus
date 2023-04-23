package com.mrs.xuecheng.content.service.impl;

import com.mrs.xuecheng.content.mapper.TeachplanMapper;
import com.mrs.xuecheng.content.model.dto.TeachplanDto;
import com.mrs.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    private TeachplanMapper teachplanMapper;


    @Override
    public List<TeachplanDto> findTeachplayTree(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }
}
