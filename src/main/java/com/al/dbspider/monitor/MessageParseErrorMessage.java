package com.al.dbspider.monitor;

/**
 * 消息解析错误
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/30 16:36
 */
public class MessageParseErrorMessage extends ErrorMessage {

    public MessageParseErrorMessage(String name, Throwable t) {
        super(name, t);
    }

    @Override
    public String getSummary() {
        return null;
    }

    @Override
    public String getContent() {
        return null;
    }
}
