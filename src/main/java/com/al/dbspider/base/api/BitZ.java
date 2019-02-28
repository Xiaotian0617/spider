package com.al.dbspider.base.api;

import lombok.Data;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

import java.math.BigDecimal;
import java.util.Map;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 17:53  王楷
 * @version 17:53 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
public interface BitZ {


    @GET("/api_v1/tickerall")
    Call<Info> tickerall();

    //@GET("api_v1/kline")
    @FormUrlEncoded
    @POST("/market/kline")
    Call<String> kline(@Field("symbol") String coin, @Field("type") String type, @Field("size") String size);

    @Data
    class Info {
        long code;
        String msg;
        Map<String, Ticker> data;
    }

    @Data
    class Ticker {
        long date;
        BigDecimal last;
        BigDecimal buy;
        BigDecimal sell;
        BigDecimal high;
        BigDecimal low;
        BigDecimal vol;
    }


    @Data
    class Kline {
        BigDecimal open;
        BigDecimal close;
        BigDecimal high;
        BigDecimal low;
        BigDecimal vol;
        long time;
        String jys;
        String coin;
        String unit;
    }
}
