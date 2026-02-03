package cn.lizongyi.shareaccount.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JSON工具类
 * 
 * @author 林淮川 linhuaichuan@itbox.cn
 * @date 2024/01/29
 */
@Slf4j
public class JacksonUtils {

    private static final String JSON_TO_STRING_ERROR = "json to string error{}";

    private static final ObjectMapper OBJECT_MAPPER;

    private static final ObjectMapper DEFAULT_MAPPER;

    static {
        // 是否输出transient字段
        OBJECT_MAPPER = JsonMapper.builder().configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, false).build();
        // 所有的日期都统一用yyyy-MM-dd HH:mm:ss格式
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 是否允许使用注释
        // objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        // 字段允许去除引号
        // objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 允许单引号
        // objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 允许转义字符
        // objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        // 严格重复检测
        // objectMapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);

        // 空对象不出错
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 识别不带引号的key
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 识别单引号的key
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        // 不输出空值字段
        OBJECT_MAPPER.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        // 不检测失败字段映射:忽略
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 浮点数默认为 BigDecimal
        OBJECT_MAPPER.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        // 时间字段输出时间戳
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        // 时间输出为毫秒而非纳秒
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        // 时间读取为毫秒而非纳秒
        OBJECT_MAPPER.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

        // 序列化后带有@class属性
        // objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
        // ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);

        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.registerModule(new Jdk8Module());
        // 添加Long精度恢复
        SimpleModule longModule = new SimpleModule();
        longModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        longModule.addSerializer(Long.class, ToStringSerializer.instance);
        OBJECT_MAPPER.registerModule(longModule);

        DEFAULT_MAPPER = JsonMapper.builder().configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, false).build();
    }

    /**
     * 对象转json字符串
     *
     * @param value 需要转换的对象
     * @return json格式字符串
     */
    public static String objToStr(Object value) {
        return objToStr(value, true);
    }

    /**
     * 对象转json字符串
     *
     * @param value 需要转换的对象
     * @param hasClass 序列化是否带@class属性
     * @return json格式字符串
     */
    public static String objToStr(Object value, boolean hasClass) {
        String result = "";
        try {
            result = hasClass ? OBJECT_MAPPER.writeValueAsString(value) : DEFAULT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            log.warn(JSON_TO_STRING_ERROR, ex.getMessage());
        }
        return result;
    }

    /**
     * 字符串转泛型对象
     * 
     * @param <T> 泛型
     *
     * @param content 转换前字符串
     * @param valueType 转换类
     * @return <T> 转换后对象
     */
    public static <T> T strToObj(String content, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(content, valueType);
        } catch (JsonProcessingException e) {
            try {
                return DEFAULT_MAPPER.readValue(content, valueType);
            } catch (JsonProcessingException ex) {
                log.warn(JSON_TO_STRING_ERROR, ex.getMessage());
                return null;
            }
        }
    }

    /**
     * 字符串转泛型对象（慎用）
     * 
     * @param <T> 泛型
     *
     * @param content 转换前字符串
     * @param type 转换原始(类型擦除)类
     * @return <T> 转换后对象
     */
    public static <T> T strToObj(String content, Type type) {
        TypeFactory.rawClass(type);
        return strToObj(content, new TypeReference<>() {
            @Override
            public Type getType() {
                return type;
            }
        });
    }

    /**
     * 字符串转泛型对象（超复杂类型专用）
     * 
     * @param <T> 泛型
     *
     * @param content 转换前字符串
     * @param typeReference 转换泛型类，例如new TypeReference<Response<List<User>>>(){}
     * @return <T> 转换后对象
     */
    public static <T> T strToObj(String content, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(content, typeReference);
        } catch (JsonProcessingException e) {
            try {
                return DEFAULT_MAPPER.readValue(content, typeReference);
            } catch (JsonProcessingException ex) {
                log.warn(JSON_TO_STRING_ERROR, ex.getMessage());
                return null;
            }
        }
    }

    /**
     * 字符串转泛型对象（超复杂类型专用）
     * 
     * @param <T> 泛型
     * @return <T> 转换后对象
     * 
     * @param content 转换前字符串
     * @param javaType 转换原始类
     */
    public static <T> T strToObj(String content, JavaType javaType) {
        try {
            return OBJECT_MAPPER.readValue(content, javaType);
        } catch (JsonProcessingException e) {
            try {
                return DEFAULT_MAPPER.readValue(content, javaType);
            } catch (JsonProcessingException ex) {
                log.warn(JSON_TO_STRING_ERROR, ex.getMessage());
                return null;
            }
        }
    }

    /**
     * json字符串转换为List
     * 
     * @param <T> 泛型
     * 
     * @param content 转换前字符串
     * @param valueType 列表元素转换类
     * @return 转换后列表
     */
    public static <T> List<T> strToList(String content, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(content,
                OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, valueType));
        } catch (JsonProcessingException ex) {
            try {
                return DEFAULT_MAPPER.readValue(content,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, valueType));
            } catch (JsonProcessingException e) {
                log.warn(JSON_TO_STRING_ERROR, ex.getMessage());
                return new ArrayList<>();
            }
        }
    }

    /**
     * json字符串转换为Map
     * 
     * @param <K> key泛型
     * @param <V> v泛型
     * 
     * @param content 转换前字符串
     * @param keyType key转换类
     * @param valueType value转换类
     * @return <K, V> 转换后Map
     */
    public static <K, V> Map<K, V> strToMap(String content, Class<K> keyType, Class<V> valueType) {
        try {
            return OBJECT_MAPPER.readValue(content,
                OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, keyType, valueType));
        } catch (JsonProcessingException ex) {
            try {
                return DEFAULT_MAPPER.readValue(content,
                    OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, keyType, valueType));
            } catch (Exception e) {
                log.warn(JSON_TO_STRING_ERROR, ex.getMessage());
                return Collections.emptyMap();
            }
        }
    }

    /**
     * 将JSON字符串转换为指定类型的Java对象
     * 
     * @param json JSON字符串
     * @param clazz 要转换的目标类
     * @param <T> 目标类的类型
     * @return 转换后的Java对象，若转换失败则返回null
     */
    public static <T> T strToBean(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            try {
                return DEFAULT_MAPPER.readValue(json, clazz);
            } catch (JsonProcessingException ex) {
                log.warn(JSON_TO_STRING_ERROR, ex.getMessage());
                return null;
            }
        }
    }

    /**
     * 将Map转成指定的Bean (利用Spring BeanMap进行浅拷贝)
     * 
     * @param map 集合
     * @param clazz Bean类
     * @return Bean对象
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T mapToBean(Map map, Class<T> clazz) {
        if (CollectionUtil.isEmpty(map)) {
            return null;
        }
        return (T)BeanUtil.mapToBean(map, clazz);
    }

    /**
     * 将Bean转成Map (利用Spring BeanMap进行浅拷贝，深拷贝可以试用dubbo的PojoUtils.generalize)
     * 
     * @param obj 对象
     * @return map集合
     */
    public static Map<String, Object> beanToMap(Object obj) {
        return BeanUtil.beanToMap(obj);

    }

    /**
     * 读取JSON字符串并返回对应的JsonNode对象。
     * 
     * @param json JSON字符串
     * @return JsonNode对象，如果转换失败则返回null
     */
    public static JsonNode readTree(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            try {
                return DEFAULT_MAPPER.readTree(json);
            } catch (JsonProcessingException ex) {
                log.warn(JSON_TO_STRING_ERROR, ex.getMessage());
                return null;
            }
        }
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}