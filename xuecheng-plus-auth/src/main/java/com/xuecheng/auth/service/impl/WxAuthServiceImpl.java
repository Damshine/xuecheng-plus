package com.xuecheng.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.auth.service.WxAuthService;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * description: WxAuthServiceImpl
 * date: 2023/5/29 22:32
 * author: MR.孙
 */
@Slf4j
@Service("wx_authservice")
public class WxAuthServiceImpl implements AuthService, WxAuthService {


    @Autowired
    RestTemplate restTemplate;

    @Autowired
    WxAuthServiceImpl currentProxy;


    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    XcUserRoleMapper xcUserRoleMapper;

    @Value("${wx.appid}")
    String appid;

    @Value("${wx.secret}")
    String secret;



    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {

        //账号
        String username = authParamsDto.getUsername();

        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if(user==null){
            //返回空表示用户不存在
            throw new RuntimeException("账号不存在");
        }

        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user,xcUserExt);
        return xcUserExt;

    }

    @Override
    public XcUser wxAuth(String code) {

        //用授权码换取令牌
        if (StringUtils.isBlank(code)) {
            throw new RuntimeException("没有获得授权");
        }

        //申请令牌
        Map<String, String> wxToken = getWxToken(code);
        //获取令牌
        String access_token = wxToken.get("access_token");
        String openid = wxToken.get("openid");
        //根据access_token令牌获取用户信息
        Map<String, String> userinfo = getUserinfo(access_token, openid);

        //同步到数据库
         XcUser xcUser = currentProxy.addWxUser(userinfo);


        return xcUser;
    }

    @Transactional
    public XcUser addWxUser(Map<String, String> userInfo_map) {

        String unionid = userInfo_map.get("unionid");
        String nickname = userInfo_map.get("nickname");

        //查询数据库，看用户是否存在用户表中
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid, unionid));
        if(xcUser !=null){
            return xcUser;
        }

        xcUser = new XcUser();
        String userId = UUID.randomUUID().toString();
        xcUser.setId(userId);
        xcUser.setUsername(unionid);
        xcUser.setPassword(unionid);
        xcUser.setWxUnionid(unionid);
        xcUser.setNickname(nickname);
        xcUser.setName(nickname);
        xcUser.setUtype("101001");//学生类型
        xcUser.setStatus("1");//用户状态
        xcUser.setCreateTime(LocalDateTime.now());

        //插入
        int insert = xcUserMapper.insert(xcUser);


        //向用户角色关联表插入记录
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(userId);
        xcUserRole.setRoleId("17"); //学生

        xcUserRole.setCreateTime(LocalDateTime.now());
        xcUserRoleMapper.insert(xcUserRole);
        return xcUser;
    }

    private Map<String, String> getUserinfo(String access_token, String openid) {

        String url_template = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        String url = String.format(url_template, access_token, openid);

        //发送请求
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        String wxResponse = exchange.getBody();

        String result = new String(wxResponse.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);


        //解析json，将结果转为Map类型
        Map<String, String> map = JSON.parseObject(result, Map.class);
        return map;
    }

    private Map<String, String> getWxToken(String code) {

        //构造请求
        String url_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String url = String.format(url_template, appid, secret, code);
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        String wxResponse  = exchange.getBody();

        //解析json，将结果转为Map类型
        Map<String, String> map = JSON.parseObject(wxResponse, Map.class);

        return map;
    }


}
