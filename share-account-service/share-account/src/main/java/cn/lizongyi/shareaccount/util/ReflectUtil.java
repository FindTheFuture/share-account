package cn.lizongyi.shareaccount.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 反射工具类
 * 
 * @author 林淮川 linhuaichuan@itbox.cn
 * @date 2024/02/07
 */
@Slf4j
public class ReflectUtil {
    /**
     * 存储类和字段的映射关系的ConcurrentHashMap
     */
    private static final Map<Class<?>, Map<String, Field>> CLASS_FIELD_MAP = new ConcurrentHashMap<>();
    /**
     * 存储类和字段的列表的ConcurrentHashMap
     */
    private static final Map<Class<?>, List<Field>> CLASS_FIELD_LIST = new ConcurrentHashMap<>();
    /**
     * Map集合指定初始值大小
     */
    private static final int INITIAL_CAPACITY = 16;

    /**
     * 通过反射赋值
     *
     * @param obj 待赋值的对象
     * @param fieldName 字段名
     * @param fieldValue 要赋的值
     */
    public static void setFieldValue(Object obj, String fieldName, Object fieldValue) {
        if (obj == null || !StringUtils.hasText(fieldName)) {
            return;
        }
        Field field = getField(obj.getClass(), fieldName);
        if (field == null) {
            log.error("反射赋值失败, 在类{}中没有找到字段{}", obj.getClass().getName(), fieldName);
            return;
        }
        try {
            field.setAccessible(true);
            field.set(obj, fieldValue);
        } catch (IllegalAccessException e) {
            log.error("setFieldValue error", e);
        }
    }

    /**
     * 获取对象的指定字段值，如果字段值为null，则返回默认值
     *
     * @param obj 对象
     * @param fieldName 字段名
     * @param defaultValue 默认值
     * @param <T> 泛型
     * @return 字段值，如果字段值为null，则返回默认值
     */
    public static <T> T getFieldValue(Object obj, String fieldName, T defaultValue) {
        T fieldValue = getFieldValue(obj, fieldName);
        return null == fieldValue ? defaultValue : fieldValue;
    }

    /**
     * 使用反射获取对象的字段值
     *
     * @param obj 目标对象
     * @param fieldName 字段名
     * @return 目标对象的字段值，如果字段不存在则返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object obj, String fieldName) {
        if (obj == null || !StringUtils.hasText(fieldName)) {
            return null;
        }
        Field field = getField(obj.getClass(), fieldName);
        if (field == null) {
            log.error("反射取值失败, 在类{}中没有找到字段{}", obj.getClass().getName(), fieldName);
            return null;
        }
        try {
            field.setAccessible(true);
            return (T)field.get(obj);
        } catch (IllegalAccessException e) {
            log.error("getFieldValue error", e);
        }
        return null;
    }

    /**
     * 获取指定类的指定字段
     *
     * @param clazz 类对象
     * @param fieldName 字段名
     * @return 字段对象
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        if (clazz == null || !StringUtils.hasText(fieldName)) {
            return null;
        }
        if (CLASS_FIELD_MAP.containsKey(clazz)) {
            return CLASS_FIELD_MAP.get(clazz).get(fieldName);
        }
        if (CLASS_FIELD_LIST.containsKey(clazz)) {
            return CLASS_FIELD_MAP.get(clazz).get(fieldName);
        }
        getClassAllFields(clazz);
        return CLASS_FIELD_MAP.get(clazz).get(fieldName);
    }

    /**
     * 获取类的所有字段(包含父类)
     *
     * @param clazz 类对象
     * @return 所有字段
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        if (null == clazz) {
            return null;
        }
        if (CLASS_FIELD_LIST.containsKey(clazz)) {
            return CLASS_FIELD_LIST.get(clazz);
        }
        getClassAllFields(clazz);
        return CLASS_FIELD_LIST.get(clazz);
    }

    /**
     * 获取类的所有属性(包含父类)
     *
     * @param clazz 类对象
     */
    private static void getClassAllFields(Class<?> clazz) {
        if (clazz == null) {
            return;
        }
        Class<?> originalClass = clazz;
        List<Field> fields = new ArrayList<>();
        Map<String, Field> fieldMap = new HashMap<>(INITIAL_CAPACITY);
        while (clazz != null) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                fields.add(field);
                fieldMap.put(field.getName(), field);
            }
            clazz = clazz.getSuperclass();
        }
        CLASS_FIELD_MAP.putIfAbsent(originalClass, fieldMap);
        CLASS_FIELD_LIST.putIfAbsent(originalClass, fields);
    }

    /**
     * 将对象转换为Map
     * 
     * @param object 要转换的对象
     * @return 转换后的Map
     * @throws IllegalAccessException 如果无法访问字段
     */
    public static Map<String, Object> beanToMap(Object object) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>(INITIAL_CAPACITY);
        for (Field field : getAllFields(object.getClass())) {
            int mod = field.getModifiers();
            // 跳过静态和final字段
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(object);
            // 跳过null值
            if (null == value) {
                continue;
            }
            map.put(field.getName(), value);
        }
        return map;
    }

    /**
     * map转对象
     * 
     * @param map 要转换的map对象
     * @param beanClass 要转换的目标对象的类
     * @return 转换后的对象
     * @throws Exception 转换失败时抛出异常
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T mapToBean(Map<String, Object> map, Class<T> beanClass) throws Exception {
        T obj = beanClass.getDeclaredConstructor().newInstance();
        for (Field field : getAllFields(beanClass)) {
            int mod = field.getModifiers();
            // 忽略static 和 final 属性，以及 map 无对应值的操作
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod) || !map.containsKey(field.getName())
                || Objects.isNull(map.get(field.getName()))) {
                continue;
            }
            field.setAccessible(true);
            Object value = map.get(field.getName());
            Class<?> fieldType = field.getType();
            if (fieldType.isAssignableFrom(String.class)) {
                // 解决value类型丢失
                if (value instanceof String) {
                    field.set(obj, value);
                } else {
                    field.set(obj, value.toString());
                }
            } else if (fieldType.isAssignableFrom(Integer.TYPE) || fieldType.isAssignableFrom(Integer.class)) {
                field.set(obj, Integer.parseInt(value.toString()));
            } else if (fieldType.isAssignableFrom(Long.TYPE) || fieldType.isAssignableFrom(Long.class)) {
                field.set(obj, Long.parseLong(value.toString()));
            } else if (fieldType.isAssignableFrom(Double.TYPE) || fieldType.isAssignableFrom(Double.class)) {
                field.set(obj, Double.parseDouble(value.toString()));
            } else if (fieldType.isAssignableFrom(List.class)) {
                // 获取实际的类型参数
                Type[] actualTypeArguments = ((ParameterizedType)field.getGenericType()).getActualTypeArguments();
                // List：只有一个类型参数，元素类型是Class（如非类型变量）
                if (actualTypeArguments.length == 1 && !actualTypeArguments[0].getTypeName().startsWith("java.lang.")
                    && actualTypeArguments[0] instanceof Class) {
                    field.set(obj, ((List<?>)map.get(field.getName())).stream().map(item -> {
                        try {
                            return mapToBean((Map)item, (Class<?>)actualTypeArguments[0]);
                        } catch (Exception e) {
                            return null;
                        }
                    }).collect(Collectors.toList()));
                } else {
                    field.set(obj, JacksonUtils.strToBean(JacksonUtils.objToStr(map.get(field.getName())), fieldType));
                }
            } else if (fieldType.getName().startsWith("cn.itbox")) {
                // 其他嵌套自定义类型
                field.set(obj, mapToBean((Map)map.get(field.getName()), fieldType));
            } else {
                // 处理嵌套其他对象
                field.set(obj, JacksonUtils.strToBean(JacksonUtils.objToStr(map.get(field.getName())), fieldType));
            }
        }
        return obj;
    }
}