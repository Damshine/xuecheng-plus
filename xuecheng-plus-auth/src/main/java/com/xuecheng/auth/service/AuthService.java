package com.xuecheng.auth.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

public interface AuthService {

    /**
     *  认证方法
     * @param authParamsDto
     * @return
     */
    XcUserExt execute(AuthParamsDto authParamsDto);



}
