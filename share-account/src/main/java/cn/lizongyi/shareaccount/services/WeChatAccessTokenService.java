package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.config.WeChatConfig;
import cn.lizongyi.shareaccount.dao.AccessTokenMapper;
import cn.lizongyi.shareaccount.entity.AccessToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;


/**
 * 刷新获取 access_token
 */
@Service
public class WeChatAccessTokenService {

    @Autowired
    private AccessTokenMapper accessTokenMapper;

    @Autowired
    private WeChatConfig weChatConfig;

    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    // 定义提前刷新 access_token 的时间窗口（30分钟）
    private static final long REFRESH_WINDOW_MINUTES = 30;

    @Scheduled(cron = "0 0/5 * * * ?") // 每5分钟检查一次
    public void refreshAccessTokenIfNecessary() throws Exception {
        Optional<AccessToken> latestTokenOpt = accessTokenMapper.findLatest();
        if (latestTokenOpt.isEmpty() || isAccessTokenExpiringSoon(latestTokenOpt.get())) {
            String newAccessTokenAndExpiresIn = fetchNewAccessToken();
            storeAccessToken(newAccessTokenAndExpiresIn);
            System.out.println("Access token refreshed.");
        } else {
            System.out.println("Access token is still valid.");
        }
    }

    private boolean isAccessTokenExpiringSoon(AccessToken accessToken) {
         return Instant.now().plus(REFRESH_WINDOW_MINUTES, ChronoUnit.MINUTES).isAfter(accessToken.getExpiresAt().toInstant());
    }

    private String fetchNewAccessToken() throws Exception {
        String url = String.format(ACCESS_TOKEN_URL, weChatConfig.getAppid(), weChatConfig.getAppsecret());
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() == 200) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> resultMap = mapper.readValue(jsonResponse, Map.class);

                String accessToken = (String) resultMap.get("access_token");
                int expiresIn = ((Number) resultMap.get("expires_in")).intValue();

                // 返回 access_token 和 expires_in 给调用者
                return accessToken + ":" + expiresIn;
            } else {
                throw new RuntimeException("获取access_token失败: " + response.getStatusLine().getStatusCode());
            }
        }
    }

    private void storeAccessToken(String accessTokenAndExpiresIn) throws Exception {
        String[] parts = accessTokenAndExpiresIn.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("格式不正确：access token and expires_in");
        }
        String accessToken = parts[0];
        int expiresIn = Integer.parseInt(parts[1]);

        Timestamp expiresAt = Timestamp.from(Instant.now().plusSeconds(expiresIn));

        AccessToken newAccessToken = new AccessToken();
        newAccessToken.setAccessToken(accessToken);
        newAccessToken.setExpiresAt(expiresAt);

        accessTokenMapper.saveOrUpdate(newAccessToken);
    }

    public String getValidAccessToken() throws Exception {
        Optional<AccessToken> latestTokenOpt = accessTokenMapper.findLatest();
        if (latestTokenOpt.isPresent() && !isAccessTokenExpiringSoon(latestTokenOpt.get())) {
            return latestTokenOpt.get().getAccessToken();
        } else {
            // 如果没有有效的 access_token，则立即刷新并返回新的 access_token
            String newAccessTokenAndExpiresIn = fetchNewAccessToken();
            storeAccessToken(newAccessTokenAndExpiresIn);
            return newAccessTokenAndExpiresIn.split(":")[0];
        }
    }

    public boolean uploadExpiresIn() {
        // 更新最新的 access_token 过期时间 = 当前时间，下次重新查询 access_token
        Optional<AccessToken> latestTokenOpt = accessTokenMapper.findLatest();
        if (latestTokenOpt.isPresent()) {
            AccessToken accessToken = latestTokenOpt.get();
            accessToken.setExpiresAt(new Timestamp(System.currentTimeMillis()));
            accessTokenMapper.saveOrUpdate(accessToken);
            return true;
        }
        return false;
    }
}