package cn.lizongyi.shareaccount.constants;

/**
 * @author Jingruokui
 * @description: Created by admin
 * @date 2024/3/12 10:47
 */
public final class Constants {

    public static final String CURRENCY = "currency";  // 币种
    public static final String DECIMAL_POSITION = "decimal_position";  // 小数点位数
    public static final String DEFAULT_NICKNAME = "default_nickname";  // 默认昵称
    public static final String TEST_USERNAME = "test_username";  // 游客模式-用户名的前缀-1天有效期，到期提醒用户必须登录
    public static final String TEST_PASSWORD = "test_password";  // 游客模式-密码
    public static final String DEFAULT_USER_AVATAR = "default_user_avatar";  // 默认头像
    public static final String DEFAULT_BACKEND_URL = "default_backend_url";  // 默认后端url
    public static final String FEATURE_LIST = "feature_list";  // 我的-功能列表
    
    // COS配置
    public static final String COS_APP_ID = "cos.app-id";  // COS App ID
    public static final String COS_SECRET_ID = "cos.secret-id";  // COS Secret ID
    public static final String COS_SECRET_KEY = "cos.secret-key";  // COS Secret Key
    public static final String COS_BUCKET = "cos.bucket";  // COS Bucket
    public static final String COS_REGION = "cos.region";  // COS Region
    public static final String COS_DOMAIN = "cos.domain";  // COS Domain
    public static final String COS_URL = "cos.url";  // COS URL
    
    // 微信支付V2配置
    public static final String WECHAT_V2_APPID = "wechat.v2.appid";  // 微信V2 App ID
    public static final String WECHAT_V2_APPSECRET = "wechat.v2.appsecret";  // 微信V2 App Secret
    public static final String WECHAT_V2_MCH_ID = "wechat.v2.mchId";  // 微信V2 商户号
    public static final String WECHAT_V2_MCH_KEY = "wechat.v2.mchKey";  // 微信V2 MCH Key
    public static final String WECHAT_V2_KEY_PATH = "wechat.v2.keyPath";  // 微信V2 证书路径
    public static final String WECHAT_V2_NOTIFY_URL = "wechat.v2.notifyUrl";  // 微信V2 支付回调通知地址
    
    // 微信支付V3配置
    public static final String WECHAT_V3_APPID = "wechat.v3.appid";  // 微信V3 App ID
    public static final String WECHAT_V3_MCH_ID = "wechat.v3.mchId";  // 微信V3 商户号
    public static final String WECHAT_V3_PRIVATE_KEY_PATH = "wechat.v3.privateKeyPath";  // 微信V3 商户API证书路径
    public static final String WECHAT_V3_PUBLIC_KEY_PATH = "wechat.v3.publicKeyPath";  // 微信V3 平台证书路径
    public static final String WECHAT_V3_SERIAL_NO = "wechat.v3.serialNo";  // 微信V3 商户证书序列号
    public static final String WECHAT_V3_NOTIFY_URL = "wechat.v3.notifyUrl";  // 微信V3 支付回调通知地址
    public static final String WECHAT_V3_API_V3_KEY = "wechat.v3.apiV3Key";  // 微信V3 API密钥
    
    // JWT配置
    public static final String JWT_SECRET = "jwt.secret";  // JWT密钥

    // 游客模式配置
    public static final String GUEST_MODE_ENABLED = "guest.mode.enabled";           // 游客模式开关：默认开启
    public static final String GUEST_CLEANUP_ENABLED = "guest.cleanup.enabled";     // 游客清理开关：默认开启
    public static final String GUEST_CLEANUP_DAYS = "guest.cleanup.days";          // 游客数据保留天数：默认1天
    public static final String GUEST_SAMPLE_BILL_ENABLED = "guest.sample.bill.enabled"; // 首次游客登录是否注入示例账单：默认开启


    /**
     * 起点
     */
    public static final Integer PASSING_POINT_ORIGIN = 0;
    /**
     * 终点
     */
    public static final Integer PASSING_POINT_DEST = 100;
    /**
     * 乘客位置
     */
    public static final Integer PASSING_POINT_PASSENGER = 101;
    /**
     * 双引号
     */
    public static final String DOUBLE_QUOTATION_MACK = "\"";
    /**
     * 分隔符-
     */
    public static final String SEPARATOR_1 = "-";
    /**
     * 分隔符#
     */
    public static final String SEPARATOR_2 = "#";
    /**
     * 分隔符_
     */
    public static final String SEPARATOR_3 = "_";
    /**
     * 分隔符"一个空格"
     */
    public static final String SEPARATOR_4 = " ";
    /**
     * 星号
     */
    public static final String ASTERISK = "*";
    /**
     * 无节点
     */
    public static final String NO_NODE = "NO_NODE";
    /**
     * 逗号
     */
    public static final String COMMA_STRING = ",";
    /**
     * 自动驾驶的车辆
     */
    public static final String AUTO_SELF_CAR = "7#8";
    /**
     * vip
     */
    public static final String VIP = "vip";
    /**
     * 默认字符
     */
    public static final String STR_DEFAULT = "globalConfig";

    public static final String GLOBAL = "global";
    /**
     * 七级六边形密度:端内+端外(派单距离)
     */
    public static final String ALL_SOURCE_TYPE = "1_2";
    /**
     * 七级六边形密度:全量呼叫含出(派单距离)
     */
    public static final String ALL_BUSINESS_TYPE = "1";
    /**
     * 聚类接起率:端内(派单距离)
     */
    public static final String INDEX_SOURCE_TYPE = "1";
    /**
     * 聚类接起率:全量呼叫不含出(派单距离)
     */
    public static final String INDEX_BUSINESS_TYPE = "2";
    /**
     * OR
     */
    public static final String OR = "OR";
    /**
     * AND
     */
    public static final String AND = "AND";
    /**
     * 车辆到家
     */
    public static final String CAR_TO_HOME = "_carToHome";
    /**
     * 订单到家
     */
    public static final String ORDER_TO_HOME = "_orderToHome";
    /**
     * 时间格式
     */
    public static final String TIME_PATTERN_1 = "yyyy-MM-dd";
    /**
     * 时间格式
     */
    public static final String TIME_PATTERN_2 = "yyyy-MM-dd HH:mm:ss";

    public static final String TIME_PATTERN_3 = "yyyy/MM/dd HH:mm:ss";
    /**
     * 开始时间
     */
    public static final String START = "00:00:00";
    /**
     * 结束时间
     */
    public static final String END = "23:59:59";
    /**
     * 三方运力举手唯一ID {三方运力ID}^{三方二级运力ID}^{车型}
     */
    public static final String PLATFORM_UNIQUE_ID = "%s^%s^%s";
    /**
     * 日志告警开关
     */
    public static final String LOG_ALARM_SWITCH = "logAlarmSwitch";

    /**
     * 配置查询开关
     */
    public static final String COMMON_CONFIG_QUERY_SWITCH = "commonConfigQuerySwitch";

    /**
     * 日志开关
     */
    public static final String COMMON_CONFIG_LOG_SWITCH = "commonConfigLogSwitch";

    /**
     * 表示：“全国”、“全业务线”等
     */
    public static final String DEFAULT_VALUE = "-1";

    public static final class Redis {
        /**
         * 派单核心系统redis前缀
         */
        public static final String REDIS_KEY_PREFIX = "MATCHING:CORE:";
        /**
         * 配置中心系统redis前缀
         */
        public static final String REDIS_CONFIG_KEY_PREFIX = "STRATEGY:CONFIG:";
        /**
         * 策略配置服务-redis前缀
         */
        public static final String REDIS_STRATEGY_CONFIG_APP_KEY_PREFIX = "STRATEGY:CONFIG:APP:";
        /**
         * 车辆特征redis key
         */
        public static final String KEY_CAR_FEATURE = REDIS_KEY_PREFIX + "CAR_FEATURE:%s";
        /**
         * 订单特征redis key
         */
        public static final String KEY_ORDER_FEATURE = REDIS_KEY_PREFIX + "ORDER_FEATURE:%s";
        /**
         * key
         */
        public static final String KEY_ORDER_POOL = REDIS_KEY_PREFIX + "ORDER_POOL:%s:%s";
        /**
         * 预约订单池redis key
         */
        public static final String KEY_APPOINT_ORDER_POOL = REDIS_KEY_PREFIX + "APPOINT_ORDER_POOL:%s:%s";
        /**
         * 订单有多少司机抢 key
         */
        public static final String KEY_GRAB_ORDER = REDIS_KEY_PREFIX + "GRAB_ORDER:%s";
        /**
         * 订单第一辆车抢单标识
         */
        public static final String KEY_GRAB_ORDER_FIRST = REDIS_KEY_PREFIX + "GRAB_ORDER_FIRST:%s";
        /**
         * 订单id对应的当前抢单活动
         */
        public static final String KEY_GRAB_ORDER_ACTIVITY = REDIS_KEY_PREFIX + "GRAB_ORDER_ACTIVITY:%s";
        /**
         * 抢单池redis key
         */
        public static final String KEY_GRAB_ORDER_POOL = REDIS_KEY_PREFIX + "GRAB_ORDER_POOL:%s";
        /**
         * 出租车订单池redis ZSet key
         */
        public static final String KEY_TAXI_ORDER_POOL = REDIS_KEY_PREFIX + "TAXI_ORDER_POOL:%s:%s";
        /**
         * 出租车抢单池redis ZSet key
         */
        public static final String KEY_TAXI_GRAB_ORDER_POOL = REDIS_KEY_PREFIX + "TAXI_GRAB_ORDER_POOL:%s";
        /**
         * 车辆当前抢单播单信息redis key
         */
        public static final String KEY_CAR_GRAB_BROADCAST_ORDER = REDIS_KEY_PREFIX + "CAR_GRAB_BROADCAST_ORDER:%s";
        /**
         * 车辆拒单列表redis ZSet key
         */
        public static final String KEY_CAR_REJECT_ORDER = REDIS_KEY_PREFIX + "CAR_REJECT_ORDER:%s";
        /**
         * 车辆抢单列表redis ZSet key
         */
        public static final String KEY_CAR_GRAB_ORDER = REDIS_KEY_PREFIX + ":CAR_GRAB_ORDER:%s";
        /**
         * 车辆池redis key
         */
        public static final String KEY_CAR_POOL = REDIS_KEY_PREFIX + "CAR_POOL:%s:%s";
        /**
         * 车辆分桶redis key
         */
        public static final String KEY_CAR_POOL_BUCKET = REDIS_KEY_PREFIX + "CAR_POOL_BUCKET:%s:%s";
        /**
         * 预约单自动抢单车辆优先桶
         */
        public static final String KEY_CAR_APPOINT_GRABBER_BUCKET =
            REDIS_KEY_PREFIX + "{CAR_APPOINT_GRABBER:%s}:BUCKET:%s";
        /**
         * 预约单自动抢单车辆优先桶司机总数
         */
        public static final String KEY_CAR_APPOINT_GRABBER_NUM = REDIS_KEY_PREFIX + "{CAR_APPOINT_GRABBER:%s}:NUM";
        /**
         * 订单车辆绑定关系
         */
        public static final String KEY_BIND_ORDER = REDIS_KEY_PREFIX + "BIND_ORDER_CAR:%s";
        /**
         * 车辆订单绑定关系
         */
        public static final String KEY_BIND_CAR = REDIS_KEY_PREFIX + "BIND_CAR_ORDER:%s";
        /**
         * 订单锁
         */
        public static final String KEY_ORDER_LOCK = REDIS_KEY_PREFIX + "ORDER_LOCK:%s";
        /**
         * 订单锁，等待时长，单位：毫秒
         */
        public static final String ORDER_LOCK_WAIT = "ORDER_LOCK_WAIT";
        /**
         * 车辆锁
         */
        public static final String KEY_CAR_LOCK = REDIS_KEY_PREFIX + "CAR_LOCK:%s";
        /**
         * 车辆锁，等待时长，单位：毫秒
         */
        public static final String CAR_LOCK_WAIT = "CAR_LOCK_WAIT";
        /**
         * 车辆预绑单(司机举手)订单信息 %s: vin Value: Json String
         */
        public static final String KEY_CAR_PRE_BIND_ORDER = REDIS_KEY_PREFIX + "CAR_PRE_BIND_ORDER:%s";
        /**
         * 城市锁
         */
        public static final String KEY_CITY_LOCK = REDIS_KEY_PREFIX + "CITY_LOCK:%s";
        /**
         * 城市订单抓取任务下次执行时间
         */
        public static final String KEY_CITY_NEXT_TIME = REDIS_KEY_PREFIX + "CITY_NEXT_TIME:%s";

        /**
         * 城市锁
         */
        public static final String KEY_TAXI_CITY_LOCK = REDIS_KEY_PREFIX + "CITY_TAXI_LOCK:%s";
        /**
         * 城市订单抓取任务下次执行时间
         */
        public static final String KEY_TAXI_CITY_NEXT_TIME = REDIS_KEY_PREFIX + "CITY_TAXI_NEXT_TIME:%s";

        /**
         * 城市锁等待时间
         */
        public static final String KEY_CITY_LOCK_WAIT = "KEY_CITY_LOCK_WAIT";
        /**
         * 聚合运力订单锁
         */
        public static final String KEY_PLATFORM_ORDER_LOCK = REDIS_KEY_PREFIX + "PLATFORM_ORDER_LOCK:%s";
        /**
         * 围栏内车辆下线时间
         */
        public static final String KEY_RAIL_CAR_OFFLINE = REDIS_KEY_PREFIX + "RAIL_CAR_OFFLINE:%s:%s";
        /**
         * 围栏内车辆订单乘客取消次数
         */
        public static final String KEY_RAIL_CAR_ORDER_PASSENGER_CANCEL_COUNT =
            REDIS_KEY_PREFIX + "RAIL_CAR_ORDER_PASSENGER_CANCEL_COUNT:%s:%s";
        /**
         * 围栏内车辆短单信息
         */
        public static final String KEY_RAIL_CAR_SHORT_ORDER = REDIS_KEY_PREFIX + "RAIL_CAR_SHORT_ORDER:%s:%s";
        /**
         * 围栏内车辆短单预计返回围栏时间
         */
        public static final String FIELD_RAIL_CAR_SHORT_RETURN_RAIL_TIME = "PLAN_RETURN_TIME";
        /**
         * 围栏优先队列
         */
        public static final String KEY_RAIL_PRIORITY = REDIS_KEY_PREFIX + "RAIL_PRIORITY:%s:%s";
        /**
         * 围栏普通队列
         */
        public static final String KEY_RAIL_NORMAL = REDIS_KEY_PREFIX + "RAIL_NORMAL:%s:%s";
        /**
         * 围栏锁
         */
        public static final String KEY_RAIL_LOCK = REDIS_KEY_PREFIX + "RAIL_LOCK:%s:%s";
        /**
         * 车辆全局锁信息
         */
        public static final String GLOBAL_CAR_LOCK = REDIS_KEY_PREFIX + "GLOBAL_CAR_LOCK:{%s}";
        /**
         * 可派单城市redis key
         */
        public static final String DISPATCH_CITY_POOL = REDIS_KEY_PREFIX + "DISPATCH_CITY_POOL";

        /**
         * 聚合平台订单key %s订单ID
         */
        public static final String KEY_PLATFORM_ORDER = REDIS_KEY_PREFIX + "PLATFORM_ORDER:%s";

        /**
         * 聚合平台派单结果key %s订单ID
         */
        public static final String KEY_PLATFORM_RESULT = REDIS_KEY_PREFIX + "PLATFORM_RESULT:%s";

        /**
         * 聚合平台订单池 %s城市ID
         */
        public static final String KEY_PLATFORM_ORDER_POOL = REDIS_KEY_PREFIX + "PLATFORM_ORDER_POOL:%s";

        /**
         * 三方订单城市最近一次播单时间
         */
        public static final String KEY_PLATFORM_CITY_DISPATCH_TIME = REDIS_KEY_PREFIX + "PLATFORM_CITY_DISPATCH_TIME";
        /**
         * 用户倾斜redis key %s用户id按150分桶
         */
        public static final String KEY_USER_TILT = REDIS_KEY_PREFIX + "USER_TILT:%s";
        /**
         * 端内外同呼标识key
         */
        public static final String KEY_MIXED_CALL_IDENTITY = REDIS_KEY_PREFIX + "MIXED_CALL_IDENTITY:%s";

        /**
         * 配置中心阿波罗配置redis key:serviceName
         */
        public static final String KEY_DECISION_CONFIG = REDIS_CONFIG_KEY_PREFIX + "DECISION_CONFIG:%s";
        /**
         * 配置中心指标配置redis key:hash节点
         */
        public static final String KEY_INDEX_CONFIG = REDIS_CONFIG_KEY_PREFIX + "INDEX_CONFIG:%s";
        /**
         * 配置中心六边形订单积压实时指标配置redis key:城市+来源+产品线车型等级
         */
        public static final String KEY_HEX_ORD_STOCK = REDIS_CONFIG_KEY_PREFIX + "HEX_ORD_STOCK:%s";
        /**
         * 配置中心当日订单实时指标配置redis key:产品线车型等级
         */
        public static final String KEY_TODAY_ORD = REDIS_CONFIG_KEY_PREFIX + "TODAY_ORD:%s";
        /**
         * 配置中心司机群组流水实时指标配置redis key:司机群组code
         */
        public static final String KEY_DRIVER_FLOW = REDIS_CONFIG_KEY_PREFIX + "DRIVER_FLOW:%s";
        /**
         * 配置中心司机群组在线时长实时指标配置redis key:司机群组code
         */
        public static final String KEY_ONLINE_DURATION = REDIS_CONFIG_KEY_PREFIX + "ONLINE_DURATION:%s";
        /**
         * 配置中心司机群组巡游时长实时指标配置redis key:司机群组code
         */
        public static final String KEY_CRUISE_DURATION = REDIS_CONFIG_KEY_PREFIX + "CRUISE_DURATION:%s";
        /**
         * 实时端内全城接起率相关指标配置redis key:cityCode
         */
        public static final String KEY_ORDER_REPLY = REDIS_CONFIG_KEY_PREFIX + "ORDER_REPLY:%s";
        /**
         * 实时端内全城六边形接起率相关指标配置redis key:cityCode sourceType
         */
        public static final String KEY_HEX_ORDER_REPLY = REDIS_CONFIG_KEY_PREFIX + "HEX_ORDER_REPLY:%s";
        /**
         * 实时七级六边形呼叫接起率密度相关指标配置redis key:cityCode
         */
        public static final String KEY_HEX_CALL_REPLY_RATE = REDIS_CONFIG_KEY_PREFIX + "HEX_CALL_REPLY_RATE:%s";
        /**
         * 配置中心司机群组天周月离线指标配置redis key:司机群组code
         */
        public static final String KEY_OFFLINE_DRIVE = REDIS_CONFIG_KEY_PREFIX + "OFFLINE_DRIVE:%s";
        /**
         * 配置中心司机群组天周月离线指标配置redis key:司机群组code
         */
        public static final String KEY_OFFLINE_PRICE_RANGE = REDIS_CONFIG_KEY_PREFIX + "OFFLINE_PRICE_RANGE:%s";
        /**
         * 配置中心三方订单7天内接起率及价格分位离线指标配置redis key:cityCode
         */
        public static final String KEY_OFFLINE_THIRD_LIMIT = REDIS_CONFIG_KEY_PREFIX + "OFFLINE_THIRD_LIMIT:%s";
        /**
         * 配置中心城市功勋值lv档fph及中位值离线指标配置redis key:cityCode
         */
        public static final String KEY_LV_FPH = REDIS_CONFIG_KEY_PREFIX + "LV_FPH:%s";
        /**
         * 配置中心渠道完单率离线指标配置redis key:cityCode
         */
        public static final String KEY_SOURCE_COMMISSION = REDIS_CONFIG_KEY_PREFIX + "SOURCE_COMMISSION:%s";
        /**
         * 配置中心雪融区离线指标配置redis key:cityCode
         */
        public static final String KEY_SNOW_AREA = REDIS_CONFIG_KEY_PREFIX + "SNOW_AREA:%s";
        /**
         * 配置中心订单等级划分离线指标配置redis key:cityCode
         */
        public static final String KEY_CRUISE_DURATION_THRESHOLD =
            REDIS_CONFIG_KEY_PREFIX + "CRUISE_DURATION_THRESHOLD:%s";
        /**
         * 配置中心订单等级划分离线指标配置redis key:cityCode
         */
        public static final String KEY_ORDER_PRICE_RANGE = REDIS_CONFIG_KEY_PREFIX + "ORDER_PRICE_RANGE:%s";
        /**
         * 配置中心全渠道日均7级六边形呼叫累计占比离线
         */
        public static final String KEY_HEX_CALL_TOTAL_RATE = REDIS_CONFIG_KEY_PREFIX + "HEX_CALL_TOTAL_RATE:%s";
        /**
         * 配置中心聚合类接起率指标配置redis key:cityCode sourceType
         */
        public static final String KEY_GATHER_REPLY = REDIS_CONFIG_KEY_PREFIX + "GATHER_REPLY:%s";
        /**
         * 配置中心载霜率指标配置redis key:cityCode
         */
        public static final String KEY_FROST_LOADING = REDIS_CONFIG_KEY_PREFIX + "FROST_LOADING:%s";
        /**
         * 配置中心阿波罗配置redis key:serviceName
         */
        public static final String KEY_DECISION_CONFIG_TIMESTAMP =
            REDIS_CONFIG_KEY_PREFIX + "DECISION_CONFIG_TIME_STAMP:%s";
        /**
         * 业务线优先级策略 redis key:cityCode areaCode
         */
        public static final String KEY_BIZ_STRATEGY = REDIS_CONFIG_KEY_PREFIX + "BIZ_STRATEGY:%s";
        /**
         * 资源省市区 redis key:resource code
         */
        public static final String KEY_RESOURCE_CITY = REDIS_CONFIG_KEY_PREFIX + "RESOURCE_CITY:%s";
        /**
         * 业产车json
         */
        public static final String KEY_RESOURCE_BUSINESS_GRADE_PRODUCT =
            REDIS_CONFIG_KEY_PREFIX + "RESOURCE_BUSINESS_GRADE_PRODUCT";
        /**
         * 资源全部城市
         */
        public static final String KEY_RESOURCE_ALL_CITY = REDIS_CONFIG_KEY_PREFIX + "RESOURCE_ALL_CITY";
        /**
         * 策略配置 redis key : ruleType+维度
         */
        public static final String KEY_STRATEGY_CONFIG = REDIS_CONFIG_KEY_PREFIX + "STRATEGY_CONFIG:%s";
        /**
         * 节假日数据 redis key : cityCode
         */
        public static final String KEY_HOLIDAY_DATA = REDIS_CONFIG_KEY_PREFIX + "HOLIDAY_DATA:%s";
        /**
         * 司乘绑定KEY：passengerId
         */
        public static final String KEY_PASSENGER_DRIVER_BIND = REDIS_KEY_PREFIX + "PASSENGER_DRIVER:BIND:%s";
        /**
         * 同呼锁
         */
        public static final String KEY_SAME_DEMAND_LOCK = REDIS_KEY_PREFIX + "SAME:DEMAND:LOCK:%s";

        /**
         * 同呼需求缓存
         */
        public static final String KEY_SAME_DEMAND = REDIS_KEY_PREFIX + "SAME:DEMAND:%s";

        /**
         * 同呼需求推送结果缓存
         */
        public static final String KEY_SAME_CAR_ORDER = REDIS_KEY_PREFIX + "SAME:DEMAND:CAR:ORDER:%s";

        /**
         * 司机意愿度离线数据key:cityCode:分片
         */
        public static final String DRIVER_WILLING_OFFLINE_FEATURE_KEY =
            Redis.REDIS_KEY_PREFIX + "DRIVER_WILLING:CAR:OFFLINE_FEATURE:%s:%d";

        /**
         * 司机意愿度离线数据key:cityCode:分片
         */
        public static final String DRIVER_WILLING_OFFLINE_FEATURE_KEY_V2 =
            Redis.REDIS_KEY_PREFIX + "DRIVER_WILLING:CAR:OFFLINE_FEATURE:V2:%s:%d";

        /**
         * 订单起终点网格离线特征key:cityCode:小时:分片
         */
        public static final String GRID_DIM_OFFLINE_FEATURE_KEY =
            Redis.REDIS_KEY_PREFIX + "DRIVER_WILLING" + ":GRID:OFFLINE_FEATURE:%s:%d:%d";

        /**
         * 三方引流费比例 hash filed:产品线 value:比例
         */
        public static final String THIRD_DRAINAGE_FEE_RATE_KEY = Redis.REDIS_KEY_PREFIX + "THIRD:DRAINAGE_FEE_RATE";

        /**
         * 企业和企业组数据 redis key :date+企业id
         */
        public static final String KEY_ENTERPRISE_GROUP = REDIS_CONFIG_KEY_PREFIX + "ENTERPRISE_GROUP:%s";

        /**
         * 派单距离策略最大eta配置
         */
        public static final String KEY_DISTANCE_MAX_ETA = REDIS_CONFIG_KEY_PREFIX + "DISTANCE_MAX_ETA";

        /**
         * 订单车辆绑定关系KEY
         */
        public static final String KEY_BIND_ORDER_LOCK = REDIS_KEY_PREFIX + "matching:lock:order:%s";
        /**
         * 车辆订单绑定关系KEY
         */
        public static final String KEY_BIND_CAR_LOCK = REDIS_KEY_PREFIX + "matching:lock:car:%s";

        /**
         * 车辆中间状态KEY
         */
        public static final String KEY_CAR_MIDDLE_STATUS = REDIS_KEY_PREFIX + "matching:car:middle:status:%s";

        /**
         * 轮播次数KEY
         */
        public static final String KEY_ROUND_ROBIN = REDIS_KEY_PREFIX + "ROUND_ROBIN:%s:%s";

        /**
         * 订单发生改派订单的车辆 KEY
         */
        public static final String KEY_ORDER_REASSIGNMENT_CAR = REDIS_KEY_PREFIX + "ORDER_REASSIGNMENT_CAR:%s";

        /**
         * 车辆绑定预约单KEY
         */
        public static final String KEY_CAR_BIND_APP_ORDER = REDIS_KEY_PREFIX + "CAR_BIND_APP_ORDER:%s";

        /**
         * 策略配置服务-方案刷新Job锁
         */
        public static final String KEY_PLAN_STATE_REFRESH_LOCK =
            REDIS_STRATEGY_CONFIG_APP_KEY_PREFIX + "PLAN_STATE_REFRESH_LOCK:%s";

        /**
         * 郑州演练前置数据KEY
         */
        public static final String KEY_CAR_SHOW = REDIS_KEY_PREFIX + "SHOW";

        /**
         * 缓存开关
         */
        public static final String KEY_SWITCH = REDIS_KEY_PREFIX + "SWITCH";
    }

    public static final class Product {
        /**
         * 快
         */
        public static final Integer QUICKER = 4;

        /**
         * 惠
         */
        public static final Integer ENJOY = 6;
        /**
         * 自动驾驶
         */
        public static final Integer AUTO = 7;
    }

    /**
     * 车型
     */
    public static final class VehicleType {

        public static final String QUICKER_1 = Product.QUICKER + "#1";

        public static final String ENJOY_10 = Product.ENJOY + "#10";

        public static final String AUTO_8 = Product.AUTO + "#8";
    }

    public static final class OrderType {
        public static final String NORMAL_REAL_ORDER = "normalRealOrder";
        public static final String APPOINT_ORDER = "appointOrder";
        public static final String LONG_APPOINT_ORDER = "longAppointOrder";
        public static final String TAXI_GRABBER_ORDER = "taxiGrabberOrder";
        public static final String RAIL_ORDER = "railOrder";
        public static final String CARPOOL_ORDER = "carpoolOrder";
        public static final String ORIENT_ORDER = "orientOrder";
        public static final String QUICK_ORDER = "quickOrder";
        public static final String PLATFORM_ORDER = "platformOrder";
        public static final String ALL_ORDER = "allPushOrder";
    }

    /**
     * 时间格式
     *
     * @author guofeng
     * @date 2024/08/08
     */
    public static final class TimeFormat {
        /**
         * yyyy-MM-dd HH:mm:ss
         */
        public static final String YYYY_MM_DD_HH_MM_SS = "yyyy/MM/dd HH:mm:ss";
        /**
         * yyyyMMddHHmm
         */
        public static final String YYYYMMDDHHMM = "yyyyMMddHHmm";
        /**
         * yyyy-MM-dd
         */
        public static final String YYYY_MM_DD = "yyyy-MM-dd";
        /**
         * yyyyMMdd
         */
        public static final String YYYYMMDD = "yyyyMMdd";
        /**
         * HH:mm:ss
         */
        public static final String HHMMSS = "HH:mm:ss";
        /**
         * HH:mm
         */
        public static final String HHMM = "HH:mm";
    }

    public static String SENDTIMEENUMNAME = "今天";
    public static String SENDTIMEENUMNETNAME = "明天";
    public static String SENDTIMEENUMNETNAME2 = "后天";
    public static String CAN_ACTIVITY_TITLE = "新用户立减15元";

    // 发件人邮箱地址
    public static String EMAIL_ADMIN_ACCOUNT = "526140450@qq.com";
}