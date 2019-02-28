package com.al.dbspider.base.api;


import com.al.dbspider.base.CustomConvertFactory;
import org.apache.commons.codec.digest.DigestUtils;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.*;

/**
 * http://app.mytoken.io/
 */
public interface MyToken {

    //MyToken API = testApi();

    static MyToken testApi() {
        return new Retrofit.Builder()
//                .client(client)
                .addConverterFactory(new CustomConvertFactory())
                .baseUrl("https://api2.mytoken.org")
                .build()
                .create(MyToken.class);
    }


    //https://api2.mytoken.org/market/topmarketlist?timestamp=1516766687198&code=b855ceba8415b77d530d250e42f7f686&v=1.4.0&platform=m&
    @GET("market/topmarketlist/?v=1.4.0&platform=m")
    @Headers("user-agent:Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1")
    Call<String> marketList(@Query("timestamp") Long ts, @Query("code") String code);

    default Call<String> marketList() {
        long ts = System.currentTimeMillis();
        return marketList(ts, code(ts));
    }

    /**
     * https://api2.mytoken.org/currency/kline?market_id=126&symbol=BTC&anchor=USD&period=15m&timestamp=1516774993820&code=05d660e5e483f0c7b7fccc78b637be23&v=1.4.0&platform=m&
     *
     * @param marketId
     * @param symbol
     * @param unit
     * @param ts
     * @param type     1m
     * @param code
     * @return
     */
    @GET("currency/kline/?v=1.4.0&platform=m")
    @Headers("user-agent:Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1")
    Call<String> kline(@Query("market_id") String marketId,
                       @Query("symbol") String symbol,
                       @Query("anchor") String unit,
                       @Query("period") String type,
                       @Query("limit") int limit,
                       @Query("timestamp") Long ts,
                       @Query("code") String code);

    default Call<String> kline(String marketId, String symbole, String unit) {
        long ts = System.currentTimeMillis();
        return kline(marketId, symbole, unit, "1m", 10, ts, code(ts));
    }

    //https://api2.mytoken.org/ticker/pairlist?page=1&market_id=338&timestamp=1516856937176&code=47e2619e8dc581edb1b4ba30be0dbe22&v=1.4.0&platform=m
    //上面的会返回错误信息，从网站上又找了下面新的接口
    @FormUrlEncoded
    @POST("currency/currencylist")
    @Headers("user-agent:Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1")
    Call<String> pair(@Field("market_id") String marketId,
                      @Field("page") int page,
                      @Field("size") int size,
                      @Field("direction") String dir,
                      @Field("sort") String rank,
                      @Field("timestamp") Long ts,
                      @Field("code") String code,
                      @Field("v") String version,
                      @Field("platform") String platform,
                      @Field("language") String lang);

    default Call<String> pair(String marketId, int page) {
        long ts = System.currentTimeMillis();
        return pair(marketId, page, 20, "asc", "rank", ts, code(ts), "1.4.0", "m", "zh_CN");
    }

    /**
     * 计算mytoken中的验证参数.
     * code = md5(t+'9527'+t.substr(0,6));
     * 其中t为当前时间戳字符串, 加上9527再加上t的前6位，计算md5即为code
     *
     * @param ts 当前时间戳
     * @return code
     */
    default String code(Long ts) {
        String t = String.valueOf(ts);
        return DigestUtils.md5Hex(t.concat("9527").concat(t.substring(0, 6)));
    }
}
