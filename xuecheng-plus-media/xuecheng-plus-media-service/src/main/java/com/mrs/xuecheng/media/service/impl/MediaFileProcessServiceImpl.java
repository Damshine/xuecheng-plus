package com.mrs.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mrs.xuecheng.media.mapper.MediaFilesMapper;
import com.mrs.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.mrs.xuecheng.media.mapper.MediaProcessMapper;
import com.mrs.xuecheng.media.model.po.MediaFiles;
import com.mrs.xuecheng.media.model.po.MediaProcess;
import com.mrs.xuecheng.media.model.po.MediaProcessHistory;
import com.mrs.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * description: MediaFileProcessServiceImpl
 * date: 2023/5/22 11:56
 * author: MR.孙
 */
@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;


    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
    }

    @Override
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result > 0;
    }


    @Transactional
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //更新任务结果前，先判断是否有任务记录,如果不存在直接返回
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            return ;
        }

        //处理失败，更新MediaProcess状态
        LambdaQueryWrapper<MediaProcess> queryWrapperById  = new LambdaQueryWrapper<>();

        //处理失败
        if ("3".equals(status)) {
            MediaProcess mediaProcess_u = new MediaProcess();
            mediaProcess_u.setStatus("3");
            mediaProcess_u.setErrormsg(errorMsg);
            mediaProcess_u.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcessMapper.update(mediaProcess_u, queryWrapperById );
            log.debug("更新任务处理状态为失败，任务信息:{}",mediaProcess_u);
            return ;

        }

        //如果成功，更新media_file中url、并插入media_process_history表，并删除media_process记录
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles != null) {
            //更新媒资文件表中的访问url
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }

        //处理成功，更新url和状态
        mediaProcess.setUrl(url);
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcessMapper.updateById(mediaProcess);


        //添加到历史记录
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        //删除mediaProcess
        mediaProcessMapper.deleteById(mediaProcess.getId());

    }
}
