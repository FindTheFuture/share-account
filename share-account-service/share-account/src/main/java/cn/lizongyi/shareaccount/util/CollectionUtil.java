package cn.lizongyi.shareaccount.util;

import cn.lizongyi.shareaccount.constants.Constants;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 集合工具类
 * 
 * @author justin
 * @date 2021/2/3 19:35
 */
public class CollectionUtil {

    /**
     * Map集合指定初始值大小
     */
    private static final int INITIAL_CAPACITY = 16;

    /**
     * 集合转字符串
     *
     * @param collection 集合
     * @param <T> 泛型
     * @return 字符串
     */
    public static <T> String collectionToString(Collection<T> collection) {
        return collectionToString(collection, Constants.COMMA_STRING);
    }

    /**
     * 集合转字符串
     *
     * @param collection 集合
     * @param comma 连接符
     * @param <T> 泛型
     * @return 字符串
     */
    public static <T> String collectionToString(Collection<T> collection, String comma) {
        if (CollectionUtils.isEmpty(collection)) {
            return "";
        }
        if (comma == null) {
            comma = Constants.COMMA_STRING;
        }
        // 使用String.join方法简化代码，提高可读性
        return collection.stream()
            .map(object -> object == null ? "null" : object.toString())
            .collect(Collectors.joining(comma));
    }

    /**
     * 字符串转列表 (字符串是用逗号隔开的多个字符)
     *
     * @param str 要转换的字符串
     * @return 转换后的字符串列表
     */
    public static List<String> stringToList(String str) {
        if (!StringUtils.hasText(str)) {
            return new ArrayList<>();
        }
        return Stream.of(str.split(Constants.COMMA_STRING)).collect(Collectors.toList());
    }

    public static List<Long> stringToLongList(String str) {
        if (!StringUtils.hasText(str)) {
            return new ArrayList<>();
        }

        List<String> stringList = stringToList(str);

        List<Long> longList = new ArrayList<>(stringList.size());

        for (String part : stringList) {
            try {
                longList.add(Long.parseLong(part.trim()));
            } catch (NumberFormatException e) {
                // 处理无法解析的情况，例如非数字字符
                System.err.println("无法解析数字: " + part);
            }
        }

        return longList;
    }

    /**
     * 向Map集合中 添加指定的key-value对
     *
     * @param map Map集合
     * @param key 要添加的key
     * @param value 要添加的value
     */
    public static <K, V> void mapSetAdd(Map<K, Set<V>> map, K key, V value) {
        if (map == null) {
            map = new HashMap<>(INITIAL_CAPACITY);
        }
        if (!map.containsKey(key)) {
            map.put(key, new HashSet<>());
        }
        map.get(key).add(value);
    }

    /**
     * 向Map集合中添加指定的key-value对
     *
     * @param map Map集合
     * @param key 要添加的key
     * @param value 要添加的value
     */
    public static <K, V> void mapListAdd(Map<K, List<V>> map, K key, V value) {
        if (map == null) {
            map = new HashMap<>(INITIAL_CAPACITY);
        }
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<>());
        }
        map.get(key).add(value);
    }

    /**
     * map是否为空
     *
     * @param map map
     * @return 是否为空
     */
    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    /**
     * map是否为空
     *
     * @param map map
     * @return 是否为空
     */
    public static <K, V> boolean isNotEmpty(Map<K, V> map) {
        return !isEmpty(map);
    }

    /**
     * 判断两个集合是否相等
     *
     * @param c1 第一个集合
     * @param c2 第二个集合
     * @return 如果两个集合相等则返回true，否则返回false
     */
    public static <T> boolean isCollectionEquals(Collection<T> c1, Collection<T> c2) {
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1 == null || c2 == null) {
            return false;
        }
        if (c1.size() != c2.size()) {
            return false;
        }
        List<T> c1List = c1.stream().sorted().toList();
        List<T> c2List = c2.stream().sorted().toList();
        int len = c1List.size();
        for (int i = 0; i < len; i++) {
            if (!isObjectEquals(c1List.get(i), c2List.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 对象是否equals
     *
     * @param o1 o1
     * @param o2 o2
     * @param <T> 泛型
     * @return 是否equals
     */
    private static <T> boolean isObjectEquals(T o1, T o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        return o1.equals(o2);
    }

    /**
     * 将一组数据平均分成n组
     *
     * @param source 要分组的数据源
     * @param n 平均分成n组
     * @param <T> 泛型
     * @return 返回list
     */
    public static <T> List<List<T>> averageAssignList(List<T> source, int n) {
        List<List<T>> result = new ArrayList<>();

        // 计算出余数
        int remainder = source.size() % n;
        // 计算商
        int number = source.size() / n;
        // 偏移量
        int offset = 0;
        for (int i = 0; i < n; i++) {
            List<T> value;
            if (remainder > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remainder--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }
}