package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

/**
 * file:spider
 * <p>
 * 文件简要说明
 *
 * @author 11:45  王楷
 * @version 11:45 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
public interface CoinEgg {


    @GET("coin/btc/allcoin")
    @Headers("user-agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
    Call<String> getMarketBTCList();

    @GET("coin/usc/allcoin")
    @Headers("user-agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
    Call<String> getMarketUSCList();

    @GET("api/v1/ticker/{pair}")
    Call<String> getAllTickers(@Path("{pair}") String pair);


}
