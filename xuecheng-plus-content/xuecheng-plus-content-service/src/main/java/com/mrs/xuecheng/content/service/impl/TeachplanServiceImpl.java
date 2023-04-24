package com.mrs.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mrs.xuecheng.content.mapper.TeachplanMapper;
import com.mrs.xuecheng.content.model.dto.SaveTeachplanDto;
import com.mrs.xuecheng.content.model.dto.TeachplanDto;
import com.mrs.xuecheng.content.model.po.Teachplan;
import com.mrs.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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

    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {

        Long id = saveTeachplanDto.getId();
        Teachplan teachplan = teachplanMapper.selectById(id);

        if(teachplan==null){
            teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            //找到同级课程计划的数量
            int count = getTeachplanCount(saveTeachplanDto.getCourseId(), saveTeachplanDto.getParentid());
            //排序字段
            teachplan.setOrderby(count+1);

            teachplanMapper.insert(teachplan);

        }else{
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            //更新
            teachplanMapper.updateById(teachplan);

        }

    }

    /**
     *     计算机新课程计划的orderby 找到同级课程计划的数量 SELECT count(1) from teachplan where course_id=117 and parentid=268
     */
    public int getTeachplanCount(Long courseId,Long parentId){

        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count.intValue();

    }

}