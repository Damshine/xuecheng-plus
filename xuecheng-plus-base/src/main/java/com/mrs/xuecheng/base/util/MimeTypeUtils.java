package com.mrs.xuecheng.base.util;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

/**
 * description: MimeTypeUtils
 * date: 2023/5/4 13:26
 * author: MR.孙
 */
public class MimeTypeUtils {

    /**
     * 根据扩展名获取mimetype
     *
     * @param extension
     * @return
     */
    public static String getMimeTypeByExtension(String extension) {

        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (StringUtils.isNotEmpty(extension)) {
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            if (extensionMatch != null) {
                contentType = extensionMatch.getMimeType();
            }

        }

        return contentType;
    }


}
