package cn.lizongyi.shareaccount.util;

import org.springframework.stereotype.Component;

/**
 * Redis 分布式锁
 * 
 * @author 林淮川 linhuaichuan@itbox.cn
 * @version 1.0.0
 * @date 2024-07-18
 */
@Component
public class RedisLock {
    /**
     * Redisson客户端
     */
    /*@Autowired
    private RedissonClient redissonClient;

    *//**
     * 获取Map中item的锁，并等待
     *
     * @param item 元素
     * @param leaseTime 租赁时间(占用)
     * @param waitTime 等待时间
     *//*
    public boolean lockAndWait(String key, String item, Integer leaseTime, int waitTime) {
        RMap<String, String> map = redissonClient.getMap(key);
        RLock keyLock = map.getLock(item);
        try {
            return keyLock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    *//**
     * 释放Map中item的锁
     *
     * @param item 元素
     *//*
    public boolean unLock(String key, String item) {
        try {
            RMap<String, String> map = redissonClient.getMap(key);
            RLock keyLock = map.getLock(item);
            keyLock.unlock();
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }*/
}