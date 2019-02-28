package com.al.dbspider.utils;

import com.ailu.paas.AiluPaasNotice;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.DigestException;
import java.util.List;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author HYK
 * @Company 河南艾鹿
 * @Date 2018/1/10 0010 15:18
 */


@Slf4j
@Component
public class AiluPaasUtil {


    @Value("${spring.application.sms.appKey}")
    private String appKey;


    @Value("${spring.application.sms.secret}")
    private String secret;

    @Value("#{'${spring.application.sms.phones}'.split(',')}")
    private List<String> phones;

    private final String ex_content = "您关注的%s数据异常：%s。请及时查看";


    public AiluPaasNotice getAiluPaasNotice() {
        return AiluPaasNotice.getAiluPaas(appKey, secret);
    }

    /**
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return
     * @description 向管理员发送预警
     * @date 2018/1/11
     * @author mr.wang
     * @see
     * @since
     */
    public void sendErrorMsgToPhone(String subject, String content) {
        phones.forEach(phone -> {
            try {
                JSONObject jsonObject = getAiluPaasNotice().smsSendMsg(phone, String.format(ex_content, "蜘蛛项目", content), "2416872");
                log.warn("错误发送信息至管理员,发送内容{},发送结果{}", content, jsonObject.toJSONString());
                Thread.sleep(1000);
            } catch (DigestException e) {
                log.error("send msg to admin error!", e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

}
