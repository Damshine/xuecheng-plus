package com.mrs.xuecheng.media.api;


import com.mrs.xuecheng.base.exception.XueChengPlusException;
import com.mrs.xuecheng.base.model.PageParams;
import com.mrs.xuecheng.base.model.PageResult;
import com.mrs.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.mrs.xuecheng.media.model.dto.UploadFileParamsDto;
import com.mrs.xuecheng.media.model.dto.UploadFileResultDto;
import com.mrs.xuecheng.media.model.po.MediaFiles;
import com.mrs.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {


    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
   public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiels(companyId, pageParams, queryMediaParamsDto);

    }

    @ApiOperation("上传文件")
    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile upload,
                                      @RequestParam(value = "folder",required=false) String folder,
                                      @RequestParam(value = "objectName",required=false) String objectName) {

        String contentType = upload.getContentType();
        //假数据
        Long companyId = 1232141425L;
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFileSize(upload.getSize());
        if(contentType.indexOf("image")>=0){
            //图片
            uploadFileParamsDto.setFileType("001001");
        }else{
            //其它
            uploadFileParamsDto.setFileType("001003");
        }
        uploadFileParamsDto.setRemark("");
        uploadFileParamsDto.setFilename(upload.getOriginalFilename());
        uploadFileParamsDto.setContentType(contentType);
        UploadFileResultDto uploadFileResultDto = null;
        try {
            uploadFileResultDto = mediaFileService.uploadFile(companyId, uploadFileParamsDto, upload.getBytes(), folder, objectName);
        } catch (IOException e) {
            XueChengPlusException.cast("上传文件过程中出错");
        }

        return uploadFileResultDto;

    }


}
