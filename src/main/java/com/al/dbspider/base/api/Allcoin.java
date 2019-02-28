package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * <a href="https://www.allcoin.com/About/APIReference/">Allcoin网</a> rest API 请求<br>
 * 限制: 3000次/5m/ip<br>
 */
public interface Allcoin {

    /**
     * 行情
     *
     * @param tradePair btc_usdt
     * @return {<br>
     * "ticker": {<br>
     * "vol": "40.463",<br> volume(in the last rolling 24 hours)
     * "last": "0.899999",<br>
     * "sell": "0.5",<br>
     * "buy": "0.225",<br>
     * "high": "0.899999",<br>
     * "low": "0.081"<br>
     * },<br>
     * "date": "1507875747359"}server time for returned time
     */
    @GET("/ticker")
    Call<String> ticker(@Query("symbol") String tradePair);


    /**
     * 深度
     *
     * @param tradePair
     * @param size      1~200
     * @return {
     * "asks": [
     * [792, 5],
     * [789.68, 0.018],
     * [788.99, 0.042],
     * [788.43, 0.036],
     * [787.27, 0.02]
     * ],
     * "bids": [
     * [787.1, 0.35],
     * [787, 12.071],
     * [786.5, 0.014],
     * [786.2, 0.38],
     * [786, 3.217],
     * [785.3, 5.322],
     * [785.04, 5.04]
     * ]
     * }
     */
    @GET("/depth")
    Call<String> depth(@Query("symbol") String tradePair, @Query("size") Integer size);

    /**
     * 历史成交记录
     *
     * @param tradePair
     * @param tid       指定
     * @return [{
     * "amount": 0.541,交易额
     * "date": 1472711925,
     * "price": 81.87,
     * "tid": 16497097,交易 id
     * "trade_type": "ask", 委托类型
     * "type": "sell"
     * }...
     */
    @GET("/trades")
    Call<String> trade(@Query("market") String tradePair, @Query("since") Long tid);


    /**
     * K线
     *
     * @param tradePair btc_usdt
     * @param period    1min
     *                  3min
     *                  5min
     *                  15min
     *                  30min
     *                  1day
     *                  3day
     *                  1week
     *                  1hour
     *                  2hour
     *                  4hour
     *                  6hour
     *                  12hour
     * @param timestamp
     * @param limit     Limit of returning data(default 1000,it only return 1000 data if that more than 1000 date )
     * @return {
     * "data": [
     * [
     * 1472107500000,
     * 3840.46, open
     * 3843.56, high
     * 3839.58, low
     * 3843.3, close
     * 492.456 volume
     * ]...
     * ],
     * "moneyType": "btc",
     * "symbol": "ltc"
     * }
     */
    @GET("/kline")
    Call<String> kline(@Query("market") String tradePair, @Query("type") String period, @Query("since") Long timestamp, @Query("size") Integer limit);


}
