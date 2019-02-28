package com.al.dbspider.newExchange;

import com.al.bcoin.WSListener;
import com.al.dbspider.config.SpiderThreadFactory;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.utils.InfluxDbMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * NOTE:
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author mr.wang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/25 10:58
 */
@Slf4j
@Data
public abstract class BaseDP {


    protected ExecutorService pool = Executors.newCachedThreadPool(SpiderThreadFactory.create(getClass().getSimpleName() + "-p"));

    //SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 1080);
    //Proxy proxy = new Proxy(Proxy.Type.HTTP, socketAddress);

    @Resource
    InfluxDbMapper influxDbMapper;

    @Autowired
    JedisPool jedisPool;
    int tidExpireTime = 60 * 60;//default

    AtomicLong tstail = new AtomicLong(0);

    long millToNano(long ts) {
        if (tstail.longValue() == 1000000) {
            tstail.set(0);
        }
        ts = ts * 1000000 + (tstail.getAndIncrement());
        return ts;
    }


    public String cacheTid(String key, String value) {
        Jedis jedis = jedisPool.getResource();
        jedis.clientSetname(key);
        String ok = null;
        try {
            ok = jedis.set(key, value, "NX", "EX", tidExpireTime);
        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + "redis 异常" + e.getMessage());
        } finally {
            jedis.close();
        }
        //todo 未处理异常情况
        return ok;
    }

    //TODO redis pool 监控,外置方法无效,暂放这里看效果
    @Scheduled(cron = "0/1 * * * * ?")
    @Async
    public void redisPoolMonitor() {
        log.info("{} jedis pool actives {} , idles {} , waiters {}", getClass().getSimpleName(), jedisPool.getNumActive(), jedisPool.getNumIdle(), jedisPool.getNumWaiters());
    }

    public void start() {
        log.warn("{} start websocket...", this.getClass().getSimpleName());
        onSubscribe();
    }

    public void onMessage(WSListener.Info info) {
        pool.submit(new MessageHandler(this, info));
    }

    protected abstract void onSubscribe();

    /**
     * 在线程中处理message，避免message过多时阻塞websocket线程
     *
     * @param info
     */
    protected List<OnlyKey> onMessageInPool(WSListener.Info info) {
        List<OnlyKey> infoForModel = getInfoForModel(info);
        log.info("{}解析后的数据,{}", this.getClass().getName(), infoForModel);
        return infoForModel;
    }

    protected abstract List<OnlyKey> getInfoForModel(WSListener.Info info);

    private class MessageHandler implements Runnable {
        private final WSListener.Info info;
        private final BaseDP client;

        private MessageHandler(BaseDP client, WSListener.Info info) {
            this.client = client;
            this.info = info;
        }

        @Override
        public void run() {
            List<OnlyKey> onlyKeys = client.onMessageInPool(info);
            try {
                if (onlyKeys != null && onlyKeys.size() > 0) {
                    influxDbMapper.postData(onlyKeys);
                }
            } catch (Throwable e) {
                log.error("发送到Kafka出错", e);
            }
        }
    }

}
