package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.dao.UserMessageSubscriptionMapper;
import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.enums.SendMessageKeyEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WeChatMessageSender {

    private static final String TEMPLATE_MESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=%s";

    @Autowired
    private WeChatAccessTokenService accessTokenService;

    @Autowired
    private UserMessageSubscriptionMapper subscriptionMapper;

    /**
     * 发送模板消息给指定用户。
     *
     * @param user 接收者
     * @param sendMessageKeyEnum 模板枚举
     * @param data 模板数据
     */
    public Boolean sendTemplateMessage(User user, SendMessageKeyEnum sendMessageKeyEnum, Map<String, Object> data) throws Exception {
        log.info("开始发送【{}】消息给用户{}", sendMessageKeyEnum.getName(), user.getId());

        // 获取有效的 access_token
        String accessToken = accessTokenService.getValidAccessToken();
        String url = String.format(TEMPLATE_MESSAGE_URL, accessToken);

        if (user.getOpenid() == null) {
            log.warn("用户{}没有绑定openid，无法发送消息", user.getId());
            return false;
        }

        // 构建消息体
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> messageBody = Map.of(
                "touser", user.getOpenid(),
                "template_id", sendMessageKeyEnum.getTemplateId(),
                "data", data,
                "page", sendMessageKeyEnum.getPage(),   // 点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转
                "miniprogram_state", "trial",          // 跳转小程序类型：developer 为开发版；trial 为体验版；formal 为正式版；默认为正式版
                "lang", "zh_CN"                         // 进入小程序查看”的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
        );
        log.info("发送消息 接收者  userId={}   phone:{}   openId={}", user.getId(), user.getPhone(), user.getOpenid());
        log.info("发送消息 template_id={}", sendMessageKeyEnum.getTemplateId());
        log.info("发送消息 data={}", data);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(url);
            request.setEntity(new StringEntity(mapper.writeValueAsString(messageBody), "UTF-8"));
            request.setHeader("Content-Type", "application/json");

            // 执行请求并检查响应状态码
            var response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                String jsonResponse = EntityUtils.toString(response.getEntity(), "UTF-8");
                Map<String, Object> resultMap = mapper.readValue(jsonResponse, Map.class);
                Integer errcode = (Integer) resultMap.get("errcode");
                if ("ok".equals(resultMap.get("errmsg"))) {
                    log.info("【{}】消息发送成功", sendMessageKeyEnum.getName());
                    // 发送成功后减少授权次数
                    subscriptionMapper.decreaseAuthorizeCount(user.getId(), sendMessageKeyEnum.getTemplateId());
                    return true;
                } if (errcode != null && errcode == 40001) {
                    log.info("【{}】消息发送失败：accessToken过期{}，需要重新获取", sendMessageKeyEnum.getName(), accessToken);
                    accessTokenService.uploadExpiresIn();
                    return false;
                } if (errcode != null && errcode == 43101) {
                    log.info("【{}】消息发送失败：用户{} 拒绝接收通知，无法发送消息", sendMessageKeyEnum.getName(), user.getId());
                    return false;
                } else {
                    log.info("【{}】消息发送失败：{}", sendMessageKeyEnum.getName(), jsonResponse);
                    return false;
                }
            } else {
                log.info("【{}】消息发送失败：HTTP request failed with status code:{} ", sendMessageKeyEnum.getName(), response.getStatusLine().getStatusCode());
                return false;
            }
        }
    }



    /**
     * 创建一个包含 value 的 Map 条目。
     *
     * @param value 消息内容
     * @return 包含 value 的 Map
     */
    public Map<String, Object> createDataEntry(Object value) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("value", value);
        return entry;
    }
}