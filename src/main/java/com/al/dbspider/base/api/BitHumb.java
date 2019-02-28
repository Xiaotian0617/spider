package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * https://www.bithumb.com/u1/US127
 */
public interface BitHumb {

    /**
     * exchange last transaction information
     *
     * @return all tickers KRW
     */
    @GET("public/ticker/ALL")
    Call<String> tickers();


    /**
     * exchange last transaction information
     *
     * @param currency BTC, ETH, DASH, LTC, ETC, XRP, BCH, XMR, ZEC, QTUM, BTG, EOS
     * @return ticker in KRW
     */
    @GET("public/ticker/{cur}")
    Call<String> ticker(@Path("cur") String currency);

    @GET("public/orderbook/ALL")
    Call<String> depths();

    @GET("public/orderbook/{cur}")
    Call<String> depth(@Path("cur") String currency);

    @GET("public/recent_transactions/{cur}")
    Call<String> trade(@Path("cur") String currency);


}
