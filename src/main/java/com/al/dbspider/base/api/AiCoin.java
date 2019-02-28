package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface AiCoin {

    String[] tabs = {"btc", "bch", "eth", "bigone", "okex", "binance", "zb", "gate", "bitfinex", "bitflyer", "huobipro", "bibox", "bitstamp", "coinegg", "rightbtc", "poloniex", "bittrex", "bitmex", "liqui", "kraken", "aex", "others"};

    /**
     * https://www.aicoin.net.cn/api/mobile/home?column=default&tab=bigone
     * 手机版主页接口
     *
     * @return
     */
    @GET("https://www.aicoin.net.cn/api/mobile/home?column=default")
    @Headers({
            "User-Agent: Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Mobile Safari/537.36",
            "Accept-Language: en-US;q=0.8,en;q=0.7",
            "Referer: https://www.aicoin.net.cn/amp/mobile"
    })
    Call<String> pairs(@Query("tab") String tab);

    /**
     * https://www.aicoin.net.cn/api/mobile/home?column=default&tab=bigone
     * 手机版主页接口
     *
     * @return
     */
    @GET("https://www.aicoin.net.cn/api/mobile/home?column=default&tickerOnly=1")
    @Headers({
            "User-Agent: Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Mobile Safari/537.36",
            "Accept-Language: en-US;q=0.8,en;q=0.7",
            "Referer:https://www.aicoin.net.cn/amp/mobile"
    })
    Call<String> ticker(@Query("tab") String tab);

    /**
     * https://widget.aicoin.net.cn/chart/api/data/period?symbol=okcoinfuturesbtcquarterusd&step=900
     *
     * @param symbol aicoin币symbol
     * @param second k线类型，s为单位，如1分钟K线就传60
     * @return
     */
    @GET("chart/api/data/period")
    @Headers({
            "User-Agent: Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Mobile Safari/537.36",
            "Accept-Language: en-US;q=0.8,en;q=0.7",
            "Referer: https://widget.aicoin.net.cn/chart"
    })
    Call<String> kline(@Query("symbol") String symbol, @Query("step") int second);

    default Call<String> kline(String symbol) {
        return kline(symbol, 60);
    }

    default List<Call<String>> allPairs() {
        return Stream.of(tabs).map(this::pairs).collect(Collectors.toList());
    }

    default List<Call<String>> allTickers() {
        return Stream.of(tabs).map(this::ticker).collect(Collectors.toList());
    }

}
