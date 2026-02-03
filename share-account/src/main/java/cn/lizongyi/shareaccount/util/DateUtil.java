package cn.lizongyi.shareaccount.util;

import cn.lizongyi.shareaccount.constants.Constants;
import cn.lizongyi.shareaccount.constants.NumConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import static cn.lizongyi.shareaccount.constants.NumConstant.*;

/**
 * 日期转换工具
 *
 * @author justin
 * @date 2022/5/19 20:32
 */
@Slf4j
public class DateUtil {
    private static final ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>> FORMATTERS = new ConcurrentHashMap<>();

    private static final ThreadLocal<SimpleDateFormat> SDF =
            ThreadLocal.withInitial(() -> new SimpleDateFormat(Constants.TimeFormat.YYYYMMDD));

    private static final ThreadLocal<SimpleDateFormat> SDF_WITH_SPLIT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat(Constants.TimeFormat.YYYY_MM_DD));

    private static final ThreadLocal<SimpleDateFormat> SDF_HMS =
            ThreadLocal.withInitial(() -> new SimpleDateFormat(Constants.TimeFormat.HHMMSS));

    private static final ThreadLocal<SimpleDateFormat> SDF_YMDHM =
            ThreadLocal.withInitial(() -> new SimpleDateFormat(Constants.TimeFormat.YYYYMMDDHHMM));

    private static final ThreadLocal<SimpleDateFormat> SDF_YMDHMS =
            ThreadLocal.withInitial(() -> new SimpleDateFormat(Constants.TimeFormat.YYYY_MM_DD_HH_MM_SS));

    private static final ThreadLocal<SimpleDateFormat> SDF_HM =
            ThreadLocal.withInitial(() -> new SimpleDateFormat(Constants.TimeFormat.HHMM));

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter HHMMSS_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    public static final int SECONDS_TO_MILLISECONDS = 1000;

    public static final int MINUTES_TO_SECONDS = 60;

    /**
     * 通过时间获取HHmm时间
     *
     * @param time 时间戳
     * @return HH:mm字符串
     */
    public static String getHHmm(Long time) {
        Date date = new Date(time);
        return SDF_HM.get().format(date);
    }

    /**
     * 通过时间戳获取HHmmss时间
     *
     * @param time 时间戳
     * @return HH:mm:ss字符串
     */
    public static String getHHmmss(Long time) {
        Date date = new Date(time);
        return SDF_HMS.get().format(date);
    }

    /**
     * 通过时间戳获取yyyyMMdd日期
     */
    public static String getYyyyMMdd(Long time) {
        Date date = new Date(time);
        return SDF.get().format(date);
    }

    /**
     * 通过时间戳获取yyyy-MM-dd日期
     */
    public static String getYyyyMMddWithSplit(Long time) {
        Date date = new Date(time);
        return SDF_WITH_SPLIT.get().format(date);
    }

    /**
     * 通过时间戳获取yyyy-MM-dd HH:mm:ss日期
     */
    public static String getTimeStr(Long time) {
        Date date = new Date(time);
        return SDF_YMDHMS.get().format(date);
    }

    public static String getTimeStr(Timestamp time) {
        if(time == null){
            return null;
        }
        return SDF_YMDHMS.get().format(time);
    }

    public static Timestamp convertToTimestamp(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
            return Timestamp.valueOf(dateTime);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date time format: " + dateTimeStr, e);
        }
    }


    /**
     * 通过时间戳获取yyyy-MM-dd日期
     */
    public static Date getYyyyMMddWithSplit(String time) {
        try {
            return SDF_WITH_SPLIT.get().parse(time);
        } catch (ParseException e) {
            log.error("getYyyyMMddWithSplit error, time:{}", time, e);
            return null;
        }
    }

    /**
     * 通过时间戳获取yyyyMMddHHmm日期
     */
    public static String getYyyyMMddHHmm(Long time) {
        Date date = new Date(time);
        return SDF_YMDHM.get().format(date);
    }

    public static Date getYyyyMMddHHmmStrToDate(String time) {
        try {
            return SDF_YMDHM.get().parse(time);
        } catch (ParseException e) {
            log.error("strToDate error, time:{}", time, e);
            return null;
        }
    }

    /**
     * 通过yyyy-MM-dd HH:mm:ss 获取时间
     */
    public static Date strToDate(String time) {
        try {
            return SDF_YMDHMS.get().parse(time);
        } catch (ParseException e) {
            log.error("strToDate error, time:{}", time, e);
            return null;
        }
    }

    /**
     * 判断当前时间是否在[startTime, endTime]区间，注意时间格式要一致 HH:mm:ss
     *
     * @param nowTime   当前时间
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return boolean
     */
    public static boolean isEffectiveDate(String nowTime, String startTime, String endTime) {
        try {
            Date nowDate = SDF_HMS.get().parse(nowTime);
            Date startDate = SDF_HMS.get().parse(startTime);
            Date endDate = SDF_HMS.get().parse(endTime);
            return TimeUtil.isEffectiveDate(nowDate, startDate, endDate);
        } catch (Exception e) {
            log.error("isEffectiveDate error", e);
        }
        return false;
    }

    /**
     * 判断当前时间是否在指定的开始时间和结束时间之间。
     *
     * @param startTime 开始时间 Timestamp
     * @param endTime 结束时间 Timestamp
     * @return 如果当前时间在startTime和endTime之间（包含相等），返回true；否则返回false。
     */
    public static boolean isCurrentTimeInRange(Timestamp startTime, Timestamp endTime) {
        // 获取当前时间的Timestamp表示
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        // 检查当前时间是否在startTime和endTime之间
        return !currentTime.before(startTime) && !currentTime.after(endTime);
    }


    /**
     * 获取当前时间是周几
     */
    public static Integer getDayOfWeek(Long time) {
        Integer[] weekday = {INT_7, INT_1, INT_2, INT_3, INT_4, INT_5, INT_6};
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        return weekday[Math.max(calendar.get(Calendar.DAY_OF_WEEK) - 1, 0)];
    }

    /**
     * 获取当前时间是几号
     */
    public static Integer getDayOfMonth(Long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取当前时间小时
     *
     * @return 24小时制
     */
    public static Integer getHourOfDay() {
        Calendar calendar = Calendar.getInstance();
        // 24小时制
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 毫秒转小时
     *
     * @param timeStamp 毫秒
     * @return 小时
     */
    public static BigDecimal getHour(Long timeStamp) {
        if (timeStamp == null) {
            return null;
        }
        // 计算小时数
        long hours = timeStamp / (1000 * 60 * 60);
        BigDecimal result = BigDecimal.valueOf(hours);
        // 保留两位小数，向舍去
        result = result.setScale(2, RoundingMode.DOWN);
        return result;
    }

    /**
     * 时间是否距离当前时间在指定分钟内
     *
     * @param oldTime 指定时间
     * @param sdfType 格式 1：sdfYMdHm 其他：sdfYMdHms
     * @param timeOut 分钟
     */
    public static boolean isInTimeLimit(String oldTime, int sdfType, int timeOut) {
        try {
            long d1;
            if (sdfType == 1) {
                d1 = SDF_YMDHM.get().parse(oldTime).getTime();
            } else {
                d1 = SDF_YMDHMS.get().parse(oldTime).getTime();
            }
            long d2 = System.currentTimeMillis();
            if (d2 - d1 < timeOut * MINUTES_TO_SECONDS * SECONDS_TO_MILLISECONDS) {
                return true;
            }
        } catch (ParseException e) {
            log.error("DateUtil isInTimeLimit error, oldTime:{}, timeOut:{}", oldTime, timeOut);
        }
        return false;
    }

    /**
     * 时间是否大于当前时间在指定分钟内 是返回该时间 否则返回当前时间+指定时间
     *
     * @param time    指定时间
     * @param timeOut 分钟
     */
    public static String isMoreTimeLimit(String time, int timeOut) {
        try {
            long d1 = SDF_YMDHMS.get().parse(time).getTime();
            long d2 = System.currentTimeMillis();
            if (d1 - d2 < timeOut * MINUTES_TO_SECONDS * SECONDS_TO_MILLISECONDS) {
                return SDF_YMDHMS.get().format(new Date(d2 + timeOut * MINUTES_TO_SECONDS * SECONDS_TO_MILLISECONDS));
            }
        } catch (ParseException e) {
            log.error("DateUtil isMoreTimeLimit error, time:{}, timeOut:{}", time, timeOut);
        }
        return time;
    }

    /**
     * 获取当天0点0分0秒0毫秒
     */
    public static Date getDayStart() {
        return getDayStart(null);
    }

    /**
     * 获取当天23点59分59秒999毫秒
     */
    public static Date getDayEnd() {
        return getDayStart(null);
    }

    /**
     * 获取指定日期的0点0分0秒0毫秒
     */
    public static Date getDayStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取指定日期的23点59分59秒999毫秒
     *
     * @param date 指定日期
     */
    public static Date getDayEnd(Date date) {
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }
        calendar.set(Calendar.HOUR_OF_DAY, NumConstant.INT_23);
        calendar.set(Calendar.MINUTE, NumConstant.INT_59);
        calendar.set(Calendar.SECOND, NumConstant.INT_59);
        calendar.set(Calendar.MILLISECOND, NumConstant.INT_999);
        return calendar.getTime();
    }

    /**
     * 将字符串转换为Calendar对象
     *
     * @param time 要转换的字符串时间，格式为HH:mm:ss
     * @return 转换后的Calendar对象，如果字符串为空则返回null
     */
    public static Calendar strToCal(String time) {
        if (!StringUtils.hasText(time)) {
            return null;
        }
        // 设置日期格式
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        Calendar begin = Calendar.getInstance();
        try {
            begin.setTime(df.parse(time));
        } catch (Exception e) {
            log.error("strToCal error: {}", e.getMessage());
        }
        return begin;
    }

    /**
     * 判断d1是否大于d2
     *
     * @param d1 d1 HH:mm:ss
     * @param d2 d2 HH:mm:ss
     * @return 1大于 -1小于 0等于 空异常
     */
    public static Integer compareDate(String d1, String d2) {
        SimpleDateFormat df = SDF_HMS.get();
        try {
            Date dt1 = df.parse(d1);
            Date dt2 = df.parse(d2);
            return Long.compare(dt1.getTime(), dt2.getTime());
        } catch (Exception e) {
            log.error("DateUtil compareDate error, d1:{}, d2:{}", d1, d2);
        }
        return null;
    }

    /**
     * 将yyyy-MM-dd HH:mm:ss格式日期转为yyyyMMddHHmm格式返回
     *
     * @param oldTime 要转换的时间字符串
     * @return 转换后的时间字符串
     */
    public static String convertTime(String oldTime) {
        try {
            Date date = SDF_YMDHMS.get().parse(oldTime);
            return SDF_YMDHM.get().format(date);
        } catch (Exception e) {
            log.error("DateUtil convertTime error, oldTime:{}", oldTime);
        }
        return "";
    }

    /**
     * 返回当前时间所属15分钟时间片 例如当前为2022-08-31 16:24:56 返回2022-08-31 16:15:00
     *
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getNowTimeGroup() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 59);
        // 设置到15分钟之前
        cal.set(Calendar.MINUTE, (cal.get(Calendar.MINUTE) / 15) * 15);
        String previousTime = SDF_YMDHMS.get().format(cal.getTime());
        String newMinute = String.format("%02d", cal.get(Calendar.MINUTE));
        return previousTime.substring(0, previousTime.length() - 5) + newMinute + ":00";
    }

    /**
     * 将给定的时间段拆分成指定的时间间隔，并返回拆分后的结果。
     *
     * @param start    开始时间，格式为HH:mm:ss
     * @param end      结束时间，格式为HH:mm:ss
     * @param interval 时间间隔，单位为秒
     * @return 拆分后的时间段列表
     */
    public static int splitTime(String start, String end, Integer interval) {
        return splitTime(start, end, interval, getHHmmss(System.currentTimeMillis()));
    }

    /**
     * 将时间段按间隔切成 不同的分片 eg:时间段 07:00:00-13:00:00 按5分钟一个切片 计算当前时间所在分片的索引 索引从0开始，如果计算出错返回 -1
     *
     * @param start    开始时间
     * @param end      结束时间
     * @param interval 间隔时间（分钟）
     * @param abTime   ab测试时间
     * @return 拆分后的索引
     */
    public static int splitTime(String start, String end, Integer interval, String abTime) {
        int index = INT_F_1;
        try {
            // 当前时间
            Date currentDate = new Date();
            String todayString = new SimpleDateFormat(Constants.TIME_PATTERN_1).format(currentDate);
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.TIME_PATTERN_2);
            Date startDate = dateFormat.parse(todayString + Constants.SEPARATOR_4 + start);
            Date endDate = dateFormat.parse(todayString + Constants.SEPARATOR_4 + end);
            Date abDate = dateFormat.parse(todayString + Constants.SEPARATOR_4 + abTime);
            long abTestTime = abDate.getTime();
            long startTime = startDate.getTime();
            long endTime = endDate.getTime();

            if (abTestTime >= startTime && abTestTime <= endTime) {
                // 分钟换算成毫秒
                long intervalTime = interval * INT_60000;

                index = (int) ((abTestTime - startTime) / intervalTime);
            }
        } catch (ParseException e) {
            log.error("splitTime error: {}", e.getMessage());
        }

        return index;
    }

    /**
     * 根据给定的时间间隔和周期数计算一个交替的索引。
     *
     * @param interval    时间间隔
     * @param cycleNumber 周期数
     * @return 交替的索引
     */
    public static int alternatelyStrategy(int interval, int cycleNumber) {
        if (interval == 0 || cycleNumber == 0) {
            return 0;
        }
        // 获取时间切片
        int index = splitTime("00:00:00", "23:59:59", interval);
        if (index == INT_F_1) {
            return 0;
        }
        return index % cycleNumber + 1;
    }

    /**
     * 把ISO 8601 格式的日期转换成Timestamp
     *
     * @param isoDateTime ISO 8601 格式日期
     * @return Timestamp
     */
    public static Timestamp transformTimestamp(String isoDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        // 将字符串解析为LocalDateTime
        LocalDateTime localDateTime = LocalDateTime.parse(isoDateTime, formatter);

        // 转换为Timestamp，指定时区
        return Timestamp.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 当前时间到今天凌晨剩余秒数
     *
     * @return long 秒
     */
    public static Long timeDiffTodaySeconds() {
        LocalDateTime now = LocalDateTime.now();

        LocalDate today = LocalDate.now();
        LocalDateTime endOfToday = LocalDateTime.of(today, LocalTime.MAX);

        return TimeUtil.calculateTimeDiffBySeconds(now, endOfToday);
    }

    /**
     * 计算时间偏移
     *
     * @param currentDateTime 当前时间
     * @param seconds         秒数
     * @param forward         true:代表当前时间往后推多少秒；false:代表当前时间往前推多少秒
     * @return {@link LocalDateTime }
     */
    public static Date shiftTime(Date currentDateTime, long seconds, boolean forward) {
        if (forward) {
            return covertDate(covertLocalDateTime(currentDateTime).plusSeconds(seconds));
        } else {
            return covertDate(covertLocalDateTime(currentDateTime).minusSeconds(seconds));
        }
    }

    /**
     * Date转换成LocalDateTime
     *
     * @param date 时间
     * @return {@link LocalDateTime }
     */
    public static LocalDateTime covertLocalDateTime(Date date) {
        // Date 转换为 Instant
        Instant instant = date.toInstant();

        // Instant 转换为 LocalDateTime
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * LocalDateTime转换成Date
     *
     * @param localDateTime 时间
     * @return {@link Date }
     */
    public static Date covertDate(LocalDateTime localDateTime) {
        // 获取中国上海时区的 ZoneId
        ZoneId chinaZone = ZoneId.of("Asia/Shanghai");

        // 将 LocalDateTime 转换为 ZonedDateTime
        ZonedDateTime zonedDateTime = localDateTime.atZone(chinaZone);

        // 将 ZonedDateTime 转换为 Instant
        Instant instant = zonedDateTime.toInstant();

        // 将 Instant 转换为 Date
        return Date.from(instant);
    }

    /**
     * 获取当前时间，时间格式：yyyy-MM-dd HH:mm:ss
     *
     * @return {@link String }
     */
    public static String getNow() {
        // 使用 LocalDateTime 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 将 LocalDateTime 转换为 Date
        Date date = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());

        // 将 Date 转换成 String
        return SDF_YMDHMS.get().format(date);
    }

    /**
     * 校验时间是否在规定的时间范围内
     *
     * @param targetTime 目标时间 格式：yyyy-MM-dd HH:mm:ss
     * @param startTime  开始时间 格式：yyyy-MM-dd HH:mm:ss
     * @param endTime    结束时间 格式：yyyy-MM-dd HH:mm:ss
     * @return boolean
     */
    public static boolean isWithinRange(String targetTime, String startTime, String endTime) {
        // 目标时间
        LocalDateTime targetDateTime = LocalDateTime.parse(targetTime, DateTimeFormatter.ofPattern(Constants.TIME_PATTERN_3));
        // 开始时间
        LocalDateTime startDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern(Constants.TIME_PATTERN_3));
        // 结束时间
        LocalDateTime endDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern(Constants.TIME_PATTERN_3));

        return !targetDateTime.isBefore(startDateTime) && targetDateTime.isBefore(endDateTime);
    }

    /**
     * 将日期格式化为字符串
     *
     * @param date    日期
     * @param pattern 时间格式化
     * @return {@link String }
     */
    public static String formatDateToStr(Date date, String pattern) {
        ThreadLocal<SimpleDateFormat> formatter = FORMATTERS.computeIfAbsent(pattern, k -> ThreadLocal.withInitial(() -> new SimpleDateFormat(pattern)));
        return formatter.get().format(date);
    }


    /**
     * 获取几小时后的时间
     */
    public static Timestamp getAfterHourTime(int hour){
        // 结束时间 = 当前时间 + hour
        Instant endTime = Instant.now().plus(Duration.ofHours(hour));
        return Timestamp.from(endTime);
    }


    /**
     * 获取几分钟后的时间
     */
    public static Timestamp getAfterMinTime(int minute){
        // 结束时间 = 当前时间 + minute
        Instant endTime = Instant.now().plus(Duration.ofMinutes(minute));
        return Timestamp.from(endTime);
    }

    /**
     * 将"年-月-日"格式的日期字符串转成"年-月-日 00:00:00"的Timestamp
     *
     * @param dateString "年-月-日"格式的日期字符串
     * @return 对应于给定日期的00:00:00时刻的Timestamp对象
     */
    public static Timestamp toStartOfDayTimestamp(String dateString) {
        LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);
        LocalDateTime startOfDay = date.atStartOfDay();
        return Timestamp.valueOf(startOfDay);
    }

    /**
     * 将"年-月-日"格式的日期字符串转成"年-月-日 23:59:59"的Timestamp
     *
     * @param dateString "年-月-日"格式的日期字符串
     * @return 对应于给定日期的23:59:59时刻的Timestamp对象
     */
    public static Timestamp toEndOfDayTimestamp(String dateString) {
        LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return Timestamp.valueOf(endOfDay);
    }

    /**
     * 将Timestamp转成"年-月-日"格式的日期字符串
     *
     * @param timestamp Timestamp对象
     * @return 格式化后的日期字符串
     */
    public static String toDateString(Timestamp timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        LocalDateTime dateTime = timestamp.toLocalDateTime();
        return dateTime.toLocalDate().format(DATE_FORMATTER);
    }


    /**
     * 将Timestamp转换为HHmmss格式的字符串。
     *
     * @param timestamp 需要转换的时间戳。
     * @return 格式化后的字符串。
     */
    public static String formatTimestampToHHmmss(Timestamp timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException("时间戳不能为空");
        }

        LocalDateTime localDateTime = timestamp.toLocalDateTime();
        return HHMMSS_FORMATTER.format(localDateTime);
    }



    /**
     * 获取 x天前的 0点0分0秒 的 Timestamp。
     */
    public static Timestamp getStartOfDayTimestamp(int days) {
        // 计算days天前的日期
        LocalDate targetDate = LocalDate.now().minusDays(days);
        // 设置时间为当天的起始时间（0点0分0秒）
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        // 转换为Timestamp
        return Timestamp.valueOf(startOfDay);
    }

    /**
     * 获取 x天前的 23点59分59秒 的 Timestamp。
     */
    public static Timestamp getEndOfDayTimestamp(int days) {
        // 获取当前日期时间
        LocalDate now = LocalDate.now();
        // 计算days天前的日期
        LocalDate targetDate = now.minusDays(days);
        // 设置结束时间为当天的23:59:59
        LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);
        // 转换为Timestamp
        return Timestamp.valueOf(endOfDay);
    }

    /**
     * 获取 今天的 23点59分59秒
     */
    public static Timestamp getEndOfDayTimestamp() {
        // 获取当前日期时间
        LocalDate now = LocalDate.now();
        // 设置结束时间为当前时间的23:59:59
        return Timestamp.from(now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusSeconds(1));
    }


    /**
     * 将遵循RFC3339标准格式的时间字符串转换为Timestamp。
     * 如果时间字符串为 "1970-01-01T08:00:00+08:00"，则返回 null。
     *
     * @param rfc3339DateTimeStr 遵循RFC3339标准格式的时间字符串
     * @return 转换后的Timestamp对象或null
     */
    public static Timestamp convertRfc3339ToTimestamp(String rfc3339DateTimeStr) {
        // 检查是否为空或者是否是指定的特殊时间字符串
        if (rfc3339DateTimeStr == null || "1970-01-01T08:00:00+08:00".equals(rfc3339DateTimeStr.trim())) {
            return null;
        }

        // 使用预定义的ISO_ZONED_DATE_TIME格式化器解析RFC3339时间字符串
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(rfc3339DateTimeStr, DateTimeFormatter.ISO_ZONED_DATE_TIME);

        // 将ZonedDateTime转换为Instant
        Instant instant = zonedDateTime.toInstant();

        // 使用Instant创建Timestamp对象
        return Timestamp.from(instant);
    }


    /**
     * LocalDateTime 转 String，使用默认格式 yyyy-MM-dd HH:mm:ss
     * @param localDateTime 要转换的 LocalDateTime 对象
     * @return 格式化后的字符串，若 localDateTime 为 null 则返回 null
     */
    public static String localDateTimeToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        Date date = localDateTimeToDate(localDateTime);
        return SDF_YMDHMS.get().format(date);
    }

    /**
     * LocalDateTime 转 String，使用指定格式
     * @param localDateTime 要转换的 LocalDateTime 对象
     * @param pattern 日期时间格式
     * @return 格式化后的字符串，若 localDateTime 为 null 则返回 null
     */
    public static String localDateTimeToString(LocalDateTime localDateTime, String pattern) {
        if (localDateTime == null) {
            return null;
        }
        Date date = localDateTimeToDate(localDateTime);
        ThreadLocal<SimpleDateFormat> formatter = FORMATTERS.computeIfAbsent(pattern, k -> ThreadLocal.withInitial(() -> new SimpleDateFormat(pattern)));
        return formatter.get().format(date);
    }

    /**
     * LocalDateTime 转 String，使用指定的 DateTimeFormatter
     * @param localDateTime 要转换的 LocalDateTime 对象
     * @param formatter 日期时间格式化器
     * @return 格式化后的字符串，若 localDateTime 为 null 则返回 null
     */
    public static String localDateTimeToString(LocalDateTime localDateTime, DateTimeFormatter formatter) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(formatter);
    }

    /**
     * String 转 LocalDateTime，使用默认格式 yyyy-MM-dd HH:mm:ss
     * @param dateTimeStr 要转换的日期时间字符串
     * @return 转换后的 LocalDateTime 对象，若 dateTimeStr 为 null 或空则返回 null
     */
    public static LocalDateTime stringToLocalDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            Date date = SDF_YMDHMS.get().parse(dateTimeStr);
            return dateToLocalDateTime(date);
        } catch (ParseException e) {
            log.error("stringToLocalDateTime error, dateTimeStr:{}", dateTimeStr, e);
            return null;
        }
    }

    /**
     * String 转 LocalDateTime，使用指定格式
     * @param dateTimeStr 要转换的日期时间字符串
     * @param pattern 日期时间格式
     * @return 转换后的 LocalDateTime 对象，若 dateTimeStr 为 null 或空则返回 null
     */
    public static LocalDateTime stringToLocalDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        ThreadLocal<SimpleDateFormat> formatter = FORMATTERS.computeIfAbsent(pattern, k -> ThreadLocal.withInitial(() -> new SimpleDateFormat(pattern)));
        try {
            Date date = formatter.get().parse(dateTimeStr);
            return dateToLocalDateTime(date);
        } catch (ParseException e) {
            log.error("stringToLocalDateTime error, dateTimeStr:{}, pattern:{}", dateTimeStr, pattern, e);
            return null;
        }
    }

    /**
     * String 转 LocalDateTime，使用指定的 DateTimeFormatter
     * @param dateTimeStr 要转换的日期时间字符串
     * @param formatter 日期时间格式化器
     * @return 转换后的 LocalDateTime 对象，若 dateTimeStr 为 null 或空则返回 null
     */
    public static LocalDateTime stringToLocalDateTime(String dateTimeStr, DateTimeFormatter formatter) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    /**
     * LocalDateTime 转 Date
     * @param localDateTime 要转换的 LocalDateTime 对象
     * @return 转换后的 Date 对象
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * Date 转 LocalDateTime
     * @param date 要转换的 Date 对象
     * @return 转换后的 LocalDateTime 对象
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        Instant instant = date.toInstant();
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }


}