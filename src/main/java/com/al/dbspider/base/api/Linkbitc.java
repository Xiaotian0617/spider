package com.al.dbspider.base.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface Linkbitc {


    //https://www.linkbitc.com/Mapi/chart/getChart/marketid/imu_usdt/apikey/ecda7cb0558331e50050a0011d597669/secretkey/8bd5546aa5682a50112ea6b759b6c619afdfe006906060b826f951db7b5ae8e2
    @GET("https://www.linkbitc.com/Mapi/chart/getChart/marketid/{pair}/apikey/{key}/secretkey/{secret}")
    Call<String> ticker(@Path("pair") String pair, @Path("key") String key, @Path("secret") String secret);

    @GET("exapi/api/klinevtwo/indexshblw")
    Call<String> market();

    @GET("exapi/api/klinevtwo/get1minKLine")
    Call<String> kline();

    default Call<String> ticker(String pair) {
        return ticker(pair, "ecda7cb0558331e50050a0011d597669", "8bd5546aa5682a50112ea6b759b6c619afdfe006906060b826f951db7b5ae8e2");
    }

//    default List<Call<String>> tickers() {
//        return Arrays.stream(pair).map(this::ticker).collect(Collectors.toList());
//    }

}
