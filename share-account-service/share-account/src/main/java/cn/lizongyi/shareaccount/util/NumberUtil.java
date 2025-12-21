package cn.lizongyi.shareaccount.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * 数字工具类
 * 
 * @author justin
 * @date 2021/2/25 14:19
 */
@Slf4j
public class NumberUtil {

    /**
     * 是否是空或0
     *
     * @param num num
     * @return 是否是空或0
     */
    public static boolean isNullOrZero(Number num) {
        return num == null || num.doubleValue() == 0;
    }

    /**
     * null当做0
     *
     * @param num num
     * @return 返回值
     */
    public static Short nullAsZero(Short num) {
        return num == null ? 0 : num;
    }

    /**
     * null当做0
     *
     * @param num num
     * @return 返回值
     */
    public static Integer nullAsZero(Integer num) {
        return num == null ? 0 : num;
    }

    /**
     * null当做0
     *
     * @param num num
     * @return 返回值
     */
    public static Long nullAsZero(Long num) {
        return num == null ? 0 : num;
    }

    /**
     * null当做0
     *
     * @param num num
     * @return 返回值
     */
    public static Float nullAsZero(Float num) {
        return num == null ? 0F : num;
    }

    /**
     * null当做0
     *
     * @param num num
     * @return 返回值
     */
    public static Double nullAsZero(Double num) {
        return num == null ? 0D : num;
    }

    /**
     * null当做-1
     *
     * @param num num
     * @return 返回值
     */
    public static Double nullAsF1(Double num) {
        return num == null ? -1D : num;
    }

    /**
     * null当做0
     *
     * @param num num
     * @return 返回值
     */
    public static BigDecimal nullAsZero(BigDecimal num) {
        return num == null ? BigDecimal.ZERO : num;
    }

    /**
     * 判断是否正数
     *
     * @param d d
     * @return 返回是否正数
     */
    public static boolean isPositive(Double d) {
        BigDecimal bigDecimal = BigDecimal.valueOf(d);
        return bigDecimal.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * @param data 小数
     * @param digit 百分数保留小数点位数
     * @return 返回百分数
     * @description: java实现小数转百分数
     */
    public static String getPercent(double data, int digit) {
        NumberFormat numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMinimumFractionDigits(digit);
        return numberFormat.format(data);
    }

    /**
     * 将百分比字符串转换成小数。
     *
     * @param num 表示百分比的字符串，例如 "67.89%"。
     * @param digit 小数点后的位数。
     * @return 转换后的百分比小数，如果转换失败则返回null。
     */
    public static Double getPercentDecimal(String num, int digit) {
        try {
            // 将百分比字符串解析成double类型，例如 "67.89%" 解析后为 0.6789
            double num2 = NumberFormat.getPercentInstance().parse(num).doubleValue();
            // 使用BigDecimal确保精确度
            BigDecimal decimal = BigDecimal.valueOf(num2);
            // 设置小数点位数并返回
            return decimal.setScale(digit, RoundingMode.HALF_UP).doubleValue();
        } catch (ParseException e) {
            // 记录解析异常
            log.error("getPercentDecimal error: {}", e.getMessage());
        }
        // 如果解析失败，返回null
        return null;
    }

    /**
     * 将Double类型的数值转换为BigDecimal类型
     * 
     * @param num Double类型的数值
     * @return 转换后的BigDecimal类型的数值，如果num为null则返回null
     */
    public static BigDecimal covertBigDecimal(Double num) {
        if (Objects.nonNull(num)) {
            return BigDecimal.valueOf(num);
        }
        return null;
    }

    /**
     * 将String类型的数值转换为BigDecimal类型
     * 
     * @param num String类型的数值
     * @return 转换后的BigDecimal类型的数值，如果num为null则返回null
     */
    public static BigDecimal covertBigDecimal(String num) {
        if (StringUtils.hasText(num)) {
            return new BigDecimal(num);
        }
        return null;
    }


    /**
     * 整除，向上取整
     * 
     * @param dividend 分子
     * @param divisor 坟墓
     * @return 整除并向上取整
     */
    public static int divideAndCeil(int dividend, int divisor) {
        // 防止除数为0的情况
        if (divisor == 0) {
            throw new IllegalArgumentException("Divisor cannot be zero");
        }
        return (dividend + divisor - 1) / divisor;
    }
    public static Double loadIndexData(List<Double> list, int index){
        if (list.size()<=index) {
            return list.get(list.size()-1);
        }
        return list.get((index-1));
    }


    /**
     * 获取 6位数字
     */
    public static Integer getRandom(){
        return (int)((Math.random()*9+1)*100000);
    }


    /**
     *
     * @author lizongyi@itbox.cn
     * @date 2025/2/8
     * @param null
     * @return
     */
    public static int generateRandomNumber(int min, int max) {
        // 生成min到max之间的随机整数（包括min和max）

        Random random = new Random();
        int rangeSize = max - min + 1;
        // 生成0到rangeSize-1之间的随机整数，然后加上最小值min
        return random.nextInt(rangeSize) + min;
    }
}