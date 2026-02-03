package cn.lizongyi.shareaccount.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.util.*;

/**
 * Bean工具类
 * 
 * @author 林淮川 linhuaichuan@itbox.cn
 * @date 2024/02/06
 */
public class BeanUtil {

    /**
     * 获取对象的null属性
     *
     * @param source source
     * @return null属性字段
     */
    public static String[] getNullPropertyNames(Object source) {
        if (source == null) {
            throw new IllegalArgumentException("Source object cannot be null");
        }
        // 使用BeanWrapperImpl获取对象的所有属性描述符
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
        // 创建一个空的属性名称列表
        List<String> emptyNames = new ArrayList<>();
        // 遍历所有属性描述符
        for (java.beans.PropertyDescriptor pd : pds) {
            // 获取属性的值
            Object srcValue = src.getPropertyValue(pd.getName());
            // 如果属性值为null，则将其名称添加到空属性列表中
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        // 将空属性列表转换为数组并返回
        return emptyNames.toArray(new String[0]);
    }

    /**
     * 拷贝
     *
     * @param source 数据源
     * @param target 目标对象
     */
    public static void copy(Object source, Object target) {
        BeanUtils.copyProperties(source, target);
    }

    /**
     * 拷贝，且忽略source的null值
     *
     * @param source 数据源
     * @param target 目标对象
     */
    public static void copyIgnoreNull(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    /**
     * 通过序列化的方式深拷贝
     *
     * @param t 原始对象
     * @return 深拷贝结果
     * @throws IllegalArgumentException 如果原始对象为null
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T t) {
        // 检查原始对象是否为null
        if (t == null) {
            throw new IllegalArgumentException("Cannot deep copy null object");
        }
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut)) {
            // 序列化原始对象
            out.writeObject(t);
            out.flush();

            byte[] bytes = byteOut.toByteArray();
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                // 反序列化对象
                return (T)in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new IllegalArgumentException("Error while deep copying object", e);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error during object serialization", e);
        }
    }

    /**
     * 将对象转换为map(利用 jdk原生反射 进行浅拷贝，深拷贝可以试用dubbo的PojoUtils.generalize)
     */
    public static Map<String, Object> beanToMap(Object bean) {
        if (bean == null) {
            throw new IllegalArgumentException("Bean object cannot be null");
        }
        try {
            return ReflectUtil.beanToMap(bean);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("beanToMap Error Light copying object: " + e.getMessage(), e);
        }
    }

    /**
     * bean属性值为null或空原样输出
     * 
     * @author wangbing wbbeijing@aliyun.com
     * @date 2024/3/4
     * @param bean 要转换的对象
     * @return java.util.Map<java.lang.String,java.lang.String> 转换后的Map<String, String>
     */
    public static Map<String, String> beanToMapKeptEmpty(Object bean) {
        return beanToMapByStr(bean, true);
    }

    /**
     * 将对象转换为Map<String, String>形式：忽略null值
     */
    public static Map<String, String> beanToMapByStr(Object bean) {
        return beanToMapByStr(bean, false);
    }

    /**
     * 将对象转换为Map<String, String>形式
     *
     * @param bean 要转换的对象
     * @param hasNull 是否忽略null值
     * @return 转换后的Map<String, String>
     */
    public static Map<String, String> beanToMapByStr(Object bean, boolean hasNull) {
        if (bean == null) {
            return Collections.emptyMap();
        }
        BeanMap beanMap = BeanMap.create(bean);
        if (null == beanMap || beanMap.isEmpty()) {
            return Collections.emptyMap();
        }
        // 预先分配map的容量，减少重哈希的次数
        Map<String, String> map = new HashMap<>(beanMap.size());
        for (Object key : beanMap.keySet()) {
            Object value = beanMap.get(key);
            String keyStr = String.valueOf(key);
            String valueStr = String.valueOf(value);
            if (hasNull) {
                map.put(keyStr, ObjectUtils.isEmpty(value) ? null : valueStr);
            } else if (null != value) {
                map.put(keyStr, valueStr);
            }
        }
        return map;
    }

    /**
     * 将map封装为实体类对象 (利用 jdk原生反射 进行浅拷贝)
     */
    @SuppressWarnings("unchecked")
    public static <T> T mapToBean(Map<String, Object> map, T bean) {
        if (null != bean) {
            return (T)mapToBean(map, bean.getClass());
        }
        return null;
    }

    /**
     * 将map封装为实体类对象 (利用 jdk原生反射 进行浅拷贝)
     */
    public static <T> T mapToBean(Map<String, Object> map, Class<T> clazz) {
        if (CollectionUtil.isEmpty(map)) {
            return null;
        }
        try {
            return ReflectUtil.mapToBean(map, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException("mapToBean Error Light copying object: " + e.getMessage(), e);
        }
    }
}