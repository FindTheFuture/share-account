package cn.lizongyi.shareaccount.util;

import org.springframework.stereotype.Component;

/**
 * Redis 工具类
 *
 * @author 林淮川 linhuaichuan@itbox.cn
 * @date 2024/03/01
 */
@Component("isoRedisUtils")
@SuppressWarnings("unchecked")
public class RedisUtils {
    /*@Getter
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    *//**
     * 是否存在key
     *//*
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    *//**
     * 删除key
     *//*
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    *//**
     * 批量删除key
     *//*
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    *//**
     * String 类型get方法
     *//*
    public <T> T get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? (T)value : null;
    }

    *//**
     * String 类型 批量获取
     *
     * @param redisKeys key集合
     *//*
    public <T> List<T> get(List<String> redisKeys) {
        return (List<T>)redisTemplate.opsForValue().multiGet(redisKeys);
    }

    *//**
     * String 类型set 方法
     *
     * @param expire 过期时间，单位为 秒
     *//*
    public <T> void set(String key, T obj, int expire) {
        redisTemplate.opsForValue().set(key, obj, expire, TimeUnit.SECONDS);
    }

    *//**
     * Map类型 获取
     *//*
    public <K, V> Map<K, V> getHash(String key) {
        return (Map<K, V>)redisTemplate.opsForHash().entries(key);
    }

    *//**
     * Map类型 元素获取
     *//*
    public <V> V getHash(String key, String item) {
        return (V)redisTemplate.opsForHash().get(key, item);
    }

    *//**
     * Map类型 插入
     *
     * @param expire 过期时间，单位为 秒
     *//*
    public <K, V> boolean putHash(String key, Map<K, V> map, int expire) {
        redisTemplate.opsForHash().putAll(key, map);
        return expire(key, expire, TimeUnit.SECONDS);
    }

    *//**
     * 设置过期时间
     *//*
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    *//**
     * 获取指定key的元素总值
     *//*
    public Long size(String key) {
        return switch (Objects.requireNonNull(redisTemplate.type(key))) {
            case STRING -> hasKey(key) ? 1L : 0L;
            case LIST -> redisTemplate.boundListOps(key).size();
            case SET -> redisTemplate.boundSetOps(key).size();
            case ZSET -> redisTemplate.boundZSetOps(key).size();
            case HASH -> redisTemplate.boundHashOps(key).size();
            default -> 0L;
        };
    }*/
}
