package com.al.dbspider.dao;

import com.al.dbspider.dao.domain.KLine;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.Trade;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 14/01/2018 18:53
 */
public interface DataBuffer {
    void putMarket(Market market);

    void putKline(KLine kLine);

    void putTrade(Trade trade);

    Market getMarket();

    KLine getKline();

    Trade getTrade();
}
