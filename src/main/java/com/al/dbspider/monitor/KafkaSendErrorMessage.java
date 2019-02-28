package com.al.dbspider.monitor;

import com.al.dbspider.websocket.OnlyKeyMessage;

/**
 * 发送 kafka 错误
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/30 16:36
 */
public class KafkaSendErrorMessage extends ErrorMessage {

    private OnlyKeyMessage onlykeyMessage;

    public KafkaSendErrorMessage(String name, OnlyKeyMessage message, Throwable t) {
        super(name, t);
        this.onlykeyMessage = message;
    }

    @Override
    public String getSummary() {
        return name + "发送Kafka 出错, message = " + onlykeyMessage;
    }

    @Override
    public String getContent() {
        return null;
    }
}
