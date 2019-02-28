package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 9:22  王楷
 * @version 9:22 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
public interface Bitmex {


    /**
     * Returns the ticker for all markets.
     *
     * @return
     */
    @GET("tickers")
    Call<String> getTickerAll();


    @GET("pairs")
    Call<String> allPairs();
}
