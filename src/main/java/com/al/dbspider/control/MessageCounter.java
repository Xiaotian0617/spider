package com.al.dbspider.control;

import com.al.dbspider.dao.domain.*;
import com.al.dbspider.monitor.DataType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/27 21:20
 */
public class MessageCounter {
    Map<DataType, Counter> counters = new HashMap<>();

    public MessageCounter() {
        counters.put(DataType.TRADE, new Counter(DataType.TRADE.toString()));
        counters.put(DataType.MARKET, new Counter(DataType.MARKET.toString()));
        counters.put(DataType.KLINE, new Counter(DataType.KLINE.toString()));
        counters.put(DataType.DEPTH, new Counter(DataType.DEPTH.toString()));
    }


    public void count(List<OnlyKey> onlyKeys) {
        OnlyKey onlyKey = onlyKeys.get(0);
        int size = onlyKeys.size();
        if (onlyKey instanceof Trade) {
            counters.get(DataType.TRADE).count(size);
            return;
        }
        if (onlyKey instanceof KLine) {
            counters.get(DataType.KLINE).count(size);
            return;
        }
        if (onlyKey instanceof Market) {
            counters.get(DataType.MARKET).count(size);
            return;
        }
        if (onlyKey instanceof Depth) {
            counters.get(DataType.DEPTH).count(size);
        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        counters.values().forEach(sb::append);
        return sb.toString();
    }

    /**
     * 整体最后更新时间
     *
     * @return
     */
    public Long getLastTime() {
        OptionalLong max = counters.values().stream().mapToLong(Counter::getLastTime).max();
        return max.isPresent() ? max.getAsLong() : 0;
    }

    public Long getLastTime(DataType dataType) {
        return counters.get(dataType).getLastTime();
    }

    /**
     * 分类型的最后更新时间
     *
     * @param type
     * @return
     */
    public Long getLastTime(String type) {
        return counters.get(type).lastTime;
    }

    public Long total() {
        return counters.values().stream().mapToLong(Counter::get).sum();
    }

    public Long aggTotal() {
        return counters.values().stream().mapToLong(Counter::getAgg).sum();
    }

    public Counter getCounter(DataType type) {
        return counters.get(type);
    }

    public void set(DataType type, Counter counter) {
        counters.put(type, counter);
    }

    public static class Counter {
        private final String name;
        AtomicLong counter = new AtomicLong(0);
        AtomicLong aggCounter = new AtomicLong(0);
        long lastTime = 0;

        public Counter(String name) {
            this.name = name;
        }

        public void count(int size) {
            counter.addAndGet(size);
            aggCounter.addAndGet(1);
            lastTime = System.currentTimeMillis();
        }

        private String scale(AtomicLong agg, AtomicLong split) {
            return agg.get() == 0 ? "0" : new BigDecimal(split.get()).divide(new BigDecimal(agg.get()), 2, RoundingMode.DOWN).toString();
        }

        @Override
        public String toString() {
            return name + " " + counter + " , " + aggCounter + " , " + scale(aggCounter, counter) + " , " + LocalDateTime.ofEpochSecond(lastTime / 1000, 0, ZoneOffset.ofHours(8)) + ";\n";
        }

        public Long get() {
            return counter.get();
        }

        public Long getAgg() {
            return aggCounter.get();
        }

        public void add(Long value) {
            counter.addAndGet(value);
        }

        public void addAgg(Long value) {
            counter.addAndGet(value);
        }

        public long getLastTime() {
            return lastTime;
        }

        public void setLastTime(long lastTime) {
            this.lastTime = lastTime;
        }
    }

}
