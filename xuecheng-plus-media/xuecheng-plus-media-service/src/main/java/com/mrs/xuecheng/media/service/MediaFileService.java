package com.mrs.xuecheng.media.service;


import com.mrs.xuecheng.base.model.PageParams;
import com.mrs.xuecheng.base.model.PageResult;
import com.mrs.xuecheng.base.model.RestResponse;
import com.mrs.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.mrs.xuecheng.media.model.dto.UploadFileParamsDto;
import com.mrs.xuecheng.media.model.dto.UploadFileResultDto;
import com.mrs.xuecheng.media.model.po.MediaFiles;

import java.io.File;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author Mr.M
     * @date 2022/9/10 8:57
     */
    PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName);

    MediaFiles addMediaFilesToDb(Long companyId, String fileId, UploadFileParamsDto uploadFileParamsDto, String bucket_files, String objectName);

    RestResponse<Boolean> checkFile(String fileMd5);

    RestResponse<Boolean> checkChunk(String fileMd5, int chunk);

    RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes);

    RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);

    MediaFiles getFileById(String mediaId);


    File downloadFileFromMinIO(File file, String bucket, String objectName);

    void addMediaFilesToMinIO(String filePath, String bucket, String objectName);

    /**
     * 根据文件md5，生成在minio中的文件路径
     * @param fileMd5       文件md5
     * @param extension     文件后缀名
     * @return
     */
    String getFilePathByMd5(String fileMd5, String extension);

    File downloadFileFromMinIO(String bucket, String objectName);

    boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName);

    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath, String objectName);
}
