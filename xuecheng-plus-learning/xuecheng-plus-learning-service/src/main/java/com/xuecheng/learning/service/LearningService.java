package com.xuecheng.learning.service;

import com.mrs.xuecheng.base.model.RestResponse;

/**
 * description: LearningService
 * date: 2023/6/5 9:30
 * author: MR.孙
 */
public interface LearningService {

    RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);

}
