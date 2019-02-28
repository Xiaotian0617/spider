package com.al.dbspider.utils;

import com.al.dbspider.websocket.OnlyKeyMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.Resource;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 09/03/2018 14:09
 */
@Component
@Slf4j
public class KafkaSender {
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, String payload, String onlykey) {
        log.trace("sending payload='{}' to topic='{}'", payload, topic);
        doSend(topic, payload, onlykey);
    }

    private void doSend(String topic, String payload, String onlykey) {
        try {
            ListenableFuture<SendResult<String, String>> send = kafkaTemplate.send(topic, onlykey, payload);
            send.addCallback(result -> {
                log.debug("kafka send success topic {} offset {}", result.getRecordMetadata().topic(), result.getRecordMetadata().offset());
            }, ex -> {
                log.error(String.format("kafka send error topic %s msg %s", topic, ex.getMessage()));
            });
        } catch (Throwable t) {
            log.error("发送消息异常:" + t.getMessage(), t);
        }
    }

    public void send(OnlyKeyMessage onlyKeyMessage) {
        doSend(onlyKeyMessage.getTopic(), onlyKeyMessage.getOnlykey(), onlyKeyMessage.toString());
    }
}
