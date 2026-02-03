package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.config.WeChatConfig;
import cn.lizongyi.shareaccount.dao.SessionMapper;
import cn.lizongyi.shareaccount.dao.UserMapper;
import cn.lizongyi.shareaccount.entity.Session;
import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.enums.CanSendMessageEnum;
import cn.lizongyi.shareaccount.enums.RoleTypeEnum;
import cn.lizongyi.shareaccount.enums.SexEnum;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.LoginService;
import cn.lizongyi.shareaccount.util.JacksonUtils;
import cn.lizongyi.shareaccount.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.lizongyi.shareaccount.constants.Constants;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private WeChatConfig weChatConfig;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BaseHandler baseHandler;

    // 新增：消息服务，用于在游客登录后推送系统提醒
    @Autowired
    private cn.lizongyi.shareaccount.services.MessageService messageService;

    @Autowired
    private cn.lizongyi.shareaccount.dao.MessageMapper messageMapper;

    @Autowired
    private cn.lizongyi.shareaccount.dao.UserMessageMapper userMessageMapper;

    // 新增：用于示例账单注入所需的Mapper
    @Autowired
    private cn.lizongyi.shareaccount.dao.LedgerMapper ledgerMapper;
    @Autowired
    private cn.lizongyi.shareaccount.dao.AccountMapper accountMapper;
    @Autowired
    private cn.lizongyi.shareaccount.dao.ClassEntityMapper classEntityMapper;
    @Autowired
    private cn.lizongyi.shareaccount.dao.BillMapper billMapper;

    // 这块需要开发
    private static final int login_fail_time = 60;  // 登录失败 等待时间
    // 这是 我自己的登录token时间，不是微信的access_token,微信的在 WeChatAccessTokenService.class 类里
    private static final int SESSION_EXPIRE_TIME = 3600; // 1小时（秒）
    private static final int EXPIRES_IN = SESSION_EXPIRE_TIME * 24 * 29;     // 29天
    private static final int REFRESH_EXPIRES_IN = SESSION_EXPIRE_TIME * 24 * 90;     // 30天刷新一次token

    @Override
    public Map<String, Object> wechatLogin(String code) throws Exception {
        log.info("开始微信登录流程...");

        // 调用微信接口获取 openid 和 session_key
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject( "https://api.weixin.qq.com/sns/jscode2session?appid=" + weChatConfig.getAppid() + "&secret=" + weChatConfig.getAppsecret() + "&js_code=" + code + "&grant_type=authorization_code", String.class);

        if (result == null || result.isEmpty()) {
            throw new RuntimeException("无法从微信服务器获取响应");
        }

        Map<String, Object> resultMap = JacksonUtils.strToMap(result, String.class, Object.class);
        String openid = (String) resultMap.get("openid");
        String sessionKey = (String) resultMap.get("session_key");

        if (openid == null || sessionKey == null) {
            throw new RuntimeException("无法获取用户信息，请检查提供的code是否正确");
        }

        log.info("成功获取到openid: {}", openid);

        // 检查并清除已存在的会话
        List<Session> existingSessions = sessionMapper.findByOpenId(openid);
        if (!existingSessions.isEmpty()) {
            for (Session existingSession : existingSessions) {
                sessionMapper.deleteById(existingSession.getId());
                log.info("清除旧会话: {}", existingSession.getId());
            }
        }

        // 检查是否已经存在该用户
        User user = userMapper.findByOpenid(openid);
        if (user == null) {
            // 如果是新用户，则创建新的用户记录
            user = new User();
            user.setNickName(baseHandler.getDefaultNickName());
            user.setOpenid(openid);
            user.setCreateTime(LocalDateTime.now());
            user.setLastLoginTime(LocalDateTime.now());
            user.setSex(SexEnum.DEFAULT.getId());
            user.setRole(RoleTypeEnum.USER.getId());
            user.setValidIntegral(0);
            user.setCanSendMessage(CanSendMessageEnum.YES.getId());     // 默认发送通知
            user.setNotityBill(12);             // 默认上午12点发送通知

            boolean saveUserSuccess = userMapper.insert(user) > 0;
            if (saveUserSuccess) {
                log.info("创建新用户id: {}", user.getId());

                baseHandler.initializeTasksForUser(user.getId());
            } else {
                log.error("创建新用户失败: {}", openid);
                return null;
            }
        } else {
            // 更新最后一次登录时间
            userMapper.updateLastLoginTime(openid);
            log.info("更新用户登录时间: {}", openid);
        }

        //baseHandler.initializeTasksForUser(user.getId());

        // 获取设备信息（这里可以实现更复杂的逻辑）
        String deviceInfo = getDeviceInfo();

        // 生成 JWT token 和刷新 token
        String accessToken = jwtUtil.generateToken(openid, EXPIRES_IN);
        String refreshToken = jwtUtil.generateToken(openid, REFRESH_EXPIRES_IN);

        // 返回用户信息给前端
        Map<String, Object> response = new HashMap<>();
        response.put("token", accessToken);
        response.put("refresh_token", refreshToken);
        response.put("expires_in", REFRESH_EXPIRES_IN);
        response.put("additionalId", user.getId());
        response.put("thunder", baseHandler.getUserRole(user.getId()));
        response.put("canSendMessage", user.getCanSendMessage());

        return response;
    }


    @Override
    public Map<String, Object> refreshToken(String oldRefreshToken) throws Exception {
        log.info("开始刷新token");
        // 解析旧的刷新 JWT
        Claims claims = jwtUtil.extractAllClaims(oldRefreshToken);
        String openid = claims.getSubject();

        // 检查旧刷新 token 是否未过期
        if (jwtUtil.isTokenExpired(oldRefreshToken)) {
            throw new RuntimeException("刷新token已过期");
        }

        // 使用 JwtUtil 生成新的 JWT
        String newAccessToken = jwtUtil.generateToken(openid, EXPIRES_IN);
        String refreshToken = jwtUtil.generateToken(openid, REFRESH_EXPIRES_IN); // 刷新 token有效期长一些

        // 返回用户信息给前端
        Map<String, Object> response = new HashMap<>();
        response.put("token", newAccessToken);
        response.put("refresh_token", refreshToken);
        response.put("expires_in", REFRESH_EXPIRES_IN);

        return response;
    }

    private String getDeviceInfo() {
        // 这里可以实现获取设备信息的逻辑，例如从HTTP请求头中提取信息
        // 或者从前端传递过来的参数中获取
        // 为了简化示例，这里直接返回一个示例值
        return "example-device-info";
    }

    @Override
    public Map<String, Object> guestLogin() throws Exception {
        // 配置中心：游客模式开关，缺省或非法值时默认开启
        String guestModeSwitch = baseHandler.getCongigValue(null, Constants.GUEST_MODE_ENABLED);
        if (guestModeSwitch != null && ("false".equalsIgnoreCase(guestModeSwitch.trim()) || "0".equals(guestModeSwitch.trim()))) {
            log.warn("游客模式开关关闭（guest.mode.enabled={}），拒绝游客登录", guestModeSwitch);
            throw new RuntimeException("游客模式已关闭，请使用微信登录");
        }

        // 已存在游客登录态则复用，不再新建用户
        Long currentUserId = baseHandler.getUserId();
        if (currentUserId != null && baseHandler.isGuestUser(currentUserId)) {
            User existing = baseHandler.getUserById(currentUserId);
            if (existing != null) {
                userMapper.updateLastLoginTime(existing.getOpenid());
                String accessToken = jwtUtil.generateToken(existing.getOpenid(), EXPIRES_IN);
                String refreshToken = jwtUtil.generateToken(existing.getOpenid(), REFRESH_EXPIRES_IN);
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("token", accessToken);
                response.put("refresh_token", refreshToken);
                response.put("expires_in", REFRESH_EXPIRES_IN);
                response.put("additionalId", existing.getId());
                response.put("thunder", baseHandler.getUserRole(existing.getId()));
                response.put("canSendMessage", existing.getCanSendMessage());
                return response;
            }
        }

        String guestOpenid = "guest_" + java.util.UUID.randomUUID().toString();
        log.info("开始游客登录，openid={}", guestOpenid);

        // 创建游客用户
        User user = new User();
        String testNamePrefix = baseHandler.getTestUserName();
        if (testNamePrefix == null || testNamePrefix.isEmpty()) {
            testNamePrefix = "游客";
        }
        user.setNickName(testNamePrefix)
            .setOpenid(guestOpenid)
            .setCreateTime(java.time.LocalDateTime.now())
            .setLastLoginTime(java.time.LocalDateTime.now())
            .setSex(SexEnum.DEFAULT.getId())
            .setRole(RoleTypeEnum.USER.getId())
            .setValidIntegral(0)
            .setCanSendMessage(CanSendMessageEnum.NO.getId())
            .setNotityBill(1);

        boolean saveUserSuccess = userMapper.insert(user) > 0;
        if (!saveUserSuccess) {
            log.error("创建游客用户失败 openid={}", guestOpenid);
            throw new RuntimeException("游客登录失败");
        }
        log.info("创建游客用户id: {}", user.getId());

        // 初始化默认账本与账户
        try {
            baseHandler.initializeTasksForUser(user.getId());
        } catch (Exception initEx) {
            log.warn("初始化游客默认账本/账户失败: {}", initEx.getMessage());
        }

        // 发送系统消息提醒游客模式限制与有效期
        try {
            // 复用单条系统消息：若存在标题为“游客模式提示”的系统消息，则只为当前用户创建关联；否则创建一次消息
            java.util.List<cn.lizongyi.shareaccount.entity.Message> sysMsgs = messageMapper.selectByType(1, 50);
            Long msgId = null;
            if (sysMsgs != null) {
                for (cn.lizongyi.shareaccount.entity.Message m : sysMsgs) {
                    if ("游客模式提示".equals(m.getTitle())) { msgId = m.getId(); break; }
                }
            }
            if (msgId == null) {
                cn.lizongyi.shareaccount.entity.Message message = new cn.lizongyi.shareaccount.entity.Message();
                message.setTitle("游客模式提示");
                message.setContent("您正在使用游客模式：账号有效期为1天，期满后请使用微信登录以保留数据。");
                message.setType(1);
                message.setPriority(1);
                message.setStatus(0);
                message.setCreatedAt(new java.util.Date());
                message.setUpdatedAt(new java.util.Date());
                messageMapper.insert(message);
                msgId = message.getId();
            }
            cn.lizongyi.shareaccount.entity.UserMessage exists = userMessageMapper.selectByUserIdAndMessageId(user.getId(), msgId);
            if (exists == null) {
                cn.lizongyi.shareaccount.entity.UserMessage userMessage = new cn.lizongyi.shareaccount.entity.UserMessage();
                userMessage.setMessageId(msgId);
                userMessage.setUserId(user.getId());
                userMessage.setIsRead(0);
                userMessage.setCreatedAt(new java.util.Date());
                userMessageMapper.insert(userMessage);
            }
        } catch (Exception ex) {
            log.warn("发送游客提示消息失败: {}", ex.getMessage());
        }

        // 首次游客登录：可选注入“示例账单”
        try {
            String sampleBillEnabled = baseHandler.getCongigValue(null, Constants.GUEST_SAMPLE_BILL_ENABLED);
            boolean shouldInjectSample = (sampleBillEnabled == null) ||
                    "true".equalsIgnoreCase(sampleBillEnabled) ||
                    "1".equals(sampleBillEnabled);
            if (shouldInjectSample) {
                // 找到默认账本
                java.util.List<cn.lizongyi.shareaccount.entity.Ledger> ledgers = ledgerMapper.findByUserId(user.getId());
                cn.lizongyi.shareaccount.entity.Ledger defaultLedger = null;
                if (ledgers != null && !ledgers.isEmpty()) {
                    for (cn.lizongyi.shareaccount.entity.Ledger l : ledgers) {
                        if (l.getIsDefault() != null && l.getIsDefault() == 1) {
                            defaultLedger = l;
                            break;
                        }
                    }
                    if (defaultLedger == null) {
                        defaultLedger = ledgers.get(0);
                    }
                }
 
                 // 找到默认账户
                java.util.List<cn.lizongyi.shareaccount.entity.Account> accounts = accountMapper.findByUserId(user.getId());
                cn.lizongyi.shareaccount.entity.Account defaultAccount = null;
                if (accounts != null && !accounts.isEmpty()) {
                    for (cn.lizongyi.shareaccount.entity.Account a : accounts) {
                        if (a.getIsDefault() != null && a.getIsDefault() == 1) {
                            defaultAccount = a;
                            break;
                        }
                    }
                    if (defaultAccount == null) {
                        defaultAccount = accounts.get(0);
                    }
                }
 
                 // 选择一个系统支出分类（顶层ID=1），优先取其子分类
                Long classId = null;
                java.util.List<cn.lizongyi.shareaccount.entity.ClassEntity> expenseChildren = classEntityMapper.selectAllMainByParentId(1L);
                if (expenseChildren != null && !expenseChildren.isEmpty()) {
                    classId = expenseChildren.get(0).getId();
                }

                if (defaultLedger != null && defaultAccount != null) {
                    cn.lizongyi.shareaccount.entity.Bill sample = new cn.lizongyi.shareaccount.entity.Bill();
                    sample.setUserId(user.getId());
                    sample.setLedgerId(defaultLedger.getId());
                    sample.setAccountId(defaultAccount.getId());
                    sample.setClassId(classId);
                    sample.setTopClassId(1L); // 顶层支出分类
                    sample.setIsBudget(0);
                    sample.setBillTime(java.time.LocalDateTime.now());
                    sample.setPrice(-5000L); // -50.00 作为示例支出
                    sample.setStatus(0);
                    sample.setMemo("示例账单：xx消费");
                    sample.setCreateTime(java.time.LocalDateTime.now());
                    try {
                        billMapper.insert(sample);
                        log.info("游客示例账单已注入: userId={}, billId={}", user.getId(), sample.getId());
                    } catch (Exception insertEx) {
                        log.warn("注入示例账单失败: {}", insertEx.getMessage());
                    }
                } else {
                    log.warn("未找到默认账本或账户，跳过示例账单注入");
                }
            }
        } catch (Exception e) {
            log.warn("处理游客示例账单逻辑时异常: {}", e.getMessage());
        }

        // 生成 JWT token 和刷新 token
        String accessToken = jwtUtil.generateToken(guestOpenid, EXPIRES_IN);
        String refreshToken = jwtUtil.generateToken(guestOpenid, REFRESH_EXPIRES_IN);

        Map<String, Object> response = new HashMap<>();
        response.put("token", accessToken);
        response.put("refresh_token", refreshToken);
        response.put("expires_in", REFRESH_EXPIRES_IN);
        response.put("additionalId", user.getId());
        response.put("thunder", baseHandler.getUserRole(user.getId()));
        response.put("canSendMessage", user.getCanSendMessage());
        // 引导卡片：前端可根据此字段渲染游客模式引导
        Map<String, Object> guideCard = new HashMap<>();
        guideCard.put("title", "游客快速引导");
        guideCard.put("content", "这是临时体验账号，数据保留1天；绑定微信可保留数据并开启通知。示例账单已创建，去看看吧。");
        guideCard.put("ctaText", "绑定微信");
        guideCard.put("ctaAction", "/login/upgrade");
        guideCard.put("expiresHours", 24);
        response.put("guideCard", guideCard);
        return response;
    }

    @Override
    public Map<String, Object> upgradeGuest(String code) throws Exception {
        log.info("开始游客升级为正常用户流程...");
        Long userId = baseHandler.getUserId();
        if (userId == null) {
            throw new RuntimeException("请先登录");
        }
        if (!baseHandler.isGuestUser(userId)) {
            throw new RuntimeException("仅游客账号可升级");
        }

        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject("https://api.weixin.qq.com/sns/jscode2session?appid=" + weChatConfig.getAppid() + "&secret=" + weChatConfig.getAppsecret() + "&js_code=" + code + "&grant_type=authorization_code", String.class);
        if (result == null || result.isEmpty()) {
            throw new RuntimeException("无法从微信服务器获取响应");
        }
        Map<String, Object> resultMap = cn.lizongyi.shareaccount.util.JacksonUtils.strToMap(result, String.class, Object.class);
        String openid = (String) resultMap.get("openid");
        String sessionKey = (String) resultMap.get("session_key");
        if (openid == null || sessionKey == null) {
            throw new RuntimeException("无法获取用户信息，请检查提供的code是否正确");
        }

        // 检查是否已有同openid用户
        User conflict = userMapper.findByOpenid(openid);
        if (conflict != null && !conflict.getId().equals(userId)) {
            throw new RuntimeException("该微信已绑定其他账号，请直接使用微信登录");
        }

        int updated = userMapper.upgradeGuestOpenid(userId, openid, CanSendMessageEnum.YES.getId());
        if (updated <= 0) {
            throw new RuntimeException("升级失败，请稍后重试");
        }

        // 生成新的令牌
        String accessToken = jwtUtil.generateToken(openid, EXPIRES_IN);
        String refreshToken = jwtUtil.generateToken(openid, REFRESH_EXPIRES_IN);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("token", accessToken);
        response.put("refresh_token", refreshToken);
        response.put("expires_in", REFRESH_EXPIRES_IN);
        response.put("additionalId", userId);
        response.put("thunder", baseHandler.getUserRole(userId));
        response.put("canSendMessage", CanSendMessageEnum.YES.getId());

        // 推送升级成功消息
        try {
            cn.lizongyi.shareaccount.request.CreateMessageRequest request = new cn.lizongyi.shareaccount.request.CreateMessageRequest();
            request.setTitle("账号升级成功");
            request.setContent("已绑定微信并开启通知，您现在可以使用全部功能。");
            request.setType(1);
            request.setPriority(0);
            request.setTarget("specific");
            request.setUserIds(java.util.Collections.singletonList(userId));
            messageService.saveMessage(request, userId);
        } catch (Exception ex) {
            log.warn("发送升级成功消息失败: {}", ex.getMessage());
        }

        return response;
    }
}