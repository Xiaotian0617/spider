package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;

public interface KuCoin {

    @GET("https://kitchen-6.kucoin.com/v1/market/open/symbols?market=&c=&lang=zh_CN")
    Call<String> tickers();

}
