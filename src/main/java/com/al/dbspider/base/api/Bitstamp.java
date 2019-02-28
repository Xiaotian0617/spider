package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
*
* @Version 1.0
* @Since JDK1.8
* @Author junxiaoyang
* @Company 洛阳艾鹿网络有限公司
* @Date 2018-10-22
*/
public interface Bitstamp {


    /**
     * json format:
     * {
     * base_decimals: 8,
     * minimum_order: "5.0 USD",
     * name: "LTC/USD",
     * counter_decimals: 2,
     * trading: "Enabled",
     * url_symbol: "ltcusd",
     * description: "Litecoin / U.S. dollar"
     * },
     * @return
     */
    @GET("trading-pairs-info/")
    Call<String> allPairs();


    /**
     * {
     * high: "6421.58",
     * last: "6398.96",
     * timestamp: "1540348021",
     * bid: "6398.96",
     * vwap: "6393.80",
     * volume: "2970.25805092",
     * low: "6354.26",
     * ask: "6401.77",
     * open: "6396.24"
     * }
     * @param pair
     * @return
     */
    @GET("ticker/{url_symbol}/")
    Call<String> ticker(@Path("url_symbol") String pair);
}
