package com.mrs.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.mrs.xuecheng.base.exception.XueChengPlusException;
import com.mrs.xuecheng.base.model.PageParams;
import com.mrs.xuecheng.base.model.PageResult;
import com.mrs.xuecheng.base.model.RestResponse;
import com.mrs.xuecheng.media.mapper.MediaFilesMapper;
import com.mrs.xuecheng.media.service.MediaFileService;
import com.mrs.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.mrs.xuecheng.media.model.dto.UploadFileParamsDto;
import com.mrs.xuecheng.media.model.dto.UploadFileResultDto;
import com.mrs.xuecheng.media.model.po.MediaFiles;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaFileService currentProxy;

    //普通文件桶
    @Value("${minio.bucket.files}")
    private String bucket_Files;

    //视频文件存储的桶
    @Value("${minio.bucket.videofiles}")
    private String bucket_videofiles;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }


    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {

        //生成文件id，文件的md5值
        String fileId = DigestUtils.md2Hex(bytes);
        //文件名称
        String filename = uploadFileParamsDto.getFilename();

        //构造objectname
        if (StringUtils.isEmpty(objectName)) {
            objectName = fileId + filename.substring(filename.lastIndexOf("."));
        }

        //目录名
        if (StringUtils.isEmpty(folder)) {
            //通过日期构造文件存储路径
            folder = getFileFolder(new Date(), true, true, true);
        } else if (folder.indexOf("/") < 0) {
            folder = folder + "/";
        }

        //对象名称 = 目录名/对象名
        objectName = folder + objectName;
        MediaFiles mediaFiles = null;


        try {
            //上传至文件系统
            addMediaFilesToMinIO(bytes, bucket_Files, objectName);
            //写入文件表
            mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileId, uploadFileParamsDto, bucket_Files , objectName);
            UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
            BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
            return uploadFileResultDto;
       
        } catch (Exception e) {
            e.printStackTrace();
            XueChengPlusException.cast("上传过程中出错");

        }


        return null;
    }

    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileId, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        //从数据库查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles == null) {
            //没有此文件信息
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileId);
            mediaFiles.setFileId(fileId);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setFilePath(objectName);

            //保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                XueChengPlusException.cast("保存文件信息失败");
            }
        }

        return mediaFiles;
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {

        //查询文件信息
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {

            //获取桶
            String bucket = mediaFiles.getBucket();
            //获取存储目录
            String filePath = mediaFiles.getFilePath();

            //文件流
            InputStream stream = null;
            //数据库存在，然后判断在minio中是否存在
            try {
                stream = minioClient.getObject(
                        GetObjectArgs.builder()
                        .bucket(bucket)
                        //文件名
                        .object(filePath)
                        .build());
            } catch (Exception e) {
                //文件已存在
                return  RestResponse.success(true);
            }
        }


        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //得到分块文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;
        //文件流
        InputStream fileInputStream = null;

        try {
            fileInputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket_videofiles)
                            .object(chunkFilePath)
                            .build());

            if (fileInputStream != null) {
                //分块已存在
                return RestResponse.success(true);
            }
        } catch (Exception e) {

        }
        //分块不存在
        return RestResponse.success(false);
    }


    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + "chunk" + "/";
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes) {

        //得到分块文件的目录路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunk;

        try {
            //将文件存储至minIO
            addMediaFilesToMinIO(bytes, bucket_videofiles, chunkFilePath);

        } catch (Exception ex) {
            ex.printStackTrace();
            return RestResponse.validfail(false,"上传分块失败");
        }
        return RestResponse.success();

    }


    /**
     * 下载分块
     * @param fileMd5
     * @param  chunkTotal 分块数量
     * @return java.io.File[] 分块文件数组
     */
    private File[] checkChunkStatus(String fileMd5, int chunkTotal) {

        //得到分块文件的目录路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File[] files = new File[chunkTotal];
        //检查分块文件是否上传完毕
        for (int i = 0; i < chunkTotal; i++) {
            String chunkFilePath = chunkFileFolderPath + i;
            //下载文件
            File chunkFile = null;
            try {
                chunkFile = File.createTempFile("chunk" + i, null);
            } catch (IOException e) {
                e.printStackTrace();
                XueChengPlusException.cast("下载分块时创建临时文件出错");
            }

            downloadFileFromMinIO(chunkFile, bucket_videofiles, chunkFilePath);
            files[i] = chunkFile;
        }

        return files;
    }

    /**
     * 根据桶和文件路径从minio下载文件
     * @param file
     * @param bucket
     * @param objectName
     * @return
     */
    private File downloadFileFromMinIO(File file, String bucket, String objectName) {

        InputStream fileInputStream = null;
        OutputStream fileOutputStream = null;
        try {
            fileInputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build());
            try {
                fileOutputStream = new FileOutputStream(file);
                IOUtils.copy(fileInputStream, fileOutputStream);

            } catch (IOException e) {
                XueChengPlusException.cast("下载文件" + objectName + "出错");
            }


        } catch (Exception e) {
            e.printStackTrace();
            XueChengPlusException.cast("文件不存在" + objectName);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }


    /**
     * 合并分块前要检查分块文件是否全部上传完成，如果完成则将已经上传的分块文件下载下来，然后再进行合并
     * @param companyId
     * @param fileMd5
     * @param chunkTotal
     * @param uploadFileParamsDto
     * @return
     */
    @Override
    public RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {

        //下载分块
        File[] chunkFiles = checkChunkStatus(fileMd5, chunkTotal);

        //得到合并后文件的扩展名
        String filename = uploadFileParamsDto.getFilename();
        //获取扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //声明临时合并文件
        File tempMergeFile = null;

        try {

            try {
                //创建一个临时文件作为分块合并后的文件
                tempMergeFile = File.createTempFile("merge", extension);
            } catch (IOException e) {
                XueChengPlusException.cast("创建临时合并文件出错");
            }


        //创建合并分块的流
        try ( RandomAccessFile raf_w = new RandomAccessFile(tempMergeFile, "rw")) {
            byte[] b = new byte[1024];
            //遍历每个分块
            for (File chunkFile : chunkFiles) {
                try ( RandomAccessFile raf_r = new RandomAccessFile(chunkFile, "r")) {
                    int len = -1;
                    while ((len = raf_r.read(b)) != -1) {
                        raf_w.write(b, 0, len);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            XueChengPlusException.cast("合并文件过程出错");
        }


        //校验合并后的文件是否正确
        try {
            FileInputStream mergeFileInputStream = new FileInputStream(tempMergeFile);
            //合并后文件的md5值
            String mergeMd5Hex = DigestUtils.md5Hex(mergeFileInputStream);

            //校验
            if ( !fileMd5.equals(mergeMd5Hex) ) {
                log.debug("合并文件校验不通过,文件路径:{},原始文件md5:{}", tempMergeFile.getAbsolutePath(), fileMd5);
                XueChengPlusException.cast("合并文件校验不通过");
            }
        } catch (IOException e) {
            log.debug("合并文件校验出错,文件路径:{},原始文件md5:{}", tempMergeFile.getAbsolutePath(), fileMd5);
            XueChengPlusException.cast("合并文件校验出错");
        }

        //拿到合并文件在minio中的存储路径
        String mergeFilePath = getFilePathByMd5(fileMd5, extension);
        //将合并文件上传到minio中
        addMediaFilesToMinIO(tempMergeFile.getAbsolutePath(), bucket_videofiles, mergeFilePath);


        //将文件信息入库
        uploadFileParamsDto.setFileSize(tempMergeFile.length());
        addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_videofiles, mergeFilePath);

        return RestResponse.success(true);
        } finally {
            //删除临时分块文件
            if (chunkFiles != null) {
                for (File chunkFile : chunkFiles) {
                    if (chunkFile.exists()) {
                        chunkFile.delete();
                    }
                }
            }
            //删除合并的临时文件
            if(tempMergeFile!=null){
                tempMergeFile.delete();
            }

        }
    }

    private String getFilePathByMd5(String fileMd5, String extension) {

        return fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/"  + fileMd5 + extension;

    }

    //将文件上传到文件系统
    public void addMediaFilesToMinIO(String filePath, String bucket, String objectName){
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(filePath)
                    .build();
            //上传
            minioClient.uploadObject(uploadObjectArgs);
        } catch (Exception e) {
            XueChengPlusException.cast("文件上传到文件系统失败");
        }
    }


    //将文件上传到分布式文件系统
    private void addMediaFilesToMinIO(byte[] bytes, String bucket, String objectName) {

        //资源的媒体类型
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//默认未知二进制流

        if (objectName.indexOf(".") >= 0) {
            //取objectName中的扩展名
            String extension = objectName.substring(objectName.lastIndexOf("."));
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            if (extensionMatch != null) {
                contentType = extensionMatch.getMimeType();
            }

        }


        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    //InputStream stream, long objectSize 对象大小, long partSize 分片大小(-1表示5M,最大不要超过5T，最多10000)
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .contentType(contentType)
                    .build();
            //上传到minio
            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            XueChengPlusException.cast("上传文件到文件系统出错");
        }
    }


    /**
     * 根据日期拼接目录
     *
     * @param date
     * @param year
     * @param month
     * @param day
     * @return
     */
    private String getFileFolder(Date date, boolean year, boolean month, boolean day) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        //获取当前日期字符串
        String dateString = dateTimeFormatter.format(localDate);

        //取出年、月、日
        String[] dateStringArray = dateString.split("-");
        StringBuffer folderString = new StringBuffer();
        if (year) {
            folderString.append(dateStringArray[0]);
            folderString.append("/");
        }

        if (month) {
            folderString.append(dateStringArray[1]);
            folderString.append("/");
        }
        if (day) {
            folderString.append(dateStringArray[2]);
            folderString.append("/");
        }

        return folderString.toString();
    }
}
