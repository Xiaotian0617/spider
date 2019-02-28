package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;

public interface Bittrex {


    // 一次查询所有交易对
    @GET("v2.0/pub/Markets/GetMarketSummaries")
    Call<String> ticker();

}
