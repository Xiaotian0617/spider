package com.al.dbspider.config;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.control.MessageCounter;
import com.al.dbspider.dao.domain.OnlyKey;

import java.util.List;

public interface ExchangeConnection {
    boolean start();

    ExchangeConstant getExchangeName();

    void postData(List<OnlyKey> onlyKeys);

    MessageCounter getMessageCounter();
}
