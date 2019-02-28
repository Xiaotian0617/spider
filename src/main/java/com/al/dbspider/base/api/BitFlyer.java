package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 19:50  王楷
 * @version 19:50 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
public interface BitFlyer {

    /**
     * Returns  all markets .
     *
     * @return
     */
    @GET("v1/getmarkets")
    Call<String> getMarketList();


    /**
     * Returns the ticker for all markets.
     *
     * @param code 从getMarketList（)方法中拿到
     * @return
     */
    @GET("v1/getticker")
    Call<String> getMarketAll(@Query("product_code") String code);


}
