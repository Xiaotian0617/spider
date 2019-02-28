package com.al.dbspider.monitor;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.control.ExchangeStatus;
import com.al.dbspider.control.MessageCounter;
import com.al.dbspider.websocket.BaseWebsocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/8/1 13:51
 */
@Component
public class ExchangeWebsocketManager {
    @Value("${monitor.data-status-interval}")
    int dataStatusInterval;

    @Autowired
    List<BaseWebsocket> exchangeConnections;

    Map<ExchangeConstant, ExchangeStatus> exchangeStatusMap = new HashMap<>();
    Map<ExchangeConstant, MessageCounter> messageCounters = new HashMap<>();

    @Autowired
    Map<ExchangeConstant, List<BaseWebsocket>> baseWebsockets = new HashMap<>();


    public void init() {
        exchangeConnections.forEach(exchangeConnection -> {
            MessageCounter messageCounter = exchangeConnection.getMessageCounter();
            ExchangeConstant exchangeName = exchangeConnection.getExchangeName();
            messageCounters.putIfAbsent(exchangeName, messageCounter);
            exchangeStatusMap.putIfAbsent(exchangeName, new ExchangeStatus(exchangeName, dataStatusInterval));
        });
    }

    private void mergerKlineCounter(MessageCounter target, MessageCounter from) {
        MessageCounter.Counter counterFrom = from.getCounter(DataType.KLINE);
        MessageCounter.Counter counterTarget = target.getCounter(DataType.KLINE);
        counterTarget.add(counterFrom.get());
        counterTarget.addAgg(counterFrom.getAgg());
    }

    public Map<ExchangeConstant, MessageCounter> messageCounters() {
        MessageCounter messageCounter = messageCounters.get(ExchangeConstant.Binance);
        baseWebsockets.forEach((s, baseWebsockets) -> baseWebsockets.forEach(baseWebsocket -> mergerKlineCounter(messageCounter, baseWebsocket.getMessageCounter())));
        return messageCounters;
    }

    @Scheduled(fixedDelay = 1000, initialDelay = 90000)
    public void refreshStatus() {
        long now = Instant.now().toEpochMilli();
        exchangeStatusMap.forEach((exchangeConstant, exchangeStatus) -> {
            MessageCounter messageCounter = messageCounters.get(exchangeConstant);
            BaseWebsocket exchangeWebsocket = baseWebsockets.get(exchangeConstant).get(0);
            for (DataType dataType : DataType.values()) {
                boolean isSubscribe = exchangeWebsocket.isSubscribe(dataType);
                if (isSubscribe) {
                    if (isDataNormal(now, messageCounter.getLastTime(dataType))) {
                        exchangeStatus.setStatus(dataType, 1, messageCounter.getLastTime(dataType));
                        exchangeStatus.setDescribe(dataType, "数据正常");
                    } else {
                        exchangeStatus.setStatus(dataType, 5, messageCounter.getLastTime(dataType));
                        exchangeStatus.setDescribe(dataType, "数据中断");
                    }
                } else {
                    exchangeStatus.setStatus(dataType, 3, messageCounter.getLastTime(dataType));
                    exchangeStatus.setDescribe(dataType, "未订阅");
                }
            }
        });
    }

    private boolean isDataNormal(long now, Long lastTime) {
        return !(now - lastTime >= dataStatusInterval);
    }

    public Map<ExchangeConstant, ExchangeStatus> getStatus() {
        return exchangeStatusMap;
    }

    public Map<ExchangeConstant, MessageCounter> getDataCounter() {
        return messageCounters;
    }
}
