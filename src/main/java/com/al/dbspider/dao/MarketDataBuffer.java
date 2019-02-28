package com.al.dbspider.dao;

import com.al.dbspider.dao.domain.KLine;
import com.al.dbspider.dao.domain.Market;
import com.al.dbspider.dao.domain.Trade;
import com.google.common.collect.Queues;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 由于 websocket onmessage 是单线程,用作接收数据
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 14/01/2018 18:46
 */
@Service
public class MarketDataBuffer implements DataBuffer {
    private ConcurrentLinkedQueue<Market> markets = Queues.newConcurrentLinkedQueue();
    private ConcurrentLinkedQueue<KLine> kLines = Queues.newConcurrentLinkedQueue();
    private ConcurrentLinkedQueue<Trade> trades = Queues.newConcurrentLinkedQueue();

    @Override
    public void putMarket(Market market) {
        markets.offer(market);
    }

    @Override
    public void putKline(KLine kLine) {
        kLines.offer(kLine);
    }

    @Override
    public void putTrade(Trade trade) {
        trades.offer(trade);
    }

    @Override
    public Market getMarket() {
        return markets.poll();
    }

    @Override
    public KLine getKline() {
        return kLines.poll();
    }

    @Override
    public Trade getTrade() {
        return trades.poll();
    }
}
