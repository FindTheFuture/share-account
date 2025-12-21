package cn.lizongyi.shareaccount.util;

import cn.lizongyi.shareaccount.services.WeChatAccessTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@Slf4j
@Service
public class WxQRcodeUtil {
    private static final String CREATE_QRCODE_URL = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=%s";

    @Autowired
    private WeChatAccessTokenService accessTokenService;

    /**
     * @param page 小程序页面路径
     * @param scene 需要传递的参数，最大32个可见字符
     */
    public String generateQRCode(String page, String scene) throws Exception {

        // 获取有效的 access_token
        String accessToken = accessTokenService.getValidAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            log.error("Failed to retrieve valid access token.");
            return null;
        }

        try {
            String base64QRCode = getQRCodeAsBase64(accessToken, page, scene);
            return base64QRCode;
        } catch (Exception e) {
            log.error("Error generating QR code: {}", e.getMessage(), e);
            throw e;
        }
    }

    // 如果没生成二维码，很有可能就是 accessToken过期了
    // https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/qrcode-link/qr-code/getUnlimitedQRCode.html
    private static String getQRCodeAsBase64(String accessToken, String page, String scene) throws IOException {
        log.info("Attempting to create QR code with page: {} and scene: {}", page, scene);

        URL url = new URL(String.format(CREATE_QRCODE_URL, accessToken));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        // 确保page和scene的长度符合要求
        if (page != null && page.length() > 128) {
            throw new IllegalArgumentException("Page path exceeds maximum allowed length.");
        }
        if (scene != null && scene.length() > 32) {
            throw new IllegalArgumentException("Scene value exceeds maximum allowed length.");
        }

        String jsonInputString = String.format("{\"page\":\"%s\",\"scene\":\"%s\"}", page, scene);
        log.info("Sending JSON input string: {}", jsonInputString);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        InputStream inputStream;
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            log.error("Error from WeChat API: {} - Response: {}", responseCode, response.toString());
            throw new IOException("WeChat API error: " + response.toString());
        }

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        return Base64.getEncoder().encodeToString(result.toByteArray());
    }
}