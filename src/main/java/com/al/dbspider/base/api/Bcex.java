package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Bcex {

    @GET("https://www.bcex.ca/coins/markets?")
    Call<String> tickers(@Query("t") String random);

    default Call<String> tickers() {
        return tickers(String.valueOf(Math.random()));
    }
}
