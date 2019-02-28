package com.al.dbspider.config;

import com.al.dbspider.utils.OperationFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018 2018/10/22 14:17
 */
@Component
public class BinanceConfig {

    @Autowired
    OperationFileUtils operationFileUtils;

    @Value(value = "${file-path.binance}")
    private String filePath;

    //币安默认币种
    public static String binanceSymbols = "";

    @PostConstruct
    void init(){
        binanceSymbols = operationFileUtils.readFile(filePath);
    }

}
