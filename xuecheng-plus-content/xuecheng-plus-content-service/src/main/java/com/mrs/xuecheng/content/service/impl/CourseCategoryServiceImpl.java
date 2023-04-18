package com.mrs.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrs.xuecheng.content.mapper.CourseCategoryMapper;
import com.mrs.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.mrs.xuecheng.content.model.po.CourseCategory;
import com.mrs.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 课程分类 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;


    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {

        //查询数据库得到的课程分类
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        //最终列表
        List<CourseCategoryTreeDto> categoryTreeDtos = new ArrayList<>();
        HashMap<String, CourseCategoryTreeDto> mapTemp = new HashMap<>();


        //其实就是先找到根目录下的二级结点，添加到集合中(就是最后返回的list)  遍历的过程中还要把当前结点的父节点找到，并设置当前结点到父结点即可
        courseCategoryTreeDtos.stream().forEach(item->{
            //临时保存每个结点
            mapTemp.put(item.getId(), item);
            //只将根节点的下级结点(第二级结点)保存到list中
            if (id.equals(item.getParentid())) {
                //如果是根节点下的二级结点，则添加
                categoryTreeDtos.add(item);
            }

            //拿到当前结点的上级结点并把下级结点的数据设置其childrenTreeNodes属性
            CourseCategoryTreeDto courseCategoryTreeDto = mapTemp.get(item.getParentid());
            if (courseCategoryTreeDto!=null) {
                //如果childrenTreeNodes为空则创建对象
                if (courseCategoryTreeDto.getChildrenTreeNodes()==null) {
                    courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                //向结点的下级结点list加入结点
                courseCategoryTreeDto.getChildrenTreeNodes().add(item);
            }

        });

        return categoryTreeDtos;
    }
}
