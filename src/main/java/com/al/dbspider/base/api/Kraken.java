package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 12:36  王楷
 * @version 12:36 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
public interface Kraken {

    List<String> pairs = new ArrayList<String>();

    /**
     * 目的是为了要交易对
     *
     * @return
     */
    @GET("AssetPairs?info=margin")
    Call<String> assetPairs();


    /**
     * 币对儿列表 例如：BCHEUR,BCHUSD
     *
     * @param pair
     * @return
     */
    @GET("Ticker")
    Call<String> getTickers(@Query("pair") String pair);


}
