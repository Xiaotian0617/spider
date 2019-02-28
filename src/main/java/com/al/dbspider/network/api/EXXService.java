package com.al.dbspider.network.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by 郭青枫 on 2018/1/3 0003.
 */
public interface EXXService {
    @GET("tickers")
    Call<ResponseBody> tickers();
}
