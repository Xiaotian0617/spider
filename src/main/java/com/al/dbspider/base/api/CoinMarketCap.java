package com.al.dbspider.base.api;

import com.al.dbspider.dao.domain.MarketCap;
import lombok.Data;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

/**
 * https://coinmarketcap.com/api/
 */
public interface CoinMarketCap {

    /**
     * @param limit   return a maximum of [limit] results (default is 100, use 0 to return all results)
     * @param convert return price, 24h volume, and market cap in terms of another currency. Valid values are: "CNY", "EUR", "HKD"...
     * @return call for execute
     */
    @GET("/v1/ticker/")
    Call<String> ticker(@Query("limit") int limit, @Query("convert") String convert);

    /**
     * @param id      coin id
     * @param convert return price, 24h volume, and market cap in terms of another currency. Valid values are: "CNY", "EUR", "HKD"...
     */
    @GET("v1/ticker/{id}/")
    Call<String> ticker(@Path("id") String id, @Query("convert") String convert);

    @Data
    class Caps {
        List<MarketCap> li;
    }

}
