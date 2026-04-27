package cn.lizongyi.shareaccount.services;

import java.util.Map;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-12-05
 * @description
 */

public interface LoginService {
    Map<String, Object> wechatLogin(String code) throws Exception;
    Map<String, Object> refreshToken(String oldToken) throws Exception;
    Map<String, Object> guestLogin() throws Exception;
    // 新增：游客升级为正常用户
    Map<String, Object> upgradeGuest(String code) throws Exception;
    // 新增：短信验证码登录
    Map<String, Object> smsLogin(String phone, String code) throws Exception;
}
