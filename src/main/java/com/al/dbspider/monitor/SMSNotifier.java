package com.al.dbspider.monitor;

import com.al.dbspider.utils.AiluPaasUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/31 18:13
 */
@Component
public class SMSNotifier implements Notifier {
    @Resource
    AiluPaasUtil ailuPaasUtil;

    @Override
    public void notify(Message message) {
        ailuPaasUtil.sendErrorMsgToPhone(message.getSummary(), message.getContent());
    }
}
