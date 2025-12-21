package cn.lizongyi.shareaccount.util;

import cn.lizongyi.shareaccount.constants.NumConstant;
import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 *
 * @author justin
 * @date 2022/7/13 14:18
 */
@Slf4j
public class TimeUtil {

    /**
     * 日期映射
     */
    private static final int[] DAY_OF_WEEK_MAPPING = new int[] {0, 7, 1, 2, 3, 4, 5, 6};

    private static final ThreadLocal<SimpleDateFormat> SDF_HMS =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("HH:mm:ss"));

    /**
     * 比较一个 HH:mm:ss 是否在一个时间段内 如：14:33:00 是否在 09:30:00 和 12:00:00 内
     *
     * @param time 给定的时间，格式为HH:mm:ss
     * @param start 起始时间，格式为HH:mm:ss
     * @param end 结束时间，格式为HH:mm:ss
     * @return 如果给定的时间在指定的时间范围内，返回true；否则返回false
     * @throws ParseException 解析异常
     */
    public static boolean timeIsInRound(String time, String start, String end) throws ParseException {
        Date now = SDF_HMS.get().parse(time);
        Date beginTime = SDF_HMS.get().parse(start);
        Date endTime = SDF_HMS.get().parse(end);
        return isEffectiveDate(now, beginTime, endTime);
    }

    /**
     * 比较一个 HH:mm:ss 是否在一个时间段内 如：14:33:00 是否在 09:30:00 和 12:00:00 内
     *
     * @param time 给定的时间，格式为HH:mm:ss
     * @param start 起始时间，格式为HH:mm:ss
     * @param end 结束时间，格式为HH:mm:ss
     * @return 如果给定的时间在指定的时间范围内，返回true；否则返回false
     */
    public static boolean timeIsInRound(LocalTime time, LocalTime start, LocalTime end) {
        return time.isAfter(start) && time.isBefore(end);
    }

    /**
     * 14:33:00 是否在 09:30:00 - 12:00:00 内
     *
     * @param time 14:33:00
     * @param round 09:30:00 - 12:00:00
     * @return boolean
     * @throws ParseException 解析异常
     */
    public static boolean timeIsInRound(String time, String round) throws ParseException {
        String[] roundTime = round.split("-");
        return timeIsInRound(time, roundTime[0], roundTime[1]);
    }

    /**
     * 14:33:00 是否在 09:30:00 - 12:00:00 内
     *
     * @param round 09:30:00 - 12:00:00
     * @return boolean
     * @throws ParseException 解析异常
     */
    public static boolean nowIsInRound(String round) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        String now = SDF_HMS.get().format(calendar.getTime());
        String[] roundTime = round.split("-");
        return timeIsInRound(now, roundTime[0], roundTime[1]);
    }

    /**
     * 判断两个时间范围是否有交集
     *
     * @param compareStart 比较时间段开始时间 09:30:00
     * @param compareEnd 比较时间段结束时间 12:00:00
     * @param limitStart 参考时间段开始时间 09:31:00
     * @param limitEnd 参考时间段结束时间 12:10:00
     * @return boolean
     */
    public static boolean timeBeMixed(Date compareStart, Date compareEnd, Date limitStart, Date limitEnd) {
        return !(compareEnd.getTime() < limitStart.getTime() || compareStart.getTime() > limitEnd.getTime());
    }

    /**
     * 判断两个时间范围是否有交集
     *
     * @param compareStart 比较时间段开始时间
     * @param compareEnd 比较时间段结束时间
     * @param limitStart 参考时间段开始时间
     * @param limitEnd 参考时间段结束时间
     * @return boolean
     * @throws ParseException 解析异常
     */
    public static boolean timeStrBeMixed(String compareStart, String compareEnd, String limitStart, String limitEnd)
        throws ParseException {
        return timeBeMixed(SDF_HMS.get().parse(compareStart), SDF_HMS.get().parse(compareEnd),
            SDF_HMS.get().parse(limitStart), SDF_HMS.get().parse(limitEnd));
    }

    /**
     * 日期时间差，返回秒
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 时间差（秒）
     */
    public static long calculateTimeDifferenceBySeconds(LocalTime startDate, LocalTime endDate) {
        return ChronoUnit.SECONDS.between(startDate, endDate);
    }

    /**
     * Date转LocalDateTime
     *
     * @param date 日期
     */
    public static LocalDateTime date2LocalDateTime(Date date) {
        Instant instant = date.toInstant();
        // (时区)
        ZoneId zoneId = ZoneId.systemDefault();
        return instant.atZone(zoneId).toLocalDateTime();
    }

    /**
     * 判断时间是否在指定的范围内
     *
     * @param nowTime 当前时间
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 如果当前时间在指定的时间范围内，返回true；否则返回false
     */
    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() == startTime.getTime() || nowTime.getTime() == endTime.getTime()) {
            return true;
        }
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);
        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        return date.after(begin) && date.before(end);
    }

    /**
     * 匹配时间范围
     *
     * @param startTime 开始时间，格式为HH:mm
     * @param endTime 结束时间，格式为HH:mm
     * @param nowTimeStamp 当前时间戳
     * @return 如果当前时间在指定的时间范围内，返回true；否则返回false
     */
    public static boolean matchTimeRange(String startTime, String endTime, Long nowTimeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(nowTimeStamp));
        String nowTime = String.format("%s:%s", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        DateFormat df = new SimpleDateFormat("HH:mm");
        try {
            Date start = df.parse(startTime);
            Date end = df.parse(endTime);
            Date now = df.parse(nowTime);
            return now.getTime() >= start.getTime() && now.getTime() <= end.getTime();
        } catch (ParseException e) {
            log.error("matchTimeRange error: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 获取当前的时间戳
     */
    public static long getCurrentTimeSec() {
        return System.currentTimeMillis() / NumConstant.INT_1000;
    }

    /**
     * 获取当天零点的时间戳
     */
    public static long getTodayZeroTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, NumConstant.INT_0);
        calendar.set(Calendar.MINUTE, NumConstant.INT_0);
        calendar.set(Calendar.SECOND, NumConstant.INT_0);
        return calendar.getTimeInMillis();
    }

    /**
     * 获取时间的星期几（星期一返回1，星期日返回7，以此类推）
     *
     * @param calendar 时间
     * @return 星期几
     */
    public static int getNaturalDayOfWeek(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // 日历里每周的第一天是星期日，需要做一下映射
        return (dayOfWeek >= 0 && dayOfWeek < DAY_OF_WEEK_MAPPING.length) ? DAY_OF_WEEK_MAPPING[dayOfWeek]
            : DAY_OF_WEEK_MAPPING[0];
    }

    /**
     * 获取时间的星期几（星期一返回1，星期日返回7，以此类推）
     *
     * @param now 时间
     * @return 星期几
     */
    public static int getNaturalDayOfWeek(LocalDate now) {
        return now.getDayOfWeek().getValue();
    }

    /**
     * 获取时间的日时间戳（单位：秒）
     *
     * @param calendar 时间
     * @return 日时间戳
     */
    public static int getDayTimestampInSecond(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return hour * NumConstant.INT_60 * NumConstant.INT_60 + minute * NumConstant.INT_60 + second;
    }

    /**
     * 日期时间差，返回秒
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 时间差（以秒为单位）
     */
    public static long calculateTimeDiffBySeconds(LocalDateTime startDate, LocalDateTime endDate) {
        return ChronoUnit.SECONDS.between(startDate, endDate);
    }

    /**
     * 判断是否在同一天
     *
     * @param date1 第一个时间
     * @param date2 第二个时间
     * @return 如果两个时间在同一天，则返回true；否则返回false
     */
    public static boolean isInSameDay(LocalDateTime date1, LocalDateTime date2) {
        return date1.getYear() == date2.getYear() && date1.getDayOfYear() == date2.getDayOfYear();
    }

    /**
     * 将时间戳转换为LocalTime对象
     *
     * @param timeTamp 时间戳
     * @return 转换后的LocalTime对象
     */
    public static LocalTime longToLocalTime(Long timeTamp) {
        return Instant.ofEpochMilli(timeTamp).atZone(ZoneId.systemDefault()).toLocalTime();
    }
}