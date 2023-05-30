package com.xuecheng.auth.service.impl;

import com.xuecheng.auth.service.AuthService;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import org.springframework.stereotype.Service;

/**
 * description: WxAuthServiceImpl
 * date: 2023/5/29 22:32
 * author: MR.孙
 */
@Service("wx_authservice")
public class WxAuthServiceImpl implements AuthService {
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        return null;
    }
}
