package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * https://api.bitfinex.com/
 */
public interface Bitfinex {

    /**
     * exchange get all symbols
     *
     * @return all pairs
     */
    @GET("v1/symbols")
    Call<String> symbols();

    /**
     * Various statistics about the requested pair.
     * <p>
     * Key: Allowed values: "funding.size", "credits.size", "credits.size.sym", "pos.size"
     * Size: Available values: '1m'
     * Symbol: The symbol you want information about : 'tBTCUSD','fUSD'
     * Side: Available values: "long", "short"
     * Section: Available values: "last", "hist"
     * <p>
     * Available Keys:
     * pos.size	Total Open Position (long / short)	:1m :SYM_TRADING :SIDE	pos.size:1m:tBTCUSD:long , pos.size:1m:tBTCUSD:short
     * funding.size	Total Active Funding	:1m :SYM_FUNDING	funding.size:1m:fUSD
     * credits.size	Active Funding used in positions	:1m :SYM_FUNDING	credits.size:1m:fUSD
     * credits.size.sym	Active Funding used in positions (per trading symbol)	:1m :SYM_FUNDING :SYM_TRADING	credits.size.sym:1m:fUSD:tBTCUSD
     *
     * @return
     */
    @GET("v2/stats1/{key}:{size}:{symbol}:{side}/{section}")
    Call<String> stats(@Path("key") String key, @Path("size") String size, @Path("symbol") String symbol, @Path("side") String side, @Path("section") String section);


}
