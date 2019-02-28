package com.al.dbspider;

import com.al.bcoin.Bitfinex;
import com.al.bcoin.Bitmex;
import com.al.bcoin.OKEx;
import com.al.dbspider.base.BaseRest;
import com.al.dbspider.base.ExchangeConstant;
import com.al.dbspider.websocket.BaseWebsocket;
import com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.annotation.EnableKafka;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Slf4j
@EnableConfigurationProperties
@EnableKafka
public class SpiderApplication {

    public static void main(String[] args) {

        SpringApplication application = new SpringApplication(SpiderApplication.class);
        application.addListeners(new ApplicationPidFileWriter(new File("spider.pid")));
        application.run(args);
        log.info("Boot Server started.");
    }

    @Bean
    public OkHttpClient.Builder buildOkHttpClientBuilder(@Value("${spring.application.proxy.enable}") boolean proxy, @Value("${spring.application.proxy.type}") String type, @Value("${spring.application.proxy.url}") String url, @Value("${spring.application.proxy.port}") int port) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS).pingInterval(3000, TimeUnit.MILLISECONDS);
        if (proxy) {
            log.info("代理模式已启动！");
            if (Proxy.Type.HTTP.name().equalsIgnoreCase(type)) {
                builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(url, port)));
            } else if (Proxy.Type.SOCKS.name().equalsIgnoreCase(type)) {
                builder.proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(url, port)));
            } else {
                builder.proxy(new Proxy(Proxy.Type.DIRECT, new InetSocketAddress(url, port)));
            }
        }
        return builder;
    }

    @Bean
    public Retrofit.Builder buildRetrofitBuilder(OkHttpClient.Builder builder) {
        return new Retrofit.Builder().client(builder.build());
    }

    @Bean
    public Bitfinex.Api buildBitfinexBuilder(OkHttpClient.Builder builder) {
        return new Retrofit.Builder()
                .baseUrl(Bitfinex.Api.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(new Retrofit2ConverterFactory())
                .client(builder.build())
                .build()
                .create(Bitfinex.Api.class);
    }

    @Bean
    public OKEx.Api buildOKExBuilder(OkHttpClient.Builder builder) {
        return new Retrofit.Builder()
                .baseUrl(OKEx.Api.REST_BASE_RUL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(new Retrofit2ConverterFactory())
                .client(builder.build())
                .build()
                .create(OKEx.Api.class);
    }

    @Bean
    public Bitmex.Api buildBitmexBuilder(OkHttpClient.Builder builder) {
        return new Retrofit.Builder()
                .baseUrl(Bitmex.Api.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(new Retrofit2ConverterFactory())
                .client(builder.build())
                .build()
                .create(Bitmex.Api.class);
    }

    @Bean
    public OkHttpClient buildOkHttpClient(OkHttpClient.Builder builder) {
        return builder.build();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(20);
    }

    @Bean
    public Map<ExchangeConstant, BaseRest> baseRest(Map<String, BaseRest> baseRestMap) {
        HashMap<ExchangeConstant, BaseRest> map = new HashMap<>();
        baseRestMap.forEach((s, baseRest) -> map.put(baseRest.getExchangeName(), baseRest));
        return map;
    }

    @Bean
    public Map<ExchangeConstant, List<BaseWebsocket>> setBaseWebsockets(List<BaseWebsocket> baseWebsockets) {
        Map<ExchangeConstant, List<BaseWebsocket>> baseWebsocketMap = new HashMap<>();
        baseWebsockets.forEach(baseWebsocket -> {
            List<BaseWebsocket> exchangeWebsockets = baseWebsocketMap.get(baseWebsocket.getExchangeName());
            if (exchangeWebsockets == null) {
                baseWebsocketMap.put(baseWebsocket.getExchangeName(), Lists.newArrayList(baseWebsocket));
                return;
            }
            exchangeWebsockets.add(baseWebsocket);
        });
        return baseWebsocketMap;
    }
}

