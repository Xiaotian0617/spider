package com.al.dbspider.utils;

import com.al.dbspider.dao.domain.KLine;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.MarketCap;
import com.al.dbspider.dao.domain.Trade;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PointExt {

    private final static Map<Class<?>, PointExt> cache = new HashMap<Class<?>, PointExt>(10);

    private static final int MAX_FRACTION_DIGITS = 340;

    static {
        init(Market.class);
        init(KLine.class);
        init(Trade.class);
        init(MarketCap.class);
    }

    private static final ThreadLocal<NumberFormat> NUMBER_FORMATTER =
            ThreadLocal.withInitial(() -> {
                NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
                numberFormat.setMaximumFractionDigits(MAX_FRACTION_DIGITS);
                numberFormat.setGroupingUsed(false);
                numberFormat.setMinimumFractionDigits(1);
                return numberFormat;
            });
    private static final ThreadLocal<Map<String, MeasurementStringBuilder>> CACHED_STRINGBUILDERS =
            ThreadLocal.withInitial(HashMap::new);

    private static String keyEscape(String s) {
        return s.replace(" ", "\\ ").replace(",", "\\,").replace("=", "\\=");
    }

    private static String fieldEscape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }


    private static class MeasurementStringBuilder {
        private final StringBuilder sb = new StringBuilder(128);
        private final int length;

        MeasurementStringBuilder(final String measurement) {
            this.sb.append(keyEscape(measurement));
            this.length = sb.length();
        }

        StringBuilder resetForUse() {
            sb.setLength(length);
            return sb;
        }
    }

    private Field measurement;
    private List<Field> tags = new ArrayList<>();
    private List<Field> addFields = new ArrayList<>();
    private Field time;
    private TimeUnit precision = TimeUnit.MILLISECONDS;


    private void concatenatedTags(StringBuilder sb, Object object) throws IllegalAccessException {
        for (Field tag : tags) {
            Object value = tag.get(object);
            if (value == null) {
                continue;
            }

            sb.append(',')
                    .append(tag.getName())
                    .append('=')
                    .append(keyEscape(value.toString()));
        }
        sb.append(' ');
    }

    private void concatenatedFields(StringBuilder sb, Object object) throws IllegalAccessException {
        for (Field field : addFields) {
            Object value = field.get(object);
            if (value == null) {
                continue;
            }

            sb.append(keyEscape(field.getName())).append('=');
            if (value instanceof Number) {
                if (value instanceof Double || value instanceof Float || value instanceof BigDecimal) {
                    sb.append(NUMBER_FORMATTER.get().format(value));
                } else {
                    sb.append(value).append('i');
                }
            } else if (value instanceof String) {
                String stringValue = (String) value;
                sb.append('"').append(fieldEscape(stringValue)).append('"');
            } else {
                sb.append(value);
            }

            sb.append(',');
        }

        int lengthMinusOne = sb.length() - 1;
        if (sb.charAt(lengthMinusOne) == ',') {
            sb.setLength(lengthMinusOne);
        }
    }

    private void formatedTime(final StringBuilder sb, final Object object) throws IllegalAccessException {
        if (this.time == null || this.precision == null) {
            return;
        }
        sb.append(' ').append(TimeUnit.NANOSECONDS.convert(Long.valueOf(time.get(object).toString()), precision));
    }

    /**
     * 将java bean 转为line protocal字符串
     *
     * @param obj bean
     * @return string
     */
    public static String lineProtocal(Object obj) {
        Class<?> clz = obj.getClass();
        if (!cache.containsKey(clz)) {
            init(clz);
        }

        PointExt point = cache.get(clz);

        try {
            String measurement = point.measurement.get(obj).toString();
            final StringBuilder sb = CACHED_STRINGBUILDERS
                    .get()
                    .computeIfAbsent(measurement, MeasurementStringBuilder::new)
                    .resetForUse();
            point.concatenatedTags(sb, obj);
            point.concatenatedFields(sb, obj);
            point.formatedTime(sb, obj);
            return sb.toString();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * 初始化类结构到缓存, 如果没有类没有 {@link Measurement}注解 则会报异常
     *
     * @param clz java bean class
     */
    public static void init(Class<?> clz) {
        log.info("init point {}", clz.getName());

        PointExt ext = new PointExt();

        //通过注解获取tag和time字段，没有注解全为field
        Field[] fields = clz.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            if (field.getAnnotation(Tag.class) != null) {
                ext.tags.add(field);
                continue;
            }

            Time ann = field.getAnnotation(Time.class);
            if (ann != null) {
                ext.time = field;
                ext.precision = ann.value();
                continue;
            }

            if (field.getAnnotation(Measurement.class) != null) {
                ext.measurement = field;
                continue;
            }

            ext.addFields.add(field);
        }

        cache.put(clz, ext);
    }

//    static long totalTime = 0;

//    public static void main(String[] args) {
//
//
//        for (int i = 0; i < 10000000; i++) {
//            long start = System.nanoTime();
//
//            Market market = new Market();
//            market.setId(12123L);
//            market.setLast(new BigDecimal(12312));
//            market.setLow(new BigDecimal(123123));
//            market.setTimestamp(1515955689000L);
//            market.setName("eth");
//
////            Point p = Point.measurement("market")
////                    .tag("id",market.getId().toString())
////                    .tag("name",market.getName())
////                    .addField("low", market.getLast())
////                    .addField("last",market.getLast())
////                    .time(market.getTimestamp(), TimeUnit.MILLISECONDS)
////                    .build();
//
//            log.debug(lineProtocal(market));
////            System.out.println("\n"+p.lineProtocol());
//
//            totalTime += System.nanoTime() - start;
//            log.debug("--- {}", totalTime / 1000 / 1000);
//
//        }
//
//    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Measurement {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Tag {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Time {
        TimeUnit value() default TimeUnit.MILLISECONDS;
    }

}
