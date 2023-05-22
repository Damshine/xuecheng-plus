package com.mrs.xuecheng.media.service;

import com.mrs.xuecheng.media.model.po.MediaProcess;

import java.util.List;

public interface MediaFileProcessService {

    /**
     * 获取待处理任务
     * @param shardIndex
     * @param shardTotal
     * @param count
     * @return
     */
    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

    /**
     * 开启一个任务
     * @param id
     * @return
     */
    boolean startTask(long id);


    /**
     * 保存任务结果
     * 任务处理完成需要更新任务处理结果。
     * 任务执行成功更新视频的URL、及任务处理结果，将待处理任务记录删除，同时向历史任务表添加记录。
     * @param taskId
     * @param status
     * @param fileId
     * @param url
     * @param errorMsg
     */
    void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);

}
