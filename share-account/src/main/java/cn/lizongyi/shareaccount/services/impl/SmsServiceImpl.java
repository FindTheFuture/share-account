package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.constants.Constants;
import cn.lizongyi.shareaccount.dao.SmsCodeMapper;
import cn.lizongyi.shareaccount.entity.Config;
import cn.lizongyi.shareaccount.entity.SmsCode;
import cn.lizongyi.shareaccount.services.ConfigService;
import cn.lizongyi.shareaccount.services.SmsService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    private static final String SMS_TYPE_LOGIN = "login";  // 短信验证码类型：登录标识

    private static final int SMS_CODE_LENGTH = 6;  // 验证码长度：生成6位数字验证码

    private static final int SMS_EXPIRE_MINUTES = 5;  // 验证码有效期：5分钟后过期

    private static final int SEND_INTERVAL_SECONDS = 60;  // 发送间隔：同一手机号60秒内不能重复发送

    private static final int MAX_DAILY_SEND_COUNT = 10;  // 单个手机号每天最多发送10次验证码

    private static final int MAX_IP_DAILY_SEND_COUNT = 50;  // 单个IP地址每天最多发送50次验证码

    private static final int MAX_VERIFY_FAIL_COUNT = 3;  // 验证码错误次数达到3次后锁定手机号

    private static final int LOCK_MINUTES = 30;  // 锁定时间：验证失败锁定30分钟后自动解锁

    private static final String SMS_CONFIG_KEY_ACCESS_KEY_ID = "accessKeyId";  // config表中阿里云AccessKey ID的键名

    private static final String SMS_CONFIG_KEY_ACCESS_KEY_SECRET = "accessKeySecret";  // config表中阿里云AccessKey Secret的键名

    private static final String SMS_CONFIG_KEY_SIGN_NAME = "signName";  // config表中短信签名的键名

    private static final String SMS_CONFIG_KEY_TEMPLATE_CODE = "templateCode";  // config表中短信模板Code的键名

    private static final String SMS_CONFIG_KEY_SMS_TOTAL = "smsTotal";  // config表中短信总量上限的键名

    private static final String SMS_CONFIG_KEY_SMS_USED = "smsUsed";  // config表中已使用短信数量的键名

    @Value("${sms.enabled:true}")
    private boolean smsEnabled;

    @Autowired
    private SmsCodeMapper smsCodeMapper;

    @Autowired
    private ConfigService configService;

    private com.aliyun.dypnsapi20170525.Client smsClient = null;

    private synchronized void initAliyunSms() {
        if (smsClient != null) {
            return;
        }
        Config config = configService.getConfig(null, Constants.ALIYUN_SMS_CONFIG);
        if (config != null && config.getValue() != null) {
            try {
                JSONObject jsonObject = JSONObject.parseObject(config.getValue());
                String accessKeyId = jsonObject.getString(SMS_CONFIG_KEY_ACCESS_KEY_ID);
                String accessKeySecret = jsonObject.getString(SMS_CONFIG_KEY_ACCESS_KEY_SECRET);

                System.setProperty("alibabacloud.accessKeyId", accessKeyId);
                System.setProperty("alibabacloud.accessKeySecret", accessKeySecret);

                com.aliyun.credentials.Client credentialClient = new com.aliyun.credentials.Client();
                com.aliyun.teaopenapi.models.Config openApiConfig = new com.aliyun.teaopenapi.models.Config()
                        .setCredential(credentialClient);
                openApiConfig.endpoint = "dypnsapi.aliyuncs.com";

                smsClient = new com.aliyun.dypnsapi20170525.Client(openApiConfig);
                log.info("阿里云短信服务初始化成功");
            } catch (Exception e) {
                log.error("阿里云短信服务初始化失败", e);
                throw new RuntimeException("短信服务初始化失败");
            }
        } else {
            log.error("阿里云短信配置未找到");
            throw new RuntimeException("短信服务配置未找到");
        }
    }

    private int getSmsTotal() {
        Config config = configService.getConfig(null, Constants.ALIYUN_SMS_CONFIG);
        if (config != null && config.getValue() != null) {
            try {
                JSONObject jsonObject = JSONObject.parseObject(config.getValue());
                return jsonObject.getInteger(SMS_CONFIG_KEY_SMS_TOTAL) != null ? jsonObject.getInteger(SMS_CONFIG_KEY_SMS_TOTAL) : 0;
            } catch (Exception e) {
                log.error("获取短信总量配置失败", e);
                return 0;
            }
        }
        return 0;
    }

    private int getSmsUsed() {
        Config config = configService.getConfig(null, Constants.ALIYUN_SMS_CONFIG);
        if (config != null && config.getValue() != null) {
            try {
                JSONObject jsonObject = JSONObject.parseObject(config.getValue());
                return jsonObject.getInteger(SMS_CONFIG_KEY_SMS_USED) != null ? jsonObject.getInteger(SMS_CONFIG_KEY_SMS_USED) : 0;
            } catch (Exception e) {
                log.error("获取已使用短信数量失败", e);
                return 0;
            }
        }
        return 0;
    }

    private void incrementSmsUsed() {
        Config config = configService.getConfig(null, Constants.ALIYUN_SMS_CONFIG);
        if (config != null && config.getValue() != null) {
            try {
                JSONObject jsonObject = JSONObject.parseObject(config.getValue());
                int currentUsed = jsonObject.getInteger(SMS_CONFIG_KEY_SMS_USED) != null ? jsonObject.getInteger(SMS_CONFIG_KEY_SMS_USED) : 0;
                jsonObject.put(SMS_CONFIG_KEY_SMS_USED, currentUsed + 1);
                config.setValue(jsonObject.toJSONString());
                configService.updateConfigValue(null, Constants.ALIYUN_SMS_CONFIG, jsonObject.toJSONString());
                log.info("短信已使用数量更新: 已使用={}, 总量={}, 剩余={}", currentUsed + 1, getSmsTotal(), getSmsTotal() - currentUsed - 1);
            } catch (Exception e) {
                log.error("更新短信已使用数量失败", e);
            }
        }
    }

    @Override
    public void sendSmsCode(String phone, String ipAddress) {
        LocalDateTime now = LocalDateTime.now();

        // 初始化阿里云短信服务
        initAliyunSms();

        int smsTotal = getSmsTotal();
        int smsUsed = getSmsUsed();
        if (smsTotal > 0 && smsUsed >= smsTotal) {
            log.warn("短信总量已达上限: 已使用={}, 总量={}", smsUsed, smsTotal);
            throw new RuntimeException("短信总量已达上限，请联系管理员");
        }

        int todaySendCount = smsCodeMapper.countByIpAddressSince(ipAddress, now.toLocalDate().atStartOfDay());
        if (todaySendCount >= MAX_IP_DAILY_SEND_COUNT) {
            log.warn("IP {} 当日发送次数已达上限: {}", ipAddress, todaySendCount);
            throw new RuntimeException("发送次数已达上限，请明天再试");
        }

        SmsCode latestSms = smsCodeMapper.findLatestByPhoneAndType(phone, SMS_TYPE_LOGIN);
        if (latestSms != null) {
            if (latestSms.getLockedUntil() != null && latestSms.getLockedUntil().isAfter(now)) {
                log.warn("手机号 {} 已被锁定，锁定截止时间: {}", phone, latestSms.getLockedUntil());
                throw new RuntimeException("验证失败次数过多，请" + LOCK_MINUTES + "分钟后再试");
            }

            LocalDateTime sendTime = latestSms.getCreateTime();
            if (sendTime.plusSeconds(SEND_INTERVAL_SECONDS).isAfter(now)) {
                long remainSeconds = SEND_INTERVAL_SECONDS - java.time.Duration.between(sendTime, now).getSeconds();
                log.warn("手机号 {} 发送间隔不足，还需等待 {} 秒", phone, remainSeconds);
                throw new RuntimeException("发送过于频繁，请" + remainSeconds + "秒后再试");
            }

            int phoneTodaySendCount = 0;
            for (SmsCode sms : smsCodeMapper.findRecentByPhoneAndType(phone, SMS_TYPE_LOGIN, 10)) {
                if (sms.getCreateTime().toLocalDate().equals(now.toLocalDate())) {
                    phoneTodaySendCount++;
                }
            }
            if (phoneTodaySendCount >= MAX_DAILY_SEND_COUNT) {
                log.warn("手机号 {} 当日发送次数已达上限: {}", phone, phoneTodaySendCount);
                throw new RuntimeException("发送次数已达上限，请明天再试");
            }
        }

        String code = generateSmsCode();
        log.info("生成短信验证码: phone={}, code={}", phone, code);

        if (smsEnabled) {
            try {
                Config config = configService.getConfig(null, Constants.ALIYUN_SMS_CONFIG);
                JSONObject jsonObject = JSONObject.parseObject(config.getValue());
                String signName = jsonObject.getString(SMS_CONFIG_KEY_SIGN_NAME);
                String templateCode = jsonObject.getString(SMS_CONFIG_KEY_TEMPLATE_CODE);
                String templateParam = String.format("{\"code\":\"%s\",\"min\":\"%d\"}", code, SMS_EXPIRE_MINUTES);

                com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest request = new com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest()
                        .setSignName(signName)
                        .setTemplateCode(templateCode)
                        .setPhoneNumber(phone.trim())
                        .setTemplateParam(templateParam);

                com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
                var response = smsClient.sendSmsVerifyCodeWithOptions(request, runtime);

                log.info("短信发送成功: phone={}, requestId={}, code={}, message={}",
                        phone, response.getBody().getRequestId(), response.getBody().getCode(), response.getBody().getMessage());

                if (!"OK".equals(response.getBody().getCode())) {
                    log.error("短信发送失败: phone={}, code={}, message={}",
                            phone, response.getBody().getCode(), response.getBody().getMessage());
                    throw new RuntimeException("短信发送失败: " + response.getBody().getMessage());
                }

                incrementSmsUsed();

                int remaining = smsTotal > 0 ? smsTotal - smsUsed - 1 : -1;
                if (remaining >= 0) {
                    log.info("短信剩余可用数量: {}", remaining);
                } else {
                    log.info("短信剩余可用数量: 无限制");
                }

            } catch (com.aliyun.tea.TeaException error) {
                log.error("短信发送失败: phone={}, error={}", phone, error.getMessage());
                throw new RuntimeException("短信发送失败: " + error.getMessage());
            } catch (Exception e) {
                log.error("短信发送失败: phone={}, error={}", phone, e.getMessage());
                throw new RuntimeException("短信发送失败，请稍后再试");
            }
        } else {
            log.info("【开发环境】模拟短信发送: phone={}, code={}", phone, code);
        }
        
        SmsCode smsCode = new SmsCode();
        smsCode.setPhone(phone.trim());
        smsCode.setCode(code);
        smsCode.setType(SMS_TYPE_LOGIN);
        smsCode.setStatus(SmsCode.Status.UNUSED.getValue());
        smsCode.setExpireTime(now.plusMinutes(SMS_EXPIRE_MINUTES));
        smsCode.setErrorCount(0);
        smsCode.setCreateTime(now);
        smsCode.setIpAddress(ipAddress);

        smsCodeMapper.insert(smsCode);
        log.info("短信验证码记录已保存: phone={}, expireTime={}", phone, smsCode.getExpireTime());
    }

    @Override
    public boolean verifySmsCode(String phone, String code, String type) {
        smsCodeMapper.expireOldCodes();

        SmsCode smsCode = smsCodeMapper.findValidByPhoneAndType(phone, type);
        if (smsCode == null) {
            log.warn("未找到有效的短信验证码: phone={}, type={}", phone, type);
            return false;
        }

        if (!smsCode.getCode().equals(code)) {
            int newErrorCount = smsCode.getErrorCount() + 1;
            if (newErrorCount >= MAX_VERIFY_FAIL_COUNT) {
                smsCodeMapper.updateErrorCountAndLock(smsCode.getId(), newErrorCount, LocalDateTime.now().plusMinutes(LOCK_MINUTES));
                log.warn("验证码错误次数过多，手机号 {} 已被锁定 {} 分钟", phone, LOCK_MINUTES);
            } else {
                smsCodeMapper.updateErrorCountAndLock(smsCode.getId(), newErrorCount, null);
                log.warn("短信验证码错误: phone={}, errorCount={}/{}", phone, newErrorCount, MAX_VERIFY_FAIL_COUNT);
            }
            return false;
        }

        smsCodeMapper.updateStatus(smsCode.getId(), SmsCode.Status.USED.getValue());
        log.info("短信验证码验证成功: phone={}", phone);
        return true;
    }

    private String generateSmsCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < SMS_CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new RuntimeException("手机号不能为空");
        }
        phone = phone.trim();
        if (phone.startsWith("+")) {
            return phone;
        }
        if (phone.startsWith("86")) {
            return "+" + phone;
        }
        return "+86" + phone;
    }
}
