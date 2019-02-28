package com.al.dbspider.base.api;

import lombok.Data;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

/**
 * https://www.okex.com/rest_api.html
 */
public interface OKEx {

    /**
     * 获取OKEX合约行情
     *
     * @param symbol 交易对
     * @param type   合约类型: this_week:当周   next_week:下周   quarter:季度
     * @return
     */
    @GET("/api/v1/future_ticker.do")
    Call<String> ticker(@Query("symbol") String symbol, @Query("contract_type") String type);


    /**
     * 获取OKEX合约深度信息
     *
     * @param symbol 交易对
     * @param type   合约类型: this_week:当周   next_week:下周   quarter:季度
     * @param size   1-200
     * @param merge  可不传，默认0 ，1为合并深度
     * @return asks:卖方深度 bids:买方深度
     */
    @GET("/api/v1/future_depth.do")
    Call<String> depth(@Query("symbol") String symbol
            , @Query("contract_type") String type
            , @Query("size") Integer size
            , @Query("merge") Integer merge);

    /**
     * 获取OKEX合约交易记录信息
     *
     * @param symbol 交易对
     * @param type   合约类型: this_week:当周   next_week:下周   quarter:季度
     * @return
     */
    @GET("/api/v1/future_trades.do")
    Call<String> trade(@Query("symbol") String symbol
            , @Query("contract_type") String type);


    /**
     * @param symbol 交易对，如 ltc_btc 等
     * @return
     */
    @GET("/api/v1/ticker.do")
    Call<String> ticker(@Query("symbol") String symbol);

    /**
     * @param symbol 交易对，如 ltc_btc 等
     * @return
     */
    @GET("/api/v1/depth.do")
    Call<String> depth(@Query("symbol") String symbol);


    /**
     * @param symbol 交易对，如 ltc_btc 等
     * @param since  可传空(默认返回最近成交600条)
     * @return
     */
    @GET("/api/v1/trades.do")
    Call<String> trades(@Query("symbol") String symbol, @Query("since") Long since);

    /**
     * @param symbol 交易对，如 ltc_btc 等
     * @param type   如 1min 指1分钟
     * @param size   指定获取数据的条数,默认全部获取
     * @param ts     时间戳，返回该时间戳以后的数据(例如1417536000000) 默认全部获取
     * @return
     */
    @GET("/api/v1/kline.do")
    Call<String> kline(@Query("symbol") String symbol,
                       @Query("type") String type,
                       @Query("size") Integer size,
                       @Query("since") Long ts);

    @GET("/v2/spot/markets/products")
    Call<Products> products();

    @Data
    class Products {
        int code;
        List<Symbol> data;
    }

    @Data
    class Symbol {
        int online;
        String symbol;
    }
}
