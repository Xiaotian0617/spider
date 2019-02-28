package com.al.dbspider.network;

import com.al.dbspider.network.api.*;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;


/**
 * Created by 郭青枫 on 2018/1/3 0003.
 */

public class NetWork {
    private static GEMINIService geminiService;
    private static HKSYService hksyService;
    private static BXThailandService bxThailandService;
    private static EXXService exxService;
    private static SaveService saveService;

    public static GEMINIService getGEMINIService() {
        if (geminiService == null) {
            //交易所 baseUrl
            Retrofit retrofit = getRetrofit("https://api.gemini.com/v1/");
            geminiService = retrofit.create(GEMINIService.class);
        }
        return geminiService;
    }

    public static HKSYService getHKSYService() {
        if (hksyService == null) {
            //交易所 baseUrl
            Retrofit retrofit = getRetrofit("http://openapi.hksy.com/app/coinMarket/v1/");
            hksyService = retrofit.create(HKSYService.class);
        }
        return hksyService;
    }

    public static BXThailandService getBXThailandService() {
        if (bxThailandService == null) {
            //交易所 baseUrl
            Retrofit retrofit = getRetrofit("https://api.coinmarketcap.com/v1/");
            bxThailandService = retrofit.create(BXThailandService.class);
        }
        return bxThailandService;
    }

    public static EXXService getEXXService() {
        if (exxService == null) {
            //交易所 baseUrl
            Retrofit retrofit = getRetrofit("https://api.exx.com/data/v1/");
            exxService = retrofit.create(EXXService.class);
        }
        return exxService;
    }

    private static Converter.Factory fastjsonConverterFactory = FastJsonConverterFactory.create();
    private static CallAdapter.Factory rxJavaCallAdapterFactory = RxJava2CallAdapterFactory.create();


    private static Retrofit getRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(fastjsonConverterFactory)
                .addCallAdapterFactory(rxJavaCallAdapterFactory)
                .build();
    }

}