package com.mrs.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.mrs.xuecheng.base.exception.CommonError;
import com.mrs.xuecheng.base.exception.XueChengPlusException;
import com.mrs.xuecheng.content.config.MultipartSupportConfig;
import com.mrs.xuecheng.content.feignclient.MediaServiceClient;
import com.mrs.xuecheng.content.mapper.CourseBaseMapper;
import com.mrs.xuecheng.content.mapper.CourseMarketMapper;
import com.mrs.xuecheng.content.mapper.CoursePublishMapper;
import com.mrs.xuecheng.content.mapper.CoursePublishPreMapper;
import com.mrs.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.mrs.xuecheng.content.model.dto.CoursePreviewDto;
import com.mrs.xuecheng.content.model.dto.TeachplanDto;
import com.mrs.xuecheng.content.model.po.CourseBase;
import com.mrs.xuecheng.content.model.po.CourseMarket;
import com.mrs.xuecheng.content.model.po.CoursePublish;
import com.mrs.xuecheng.content.model.po.CoursePublishPre;
import com.mrs.xuecheng.content.service.CourseBaseInfoService;
import com.mrs.xuecheng.content.service.CoursePublishService;
import com.mrs.xuecheng.content.service.TeachplanService;
import com.mrs.xuecheng.messagesdk.model.po.MqMessage;
import com.mrs.xuecheng.messagesdk.service.MqMessageService;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * description: CoursePublishServiceImpl
 * date: 2023/5/23 21:18
 * author: MR.孙
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    CoursePublishMapper coursePublishMapper;




    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        //课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);

        //课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplayTree(courseId);
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;

    }

    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {

        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if (courseBaseInfo == null) {
            XueChengPlusException.cast("课程找不到");
        }

        //审核状态
        String auditStatus = courseBaseInfo.getAuditStatus();
        //如果课程的审核状态为已提交则不允许提交
        if (auditStatus.equals("202003")) {
            XueChengPlusException.cast("课程已提交请等待审核");
        }

        //本机构只能提交本机构的课程
        //Todo 本机构只能提交本机构的课程


        //课程的图片、计划信息没有填写也不允许提交
        String pic = courseBaseInfo.getPic();
        if (StringUtils.isEmpty(pic)) {
            XueChengPlusException.cast("请求上传课程图片");
        }

        //查询课程计划
        //课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplayTree(courseId);
        if (teachplanTree == null || teachplanTree.size() == 0) {
            XueChengPlusException.cast("请编写课程计划");
        }

        //查询到课程基本信息、营销信息、计划等信息插入到课程预发布表
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);

        //设置机构id
        coursePublishPre.setCompanyId(companyId);
        //营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        //转json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);

        //计划信息
        //转json
        String teachplanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeJson);
        //状态为已提交
        coursePublishPre.setStatus("202003");
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());

        //查询预发布表，如果有记录则更新，没有则插入
        CoursePublishPre coursePublishPreObj = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreObj == null) {
            //插入
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            //更新
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本信息表的审核状态为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");//审核状态为已提交
        courseBaseMapper.updateById(courseBase);
    }


    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {

        //查询预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            XueChengPlusException.cast("课程没有审核记录，无法发布");
        }

        //状态
        String status = coursePublishPre.getStatus();
        //课程如果没有审核通过不允许发布
        if (!status.equals("202004")) {
            XueChengPlusException.cast("课程没有审核通过不允许发布");
        }

        //向课程发布表写入数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);


        //先查询课程发布，如果有则更新，没有再添加
        CoursePublish coursePublishObj = coursePublishMapper.selectById(courseId);
        if (coursePublishObj == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }

        //向消息表写入数据
        // mqMessageService.addMessage("course_publish", courseId.toString(), null, null);
        saveCoursePublishMessage(courseId);


        //将预发布表数据删除
        coursePublishPreMapper.deleteById(courseId);

    }

    /**
     * 课程页面静态化
     * @param courseId
     * @return
     */
    @Override
    public File generateCourseHtml(Long courseId) {

        Configuration configuration = new Configuration(Configuration.getVersion());
        File htmlFile = null;
        try {
            //拿到classpath
            // String classpath = this.getClass().getResource("/").getPath();
            // configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));

            //更改为如下方式
            configuration.setTemplateLoader(new ClassTemplateLoader(this.getClass().getClassLoader(), "/templates"));
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);
            HashMap<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //Template template 模板, Object model 数据
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            //输入流
            InputStream inputStream = IOUtils.toInputStream(html);
            //创建临时文件
            htmlFile = File.createTempFile("coursepublish", ".html");

            //输出文件
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            //使用流将html写入文件
            IOUtils.copy(inputStream, outputStream);

        } catch (Exception e) {
            log.error("页面静态化出现问题,课程id:{}", courseId, e);
            e.printStackTrace();
        }


        return htmlFile;
    }

    /**
     * 上传课程静态化页面
     * @param courseId
     * @param file
     */
    @Override
    public void uploadCourseHtml(Long courseId, File file) {

        try {
            //将file转成MultipartFile
            MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
            String upload = mediaServiceClient.upload(multipartFile, "course/" + courseId + ".html");

            if (upload == null) {
                log.debug("远程调用走降级逻辑得到上传的结果为null,课程id:{}", courseId);
                XueChengPlusException.cast("上传静态文件过程中存在异常");
            }
        } catch (Exception e) {
            e.printStackTrace();
            XueChengPlusException.cast("上传静态文件过程中存在异常");
        }

    }

    /**
     * 保存消息表记录
     * @param courseId 课程id
     */
    private void saveCoursePublishMessage(Long courseId) {
        //这里是直接注入service，不用远程调用，因为消息sdk是个通用的组件，不是服务
        //消息类型为：course_publish，即该消息是处理课程发布任务的；“关联业务信息1”是课程id。
        MqMessage mqMessage = mqMessageService.addMessage("course_publish"
                , String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }


    }


}
