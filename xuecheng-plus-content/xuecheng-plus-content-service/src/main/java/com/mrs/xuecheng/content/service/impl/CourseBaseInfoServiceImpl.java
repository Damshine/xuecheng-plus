package com.mrs.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mrs.xuecheng.base.exception.XueChengPlusException;
import com.mrs.xuecheng.base.model.PageParams;
import com.mrs.xuecheng.base.model.PageResult;
import com.mrs.xuecheng.content.mapper.CourseBaseMapper;
import com.mrs.xuecheng.content.mapper.CourseCategoryMapper;
import com.mrs.xuecheng.content.mapper.CourseMarketMapper;
import com.mrs.xuecheng.content.model.dto.AddCourseDto;
import com.mrs.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.mrs.xuecheng.content.model.dto.EditCourseDto;
import com.mrs.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.mrs.xuecheng.content.model.po.CourseBase;
import com.mrs.xuecheng.content.model.po.CourseCategory;
import com.mrs.xuecheng.content.model.po.CourseMarket;
import com.mrs.xuecheng.content.service.CourseBaseInfoService;
import com.mrs.xuecheng.content.service.CourseMarketService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * description: CourseBaseInfoServiceImpl
 * date: 2023/4/13 16:33
 * author: MR.孙
 */
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private CourseMarketService courseMarketService;



    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        //构建查询条件，根据课程名称查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());
        //构建查询条件，根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        //构建查询条件，根据课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());
        //分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<CourseBase> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return courseBasePageResult;

    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {

        //合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            throw new XueChengPlusException("课程名称为空");
        }
        if (StringUtils.isBlank(dto.getMt())) {
            throw new XueChengPlusException("课程分类为空");
        }
        if (StringUtils.isBlank(dto.getSt())) {
            throw new XueChengPlusException("课程分类为空");
        }
        if (StringUtils.isBlank(dto.getGrade())) {
            throw new XueChengPlusException("课程等级为空");
        }
        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new XueChengPlusException("教育模式为空");
        }
        if (StringUtils.isBlank(dto.getUsers())) {
            throw new XueChengPlusException("适应人群为空");
        }
        if (StringUtils.isBlank(dto.getCharge())) {
            throw new XueChengPlusException("收费规则为空");
        }

        //新增对象
        CourseBase courseBaseNew = new CourseBase();
        //将填写的课程信息赋值给新增对象
        BeanUtils.copyProperties(dto, courseBaseNew);
        //设置审核状态
        courseBaseNew.setAuditStatus("202002");
        //设置发布状态
        courseBaseNew.setStatus("203001");
        //机构id
        courseBaseNew.setCompanyId(companyId);
        //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());

        //插入课程基础信息
        int insert = courseBaseMapper.insert(courseBaseNew);
        Long courseId = courseBaseNew.getId();

        //课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);
        courseMarket.setId(courseId);

        //收费规则
        String charge = dto.getCharge();

        //如果收费规则为收费，那么价格必须大于0
        if (charge.equals("201001")) {
            float price = dto.getPrice();
            if (price <= 0) {
                throw new XueChengPlusException("课程设置了收费价格不能为空且必须大于0");
            }
        }

        //插入课程营销信息
        int insert1 = courseMarketMapper.insert(courseMarket);
        if (insert<=0 || insert1<=0) {
            throw new XueChengPlusException("新增课程基本信息失败");
        }

        //添加成功
        //返回添加的课程数据
        return getCourseBaseInfo(courseId);
    }

    @Override
    public CourseBaseInfoDto getCourseBaseInfo(long courseId) {
        //根据课程id查询课程基本信息，包括基本信息和营销信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        LambdaQueryWrapper<CourseMarket> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(CourseMarket::getId, courseId);
        CourseMarket courseMarket = courseMarketMapper.selectOne(queryWrapper);
        if(courseBase == null || courseMarket == null){
            return null;
        }

        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        courseBaseInfoDto.setPrice(courseMarket.getPrice());
        courseBaseInfoDto.setCharge(courseMarket.getCharge());
        courseBaseInfoDto.setOriginalPrice(courseMarket.getOriginalPrice());
        courseBaseInfoDto.setQq(courseMarket.getQq());
        courseBaseInfoDto.setWechat(courseMarket.getWechat());
        courseBaseInfoDto.setPhone(courseMarket.getPhone());
        courseBaseInfoDto.setValidDays(courseMarket.getValidDays());


        //通过courseCategoryMapper查询分类信息，将分类名称放在courseBaseInfoDto对象
        CourseCategory mtObj = courseCategoryMapper.selectById(courseBase.getMt());
        String mtName = mtObj.getName();//大分类名称
        courseBaseInfoDto.setMtName(mtName);
        CourseCategory stObj = courseCategoryMapper.selectById(courseBase.getSt());
        String stName = stObj.getName();//小分类名称
        courseBaseInfoDto.setStName(stName);

        return courseBaseInfoDto;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {

        //课程id
        Long courseId = dto.getId();
        CourseBase courseBaseUpdate = courseBaseMapper.selectById(courseId);

        if(courseBaseUpdate==null){
            XueChengPlusException.cast("课程不存在");
        }

        if (!companyId.equals(courseBaseUpdate.getCompanyId())) {
            XueChengPlusException.cast("只允许修改本机构的课程");
        }

        BeanUtils.copyProperties(dto, courseBaseUpdate);

        //更新
        courseBaseUpdate.setChangeDate(LocalDateTime.now());
        courseBaseMapper.updateById(courseBaseUpdate);

        //查询营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);

        //收费课程必须写价格
  /*      if (charge.equals("201001")) {
            Float price = dto.getPrice();
            if (price == null || price.floatValue() <= 0) {
                XueChengPlusException.cast("课程设置了收费价格不能为空且必须大于0");
            }
        }
*/
        //请求数据库
        //对营销表有则更新，没有则添加
        saveCourseMarket(courseMarket);
        //查询课程信息
        CourseBaseInfoDto courseBaseInfo = this.getCourseBaseInfo(courseId);
        return courseBaseInfo;
    }

    private int saveCourseMarket(CourseMarket courseMarket) {
        String charge = courseMarket.getCharge();
        if(StringUtils.isBlank(charge)){
            XueChengPlusException.cast("收费规则没有选择");
        }
        if(charge.equals("201001")){
            if(courseMarket.getPrice()==null || courseMarket.getPrice().floatValue()<=0){
//                throw new RuntimeException("课程为收费价格不能为空且必须大于0");
                XueChengPlusException.cast("课程为收费价格不能为空且必须大于0");

            }
        }

        //保存
        boolean b = courseMarketService.saveOrUpdate(courseMarket);

        return b?1:0;

    }

    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        //课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        //组成要返回的数据
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);


        if (courseMarket!=null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        //向分类的名称查询出来
        CourseCategory courseCategory = courseCategoryMapper.selectById(courseBase.getMt());//一级分类
        courseBaseInfoDto.setMtName(courseCategory.getName());
        CourseCategory courseCategory2 = courseCategoryMapper.selectById(courseBase.getSt());//二级分类
        courseBaseInfoDto.setStName(courseCategory2.getName());

        return courseBaseInfoDto;
    }
}
