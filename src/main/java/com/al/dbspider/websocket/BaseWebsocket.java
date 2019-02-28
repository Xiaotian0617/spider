package com.al.dbspider.websocket;

import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.config.ExchangeConnection;
import com.al.dbspider.config.SendKafkaException;
import com.al.dbspider.control.MessageCounter;
import com.al.dbspider.dao.domain.*;
import com.al.dbspider.monitor.DataType;
import com.al.dbspider.monitor.ErrorMonitor;
import com.al.dbspider.utils.DateUtils;
import com.al.dbspider.utils.InfluxDbMapper;
import com.al.dbspider.utils.redis.ObjectRedisService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Data
public abstract class BaseWebsocket extends WebSocketListener implements ExchangeConnection {
    protected String exchangeName;
    ErrorMonitor errorMonitor;
    //
    List<OnlyKeyMessage> cachedRetryMessages = new ArrayList<>();
    MessageCounter messageCounter;

    private Map<String, List<BuyAndSellFirstPO>> hundredBuyAndSellFirstMap = new ConcurrentHashMap<>();

    public Map<String, List<BuyAndSellFirstPO>> getHundredBuyAndSellFirstMap() {
        return hundredBuyAndSellFirstMap;
    }

    @Autowired
    ObjectRedisService objectRedisService;

    /**
     * 是否订阅kline
     */
    protected boolean kline = true;
    /**
     * 是否订阅market
     */
    protected boolean market = true;
    /**
     * 是否订阅trade
     */
    protected boolean trade = true;
    /**
     * 是否订阅depth
     */
    protected boolean depth = true;
    /**
     * 是否订阅index（指数）
     */
    protected boolean index = true;

    protected String url;
    private boolean disable;

    @Value("${spring.application.cacheTid.disable:false}")
    private boolean needCacheTid;

    int tidExpireTime = 60 * 60;//default
    int threads;
    @Autowired
    OkHttpClient okHttpClient;
    @Autowired
    JedisPool jedisPool;

    @Autowired
    InfluxDbMapper influxDbMapper;
    AtomicLong tstail = new AtomicLong(0);
    WebSocket webSocket;
    //防止多连接
    boolean started;

    boolean manualClose = false;

    protected long millToNano(long ts) {
        if (tstail.longValue() == 1000000) {
            tstail.set(0);
        }
        ts = ts * 1000000 + (tstail.getAndIncrement());
        return ts;
    }

    public static ScheduledExecutorService reconnectScheduler = Executors.newSingleThreadScheduledExecutor();

    protected ScheduledThreadPoolExecutor scheduler;

    protected ThreadPoolExecutor pool;


    @PostConstruct
    public void init() {
        if (threads == 0) {
            threads = 8;
        } else {
            log.info("{} thread pool 使用数量配置 {}", this.exchangeName, threads);
        }
        pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads, SpiderThreadFactory.create(this.exchangeName + "-p"));
    }

    public BaseWebsocket() {
        scheduler = new ScheduledThreadPoolExecutor(1, SpiderThreadFactory.create(this.exchangeName + "s"));
        this.exchangeName = this.getClass().getSimpleName();
        this.errorMonitor = new ErrorMonitor(this.exchangeName, 10);
    }


    public BaseWebsocket setCorePoolSize(int corePoolSize) {
        scheduler.setCorePoolSize(corePoolSize);
        return this;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        log.info("{} open websocket...", this.exchangeName);
    }

    public void send(String text) {
        webSocket.send(text);
    }

    public abstract void subscribe();

//    protected void onReconnect() {
////        reconnectScheduler.schedule(new ReconnectTask(this.getClass()), 10, TimeUnit.SECONDS);
////    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        log.warn("{} 正在连接断开,code {} reason {}", this.exchangeName, code, reason);
//        if (scheduler != null) {
//            scheduler.shutdown();
//        }
//        if (pool != null) {
//            pool.shutdown();
//        }

    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        log.warn("{} 连接断开,code {} reason {}", this.exchangeName, code, reason);
        synchronized (this) {
            this.started = false;
        }
        if (code == 1000 || manualClose) {
            return;
        }
        log.warn("{} websocket closed , reconnect... >> {}", this.exchangeName, reason);
        start();
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        synchronized (this) {
            this.started = false;
        }
        log.error(this.exchangeName + " websocket failure: " + t.getMessage() + ",code: " + (response != null ? response.code() : 0) + ",message:" + (response != null ? response.message() : null), t);
        if (manualClose) {
            log.info("手动关闭 {} 服务", this.exchangeName);
            return;
        }
        start();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        pool.submit(new MessageHandler(this, text));
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        pool.submit(new MessageHandler(this, bytes));
    }

    /**
     * 在线程中处理message，避免message过多时阻塞websocket线程
     * onMessage和onMessageInPool 方法至少要重写一个
     */
    protected List<OnlyKey> onMessageInPool(String message) {
        log.warn("{} 未实现 string on message 方法", this.exchangeName);
        return null;
    }

    protected List<OnlyKey> onMessageInPool(ByteString message) {
        log.warn("{} 未实现 byte on message 方法", this.exchangeName);
        return null;
        //        throw new IllegalStateException("onMessage need override super");
    }

    public boolean close(int code, String reason) {
        boolean closeResult = webSocket.close(code, reason);
        if (closeResult) {
            this.manualClose = true;
        }
        return closeResult;
    }

    public boolean isSubscribe(DataType dataType) {
        if (dataType == DataType.KLINE) {
            return kline;
        }
        if (dataType == DataType.TRADE) {
            return trade;
        }
        if (dataType == DataType.MARKET) {
            return market;
        }
        if (dataType == DataType.DEPTH) {
            return depth;
        }
        return false;
    }

    class MessageHandler implements Runnable {
        private final Object message;
        private final BaseWebsocket client;

        protected MessageHandler(BaseWebsocket client, Object message) {
            this.client = client;
            this.message = message;
        }

        @Override
        public void run() {
            List<OnlyKey> onlyKeys = null;
            try {
                if (message instanceof String) {
                    onlyKeys = client.onMessageInPool((String) message);
                } else {
                    onlyKeys = client.onMessageInPool((ByteString) message);
                }
            } catch (Throwable e) {
                log.error(exchangeName + "处理消息出错:" + message, e);
//                errorMonitor.add(new MessageParseErrorMessage(exchangeName, e));
            }
            try {
                if (onlyKeys != null && onlyKeys.size() > 0) {
                    postData(onlyKeys);
                }
            } catch (SendKafkaException e) {
//                cachedRetryMessages.add((OnlyKeyMessage) onlyKeys);
//                ErrorMessage errorMessage = new KafkaSendErrorMessage(exchangeName, (OnlyKeyMessage) onlyKeys, e);
//                errorMonitor.add(errorMessage);
            } catch (Exception e) {
                log.error(exchangeName + "发送到Kafka出错", e);
            }

        }
    }

    public void getBuyAndSellFirst(List<DepthDTO> depthDTOS, int bidFirst, int askFirst) {
        if (depthDTOS.size() == 0) {
            return;
        }
        List<OnlyKey> onlyKeys = new ArrayList<>();
        List<OnlyKey> buyAndSellFirstPOS = new ArrayList<>();
        for (DepthDTO depthDTO : depthDTOS) {
            //买一
            BuyAndSellFirstPO buyFirstPO = new BuyAndSellFirstPO();
            List<DepthDTO.PriceLevel> bids = depthDTO.getTick().getBids();
            if (bids.size() != 0) {
                buyFirstPO.setOnlyKey(depthDTO.getOnlyKey());
                buyFirstPO.setPrice(bids.get(bidFirst).getPrice());
                buyFirstPO.setSide("buy");
                buyFirstPO.setTime(depthDTO.getTimestamp());
            }

            //卖一
            BuyAndSellFirstPO sellFirstPO = new BuyAndSellFirstPO();
            List<DepthDTO.PriceLevel> asks = depthDTO.getTick().getAsks();
            if (asks.size() != 0) {
                sellFirstPO.setOnlyKey(depthDTO.getOnlyKey());
                sellFirstPO.setPrice(asks.get(askFirst).getPrice());
                sellFirstPO.setSide("sell");
                sellFirstPO.setTime(depthDTO.getTimestamp());
            }

            if (buyFirstPO.getOnlyKey() == null || sellFirstPO.getOnlyKey() == null) {
                return;
            }

            List<BuyAndSellFirstPO> buyFirstPOList = getHundredBuyAndSellFirstMap().get(depthDTO.getOnlyKey() + "_buy");
            List<BuyAndSellFirstPO> sellFirstPOList = getHundredBuyAndSellFirstMap().get(depthDTO.getOnlyKey() + "_sell");
            if (buyFirstPOList == null) {
                //因为买一卖一是成对的，只需要判断买即可
                List<BuyAndSellFirstPO> buyFirstPOS1 = new ArrayList<>();
                buyFirstPOS1.add(buyFirstPO);
                List<BuyAndSellFirstPO> sellFirstPOS1 = new ArrayList<>();
                sellFirstPOS1.add(sellFirstPO);
                getHundredBuyAndSellFirstMap().put(depthDTO.getOnlyKey() + "_buy", buyFirstPOS1);
                getHundredBuyAndSellFirstMap().put(depthDTO.getOnlyKey() + "_sell", sellFirstPOS1);
            } else {
                buyFirstPOList.add(buyFirstPO);
                sellFirstPOList.add(sellFirstPO);
                //无需排序，一定是时间正序的
                /*buyFirstPOList.stream().sorted(Comparator.comparingLong(BuyAndSellFirstPO::getTime)).
                        collect(Collectors.toList());
                sellFirstPOList.stream().sorted(Comparator.comparingLong(BuyAndSellFirstPO::getTime)).
                        collect(Collectors.toList());*/
                if (buyFirstPOList.size() > 10) {
                    //因为买一卖一是成对的，只需要判断买即可,超过100条，删除时间最早的
                    buyFirstPOList.remove(0);
                    sellFirstPOList.remove(0);
                }
            }
            buyAndSellFirstPOS.add(buyFirstPO);
            buyAndSellFirstPOS.add(sellFirstPO);
        }
        influxDbMapper.postData(buyAndSellFirstPOS);
    }

    public void realTradeList(List<Trade> trades, ExchangeConstant exchangeName, Map<String, List<BuyAndSellFirstPO>> hundredBuyAndSellFirstMap) {
        List<RealTrade> tradePOList = new ArrayList<>();
        for (Trade trade : trades) {
            RealTrade temp = new RealTrade(exchangeName, trade.getSymbol(), trade.getUnit());
            BeanUtils.copyProperties(trade, temp);
            BigDecimal price = temp.getPrice();
            if (!hundredBuyAndSellFirstMap.containsKey(temp.getOnlyKey() + "_buy")) {
                log.info("{}的深度信息还未就绪", temp.getOnlyKey());
            } else {
                Object depth = objectRedisService.getHashModel("depth", temp.getOnlyKey());
                if (depth == null) {
                    log.warn("{} 的 {} 在Redis中未找到，已跳过", exchangeName.name(), temp.getOnlyKey());
                    return;
                }
                Long preTime = temp.getTimestamp() / 1000000L
                        - Long.valueOf(depth.toString());
                List<BuyAndSellFirstPO> buyFirstPOList = hundredBuyAndSellFirstMap.get(temp.getOnlyKey() + "_buy");
                List<BuyAndSellFirstPO> sellFirstPOList = hundredBuyAndSellFirstMap.get(temp.getOnlyKey() + "_sell");
                List<BuyAndSellFirstPO> newBuyFirstPOList = buyFirstPOList.stream()
                        .filter(buyAndSellFirstPO -> buyAndSellFirstPO.getTime() <= preTime)
                        .sorted(Comparator.comparingLong(BuyAndSellFirstPO::getTime))
                        .collect(Collectors.toList());
                List<BuyAndSellFirstPO> newSellFirstPOList = sellFirstPOList.stream()
                        .filter(buyAndSellFirstPO -> buyAndSellFirstPO.getTime() <= preTime)
                        .sorted(Comparator.comparingLong(BuyAndSellFirstPO::getTime))
                        .collect(Collectors.toList());
                BigDecimal firstBuy = buyFirstPOList.get(buyFirstPOList.size() - 1).getPrice();//当前的买一卖一
                BigDecimal firstSell = sellFirstPOList.get(sellFirstPOList.size() - 1).getPrice();
                BigDecimal preFirstBuy;//往前一定时间间隔的买一卖一
                BigDecimal preFirstSell;
                if (newBuyFirstPOList.size() == 0) {//如果往前一定时间间隔内没有买一卖一则取最早的一对
                    preFirstBuy = buyFirstPOList.get(0).getPrice();
                    preFirstSell = sellFirstPOList.get(0).getPrice();
                } else {
                    preFirstBuy = newBuyFirstPOList.get(0).getPrice();
                    preFirstSell = newSellFirstPOList.get(0).getPrice();
                }
                boolean real1 = false;
                if ((price.compareTo(firstBuy) == 0 || price.compareTo(firstSell) == 0)
                        && (price.compareTo(preFirstBuy) <= 0 || price.compareTo(preFirstSell) >= 0)) {
                    real1 = true;
                }
                boolean real = (price.compareTo(firstBuy) < 0 || price.compareTo(firstSell) > 0 || real1);
                if (real) {
                    temp.setMeasurement("real_trade");
                    tradePOList.add(temp);
                }
            }
        }
        if (tradePOList.size() > 0) {
            //如果真实交易不为0，则发送至kafka真实交易频道
            influxDbMapper.postData(tradePOList);
        }
    }

    public DepthDTO.PriceLevel getPriceLevel(Object obj, Boolean isSpot) {
        return null;
    }

    public DepthDTO.PriceLevel getPriceLevel(Object obj, int priceNum, int countNum) {
        JSONArray array = JSON.parseArray(obj.toString());
        DepthDTO.PriceLevel priceLevel = new DepthDTO.PriceLevel();
        priceLevel.setCount(array.getBigDecimal(countNum));
        priceLevel.setPrice(array.getBigDecimal(priceNum));
        return priceLevel;
    }

    @Override
    public boolean start() {
        synchronized (this) {
            if (isStarted()) {
                log.warn(this.exchangeName + "已启动");
                return true;
            }
        }
        try {
            log.info("启动 {}", this.exchangeName);
            if (url == null) {
                throw new RuntimeException(this.exchangeName + " URL 为 空");
            }
            open();
            synchronized (this) {
                this.started = true;
                this.manualClose = false;
            }
        } catch (Throwable e) {
            log.error("启动失败" + e.getMessage(), e);
            return false;
        }
        try {
            subscribe();
        } catch (Exception e) {
            log.error(this.exchangeName + "订阅出错" + e.getMessage(), e);
        }
        startPing();
        return true;
    }

    protected void open() {
        Request req = new Request.Builder().url(url).build();
        this.webSocket = okHttpClient.newWebSocket(req, this);
    }

    private void startPing() {
        if (isNeedPing()) {
            scheduler.scheduleWithFixedDelay(() -> {
                log.debug("{} ping", this.exchangeName);
                try {
                    ping();
                } catch (Exception e) {
                    log.error(this.exchangeName + "发送 ping 出错" + e.getMessage(), e);
                }
            }, 5, 10, TimeUnit.SECONDS);
        }
    }

    public void ping() {

    }

    protected boolean isNeedPing() {
        return false;
    }

    //TODO 在发送前判断,需要重构发送逻辑
    public String cacheTid(String key, String value) {
        if (needCacheTid) {
            return "ok";
        }
        Jedis jedis = jedisPool.getResource();
        jedis.clientSetname(key);
        String ok = null;
        try {
            ok = jedis.set(key, value, "NX", "EX", tidExpireTime);
        } catch (Exception e) {
            log.error(getExchangeName() + "redis 异常" + e.getMessage());
        } finally {
            jedis.close();
        }
        //todo 未处理异常情况
        return ok;
    }

    public abstract ExchangeConstant getExchangeName();

    @Override
    public void postData(List<OnlyKey> onlyKeys) {
        influxDbMapper.postData(onlyKeys);
        messageCounter.count(onlyKeys);
    }

    @Scheduled(cron = "0/2 * * * * ?")
    public void websocketQueueSize() {
        if (webSocket != null) {
            log.info("{} WebSocket queue size {}", this.exchangeName, webSocket.queueSize());
        }

        if (pool != null) {
            log.info("{} Thread Monitor {}", this.exchangeName, pool.toString());
        }
    }

    protected Date coverStringFormatTime(String timestamp) {
        Date date;
        try {
            date = DateUtils.convertStringToDate(timestamp, DateUtils.FULL_TIME_FORMAT_UTC);
        } catch (ParseException e) {
            log.error("解析时间出错！",e);
            return null;
        }
        return DateUtils.addMinute(date, 60 * 8);
    }

}


//重连任务
//class ReconnectTask implements Runnable {
//    private final Class<? extends BaseWebsocket> clz;
//
//    public ReconnectTask(Class<? extends BaseWebsocket> clz) {
//        this.clz = clz;
//    }
//
//    @Override
//    public void run() {
//        AppContext.getBean(clz).start();
//    }
//}

/**
 * named spdier thread factory
 */
class SpiderThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    SpiderThreadFactory(String name) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = name + "-" +
                poolNumber.getAndIncrement() +
                "-t-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

    static ThreadFactory create(String name) {
        return new SpiderThreadFactory(name);
    }

}
