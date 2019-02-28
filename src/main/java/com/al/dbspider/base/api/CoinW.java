package com.al.dbspider.base.api;

import lombok.Data;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface CoinW {

    /**
     * 查询币种以及其相应编号
     */
    @GET("newTrade/topCirculation.html?random=62")
    Call<SymbolInfo> symbol();

    /**
     * 查询市场信息
     */
    @GET("real/market2.html?random=2")
    Call<MarketInfo> market(@Query("symbol") int symbolNum);

    /**
     * 查询K线信息
     */
    @GET("kline/fulldepth.html?step=60&rand=17")
    Call<KLineInfo> kline(@Query("symbol") int symbolNum);

    @Data
    class SymbolInfo {
        List<Symbol> fMap;
    }

    @Data
    class Symbol {
        String fShortName2;
        int fid;
    }

    @Data
    class MarketInfo {
        List<String> pNew;
        List<String> vol;
    }

    @Data
    class KLineInfo {
        Period period;
    }

    @Data
    class Period {
        List<List<String>> data;
    }
}
