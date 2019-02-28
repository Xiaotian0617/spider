package com.al.dbspider.config;

import com.al.dbspider.base.BaseRest;
import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.control.MessageCounter;
import com.al.dbspider.monitor.ExchangeWebsocketManager;
import com.al.dbspider.websocket.BaseWebsocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 15/01/2018 19:28
 */
@Component
@Slf4j
public class StartWebsocketSpiderListener implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * disable 后不创建 component
     */
    @Autowired(required = false)
    Map<ExchangeConstant, List<BaseWebsocket>> websockets;

    /**
     * disable 后也创建 component
     */
    @Autowired(required = false)
    @Lazy
    Map<ExchangeConstant, BaseRest> exchanges;

    @Autowired
    ExchangeWebsocketManager exchangeWebsocketManager;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (websockets != null) {
            websockets.entrySet().stream().forEach(exchangeConstantListEntry -> {
                MessageCounter messageCounter = new MessageCounter();
                BaseRest baseRest = exchanges.get(exchangeConstantListEntry.getKey());
                if (baseRest != null) {
                    baseRest.setMessageCounter(messageCounter);
                }
                exchangeConstantListEntry.getValue().forEach(baseWebsocket -> {
                    baseWebsocket.setMessageCounter(messageCounter);
                    baseWebsocket.start();
                });
            });
        }
        if (exchanges != null) {
            exchanges.values().parallelStream().forEach(baseRest -> {
                if (baseRest.getMessageCounter() == null) {
                    MessageCounter messageCounter = new MessageCounter();
                    baseRest.setMessageCounter(messageCounter);
                }
                baseRest.start();
            });
        }
        exchangeWebsocketManager.init();
    }
}
