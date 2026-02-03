package cn.lizongyi.shareaccount.util;

import java.util.BitSet;

/**
 * 简单布隆过滤器：单机版，分布式请使用redis 布隆 Guava 布隆过滤器 参数含义： funnel
 * 指定布隆过滤器中存的是什么类型的数据，有：IntegerFunnel，LongFunnel，StringCharsetFunnel。 expectedInsertions 预期需要存储的数据量 fpp 误判率，默认是 0.03。
 * BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), size, misjudgmentRate);
 * bloomFilter.put(value); // 添加元素 bloomFilter.mightContain(value); // 判断元素是否存在
 * 
 * @author 林淮川 linhuaichuan@itbox.cn
 * @date 2024/01/29
 */
public class SimpleBloomUtil {

    /**
     * 默认大小
     */
    private static final int DEFAULT_SIZE = Integer.MAX_VALUE;

    /**
     * 最小的大小
     */
    private static final int MIN_SIZE = 1000;

    /**
     * 大小为默认大小
     */
    private int size = DEFAULT_SIZE;

    /**
     * hash函数的种子因子
     */
    private static final int[] HASH_SEEDS = new int[] {3, 5, 7, 11, 13, 17, 19, 23, 29, 31};

    /**
     * 位数组，0/1,表示特征
     */
    private BitSet bitSet = null;

    /**
     * hash函数 数组
     */
    private final HashFunction[] hashFunctions = new HashFunction[HASH_SEEDS.length];

    public SimpleBloomUtil() {
        // 按照默认大小
        init();
    }

    /**
     * 构造函数，初始化布隆过滤器
     * 
     * @param num 布隆过滤器的大小
     */
    public SimpleBloomUtil(int num) {
        // 大小初始化小于最小的大小
        if (num >= MIN_SIZE) {
            this.size = num;
        }
        init();
    }

    /**
     * 构造函数，初始化布隆过滤器
     * 
     * @param num 布隆过滤器的大小
     * @param rate 布隆过滤器使用的哈希函数数量
     */
    public SimpleBloomUtil(int num, double rate) {
        // 计算位数组的大小
        this.size = (int)(-num * Math.log(rate) / Math.pow(Math.log(2), 2));
        // hsah 函数个数
        int hashSize = (int)(this.size * Math.log(2) / num);

        // 初始化hash函数
        for (int i = 0; i < HASH_SEEDS.length; i++) {
            hashFunctions[i] = new HashFunction(hashSize, HASH_SEEDS[i]);
        }
        // 初始化位数组
        this.bitSet = new BitSet(size);
    }

    /**
     * 初始化方法
     */
    private void init() {
        // 初始化位大小
        bitSet = new BitSet(size);
        // 初始化hash函数
        for (int i = 0; i < HASH_SEEDS.length; i++) {
            hashFunctions[i] = new HashFunction(size, HASH_SEEDS[i]);
        }
    }

    /**
     * 添加元素到哈希集合中
     * 
     * @param value 要添加的元素
     */
    public void add(Object value) {
        for (HashFunction f : hashFunctions) {
            // 将hash计算出来的位置为true
            bitSet.set(f.hash(value), true);
        }
    }

    /**
     * 判断哈希集合中是否包含指定元素
     * 
     * @param value 要判断的元素
     * @return 如果哈希集合中包含指定元素，则返回true；否则返回false
     */
    public boolean contains(Object value) {
        boolean result;
        for (HashFunction f : hashFunctions) {
            result = bitSet.get(f.hash(value));
            // hash函数只要有一个计算出为false，则直接返回
            if (!result) {
                return false;
            }
        }
        return true;
    }

    /**
     * hash函数
     * 
     * @author 林淮川 linhuaichuan@itbox.cn
     * @date 1/3/2024
     */
    public static class HashFunction {
        /**
         * 位数组大小
         */
        private final int size;
        /**
         * hash种子
         */
        private final int seed;

        public HashFunction(int size, int seed) {
            this.size = size;
            this.seed = seed;
        }

        /**
         * 计算哈希值
         * 
         * @param value 要计算哈希值的值
         * @return 计算得到的哈希值
         */
        public int hash(Object value) {
            if (value == null) {
                return 0;
            } else {
                // hash值
                int hash1 = value.hashCode();
                // 高位的hash值
                int hash2 = hash1 >>> 16;
                // 合并hash值(相当于把高低位的特征结合)
                int combine = hash1 ^ hash2;
                // 相乘再取余
                return Math.abs(combine * seed) % size;
            }
        }
    }
}