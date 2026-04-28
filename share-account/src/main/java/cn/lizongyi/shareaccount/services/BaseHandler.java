package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.constants.Constants;
import cn.lizongyi.shareaccount.dao.UserMapper;
import cn.lizongyi.shareaccount.dao.LedgerMapper;
import cn.lizongyi.shareaccount.dao.AccountMapper;
import cn.lizongyi.shareaccount.entity.Picture;
import cn.lizongyi.shareaccount.entity.User;
import cn.lizongyi.shareaccount.entity.Ledger;
import cn.lizongyi.shareaccount.entity.Account;
import cn.lizongyi.shareaccount.enums.RoleTypeEnum;
import cn.lizongyi.shareaccount.request.DouyinPayConfig;
import cn.lizongyi.shareaccount.response.QueryFeatureListResponse;
import cn.lizongyi.shareaccount.util.JacksonUtils;
import cn.lizongyi.shareaccount.util.NumberUtil;
import cn.lizongyi.shareaccount.util.WxQRcodeUtil;
import cn.lizongyi.shareaccount.entity.Bill;
import cn.lizongyi.shareaccount.entity.Member;
import cn.lizongyi.shareaccount.dao.MemberMapper;
import cn.lizongyi.shareaccount.dao.BillMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 李宗义 lizongyi@itbox.cn
 * @version 1.0.0
 * @date 2024-11-19
 * @description
 */
@Slf4j
@Service
public class BaseHandler {

    private static final double RATE = 0.006; // 微信支付手费率0.6%
    private static final double MAX_DISTRIBUTION_RATIO = 0.3; // 最大分账比例30%

    @Lazy
    @Autowired
    private ConfigService configService;

    @Lazy
    @Autowired
    private FileService fileService;

    @Lazy
    @Autowired
    private UserIdService userIdService;

    @Lazy
    @Autowired
    private UserMapper userMapper;

    // 发送消息给用户
    @Lazy
    @Autowired
    private WeChatMessageSender messageSender;

    @Lazy
    @Autowired
    private WxQRcodeUtil wxQRcodeUtil;

    @Lazy
    @Autowired
    private LedgerMapper ledgerMapper;

    @Lazy
    @Autowired
    private AccountMapper accountMapper;

    @Lazy
    @Autowired
    private MemberMapper memberMapper;

    @Lazy
    @Autowired
    private BillMapper billMapper;

    @Lazy
    @Autowired
    private BillService billService;

    @Lazy
    @Autowired
    private ClassEntityService classEntityService;


    /**
     * 获取配置
     */
    public String getCongigValue(Long userId, String key){
        return configService.getConfigValue(userId, key);
    }


    /**
     * 获取默认昵称
     */
    public String getDefaultNickName() {
        return configService.getConfigValue(null, Constants.DEFAULT_NICKNAME);
    }

    /**
     * 获取默认小数点位数
     */
    public int getDecimalPosition(Long userId) {
        String decimalPosition = configService.getConfigValue(userId, Constants.DECIMAL_POSITION);
        if(decimalPosition == null){
            return 2;
        }
        return Integer.parseInt(decimalPosition);
    }

    /**
     * 获取默认头像
     */
    public String getDefaultUserAvatar(Long userId) {
        return configService.getConfigValue(userId, Constants.DEFAULT_USER_AVATAR);
    }

    /**
     * 获取默认后端地址
     */
    public String getDefaultBackendUrl(Long userId) {
        return configService.getConfigValue(userId, Constants.DEFAULT_BACKEND_URL);
    }

    /**
     * 获取游客模式-测试用户名前缀
     */
    public String getTestUserName() {
        return configService.getConfigValue(null, Constants.TEST_USERNAME);
    }

    /**
     * 获取游客模式-测试用户密码
     */
    public String getTestUserPassword() {
        return configService.getConfigValue(null, Constants.TEST_PASSWORD);
    }

    // ================================= COS配置 =================================

    /**
     * 获取COS App ID
     */
    public String getCosAppId() {
        return configService.getConfigValue(null, Constants.COS_APP_ID);
    }

    /**
     * 获取COS Secret ID
     */
    public String getCosSecretId() {
        return configService.getConfigValue(null, Constants.COS_SECRET_ID);
    }

    /**
     * 获取COS Secret Key
     */
    public String getCosSecretKey() {
        return configService.getConfigValue(null, Constants.COS_SECRET_KEY);
    }

    /**
     * 获取COS Bucket
     */
    public String getCosBucket() {
        return configService.getConfigValue(null, Constants.COS_BUCKET);
    }

    /**
     * 获取COS Region
     */
    public String getCosRegion() {
        return configService.getConfigValue(null, Constants.COS_REGION);
    }

    /**
     * 获取COS Domain
     */
    public String getCosDomain() {
        return configService.getConfigValue(null, Constants.COS_DOMAIN);
    }

    /**
     * 获取COS URL
     */
    public String getCosUrl() {
        return configService.getConfigValue(null, Constants.COS_URL);
    }

    // ================================= 微信V2配置 =================================

    /**
     * 获取微信V2 App ID
     */
    public String getWechatV2Appid() {
        return configService.getConfigValue(null, Constants.WECHAT_V2_APPID);
    }

    /**
     * 获取微信V2 App Secret
     */
    public String getWechatV2Appsecret() {
        return configService.getConfigValue(null, Constants.WECHAT_V2_APPSECRET);
    }

    /**
     * 获取微信V2 商户号
     */
    public String getWechatV2MchId() {
        return configService.getConfigValue(null, Constants.WECHAT_V2_MCH_ID);
    }

    /**
     * 获取微信V2 MCH Key
     */
    public String getWechatV2MchKey() {
        return configService.getConfigValue(null, Constants.WECHAT_V2_MCH_KEY);
    }

    /**
     * 获取微信V2 证书路径
     */
    public String getWechatV2KeyPath() {
        return configService.getConfigValue(null, Constants.WECHAT_V2_KEY_PATH);
    }

    /**
     * 获取微信V2 支付回调通知地址
     */
    public String getWechatV2NotifyUrl() {
        return configService.getConfigValue(null, Constants.WECHAT_V2_NOTIFY_URL);
    }

    // ================================= 微信V3配置 =================================

    /**
     * 获取微信V3 App ID
     */
    public String getWechatV3Appid() {
        return configService.getConfigValue(null, Constants.WECHAT_V3_APPID);
    }

    /**
     * 获取微信V3 商户号
     */
    public String getWechatV3MchId() {
        return configService.getConfigValue(null, Constants.WECHAT_V3_MCH_ID);
    }

    /**
     * 获取微信V3 商户API证书路径
     */
    public String getWechatV3PrivateKeyPath() {
        return configService.getConfigValue(null, Constants.WECHAT_V3_PRIVATE_KEY_PATH);
    }

    /**
     * 获取微信V3 平台证书路径
     */
    public String getWechatV3PublicKeyPath() {
        return configService.getConfigValue(null, Constants.WECHAT_V3_PUBLIC_KEY_PATH);
    }

    /**
     * 获取微信V3 商户证书序列号
     */
    public String getWechatV3SerialNo() {
        return configService.getConfigValue(null, Constants.WECHAT_V3_SERIAL_NO);
    }

    /**
     * 获取微信V3 支付回调通知地址
     */
    public String getWechatV3NotifyUrl() {
        return configService.getConfigValue(null, Constants.WECHAT_V3_NOTIFY_URL);
    }

    /**
     * 获取微信V3 API密钥
     */
    public String getWechatV3ApiV3Key() {
        return configService.getConfigValue(null, Constants.WECHAT_V3_API_V3_KEY);
    }

    // ================================= 抖音支付配置 =================================

    /**
     * 获取抖音支付配置（JSON格式）
     */
    public String getDouyinPayConfig() {
        return configService.getConfigValue(null, Constants.DOUYIN_PAY_CONFIG);
    }

    /**
     * 获取抖音支付配置并解析为对象
     */
    public DouyinPayConfig getDouyinPayConfigBean() {
        String configJson = getDouyinPayConfig();
        if (configJson == null || configJson.isEmpty()) {
            return null;
        }
        try {
            return JacksonUtils.strToBean(configJson, DouyinPayConfig.class);
        } catch (Exception e) {
            log.error("解析抖音支付配置失败: {}", e.getMessage(), e);
            return null;
        }
    }

    // ================================= JWT配置 =================================

    /**
     * 获取JWT密钥
     */
    public String getJwtSecret() {
        return configService.getConfigValue(null, Constants.JWT_SECRET);
    }

    /**
     * 获取 我的-功能列表
     */
    public QueryFeatureListResponse getFeatureList(Long userId){

        String configValue = configService.getConfigValue(null, Constants.FEATURE_LIST);

        if(configValue == null){
            return null;
        }
        QueryFeatureListResponse response = JacksonUtils.strToBean(configValue, QueryFeatureListResponse.class);

        Integer userRole = getUserRole(userId);

        // 获取账单数量显示阈值配置
        String billCountShowStr = configService.getConfigValue(null, Constants.BILL_COUNT_SHOW);
        int billCountShowValue = 1;
        if (billCountShowStr != null && !billCountShowStr.trim().isEmpty()) {
            try {
                billCountShowValue = Integer.parseInt(billCountShowStr.trim());
            } catch (NumberFormatException e) {
                log.warn("billCountShow 配置解析失败，使用默认值1: {}", billCountShowStr);
            }
        }
        final int billCountShow = billCountShowValue;

        // 获取用户账单数量
        int userBillCountValue = billService.countByUserId(userId);
        final int userBillCount = userBillCountValue;

        // 普通用户只能看到一部分功能
        if(userRole == RoleTypeEnum.USER.getId()){
            List<QueryFeatureListResponse.FeatureItem> squareList = response.getSquare().stream()
                    .filter(s -> s.getRole().contains(userRole.toString()))
                    .filter(s -> s.getBillCount() == null || s.getBillCount() <= 0 || userBillCount > billCountShow && s.getBillCount() > billCountShow)
                    .toList();
            List<QueryFeatureListResponse.FeatureItem> stripList = response.getStrip().stream()
                    .filter(s -> s.getRole().contains(userRole.toString()))
                    .filter(s -> s.getBillCount() == null || s.getBillCount() <= 0 || userBillCount > billCountShow && s.getBillCount() > billCountShow)
                    .toList();

            response.setSquare(squareList);
            response.setStrip(stripList);
        }
        return response;
    }

    /**
     * 获取 图片 最新访问路径
     */
    public Picture fillPicPresignUrl(Picture picture) {
        if(picture == null || picture.getStatus() == 1){
            return null;
        }
        URL url = fileService.generatePresignedUrl(picture.getPath() + picture.getName(), 30);
        picture.setAddress(url.toString());
        return picture;
    }

    /**
     * 获取userId
     */
    public Long getUserId(){
        try{
            return userIdService.getUserId();
        }catch (Exception e){
            log.error("从request中没有获取到userId", e);
            return null;
        }
    }


    /**
     * 查询用户信息
     */
    public User getUserById(Long userId){
        if(userId == null || userId == 0){
            return null;
        }
        User user = userMapper.findById(userId);
        if(user == null){
            log.info("根据id:{} 没有查询到用户", userId);
            return null;
        }
        return user;
    }

    /**
     * 查询用户信息
     */
    public User getUserByPhone(String phone){
        List<User> userList = userMapper.findByPhone(phone);
        if(CollectionUtils.isEmpty(userList)){
            log.info("根据手机号{} 没有查询到用户信息", phone);
            return null;
        }
        return userList.get(0);
    }

    /**
     * 查询用户信息
     */
    public List<User> getAllUserByPhone(String phone){
        List<User> userList = userMapper.findByPhone(phone);
        if(CollectionUtils.isEmpty(userList)){
            log.info("根据手机号{} 没有查询到用户信息", phone);
            return null;
        }
        return userList;
    }


    /**
     * 查询用户数量
     */
    public Integer getUserCount(){
        return userMapper.findUserCount();
    }

    /**
     * 判断是否为本小程序第一个用户，如果是直接判定为管理员
     */
    public void checkUserAdmin(Long userId){
        // 先检查用户数量
        int userCount = userMapper.findUserCount();
        if(userCount == 1){
            // 设置为管理员
            //log.info("设置第一个用户为管理员结果{}", success);
        }
    }


    /**
     * 判断用户 角色 默认 用户
     */
    public Integer getUserRole(Long userId){
        Integer userRole = RoleTypeEnum.USER.getId();
        if(userId == null || userId == 0L){
            return userRole;
        }
        User user = userMapper.findById(userId);
        if(user == null){
            log.info("用户null");
            return userRole;
        }
        userRole = user.getRole();
        log.info("用户角色={}", userRole);
        return userRole;
    }

    public boolean isGuestUser(Long userId) {
        if (userId == null || userId == 0L) {
            return false;
        }
        User user = userMapper.findById(userId);
        if (user == null) {
            return false;
        }
        String openid = user.getOpenid();
        return openid != null && openid.startsWith("guest_");
    }


    /**
     * 通过微信api生成二维码
     */
    public String generateQRCode(String page, String scene) throws Exception{
        return wxQRcodeUtil.generateQRCode(page, scene);
    }



    /**
     * 解冻 - 商家分账单号
     */
    public String getThawOutOrderNo(Long orderId){
        String id = "thaw_" + orderId.toString() + "_" + NumberUtil.getRandom();
        log.info("解冻时，商家分账单号：{}", id);
        return id;
    }


    /**
     * 提取out_trade_no中的orderId。
     * 假设out_trade_no格式为"orderId_{orderId}_{timestamp}"
     */
    public Long extractOrderIdFromOutTradeNo(String outTradeNo) {
        String[] parts = outTradeNo.split("_");
        if (parts.length > 1) {
            return Long.parseLong(parts[1]);
        }
        throw new IllegalArgumentException("Invalid out_trade_no format.");
    }




    /**
     * 根据期望的分账金额（以分为单位）反推用户需支付的最小金额（以分为单位）。
     *
     * @param desiredProfitSharingAmount 期望的分账金额（单位：分）
     * @return 用户需支付的最小金额（单位：分）
     */
    public static long calculateRequiredPayment(long desiredProfitSharingAmount) {
        // 使用Math.ceil来确保足够的支付金额
        long paymentAmount = (long) Math.ceil(desiredProfitSharingAmount / MAX_DISTRIBUTION_RATIO / (1 - RATE));

        // 验证实际可分账金额是否满足要求
        while (true) {
            long actualProfitSharingAmount = calculateActualProfitSharingAmount(paymentAmount);
            if (actualProfitSharingAmount >= desiredProfitSharingAmount) {
                return paymentAmount;
            }
            paymentAmount++; // 如果不满足条件，则增加支付金额
        }
    }


    /**
     * 计算实际可分账金额（以分为单位）。
     *
     * @param paymentAmount 用户支付的金额（单位：分）
     * @return 实际可分账金额（单位：分）
     */
    public static long calculateActualProfitSharingAmount(long paymentAmount) {
        // 计算手续费（单位：分）
        long fee = Math.round(paymentAmount * RATE);

        // 扣除手续费后的金额（单位：分）
        long netAmount = paymentAmount - fee;

        // 应用最大分账比例计算出可分账的最大金额（单位：分）
        return Math.round(netAmount * MAX_DISTRIBUTION_RATIO);
    }

    /**
     * 判断用户是否有权限查看账本下的所有账单。
     * 逻辑：
     * 1）如果用户是账本创建人，则有权限；
     * 2）否则，判断该用户是否为该账本的共享成员（status=0，且 bill_id 为空；parent_user_id 或 user_id 与当前用户匹配）。
     */
    public boolean canViewLedger(Long userId, Long ledgerId) {
        try {
            if (userId == null || userId <= 0 || ledgerId == null || ledgerId <= 0) {
                return false;
            }
            // 创建人直接有权限
            Ledger ledger = ledgerMapper.findById(ledgerId);
            if (ledger != null && ledger.getUserId() != null && ledger.getUserId().equals(userId)) {
                return true;
            }
            // 共享成员（账本级分享：bill_id 为空）
            List<Member> members = memberMapper.findByLedgerId(ledgerId);
            if (!CollectionUtils.isEmpty(members)) {
                for (Member m : members) {
                    Integer status = m.getStatus();
                    Long billId = m.getBillId();
                    Long parentUserId = m.getParentUserId();
                    Long sharedUserId = m.getUserId();
                    if (status != null && status == 1 && billId == null &&
                            ((parentUserId != null && parentUserId.equals(userId)) || (sharedUserId != null && sharedUserId.equals(userId)))) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("判断账本查看权限失败 ledgerId={} userId={} err={}", ledgerId, userId, e.getMessage());
            return false;
        }
    }

    /**
     * 判断用户是否有权限查看指定账单。
     * 逻辑：
     * 1）如果用户是账单创建人，则有权限；
     * 2）否则，若用户对所属账本有账本级分享权限（canViewLedger=true），也有权限；
     * 3）否则，判断该用户是否为该账单的共享成员（status=0，且 member 记录的 bill_id = 该账单ID；
     *    parent_user_id 或 user_id 与当前用户匹配）。
     */
    public boolean canViewBill(Long userId, Long billId) {
        try {
            if (userId == null || userId <= 0 || billId == null || billId <= 0) {
                return false;
            }
            Bill bill = billMapper.findById(billId);
            if (bill == null) {
                return false;
            }
            // 创建人直接有权限
            if (bill.getUserId() != null && bill.getUserId().equals(userId)) {
                return true;
            }
            Long ledgerId = bill.getLedgerId();
            // 若对所属账本有账本级权限，也可以查看该账单
            if (canViewLedger(userId, ledgerId)) {
                return true;
            }
            // 账单级分享：member 中 bill_id = 该账单ID
            // 先检查被分享人（user_id）匹配的情况
            Member byUser = memberMapper.findByLedgerIdAndUserId(ledgerId, billId, userId);
            if (byUser != null && byUser.getStatus() != null && byUser.getStatus() == 0) {
                return true;
            }
            // 再检查分享人（parent_user_id）匹配的情况（使用账本成员列表做过滤）
            List<Member> members = memberMapper.findByLedgerId(ledgerId);
            if (!CollectionUtils.isEmpty(members)) {
                for (Member m : members) {
                    Integer status = m.getStatus();
                    Long memberBillId = m.getBillId();
                    Long parentUserId = m.getParentUserId();
                    if (status != null && status == 1 && memberBillId != null && memberBillId.equals(billId) &&
                            (parentUserId != null && parentUserId.equals(userId))) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("判断账单查看权限失败 billId={} userId={} err={}", billId, userId, e.getMessage());
            return false;
        }
    }

    /**
     * 综合判断：当 billId 不为空时判断账单权限，否则判断账本权限。
     */
    public boolean canView(Long userId, Long ledgerId, Long billId) {
        if (billId != null && billId > 0) {
            return canViewBill(userId, billId);
        }
        return canViewLedger(userId, ledgerId);
    }

    /**
     * 获取当前用户可访问的账本ID列表（包含自己创建的账本与别人分享给我的账本，均需账本状态正常）。
     */
    public java.util.List<Long> getAccessibleLedgerIdsForCurrentUser() {
        try {
            Long userId = getUserId();
            java.util.List<Long> ids = new java.util.ArrayList<>();
            // 自己的账本（状态正常）
            java.util.List<cn.lizongyi.shareaccount.entity.Ledger> ownLedgers = ledgerMapper.findByUserId(userId);
            if (ownLedgers != null) {
                for (cn.lizongyi.shareaccount.entity.Ledger l : ownLedgers) {
                    if (l != null && l.getStatus() != null && l.getStatus() == 0) {
                        ids.add(l.getId());
                    }
                }
            }
            // 别人分享给我的账本（member：账本级分享 bill_id 为空，且成员状态正常）
            java.util.List<cn.lizongyi.shareaccount.entity.Member> members = memberMapper.findNormalByUserId(userId, 1);
            if (members != null) {
                for (cn.lizongyi.shareaccount.entity.Member m : members) {
                    if (m != null && m.getBillId() == null && m.getStatus() != null && m.getStatus() == 1) {
                        cn.lizongyi.shareaccount.entity.Ledger ledger = ledgerMapper.findById(m.getLedgerId());
                        if (ledger != null && ledger.getStatus() != null && ledger.getStatus() == 0) {
                            ids.add(ledger.getId());
                        }
                    }
                }
            }
            // 去重并保持插入顺序
            java.util.LinkedHashSet<Long> distinct = new java.util.LinkedHashSet<>(ids);
            log.info("获取当前用户可访问账本ID成功: {}", distinct);
            return new java.util.ArrayList<>(distinct);
        } catch (Exception e) {
            log.warn("获取当前用户可访问账本ID失败: {}", e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 初始化账本
     */
    private void initializeLedgers(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 创建共享账本（默认账本）
        Ledger sharedLedger = new Ledger();
        sharedLedger.setUserId(userId);
        sharedLedger.setName("家庭/情侣账本");
        sharedLedger.setType(0); // 0、共享账本
        sharedLedger.setProperty(0); // 0、普通账本
        sharedLedger.setClassId(-1L);
        sharedLedger.setStatus(0); // 0、正常
        sharedLedger.setMemo("");
        sharedLedger.setCreateTime(now);
        sharedLedger.setIsDefault(1); // 1、是默认账本
        
        ledgerMapper.insert(sharedLedger);
        log.info("为用户{}创建共享账本成功，账本ID: {}", userId, sharedLedger.getId());
        
        // 2. 创建个人账本
        Ledger personalLedger = new Ledger();
        personalLedger.setUserId(userId);
        personalLedger.setName("个人账本");
        personalLedger.setType(1); // 1、个人账本
        personalLedger.setProperty(0); // 0、普通账本
        personalLedger.setClassId(-1L);
        personalLedger.setStatus(0); // 0、正常
        personalLedger.setMemo("");
        personalLedger.setCreateTime(now);
        personalLedger.setIsDefault(0); // 0、不是默认账本
        
        ledgerMapper.insert(personalLedger);
        log.info("为用户{}创建个人账本成功，账本ID: {}", userId, personalLedger.getId());
    }
    
    /**
     * 初始化账户
     */
    private void initializeAccounts(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 创建储蓄账户
        Account savingAccount = new Account();
        savingAccount.setName("你的储蓄账户");
        savingAccount.setUserId(userId);
        savingAccount.setType(0); // 0、储蓄账户
        savingAccount.setIsBudget(0); // 0、计入预算
        savingAccount.setIsTotalMoney(0); // 0、计入总资产
        savingAccount.setIsDefault(1); // 默认账户
        savingAccount.setBalance(0L); // 初始余额为0
        savingAccount.setStatus(0); // 0、正常
        savingAccount.setMemo("");
        savingAccount.setCreateTime(now);
        accountMapper.insert(savingAccount);
        
        // 2. 创建信贷账户
        Account creditAccount = new Account();
        creditAccount.setName("你的信贷账户");
        creditAccount.setUserId(userId);
        creditAccount.setType(1); // 1、信贷账户
        creditAccount.setIsBudget(0); // 0、计入预算
        creditAccount.setIsTotalMoney(0); // 0、计入总资产
        creditAccount.setIsDefault(0);
        creditAccount.setBalance(0L); // 初始余额为0
        creditAccount.setStatus(0); // 0、正常
        creditAccount.setMemo("");
        accountMapper.insert(creditAccount);
        
        // 3. 创建充值账户
        Account rechargeAccount = new Account();
        rechargeAccount.setName("你的充值账户");
        rechargeAccount.setUserId(userId);
        rechargeAccount.setType(2); // 2、充值账户
        rechargeAccount.setIsBudget(0); // 0、计入预算
        rechargeAccount.setIsTotalMoney(0); // 0、计入总资产
        rechargeAccount.setIsDefault(0);
        rechargeAccount.setBalance(0L); // 初始余额为0
        rechargeAccount.setStatus(0); // 0、正常
        rechargeAccount.setMemo("");
        rechargeAccount.setCreateTime(now);
        accountMapper.insert(rechargeAccount);
        
        // 4. 创建投资理财账户
        Account investmentAccount = new Account();
        investmentAccount.setName("你的投资理财账户");
        investmentAccount.setUserId(userId);
        investmentAccount.setType(3); // 3、投资理财
        investmentAccount.setIsBudget(0); // 0、计入预算
        investmentAccount.setIsTotalMoney(0); // 0、计入总资产
        investmentAccount.setIsDefault(0);
        investmentAccount.setBalance(0L); // 初始余额为0
        investmentAccount.setStatus(0); // 0、正常
        investmentAccount.setMemo("");
        accountMapper.insert(investmentAccount);
        
        log.info("为用户{}创建4个默认账户成功", userId);
    }

    /**
     * 初始化新用户数据
     * @param userId 用户ID
     */
    public void initializeTasksForUser(Long userId) {
        log.info("开始初始化新用户数据，用户ID: {}", userId);
        
        try {
            // 初始化账本
            initializeLedgers(userId);
            
            // 初始化账户
            initializeAccounts(userId);
            
            log.info("新用户数据初始化完成，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("新用户数据初始化失败，用户ID: {}", userId, e);
        }
    }


    /**
     * 查询分类下面的子分类
     */
    public List<Long> getChildCategoryIds(Long categoryId) {
        List<Long> childCategoryIds = new ArrayList<>();
        // 如果分类id存在，递归查询所有子分类id
        if(categoryId != null && categoryId > 0) {
            childCategoryIds.add(categoryId);
            childCategoryIds.addAll(classEntityService.selectAllChildIds(categoryId));
        }
        return childCategoryIds;
    }

}
