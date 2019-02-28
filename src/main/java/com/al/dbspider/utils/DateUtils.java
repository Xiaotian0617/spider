package com.al.dbspider.utils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    //日期常量
    static enum DateConstants {
        TODAY(0), NEARLYWEEK(1), MONTH(2), NEARLYMONTH(3);
        public int value;

        DateConstants(int value) {
            this.value = value;
        }
    }

    /**
     * 显示日期的格式,yyyy-MM-dd
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    /**
     * 显示日期的格式,yyyy-MM-dd
     */
    public static final String DATE_HOUR_FORMAT = "yyyy-MM-dd HH:mm";

    public static final String DATE_HOUR_FORMAT2 = "yyyy/MM/dd HH:mm";

    /**
     * 显示日期的格式,yyyy-MM
     */
    public static final String DATE_YEAE_MONTH = "yyyy-MM";

    /**
     * 显示日期的格式,yyyy-MM-dd HH:mm:ss
     */
    public static final String TIMEF_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 显示日期的格式,yyyy-MM-dd HH:mm:ss.SSS
     */
    public static final String FULL_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 显示日期的格式,yyyy-MM-ddTHH:mm:ss.SSSZ
     */
    public static final String FULL_TIME_FORMAT_UTC = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * 显示日期的格式,yyyy年MM月dd日HH时mm分ss秒
     */
    public static final String ZHCN_TIME_FORMAT = "yyyy年MM月dd日HH时mm分ss秒";
    /**
     * 显示日期的格式,yyyy年MM月dd日HH时mm分
     */
    public static final String ZHCN_TIME_FORMAT1 = "yyyy年MM月dd日HH时mm分";

    /**
     * 中文日期  年月日
     */
    public static final String ZHCN_DAY_FORMAT = "yyyy年MM月dd日";

    /**
     * 显示日期的格式,yyyyMMddHHmmss
     */
    public static final String TIME_STR_FORMAT = "yyyyMMddHHmmss";
    /**
     * 显示日期的格式,yyyyMMddHHmmssSSS
     */
    public static final String TIMESSS_STR_FORMAT = "yyyyMMddHHmmssSSS";
    /**
     * 显示日期的格式,yyyyMMdd
     */
    public static final String DATE_STR_FORMAT = "yyyyMMdd";

    /**
     * 显示日期的格式,yyMMdd
     */
    public static final String DATE_SIMPLE_SHORT_FORMAT = "yyMMdd";

    /**
     * DateFormat,格式:yyyy-MM-dd
     */
    private static DateFormat dateFormat;

    /**
     * DateFormat,格式:yyyy-MM-dd HH:mm:ss
     */
    private static DateFormat dateTimeFormat;

    /**
     * DateFormat,格式:yyyyMMddHHmmss
     */
    private static DateFormat dateTimeStrFormat;

    /**
     * DateFormat,格式:yyyy年MM月dd日HH时mm分ss秒
     */
    private static DateFormat zhcnDateTimeStrFormat;

    static {
        dateFormat = getInstance(DATE_FORMAT);
        dateTimeFormat = getInstance(TIMEF_FORMAT);
        dateTimeStrFormat = getInstance(TIME_STR_FORMAT);
        zhcnDateTimeStrFormat = getInstance(ZHCN_TIME_FORMAT);
    }

    public static DateFormat getInstance(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format;
    }

    /**
     * 自定义日期格式  日期转为字符串
     *
     * @param date  要转换的日期
     * @param fomat 日期格式 如: "yyyy-MM-dd HH:mm:ss"
     * @return
     * @date 2018/1/13
     * @author hgl
     * @see
     * @since
     */
    public static String getDateToStringByFormat(Date date, String fomat) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat(fomat);
        return format.format(date);
    }

    /**
     * 日期转换为 剩余时间 (如: 10sec ago ; 12min ago)
     *
     * @return
     * @date 2018/1/15
     * @author hgl
     * @see
     * @since
     */
    public static String getDateToLeftTime(Date date) {
        String leftTime = "";
        if (date == null) {
            return leftTime;
        }

        String postfix = " ago";
        String sec = "sec";
        String min = "min";
        String hour = "hrs";
        String yesterday = "昨天";
        Date currentTime = new Date();
        long currentMs = System.currentTimeMillis();
        long dateMs = date.getTime();
        long letfSec = (currentMs - dateMs) / 1000;
        //不足一分钟
        if (letfSec < 60) {
            leftTime = letfSec + sec + postfix;
            return leftTime;
        }
        //不足一小时
        if (letfSec >= 60 && letfSec < 60 * 60) {

            leftTime = letfSec / 60 + min + postfix;
            return leftTime;
        }
        //不足一天
        if (letfSec >= 60 * 60 && letfSec < 60 * 60 * 24) {

            leftTime = letfSec / (60 * 60) + hour + postfix;
            return leftTime;
        }
        //大于一天
        if (letfSec >= 60 * 60 * 24) {
            leftTime = DateUtils.getDateToStringByFormat(date, DATE_HOUR_FORMAT2);
            return leftTime;
        }

        return leftTime;

    }

    /**
     * 获取当前时间在＋－n分钟后的字符串时间
     *
     * @param n int
     * @return String
     */
    public static String getTimebyMinAfter(int n) {
        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        now.add(Calendar.MINUTE, n);
        return (dateTimeFormat.format(now.getTime()));
    }

    /**
     * 获取当前时间在＋－n秒后的字符串时间
     *
     * @param n int
     * @return String
     */
    public static String getTimebySecAfter(int n) {
        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        now.add(Calendar.SECOND, n);
        return (dateTimeFormat.format(now.getTime()));
    }

    /**
     * 获取当前时间在＋－n分钟后的时间(java.util.Date)
     *
     * @param n int
     * @return String
     */
    public static Date getTimebyMinAfterDate(int n) {
        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        now.add(Calendar.MINUTE, n);
        return now.getTime();
    }

    /**
     * 获取定义的DateFormat格式
     *
     * @param formatStr
     * @return
     */
    private static DateFormat getDateFormat(String formatStr) {
        if (formatStr.equalsIgnoreCase(DATE_FORMAT)) {
            return dateFormat;
        } else if (formatStr.equalsIgnoreCase(TIMEF_FORMAT)) {
            return dateTimeFormat;
        } else {
            return getInstance(formatStr);
        }
    }

    /**
     * 将Date转换成字符串“yyyy-mm-dd hh:mm:ss”的字符串
     *
     * @param date 日期
     * @return String 字符串
     */
    public static String dateToDateString(Date date) {
        return dateToDateString(date, TIMEF_FORMAT);
    }

    /**
     * 将Date转换成formatStr格式的字符串
     *
     * @param date
     * @param formatStr
     * @return
     */
    public static String dateToDateString(Date date, String formatStr) {
        DateFormat df = getDateFormat(formatStr);
        return df.format(date);
    }

    /**
     * 将Date转换成yyyy-MM-dd的字符串
     *
     * @param date
     * @return
     */
    public static String getDateString(Date date) {
        DateFormat df = getDateFormat(DATE_FORMAT);
        return df.format(date);
    }

    /**
     * 将小时数换算成返回以毫秒为单位的时间
     *
     * @param hours
     * @return
     */
    public static long getMicroSec(BigDecimal hours) {
        BigDecimal bd;
        bd = hours.multiply(new BigDecimal(3600 * 1000));
        return bd.longValue();
    }

    /**
     * 获取今天的日期，格式自定
     *
     * @param pattern -
     *                设定显示格式
     * @return String - 返回今天的日期
     */
    public static String getToday(String pattern) {
        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        DateFormat sdf = getDateFormat(pattern);
        sdf.setTimeZone(TimeZone.getDefault());
        return (sdf.format(now.getTime()));
    }

    // 得到系统当前的分钟数,如当前时间是上午12:00,系统当前的分钟数就是12*60
    public static int getCurrentMinutes() {
        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        int iMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        return iMin;
    }

    /**
     * 获取当前日期时间yyyy年MM月dd日HH时mm分ss秒的形式
     *
     * @return 当前日期时间yyyy年MM月dd日HH时mm分ss秒的形式
     */
    public static String getCurZhCNDateTime() {
        return dateToDateString(new Date(), ZHCN_TIME_FORMAT);
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static Date getCurrentDate() {
        return new Date(System.currentTimeMillis());
    }

    /**
     * @return 得到本月月份 如09
     */
    public static String getMonth() {
        Calendar now = Calendar.getInstance();
        int month = now.get(Calendar.MONTH) + 1;
        String monStr = String.valueOf(month);

        // 对于10月份以下的月份,加"0"在前面

        if (month < 10) {
            monStr = "0" + monStr;
        }
        return monStr;
    }

    /**
     * @return 得到本月第几天
     */
    public static String getDay() {
        Calendar now = Calendar.getInstance();
        int day = now.get(Calendar.DAY_OF_MONTH);
        String dayStr = String.valueOf(day);

        // 对于10月份以下的月份,加"0"在前面

        if (day < 10) {
            dayStr = "0" + dayStr;
        }
        return dayStr;
    }

    /**
     * @return 获取指定日期月份 如09
     */
    public static String getMonth(Date date) {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        int month = now.get(Calendar.MONTH) + 1;
        String monStr = String.valueOf(month);
        // 对于10月份以下的月份,加"0"在前面
        if (month < 10) {
            monStr = "0" + monStr;
        }
        return monStr;
    }

    /**
     * @return 获取指定日期年 如2016
     */
    public static String getYear(Date date) {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        int year = now.get(Calendar.YEAR);
        String monStr = String.valueOf(year);
        return monStr;
    }

    /**
     * @return 获取指定日期天数
     */
    public static String getDay(Date date) {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        int day = now.get(Calendar.DAY_OF_MONTH);
        String dayStr = String.valueOf(day);
        // 对于10月份以下的月份,加"0"在前面
        if (day < 10) {
            dayStr = "0" + dayStr;
        }
        return dayStr;
    }

    /**
     * 根据失效时间判断是否依然有效
     *
     * @param expireTime 失效时间的字符串形式,格式要求:yyMMddHHmmss
     * @return true:失效 false:没有失效
     * @throws ParseException
     */
    public static boolean isTimeExpired(String expireTime) throws ParseException {
        boolean ret = false;

        // SimpleDateFormat不是线程安全的,所以每次调用new一个新的对象

        Date date = getInstance(TIME_STR_FORMAT).parse(expireTime);
        Calendar expire = Calendar.getInstance();
        expire.setTime(date);
        Calendar now = Calendar.getInstance();
        // 将毫秒字段设为0,保持精度一致性

        now.set(Calendar.MILLISECOND, 0);
        if (now.after(expire)) {
            ret = true;
        }
        return ret;
    }

    /**
     * 根据开始时间和可用时间计算出失效时间
     *
     * @param startTime     开始时间
     * @param availableTime 有效时长（单位：秒）
     * @return 失效时间
     * @throws ParseException
     */
    public static String getExpireTimeByCalculate(Date startTime, int availableTime) {

        Calendar expire = Calendar.getInstance();

        // 设置为开始时间

        expire.setTime(startTime);

        // 开始时间向后有效时长秒得到失效时间
        expire.add(Calendar.SECOND, availableTime);

        // 返回失效时间字符串

        // SimpleDateFormat不是线程安全的,所以每次调用new一个新的对象

        return getInstance(TIME_STR_FORMAT).format(expire.getTime());

    }

    /**
     * 将Date转换为yyyyMMddHHmmss的形式
     *
     * @param date
     * @return 日期的字符串形式, 格式：yyyyMMddHHmmss
     */
    public static String convertDateToString(Date date) {
        return getInstance(TIME_STR_FORMAT).format(date);
    }

    /**
     * 将Date转换为yyMMddHHmmss的形式
     *
     * @param date
     * @return 日期的字符串形式, 格式：yyMMddHHmmss
     */
    public static String convertDateToString(Date date, String format) {
        return getInstance(format).format(date);
    }

    /**
     * 将yyMMddHHmmss形式的字符串转换成Date的形式
     *
     * @param date yyMMddHHmmss形式的日期字符串
     * @return Date对象
     * @throws ParseException
     */
    public static Date convertStringToDate(String date) throws ParseException {
        return getInstance(TIME_STR_FORMAT).parse(date);
    }

    /**
     * 字符串转化为日期
     *
     * @param date         日期字符串
     * @param formatString 格式化字符串
     * @return
     * @throws ParseException
     */
    public static Date convertStringToDate(String date, String formatString) throws ParseException {
        return getInstance(formatString).parse(date);
    }

    /**
     * 日期转化为格式化日期
     *
     * @param date         日期
     * @param formatString 格式化字符串
     * @return
     * @throws ParseException
     */
    public static Date convertDateToDate(Date date, String formatString) throws ParseException {
        return getInstance(formatString).parse(convertDateToString(date, formatString));
    }

    /**
     * 字符串转化为格式化字符串格式
     *
     * @param date         日期
     * @param formatString 格式化字符串
     * @return
     * @throws ParseException
     */
    public static String convertStringToString(String date, String formatString) throws ParseException {
        return getInstance(formatString).format(date);
    }

    /**
     * 将yy-MM-dd形式的字符串转换成Date的形式
     *
     * @param date yy-MM-dd形式的日期字符串
     * @return Date对象
     * @throws ParseException
     */
    public static Date convertSimpleStringToDate(String date) throws ParseException {
        return getInstance(DATE_FORMAT).parse(date);
    }

    /**
     * @param date yyyyMMddHHmmss格式的日期字符转换为yyyy年MM月dd日HH时mm分ss秒格式的字符串
     * @return yyyy年MM月dd日HH时mm分ss秒格式的日期字符串
     * @throws ParseException
     */
    public static String convertStringToZhCN(String date) throws ParseException {
        return zhcnDateTimeStrFormat.format(dateTimeStrFormat.parse(date));
    }

    /**
     * 时间字符串转换成日期时间的形式
     *
     * @param date yy-MM-dd HH:mm:ss形式的日期字符串
     * @return Date对象
     * @throws ParseException
     */
    public static Date convertSimpleStringToDateTime(String date) throws ParseException {
        return getInstance(TIMEF_FORMAT).parse(date);
    }

    /**
     * 获取当天日期
     */
    public static Date getTodayDate() {
        // 获取昨日的日期
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        return today;
    }

    /**
     * 获取昨日日期 返回格式：yyyy-MM-dd
     */
    public static String getYesterdayDateStr() {
        // 获取昨日的日期
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = cal.getTime();
        return getInstance(DATE_FORMAT).format(yesterday);
    }

    /**
     * 获取昨日日期 返回格式：yyyy-MM-dd
     */
    public static Date getYesterdayDate() {
        // 获取昨日的日期
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = cal.getTime();
        return yesterday;
    }

    /**
     * 获取明天日期 返回格式：yyyy-MM-dd
     */
    public static String getTomorrowDateStr() {
        // 获取昨日的日期
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = cal.getTime();
        return getInstance(DATE_FORMAT).format(tomorrow);
    }

    /**
     * 获取明天日期 返回格式：yyyy-MM-dd
     */
    public static Date getTomorrowDate() {
        // 获取昨日的日期

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = cal.getTime();
        return tomorrow;
    }

    /**
     * 根据Calendar对象、字符串日期类型获得字符串日期
     *
     * @param calendar    Calendar对象
     * @param strDateType 字符串日期类型（1：XXXX年XX月XX日，2：XX月XX日，3，XXXX年，4：XXXX-XX-XX，5：XX-XX，6：XXXX）
     * @return
     */
    public static String getStrDate(Calendar calendar, int strDateType) {
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = (calendar.get(Calendar.MONTH) + 1) < 10 ? "0" + (calendar.get(Calendar.MONTH) + 1) : String
                .valueOf

                        ((calendar.get(Calendar.MONTH) + 1));
        String day = calendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + calendar.get(Calendar.DAY_OF_MONTH) : String
                .valueOf

                        (calendar.get(Calendar.DAY_OF_MONTH));
        String strDate = "";

        switch (strDateType) {
            case 1:
                strDate = year + "年" + month + "月" + day + "日";
                break;
            case 2:
                strDate = month + "月" + day + "日";
                break;
            case 3:
                strDate = year + "年";
                break;
            case 4:
                strDate = year + "-" + month + "-" + day;
                break;
            case 5:
                strDate = month + "-" + day;
                break;
            case 6:
                strDate = year;
                break;
            default:
                strDate = year + "-" + month + "-" + day;
                break;
        }

        return strDate;
    }

    /**
     * 根据原来的时间（Date）获得相对偏移 N 月的时间（Date）
     *
     * @param protoDate                原来的时间（java.util.Date）
     * @param monthOffset（向前移正数，向后移负数）
     * @return 时间（java.util.Date）
     */
    public static Date getOffsetMonthDate(Date protoDate, int monthOffset) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(protoDate);
        //		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - monthOffset);错误写法
        cal.add(Calendar.MONTH, -monthOffset);
        return cal.getTime();
    }

    /**
     * 根据原来的时间（Date）获得相对偏移 N 天的时间（Date）
     *
     * @param protoDate               原来的时间（java.util.Date）
     * @param dateOffset（向前移正数，向后移负数）
     * @return 时间（java.util.Date）
     */
    public static Date getOffsetDayDate(Date protoDate, int dateOffset) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(protoDate);
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - dateOffset);
        return cal.getTime();
    }

    /**
     * 根据原来的时间（Date）获得相对偏移 N 小时的时间（Date）
     *
     * @param protoDate  原来的时间（java.util.Date）
     * @param offsetHour （向前移正数，向后移负数）
     * @return 时间（java.util.Date）
     */
    public static Date getOffsetHourDate(Date protoDate, int offsetHour) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(protoDate);
        cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) - offsetHour);
        return cal.getTime();
    }

    /**
     * 获取指定月份和日子的日期(未做日期效验)
     *
     * @param currentDate 当前日期
     * @param assignYear  指定年份,-1代表年不做修改
     * @param assignMonth 指定月份,从0开始,超过月最大值自动往后加一个月年,-1代表月不做修改
     * @param assignDay   指定日,从1开始,超过日最大值往后加一个月,-1代表日不做修改
     * @return 修改后的日期
     */
    public static Date getAssignDate(Date currentDate, int assignYear, int assignMonth, int assignDay) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        if (assignYear > -1) {
            cal.set(Calendar.YEAR, assignYear);
        }
        if (assignMonth > -1) {
            cal.set(Calendar.MONTH, assignMonth);
        }
        if (assignDay > -1) {
            cal.set(Calendar.DAY_OF_MONTH, assignDay);
        }
        return cal.getTime();
    }

    /**
     * 获得两个日前之间相差的天数,有时分秒的影响
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 天数
     */
    public static int getDayCountBetweenDate(Date startDate, Date endDate) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);
        int i = 0;
        while (endCalendar.compareTo(startCalendar) >= 0) {
            startCalendar.set(Calendar.DAY_OF_MONTH, startCalendar.get(Calendar.DAY_OF_MONTH) + 1);
            i++;
        }
        return i;
    }

    /**
     * 获得两个日前之间相差的月份
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 月数
     */
    public static int getMonthCountBetweenDate(Date startDate, Date endDate) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);
        int i = 0;
        while (endCalendar.compareTo(startCalendar) >= 0) {
            startCalendar.set(Calendar.MONTH, startCalendar.get(Calendar.MONTH) + 1);
            i++;
        }
        return i;
    }

    /**
     * 获得两个日前之间相差的年
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 年
     */
    public static int getYearlyCountBetweenDate(Date startDate, Date endDate) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);
        int i = 0;
        while (endCalendar.compareTo(startCalendar) >= 0) {
            startCalendar.set(Calendar.YEAR, startCalendar.get(Calendar.YEAR) + 1);
            i++;
        }
        return i;
    }

    /**
     * 根据原来的时间（Date）获得相对偏移 N 天的时间（Date）
     *
     * @param protoDate               原来的时间（java.util.Date）
     * @param dateOffset（向前移正数，向后移负数）
     * @param type                    指定不同的格式（1：年月日，2：年月日时，3：年月日时分）
     * @return 时间（java.util.Date），没有时分秒
     */
    public static Date getOffsetSimpleDate(Date protoDate, int dateOffset, int type) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(protoDate);
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - dateOffset);
        if (type == 1) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
        }
        if (type == 2) {
            cal.set(Calendar.MINUTE, 0);
        }
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 时间转为化为字符串
     * <p>
     * 格式为：yyyyMMddHHmmssSSS
     *
     * @return
     */
    public static String getDateToString() {
        SimpleDateFormat dateFormat = (SimpleDateFormat) getInstance(TIMESSS_STR_FORMAT);
        Date date = new Date();
        String str = dateFormat.format(date);
        return str;
    }

    /**
     * 时间转为化为字符串
     * <p>
     * 格式为：yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static String getTodayTimeString() {
        SimpleDateFormat dateFormat = (SimpleDateFormat) getInstance(TIMEF_FORMAT);
        Date date = new Date();
        String str = dateFormat.format(date);
        return str;
    }

    /**
     * 增加一年
     *
     * @param s
     * @param n
     * @return
     */
    public static Date addYear(Date s, int n) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(s);
        cd.add(Calendar.YEAR, n);// 增加一天
        return cd.getTime();
    }


    /**
     * 增加一天
     *
     * @param s
     * @param n
     * @return
     */
    public static String addDay(String s, int n) {
        try {
            SimpleDateFormat sdf = (SimpleDateFormat) getInstance(DateUtils.DATE_FORMAT);
            Calendar cd = Calendar.getInstance();
            cd.setTime(sdf.parse(s));
            cd.add(Calendar.DATE, n);// 增加一天
            return sdf.format(cd.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 增加一天
     *
     * @param s
     * @param n
     * @return
     */
    public static Date addDay(Date s, int n) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(s);
        cd.add(Calendar.DATE, n);// 增加一天
        return cd.getTime();
    }

    /**
     * 增加一个月
     *
     * @param m
     * @param n
     * @return
     */
    public static Date addMonth(Date m, int n) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(m);
        cd.add(Calendar.MONTH, n);// 增加一个月
        return cd.getTime();
    }

    /**
     * 增加一个月
     *
     * @param m
     * @param n
     * @param formatstring
     * @return
     */
    public static String addMonth(Date m, int n, String formatstring) {
        try {
            SimpleDateFormat sdf = (SimpleDateFormat) getInstance(formatstring);
            Calendar cd = Calendar.getInstance();
            cd.setTime(m);
            cd.add(Calendar.MONTH, n);//增加一个月
            return sdf.format(cd.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 增加一分
     *
     * @param s
     * @param n
     * @return
     */
    public static Date addMinute(Date s, int n) {
        try {
            Calendar cd = Calendar.getInstance();
            cd.setTime(s);
            cd.add(Calendar.MINUTE, n);// 增加一天
            return cd.getTime();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 获取需要执行的统计的日期数组
     *
     * @return 格式['2011-01-01',2011-01-02']
     */
    public static String[] getExecDay(Date lastDate) {
        String[] day = null;
        // 获取昨天日期
        Date yesdate = null;
        try {
            yesdate = DateUtils.convertDateToDate(DateUtils.getYesterdayDate(), DateUtils.DATE_FORMAT);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // 获取上次最后执行日期与昨天相隔天数
        int dayCount = DateUtils.getDayCountBetweenDate(lastDate, yesdate);
        if (dayCount <= 0) {
            return day;
        }
        if (dayCount == 1) {
            return new String[]{DateUtils.getYesterdayDateStr()};
        } else {
            day = new String[dayCount];
            for (int i = 0; i < dayCount; i++) {
                String date = getInstance(DateUtils.DATE_HOUR_FORMAT).format(DateUtils.addDay(lastDate, i));
                day[i] = date;
            }
        }
        return day;
    }

    /**
     * 获取需要执行的统计的年-月数组
     *
     * @return 格式['2011-01',2011-01']
     */
    public static String[] getExecYearMonth(Date lastYearMonth) {
        String[] yearMonth = null;
        // 获取上个月日期
        Date lastMonth = DateUtils.getOffsetMonthDate(new Date(), 1);
        try {
            lastMonth = DateUtils.convertDateToDate(lastMonth, DateUtils.DATE_YEAE_MONTH);
            //			System.out.println(lastMonth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 获取上次最后执行日期与昨天相隔天数
        int monthCount = DateUtils.getMonthCountBetweenDate(lastYearMonth, lastMonth);
        if (monthCount <= 0) {
            return yearMonth;
        }
        if (monthCount == 1) {
            return yearMonth = new String[]{DateUtils.convertDateToString(lastMonth, DateUtils.DATE_YEAE_MONTH)};
        } else {
            yearMonth = new String[monthCount];
            for (int i = 0; i < monthCount; i++) {
                String date = DateUtils.addMonth(lastYearMonth, i, DateUtils.DATE_YEAE_MONTH);
                yearMonth[i] = date;
            }
        }
        return yearMonth;
    }

    /**
     * 获取这个月第一天
     *
     * @return
     */
    public static Date getFirstDayOfMonth() {
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDate = ca.getTime();
        ca.add(Calendar.MONTH, 1);
        ca.add(Calendar.DAY_OF_MONTH, -1);
        return firstDate;
    }

    /**
     * 获这个月的最后一天
     *
     * @return
     */
    public static Date getLastDayOfMonth() {
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.DAY_OF_MONTH, 1);
        ca.add(Calendar.MONTH, 1);
        ca.add(Calendar.DAY_OF_MONTH, -1);
        Date lastDate = ca.getTime();
        return lastDate;
    }

    /**
     * 获取一天的最后时间，常用于时间段查询的结束时间的处理
     *
     * @param d
     * @return 得到的时间为在d的23:59:59
     */
    public static Date getLastTimeOfDay(Date d) {
        if (null == d) {
            d = DateUtils.getCurrentDate();
        }
        String dateStr = getInstance(DATE_FORMAT).format(d);
        try {
            d = DateUtils.convertStringToDate(dateStr, DATE_FORMAT);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar ca = Calendar.getInstance();
        ca.setTime(d);
        ca.add(Calendar.DAY_OF_MONTH, 1);
        ca.add(Calendar.MILLISECOND, -1);
        d = ca.getTime();
        return d;
    }

    /**
     * 获查询日期区间
     * 今天(0), 近一周(1), 本月(2),近一月(3) ;
     *
     * @return Date[0] 开始时间  Date[1] 结束时间
     */
    public static Date[] getDateSection(int dateType) {
        Date[] dateSection = new Date[2];
        if (DateConstants.TODAY.value == dateType) {
            dateSection[0] = getTodayDate();
            dateSection[1] = dateSection[0];
        } else if (DateConstants.NEARLYWEEK.value == dateType) {
            dateSection[0] = getOffsetDayDate(getTodayDate(), 7);
            dateSection[1] = getTodayDate();
        } else if (DateConstants.NEARLYMONTH.value == dateType) {
            dateSection[0] = getOffsetMonthDate(getTodayDate(), 1);
            dateSection[1] = getTodayDate();
        } else if (DateConstants.MONTH.value == dateType) {
            dateSection[0] = getFirstDayOfMonth();
            dateSection[1] = getLastDayOfMonth();
        } else {
            dateSection = null;
        }
        return dateSection;
    }

    ;


    public static boolean isNumberic(String str) {
        if (str == null) {
            return false;
        }
        int sz = str.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(str.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算两个日期之间相差的天数
     *
     * @param smdate 较小的时间
     * @param bdate  较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    public static int daysBetween(Date smdate, Date bdate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        smdate = sdf.parse(sdf.format(smdate));
        bdate = sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long betweenDays = (time2 - time1) / (1000 * 3600 * 24);

        return Integer.parseInt(String.valueOf(betweenDays));
    }

    /**
     * @return
     * @Description
     * @author HYK
     * @data 2017
     */
    public static String getNowDate() {
        return convertDateToString(new Date(), TIMEF_FORMAT);
    }


    public static Date getStartTime() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        return todayStart.getTime();
    }

    public static Date getZeroTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime result = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0);
        return LocalDateTimeToDate(result);
    }

    public static Date getEightTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime result = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 8, 0, 0);
        return LocalDateTimeToDate(result);
    }

    public static Date getYesterdatEightTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime result = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 8, 0, 0);
        return LocalDateTimeToDate(result.minusDays(1));
    }

    public static Date getEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        return todayEnd.getTime();
    }

    /**
     * 获取距离当前时间前多少天的日期
     *
     * @param dayNum 被减数
     * @return
     */
    public static Date getBeforeNowTimeByDays(int dayNum) {
        LocalDateTime result = LocalDateTime.now().minusDays(dayNum);
        return LocalDateTimeToDate(result);
    }

    static Date LocalDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static void main(String[] args) {
        System.out.println(getZeroTime().getTime());
        System.out.println(getEightTime().getTime());
    }
}
