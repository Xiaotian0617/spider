package com.al.dbspider.config;

import org.assertj.core.util.Lists;

import java.util.List;

/**
 * Kline基础配置
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 13/01/2018 20:54
 */
public class KlineConfig {
    //private static List<String> PERIODS = Lists.newArrayList("1min","5min","15min", "30min", "60min", "1day", "1mon", "1week", "1year");
    private static List<String> PERIODS = Lists.newArrayList("1min", "1day");

    public static List<String> getPeriod() {
        return PERIODS;
    }
}
