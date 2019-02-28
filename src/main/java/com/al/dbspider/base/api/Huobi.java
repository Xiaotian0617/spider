package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

/**
 * https://api.huobi.pro
 */
public interface Huobi {
    String USERAGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.92 Safari/537.36";
    String CONTENTTYPE = "application/x-www-form-urlencoded";

    @GET("v1/common/symbols")
    Call<String> symbols(@Header("Content-Type") String contentType, @Header("User-Agent") String userAgent);

//    @GET("api/3/ticker/{pair}")
//    Call<String> ticker(@Path("pair") String pair);
//
//    @GET("api/3/depth/{pair}")
//    Call<String> depth(@Path("pair") String pair, @Query("l") int limit);
//
//    @GET("api/3/trades/{pair}")
//    Call<String> trade(@Path("pair") String pair, @Query("l") int limit);
//
//    @Data
//    class Info {
//        Long serverTime;
//        Map<String, Pair> pairs;
//    }
//
}
