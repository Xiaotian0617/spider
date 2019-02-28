package com.al.dbspider.network.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by 郭青枫 on 2018/1/3 0003.
 */
public interface BXThailandService {
    @GET("ticker/")
    Call<ResponseBody> ticker();
    //https://api.coinmarketcap.com/v1/

}
