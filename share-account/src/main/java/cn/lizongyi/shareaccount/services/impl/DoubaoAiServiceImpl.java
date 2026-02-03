package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.response.AccountResponse;
import cn.lizongyi.shareaccount.response.BillResponse;
import cn.lizongyi.shareaccount.response.LedgerResponse;
import cn.lizongyi.shareaccount.services.AccountService;
import cn.lizongyi.shareaccount.services.AiService;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.ClassEntityService;
import cn.lizongyi.shareaccount.services.ConfigService;
import cn.lizongyi.shareaccount.services.LedgerService;
import cn.lizongyi.shareaccount.entity.Picture;
import cn.lizongyi.shareaccount.entity.ClassEntity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpServerErrorException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

/**
 * 豆包AI服务实现类
 * @author lizongyi
 */
@Slf4j
@Service("doubaoAiService")
public class DoubaoAiServiceImpl implements AiService {
    
    // 配置键名常量
    private static final String CONFIG_API_KEY = "ai.doubao.api_key";
    private static final String CONFIG_BASE_URL = "ai.doubao.base_url";
    private static final String CONFIG_TIMEOUT = "ai.doubao.timeout";
    private static final String CONFIG_MAX_RETRY = "ai.doubao.max_retry";
    private static final String CONFIG_MODEL = "ai.doubao.model";
    
    // 默认配置值
    private static final String DEFAULT_BASE_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
    private static final String DEFAULT_MODEL = "doubao-seed-1-6-flash-250828";
    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000; // 5秒
    private static final int DEFAULT_READ_TIMEOUT = 30000; // 30秒
    private static final int DEFAULT_MAX_RETRY_COUNT = 2;
    
    // 配置属性
    private String apiKey;
    private String baseUrl;
    private String model;
    private int connectionTimeout;
    private int readTimeout;
    private int maxRetryCount;
    
    @Resource
    private RestTemplate restTemplate;
    
    @Resource
    private BaseHandler baseHandler;
    
    @Resource
    private ConfigService configService;
    
    @Resource
    private LedgerService ledgerService;
    
    @Resource
    private AccountService accountService;
    
    @Resource
    private ClassEntityService classEntityService;
    
    @PostConstruct
    public void init() {
        // 从数据库config表查询配置
        this.apiKey = configService.getStringValue(CONFIG_API_KEY, "");
        this.baseUrl = configService.getStringValue(CONFIG_BASE_URL, DEFAULT_BASE_URL);
        this.model = configService.getStringValue(CONFIG_MODEL, DEFAULT_MODEL);
        
        // 从数据库读取超时和重试配置，转换为整数
        try {
            this.readTimeout = Integer.parseInt(configService.getStringValue(CONFIG_TIMEOUT, String.valueOf(DEFAULT_READ_TIMEOUT)));
        } catch (NumberFormatException e) {
            log.warn("读取超时配置格式错误，使用默认值: {}", DEFAULT_READ_TIMEOUT, e);
            this.readTimeout = DEFAULT_READ_TIMEOUT;
        }
        
        try {
            this.maxRetryCount = Integer.parseInt(configService.getStringValue(CONFIG_MAX_RETRY, String.valueOf(DEFAULT_MAX_RETRY_COUNT)));
        } catch (NumberFormatException e) {
            log.warn("读取最大重试次数配置格式错误，使用默认值: {}", DEFAULT_MAX_RETRY_COUNT, e);
            this.maxRetryCount = DEFAULT_MAX_RETRY_COUNT;
        }
        
        // 连接超时固定使用默认值
        this.connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        
        log.info("初始化豆包AI服务，配置: baseUrl={}, model={}, connectionTimeout={}ms, readTimeout={}ms, maxRetryCount={}", 
                this.baseUrl, this.model, this.connectionTimeout, this.readTimeout, this.maxRetryCount);
        
        // 配置RestTemplate，设置超时
        if (this.restTemplate == null) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(this.connectionTimeout);
            factory.setReadTimeout(this.readTimeout);
            this.restTemplate = new RestTemplate(factory);
            log.info("创建新的RestTemplate实例，连接超时: {}ms, 读取超时: {}ms", 
                    this.connectionTimeout, this.readTimeout);
        }
    }
    
    @Override
    public BillResponse recognizeBillScreenshot(Picture picture, Long userId) throws IOException {
        log.info("开始识别消费截图，用户ID: {}, 图片URL: {}", userId, picture.getAddress());
        
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // 优先使用API Key作为token，兼容旧版配置
        String accessToken = getAccessToken();
        if(accessToken == null){
            log.error("获取访问令牌失败，无法继续识别");
            return null;
        }
        headers.set("Authorization", "Bearer " + accessToken);
        
        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", this.model);
        
        // 构建消息数组
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        
        // 构建内容数组
        JSONArray content = new JSONArray();
        
        // 添加图片URL（按照样例格式）
        JSONObject imageContent = new JSONObject();
        imageContent.put("type", "image_url");
        JSONObject imageUrlObj = new JSONObject();
        imageUrlObj.put("url", picture.getAddress());
        imageContent.put("image_url", imageUrlObj);
        content.add(imageContent);
        
        // 添加文本指令
        JSONObject textContent = new JSONObject();
        textContent.put("type", "text");
        
        // 构建包含所有分类信息的提示词
        String prompt = buildPromptWithCategories("识别收支账单", userId);
        
        textContent.put("text", prompt);
        content.add(textContent);
        
        message.put("content", content);
        messages.add(message);
        
        requestBody.put("messages", messages);

        log.info("准备发送请求到: {}, 请求参数: {}", baseUrl, requestBody.toString());
        
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
        
        // 带重试机制的请求发送
        String response = null;
        int retryCount = 0;
        while (retryCount <= this.maxRetryCount) {
            try {
                response = restTemplate.postForObject(baseUrl, requestEntity, String.class);
                log.info("成功接收AI响应 {}", response);
                break; // 成功则跳出循环
            } catch (HttpServerErrorException e) {
                // 服务器错误，可能是临时问题，可以重试
                retryCount++;
                    if (retryCount <= this.maxRetryCount) {
                        log.warn("服务器错误 ({}), {}/{} 重试中...", e.getStatusCode(), retryCount, this.maxRetryCount);
                    try {
                        // 指数退避
                        long waitTime = (long) (1000 * Math.pow(2, retryCount - 1));
                        TimeUnit.MILLISECONDS.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("达到最大重试次数，请求失败: {}", e.getMessage());
                    return null;
                }
            } catch (RestClientException e) {
                // 其他客户端错误，不重试
                log.error("请求发送失败: {}", e.getMessage());
                return null;
            } catch (Exception e) {
                log.error("发生异常: {}", e.getMessage());
                return null;
            }
        }
        
        if (response == null) {
            log.error("未收到AI响应");
            return null;
        }
        
        // 解析响应结果
        return parseRecognitionResult(response, userId);
    }



    /**
     * 识别用户发送的聊天内容，提取消费信息
     * @param chat 聊天内容
     * @param userId 用户ID
     * @return 识别结果，封装为Bill对象
     * @throws IOException 网络或IO异常
     */
    @Override
    public BillResponse recognizeBillChat(String chat, Long userId) throws IOException {
        // 去掉特殊字符
        chat = chat.trim();
        log.info("开始识别消费聊天内容，用户ID: {}, 聊天内容: {}", userId, chat);
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // 优先使用API Key作为token，兼容旧版配置
        String accessToken = getAccessToken();
        if(accessToken == null){
            log.error("获取访问令牌失败，无法继续识别");
            return null;
        }
        headers.set("Authorization", "Bearer " + accessToken);
        
        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", this.model);
        
        // 构建消息数组
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        
        // 添加文本指令
        JSONObject textContent = new JSONObject();
        textContent.put("type", "text");
        
        // 构建包含所有分类信息的提示词
        String prompt = buildPromptWithCategories("识别\"" + chat + "\"为收支账单", userId);
        
        message.put("content", prompt);
        messages.add(message);
        
        requestBody.put("messages", messages);

        log.info("准备发送请求到: {}, 请求参数: {}", baseUrl, requestBody.toString());

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
        
        // 带重试机制的请求发送
        String response = null;
        int retryCount = 0;
        while (retryCount <= this.maxRetryCount) {
            try {
                response = restTemplate.postForObject(baseUrl, requestEntity, String.class);
                log.info("成功接收AI响应 {}", response);
                break; // 成功则跳出循环
            } catch (HttpServerErrorException e) {
                // 服务器错误，可能是临时问题，可以重试
                retryCount++;
                    if (retryCount <= this.maxRetryCount) {
                        log.warn("服务器错误 ({}), {}/{} 重试中...", e.getStatusCode(), retryCount, this.maxRetryCount);
                    try {
                        // 指数退避
                        long waitTime = (long) (1000 * Math.pow(2, retryCount - 1));
                        TimeUnit.MILLISECONDS.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("达到最大重试次数，请求失败: {}", e.getMessage());
                    return null;
                }
            } catch (RestClientException e) {
                // 其他客户端错误，不重试
                log.error("请求发送失败: {}", e.getMessage());
                return null;
            } catch (Exception e) {
                log.error("发生异常: {}", e.getMessage());
                return null;
            }
        }
        
        if (response == null) {
            log.error("未收到AI响应");
            return null;
        }
        
        // 解析响应结果
        return parseRecognitionResult(response, userId);
    }
    
    
    /**
     * 获取访问令牌
     * @return 访问令牌
     */
    private String getAccessToken() {
        if (apiKey != null && !apiKey.isEmpty()) {
            log.info("直接使用配置的API Key作为访问令牌");
            return apiKey;
        }
        return null;
    }
    
    /**
     * 构建包含分类信息的提示词
     * @param prefix 提示词前缀
     * @param userId 用户ID
     * @return 完整的提示词
     */
    private String buildPromptWithCategories(String prefix, Long userId) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(prefix);
        // 获取当前时间，格式为YYYY-MM-dd HH:mm:ss
        String currentTime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        promptBuilder.append("，按要求返回JSONObject（如果有多个订单就汇总成一个JSONObject再返回，不是JSONArray，无多余文本）：1.memo：一句话描述所有商品/商家/支出/退款/收入等 2.amount：汇总金额（退款不计） 3.datetime：时间转YYYY-MM-dd HH:mm:ss，图里没有时间则使用当前默认时间:" + currentTime + " 4.reply：可爱搞笑风格或温暖慰藉风格女孩回复 5.category：选最匹配的ID（分类列表：");

        
        // 查询所有 正常状态下 最底层的分类（type=2）
        try {
            List<ClassEntity> classList = classEntityService.selectByUserIdAndStatus(true, userId, 0);
            if (classList != null && !classList.isEmpty()) {
                StringBuilder categoriesBuilder = new StringBuilder();
                for (ClassEntity category : classList) {
                    if (categoriesBuilder.length() > 0) {
                        categoriesBuilder.append(",");
                    }
                    categoriesBuilder.append(category.getId() + "-" + category.getName().trim());
                }
                promptBuilder.append(categoriesBuilder.toString());
            } else {
                log.warn("用户暂无可用分类列表");
            }
        } catch (Exception e) {
            log.warn("获取分类列表失败: {}", e.getMessage());
        }
        
        promptBuilder.append(")");
        return promptBuilder.toString();
    }
    
    /**
     * 解析识别结果
     * @param response API响应
     * @param userId 用户ID
     * @return 解析后的Bill对象
     */
    private BillResponse parseRecognitionResult(String response, Long userId) {
        log.info("开始解析AI识别结果");
        BillResponse bill = new BillResponse();
        bill.setUserId(userId);
        
        try {
            // 查询默认账本ID
            Long defaultLedgerId = getDefaultLedgerId(userId);
            if (defaultLedgerId != null) {
                bill.setLedgerId(defaultLedgerId);
                log.debug("设置默认账本ID: {}", defaultLedgerId);
            }
            
            // 查询默认账户ID
            Long defaultAccountId = getDefaultAccountId(userId);
            if (defaultAccountId != null) {
                bill.setAccountId(defaultAccountId);
                log.debug("设置默认账户ID: {}", defaultAccountId);
            }
            
            // 解析完整响应
            JSONObject jsonResponse = JSONObject.parseObject(response);
            
            // 检查响应结构是否正确
            if (!jsonResponse.containsKey("choices")) {
                log.error("AI响应格式错误，缺少choices字段: {}", response);
                return bill;
            }
            
            JSONArray choicesArray = null;
            try {
                choicesArray = jsonResponse.getJSONArray("choices");
            } catch (Exception e) {
                log.error("AI响应中choices不是有效的数组");
                return bill;
            }
            
            if (choicesArray.isEmpty()) {
                log.error("AI响应中choices数组为空");
                return bill;
            }
            
            // 获取AI回复内容
            JSONObject choices = choicesArray.getJSONObject(0);
            if (!choices.containsKey("message")) {
                log.error("AI响应格式错误，缺少message字段");
                return bill;
            }
            
            JSONObject message = null;
            try {
                message = choices.getJSONObject("message");
            } catch (Exception e) {
                log.error("AI响应中message不是有效的JSON对象");
                return bill;
            }
            
            if (!message.containsKey("content")) {
                log.error("AI响应格式错误，缺少content字段");
                return bill;
            }
            
            String aiContent = message.getString("content");
            log.debug("AI返回内容: {}", aiContent);
            
            // 尝试解析AI返回的JSON
            JSONObject result;
            try {
                result = JSONObject.parseObject(aiContent);
            } catch (Exception e) {
                log.error("无法解析AI返回的JSON格式: {}", aiContent, e);
                // 尝试提取JSON部分（有时候AI可能在JSON前后加文字说明）
                try {
                    Pattern jsonPattern = Pattern.compile("\\{[^}]*\\}");
                    Matcher matcher = jsonPattern.matcher(aiContent);
                    if (matcher.find()) {
                        String jsonStr = matcher.group();
                        result = JSONObject.parseObject(jsonStr);
                        log.info("成功提取并解析嵌套JSON");
                    } else {
                        throw new RuntimeException("未找到有效的JSON格式");
                    }
                } catch (Exception ex) {
                    log.error("提取JSON失败: {}", ex.getMessage());
                    return bill;
                }
            }
            
            // 提取备注信息
            if (result.containsKey("memo")) {
                String memo = result.getString("memo");
                if (memo != null && !memo.isEmpty() && !"null".equalsIgnoreCase(memo)) {
                    bill.setMemo(memo);
                    log.debug("解析到备注信息: {}", memo);
                }
            }

            // 提取备注信息
            if (result.containsKey("reply")) {
                String reply = result.getString("reply");
                if (reply != null && !reply.isEmpty() && !"null".equalsIgnoreCase(reply)) {
                    bill.setOtherMemo(reply);
                    bill.setShowOtherMemo(true);
                    log.debug("解析到其他备注信息: {}", reply);
                }
            }
            
            // 提取金额 - 转换为分存储
            if (result.containsKey("amount")) {
                String amountStr = result.getString("amount");
                if (amountStr != null && !amountStr.isEmpty() && !"null".equalsIgnoreCase(amountStr)) {
                    // 清理金额字符串，只保留数字和小数点
                    String cleanAmount = amountStr.replaceAll("[^0-9.]", "");
                    try {
                        double amount = Double.parseDouble(cleanAmount);
                        // 转换为分并存储为Long类型
                        bill.setPrice(Math.round(amount * 100));
                        log.debug("解析到金额: {} (元)", amount);
                    } catch (NumberFormatException e) {
                        log.warn("无法解析金额: {}", amountStr);
                    }
                }
            }
            
            // 提取时间
            if (result.containsKey("datetime")) {
                String datetimeStr = result.getString("datetime");
                if (datetimeStr != null && !datetimeStr.isEmpty() && !"null".equalsIgnoreCase(datetimeStr)) {
                    // 尝试解析时间
                    try {
                        // 支持更多的时间格式
                        String cleanTimeStr = datetimeStr.replaceAll("[年月日时分秒]", "-")
                                .replaceAll("\\s+", " ");
                        
                        // 尝试匹配完整的日期时间格式 (包含时分秒)
                        Pattern dateTimePattern = Pattern.compile("\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}[\\sT]\\d{1,2}:\\d{1,2}:\\d{1,2}");
                        Matcher dateTimeMatcher = dateTimePattern.matcher(cleanTimeStr);
                        
                        if (dateTimeMatcher.find()) {
                            // 找到包含时分秒的完整日期时间
                            String fullDateTimeStr = dateTimeMatcher.group();
                            // 标准化格式，确保使用 '-' 和 ' '
                            fullDateTimeStr = fullDateTimeStr.replace("/", "-")
                                                           .replace('T', ' ');
                            
                            // 尝试直接解析
                            try {
                                bill.setBillTime(fullDateTimeStr);
                                log.debug("解析到完整日期时间: {}", fullDateTimeStr);
                            } catch (Exception innerE) {
                                // 如果解析失败，尝试拆分处理
                                String[] dateTimeParts = fullDateTimeStr.split("\\s+");
                                if (dateTimeParts.length >= 2) {
                                    String datePart = dateTimeParts[0];
                                    String timePart = dateTimeParts[1];
                                    
                                    // 标准化日期部分
                                    datePart = datePart.replace("/", "-");
                                    String[] dateParts = datePart.split("-");
                                    if (dateParts.length >= 3) {
                                        String formattedDate = String.format("%s-%02d-%02d", 
                                                dateParts[0], Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[2]));
                                        
                                        // 标准化时间部分
                                        String[] timeParts = timePart.split(":");
                                        if (timeParts.length >= 3) {
                                            String formattedTime = String.format("%02d:%02d:%02d", 
                                                    Integer.parseInt(timeParts[0]), 
                                                    Integer.parseInt(timeParts[1]), 
                                                    Integer.parseInt(timeParts[2]));
                                            
                                            String fullFormattedDateTime = formattedDate + " " + formattedTime;
                                            bill.setBillTime(fullFormattedDateTime);
                                            log.debug("解析并格式化日期时间: {}", fullFormattedDateTime);
                                        }
                                    }
                                }
                            }
                        } else {
                            // 如果没有找到包含时分秒的格式，回退到只解析日期
                            Pattern datePattern = Pattern.compile("\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}");
                            Matcher dateMatcher = datePattern.matcher(cleanTimeStr);
                            if (dateMatcher.find()) {
                                String dateStr = dateMatcher.group();
                                // 标准化日期格式
                                dateStr = dateStr.replace("/", "-");
                                // 确保月和日都是两位数
                                String[] parts = dateStr.split("-");
                                if (parts.length >= 3) {
                                    String formattedDate = String.format("%s-%02d-%02d", 
                                            parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                                    bill.setBillTime(formattedDate + " 00:00:00");
                                    log.debug("仅解析到日期部分: {}", formattedDate);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("无法解析时间: {}", datetimeStr, e);
                    }
                }
            }
            
            // 提取分类ID和名称
            if (result.containsKey("category")) {
                String categoryStr = result.getString("category");
                if (categoryStr != null && !categoryStr.isEmpty() && !"null".equalsIgnoreCase(categoryStr)) {
                    try {
                        Long classId = Long.parseLong(categoryStr);
                        bill.setClassId(classId);
                        log.debug("解析到分类ID: {}", classId);
                        
                        // 根据分类ID查询topClassId
                        try {
                            Long topClassId = classEntityService.getTopClassId(classId);
                            if (topClassId != null) {
                                bill.setTopClassId(topClassId);
                                log.debug("设置顶层分类ID: {}", topClassId);
                            }
                        } catch (Exception e) {
                            log.warn("获取顶层分类ID失败: {}", e.getMessage());
                        }
                    } catch (NumberFormatException e) {
                        log.warn("无法解析分类ID: {}", categoryStr);
                    }
                }
            }
            log.info("AI识别结果解析完成");
            
        } catch (Exception e) {
            log.error("解析AI识别结果失败", e);
        }
        
        return bill;
    }
    
    /**
     * 获取用户的默认账本ID
     * @param userId 用户ID
     * @return 默认账本ID
     */
    private Long getDefaultLedgerId(Long userId) {
        try {
            List<LedgerResponse> ledgers = ledgerService.findUserLedgers();
            if (ledgers != null && !ledgers.isEmpty()) {
                // 优先查找标记为默认的账本（isDefault=1）
                for (LedgerResponse ledger : ledgers) {
                    if (ledger.getIsDefault() != null && ledger.getIsDefault() == 1) {
                        return ledger.getId();
                    }
                }
                // 如果没有默认账本，返回第一个账本
                return ledgers.get(0).getId();
            }
        } catch (Exception e) {
            log.warn("获取默认账本ID失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取用户的默认账户ID
     * @param userId 用户ID
     * @return 默认账户ID
     */
    private Long getDefaultAccountId(Long userId) {
        try {
            List<AccountResponse> accounts = accountService.getUserAccountList(userId);
            if (accounts != null && !accounts.isEmpty()) {
                // 优先查找标记为默认的账户（isDefault=1）
                for (AccountResponse account : accounts) {
                    if (account.getIsDefault() != null && account.getIsDefault() == 1) {
                        return account.getId();
                    }
                }
                // 如果没有默认账户，返回第一个账户
                return accounts.get(0).getId();
            }
        } catch (Exception e) {
            log.warn("获取默认账户ID失败: {}", e.getMessage());
        }
        return null;
    }
}