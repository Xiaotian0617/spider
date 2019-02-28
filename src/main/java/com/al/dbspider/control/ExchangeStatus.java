package com.al.dbspider.control;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.monitor.DataType;
import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/31 14:54
 */
@Data
public class ExchangeStatus {
    private final int interval;
    private ExchangeConstant name;

    Map<DataType, Status> statusMap = Maps.newHashMap();
    private Long lastTime;

    public ExchangeStatus(ExchangeConstant name, int interval) {
        this.name = name;
        this.interval = interval;
        statusMap.put(DataType.MARKET, new Status());
        statusMap.put(DataType.TRADE, new Status());
        statusMap.put(DataType.KLINE, new Status());
        statusMap.put(DataType.DEPTH, new Status());
    }


    public boolean isNormal() {
        return statusMap.get(DataType.MARKET).isNormal() && statusMap.get(DataType.KLINE).isNormal() && statusMap.get(DataType.TRADE).isNormal() && statusMap.get(DataType.DEPTH).isNormal();
    }

    public void setStatus(DataType dataType, int status, Long lastTime) {
        Status st = statusMap.get(dataType);
        st.setStatus(status);
        st.setLastTime(lastTime);
    }

    public void setDescribe(DataType dataType, String describe) {
        statusMap.get(dataType).setDescribe(describe);
    }

    public void setLastTime(Long lastTime) {
        this.lastTime = lastTime;
    }

    public Long getLastTime() {
        return lastTime;
    }

    @Data
    public static class Status {
        /**
         * <pre>
         * 1 正常
         * 2 出错
         * 3 未订阅
         * 4 未初始化
         * 5 数据中断
         * </pre>
         */
        int status = 4;
        String describe;
        long lastTime;

        public void setStatus(int status) {
            this.status = status;
        }

        public boolean isNormal() {
            return status == 1 || status == 3;
        }

    }
}
