package com.al.dbspider.base;

import com.al.dbspider.config.ExchangeConnection;
import com.al.dbspider.control.MessageCounter;
import com.al.dbspider.dao.domain.OnlyKey;
import com.al.dbspider.utils.InfluxDbMapper;
import com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * <pre>
 *
 * 交易所基类
 * 方便使用交易所rest api，不够用的话可以重写扩展
 * 1. 使用okhttp调用，通过 { #get(String)} { #post(String, String)}简单封装的方法调用
 * 2. 使用retrofit方式， 通过{ #build(String, Class)} 生成接口实例
 *
 * {@link #onSchedule(ScheduledExecutorService)} 重载的方法写具体实现
 *
 * </pre>
 */
@Slf4j
@Data
public abstract class BaseRest implements ExchangeConnection {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private ScheduledExecutorService schedule;

    @Autowired
    protected InfluxDbMapper influxDbMapper;

    //properties for rest.*.disable
    private boolean disable;

    boolean isStarted;

    private String name;
    protected MessageCounter messageCounter;

    @PostConstruct
    public void constructor() {
        name = this.getClass().getSimpleName();
    }

    /**
     * 勿执行 IO 操作
     */
    protected abstract void init();

    protected okhttp3.Response execute(Request request) throws IOException {
        try (okhttp3.Response response = okHttpClient.newCall(request).execute()) {
            return response;
        }
    }

    //simple get url
    protected String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return execute(request).body().string();
    }

    //simple post data
    protected String post(String url, String content) throws IOException {
        RequestBody body = RequestBody.create(JSON, content);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return execute(request).body().string();
    }

    /**
     * simple create retrofit cla
     *
     * @param baseUrl
     * @param tClass
     * @param <T>
     * @return
     */
    protected <T> T build(String baseUrl, Class<T> tClass) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(new CustomConvertFactory())
                .addConverterFactory(new Retrofit2ConverterFactory())
                .client(okHttpClient)
                .build()
                .create(tClass);
    }

    protected abstract void onSchedule(ScheduledExecutorService schedule);

    public boolean start() {
        try {
            if (disable) {
                return false;
            }
            log.info("启动 {}", name);
            if (!isStarted) {
                init();
                onStart();
                onSchedule(schedule);
            }
            isStarted = true;
        } catch (Throwable t) {
            log.error(name + "启动失败 :" + t.getMessage(), t);
        }
        return isStarted;
    }

    @Override
    public void postData(List<OnlyKey> onlyKeys) {
        if (onlyKeys != null && onlyKeys.size() > 0) {
            influxDbMapper.postData(onlyKeys);
            messageCounter.count(onlyKeys);
        }
    }

    protected void onStart() {
        //由子类实现
    }

    public <T> Response<T> execute(Call<T> call) {
        Response<T> response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            log.error(String.format("%s 请求失败!url = %s  ,msg = %s", name, call.request().url(), e.getMessage()), e);
        }
        return response;
    }

    public void setMessageCounter(MessageCounter messageCounter) {
        this.messageCounter = messageCounter;
    }

    public MessageCounter getMessageCounter() {
        return messageCounter;
    }

    @Value("${spring.application.cacheTid.disable:false}")
    private boolean needCacheTid;

    @Autowired
    JedisPool jedisPool;
    int tidExpireTime = 60 * 60;//default

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
}
