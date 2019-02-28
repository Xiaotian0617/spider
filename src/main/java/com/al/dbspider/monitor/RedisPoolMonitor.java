package com.al.dbspider.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/23 16:26
 */
@Component
@Slf4j
public class RedisPoolMonitor {
    @Autowired
    JedisPool jedisPool;

    @Scheduled(cron = "0/1 * * * * ?")
    @Async
    public void start() {
        log.info("jedis pool actives {} , idles {} , waiters {}", jedisPool.getNumActive(), jedisPool.getNumIdle(), jedisPool.getNumWaiters());
    }
}
