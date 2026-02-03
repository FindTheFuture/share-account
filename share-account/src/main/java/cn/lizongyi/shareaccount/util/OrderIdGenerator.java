package cn.lizongyi.shareaccount.util;

public class OrderIdGenerator {

    // 起始时间戳（自定义）
    private static final long EPOCH = 1672531200000L; // 2023-01-01 00:00:00 UTC

    // 各字段占用位数
    private static final int MACHINE_BITS = 8;
    private static final int SEQUENCE_BITS = 12;
    private static final int BUSINESS_ID_BITS = 14;

    // 最大值限制
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS); // 4095
    private static final long MAX_BUSINESS_ID = ~(-1L << BUSINESS_ID_BITS); // 16383

    // 各字段掩码（用于提取）
    private static final long MACHINE_MASK = (~(-1L << MACHINE_BITS)) << SEQUENCE_BITS + BUSINESS_ID_BITS;
    private static final long SEQUENCE_MASK = (~(-1L << SEQUENCE_BITS)) << BUSINESS_ID_BITS;

    // 当前机器ID
    private final long machineId;

    // 上次时间戳 & 序列号
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public OrderIdGenerator(long machineId) {
        if (machineId > (~(-1L << MACHINE_BITS)) || machineId < 0) {
            throw new IllegalArgumentException("机器ID超出范围");
        }
        this.machineId = machineId << BUSINESS_ID_BITS; // 提前左移，方便后续拼接
    }

    public synchronized long nextId(long businessId) {
        long timestamp = System.currentTimeMillis() - EPOCH;

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回退");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // 等待下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return (timestamp << (MACHINE_BITS + SEQUENCE_BITS + BUSINESS_ID_BITS))
                | machineId
                | (sequence << BUSINESS_ID_BITS)
                | businessId;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis() - EPOCH;
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis() - EPOCH;
        }
        return timestamp;
    }

    /**
     * 解析订单ID中的各个字段
     */
    public static class ParsedOrderId {
        public long timestamp;
        public long machineId;
        public long sequence;
        public long businessId;
    }

    public static ParsedOrderId parse(long orderId) {
        ParsedOrderId parsed = new ParsedOrderId();
        parsed.timestamp = (orderId >> (MACHINE_BITS + SEQUENCE_BITS + BUSINESS_ID_BITS));
        parsed.machineId = (orderId & MACHINE_MASK) >>> (SEQUENCE_BITS + BUSINESS_ID_BITS);
        parsed.sequence = (orderId & SEQUENCE_MASK) >>> BUSINESS_ID_BITS;
        parsed.businessId = orderId & MAX_BUSINESS_ID;
        return parsed;
    }
}