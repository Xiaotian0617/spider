package com.al.dbspider.base.api;

import lombok.Data;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

/**
 * https://github.com/binance-exchange/binance-official-api-docs/blob/master/rest-api.md
 */
public interface Binance {

    @GET("/exchange/public/product")
    Call<Products> products();

    @GET("api/v1/exchangeInfo")
    Call<String> symbols();

    /**
     * @param symbol 返回交易对ticker信息，传null为所有交易对
     */
    @GET("api/v1/ticker/24hr")
    Call<String> ticker(@Query("symbol") String symbol);

    default Call<String> ticker() {
        return ticker(null);
    }

    /**
     * @param symbol   交易对
     * @param interval k线, 1m,3m,5m,1h,2h...
     */
    @GET("api/v1/klines")
    Call<String> kline(@Query("symbol") String symbol,
                       @Query("interval") String interval,
                       @Query("limit") Integer limit,
                       @Query("startTime") Long startTime,
                       @Query("endTime") Long endTime);

    /**
     * 默认一次取5条K线
     *
     * @param symbol
     * @param interval 分钟K线
     * @return
     */
    default Call<String> kline(String interval, String symbol) {
        return kline(symbol, interval, 5, null, null);
    }

    @Data
    class Products {
        List<Symbol> data;
    }

    @Data
    class Symbol {
        String baseAsset;
        String quoteAsset;
        String status;
        String symbol;
    }
}
