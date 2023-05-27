package com.mrs.xuecheng.content.service.jobhandler;

import com.mrs.xuecheng.base.exception.XueChengPlusException;
import com.mrs.xuecheng.content.mapper.CoursePublishMapper;
import com.mrs.xuecheng.content.model.po.CoursePublish;
import com.mrs.xuecheng.content.service.CoursePublishService;
import com.mrs.xuecheng.messagesdk.model.po.MqMessage;
import com.mrs.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.mrs.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * description: CoursePublishTask
 * date: 2023/5/26 11:17
 * author: MR.孙
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;

    @Autowired
    CoursePublishMapper coursePublishMapper;


    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();//执行器的序号，从0开始
        int shardTotal = XxlJobHelper.getShardTotal();//执行器总数
        //调用抽象类的方法执行任务。查询任务列表、设置计数器防止无限等待，遍历开启线程执行下面重写的execute方法
        process(shardIndex, shardTotal, "course_publish",30,60);


    }


    //执行课程发布任务的逻辑,如果此方法抛出异常说明任务执行失败
    @Override
    public boolean execute(MqMessage mqMessage) {

        //从mqMessage拿到课程id
        Long courseId = Long.parseLong(mqMessage.getBusinessKey1());

        //课程静态化上传到minio
        generateCourseHtml(mqMessage,courseId);

        //课程索引
        saveCourseIndex(mqMessage,courseId);

        //课程缓存
        saveCourseCache(mqMessage,courseId);

        return true;
    }

    //课程缓存
    private void saveCourseCache(MqMessage mqMessage, Long courseId) {

    }


    //TODO:保存课程索引信息
    private void saveCourseIndex(MqMessage mqMessage, Long courseId) {

        //任务id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        //取出第二个阶段状态
        int stageTwo = mqMessageService.getStageTwo(taskId);

        //任务幂等性处理
        if(stageTwo>0){
            log.debug("课程索引信息已写入，无需执行...");
            return;
        }

        //查询课程信息，调用搜索服务添加索引接口
        //从课程发布表查询课程信息
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);

        //完成本阶段任务
        mqMessageService.completedStageTwo(taskId);
    }



    //生成课程静态化页面并上传至文件系统
    private void generateCourseHtml(MqMessage mqMessage, Long courseId) {
        //消息id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();

        //做任务幂等性处理
        //查询数据库取出该阶段执行状态
        int stageOne = mqMessageService.getStageOne(taskId);
        if(stageOne > 0){
            log.debug("课程静态化任务完成，无需处理...");
            return ;
        }

        //开始进行课程静态化 生成html页面

        File file = coursePublishService.generateCourseHtml(courseId);
        if(file == null){
            XueChengPlusException.cast("生成的静态页面为空");
        }

        // 将html上传到minio
        coursePublishService.uploadCourseHtml(courseId,file);

        //..任务处理完成写任务状态为完成
        mqMessageService.completedStageOne(taskId);


    }
}
