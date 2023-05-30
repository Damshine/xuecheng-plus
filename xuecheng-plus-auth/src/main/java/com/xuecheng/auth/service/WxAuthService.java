package com.xuecheng.auth.service;

import com.xuecheng.ucenter.model.po.XcUser;

public interface WxAuthService {

    XcUser wxAuth(String code);


}
