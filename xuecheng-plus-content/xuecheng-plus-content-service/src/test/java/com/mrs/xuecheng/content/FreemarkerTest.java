package com.mrs.xuecheng.content;

import com.mrs.xuecheng.content.model.dto.CoursePreviewDto;
import com.mrs.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * description: FreemarkerTest
 * date: 2023/5/26 14:44
 * author: MR.孙
 */
@SpringBootTest
public class FreemarkerTest {

    @Autowired
    CoursePublishService coursePublishService;


    /**
     * 测试页面静态化
     */
    @Test
    public void testGenerateHtmlByTemplate() throws IOException, TemplateException {

        //Configuration对象指定模板目录和编码
        Configuration configuration = new Configuration(Configuration.getVersion());

        //加载模板
        //选指定模板路径,classpath下templates下
        //得到classpath路径
        String classpath  = this.getClass().getResource("/").getPath();
        configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
        //指定模板文件名称
        Template template = configuration.getTemplate("course_template.ftl");
        //设置字符编码
        configuration.setDefaultEncoding("utf-8");

        //准备课程预览页数据
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(118L);
        Map<String, Object> map = new HashMap<>();
        map.put("model", coursePreviewInfo);

        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        //将静态化内容输出到文件中
        InputStream inputStream = IOUtils.toInputStream(content);

        FileOutputStream fileOutputStream = new FileOutputStream(new File("D:\\下载文件\\118.html"));

        IOUtils.copy(inputStream, fileOutputStream);

    }





}
