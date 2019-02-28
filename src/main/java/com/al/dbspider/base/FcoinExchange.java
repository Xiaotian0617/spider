package com.al.dbspider.base;

import com.al.dbspider.base.api.Fcoin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018-07-26
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "rest.fcoin")
public class FcoinExchange extends BaseRest {

    @Autowired
    private Fcoin fcoin;


    @Override
    protected void init() {

    }

    @Override
    protected void onSchedule(ScheduledExecutorService schedule) {
    }

    @Override
    protected void onStart() {

    }


    @Override
    public ExchangeConstant getExchangeName() {
        return ExchangeConstant.Fcoin;
    }
}
