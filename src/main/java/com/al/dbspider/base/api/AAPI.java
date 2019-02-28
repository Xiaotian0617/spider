package com.al.dbspider.base.api;

import com.al.dbspider.base.CustomConvertFactory;
import com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Converter;
import retrofit2.Retrofit;

@Configuration
public class AAPI {

    @Autowired
    private OkHttpClient okHttpClient;

    public <T> T build(String baseUrl, Class<T> clz) {
        return build(baseUrl, clz, new CustomConvertFactory(), new Retrofit2ConverterFactory());
    }

    private <T> T build(String baseUrl, Class<T> clz, Converter.Factory... factory) {
        Retrofit.Builder builder = new Retrofit.Builder();
        for (Converter.Factory f : factory) {
            builder.addConverterFactory(f);
        }
        return builder
                .client(okHttpClient)
                .baseUrl(baseUrl)
                .build()
                .create(clz);
    }

    @Bean
    public TopCoin topCoin(@Value("${websocket.topcoinws.url}") String url) {
        return build(url, TopCoin.class);
    }

    @Bean
    public Aex aex(@Value("${rest.aex.url}") String url) {
        return build(url, Aex.class);
    }

    @Bean
    public Allcoin allcoin(@Value("${rest.allcoin.url}") String url) {
        return build(url, Allcoin.class);
    }

    @Bean
    public Bibox bibox(@Value("${rest.bibox.url}") String url) {
        return build(url, Bibox.class);
    }

    @Bean
    public Bigone bigone(@Value("${rest.bigone.url}") String url) {
        return build(url, Bigone.class);
    }

    @Bean
    public Binance binance(@Value("${rest.binance.url}") String url) {
        return build(url, Binance.class);
    }

    @Bean
    public BitFlyer bitFlyer(@Value("${rest.bitflyer.url}") String url) {
        return build(url, BitFlyer.class);
    }

    @Bean
    public BitHumb bitHumb(@Value("${rest.bithumb.url}") String url) {
        return build(url, BitHumb.class);
    }

    @Bean
    public Bittrex bittrex(@Value("${rest.bittrex.url}") String url) {
        return build(url, Bittrex.class);
    }

    @Bean
    public BitZ bitZ(@Value("${rest.bitz.url}") String url) {
        return build(url, BitZ.class);
    }

    @Bean
    public Cex cex(@Value("${rest.cex.url}") String url) {
        return build(url, Cex.class);
    }

    @Bean
    public CoinEgg coinEgg(@Value("${rest.coinegg.url}") String url) {
        return build(url, CoinEgg.class);
    }

    @Bean
    public CoinMarketCap coinMarketCap(@Value("${rest.coinmarketcap.url}") String url) {
        return build(url, CoinMarketCap.class);
    }

    @Bean
    public Coinone coinone(@Value("${rest.coinone.url}") String url) {
        return build(url, Coinone.class);
    }

    @Bean
    public Gate gate(@Value("${rest.gate.url}") String url) {
        return build(url, Gate.class);
    }

    @Bean
    public Huobi huobi(@Value("${rest.huobi.url}") String url) {
        return build(url, Huobi.class);
    }

    @Bean
    public Korbit korbit(@Value("${rest.korbit.url}") String url) {
        return build(url, Korbit.class);
    }

    @Bean
    public Kraken kraken(@Value("${rest.kraken.url}") String url) {
        return build(url, Kraken.class);
    }

    @Bean
    public Liqui liqui(@Value("${rest.liqui.url}") String url) {
        return build(url, Liqui.class);
    }

    @Bean
    public OKEx okEx(@Value("${rest.okex.url}") String url) {
        return build(url, OKEx.class);
    }

    @Bean
    public Poloniex poloniex(@Value("${rest.poloniex.url}") String url) {
        return build(url, Poloniex.class);
    }

    @Bean
    public UCoin uCoin(@Value("${rest.ucoin.url}") String url) {
        return build(url, UCoin.class);
    }

    @Bean
    public Zb zb(@Value("${rest.zb.url}") String url) {
        return build(url, Zb.class);
    }

    @Bean
    public Coin900 coin900(@Value("${rest.coin900.url}") String url) {
        return build(url, Coin900.class);
    }

    ;

    @Bean
    public MyToken mytoken(@Value("${rest.mytoken.url}") String url) {
        return build(url, MyToken.class);
    }

    ;

    @Bean
    public AiCoin aiCoin(@Value("${rest.aicoin.url}") String url) {
        return build(url, AiCoin.class);
    }

    ;

    @Bean
    public Quintar quintar(@Value("${rest.quintar.url}") String url) {
        return build(url, Quintar.class);
    }

    ;

    @Bean
    public CoinW coinW(@Value("${rest.coinw.url}") String url) {
        return build(url, CoinW.class);
    }

    ;

    @Bean
    public KuCoin kucoin(@Value("${rest.kucoin.url}") String url) {
        return build(url, KuCoin.class);
    }

    @Bean
    public Bcex bcex(@Value("${rest.bcex.url}") String url) {
        return build(url, Bcex.class);
    }

    @Bean
    public Linkbitc linkbitc(@Value("${rest.linkbitc.url}") String url) {
        return build(url, Linkbitc.class);
    }

    @Bean
    public Fcoin fcoin(@Value("${rest.fcoin.url}") String url) {
        return build(url, Fcoin.class);
    }

    @Bean
    public Bitfinex bitfinex(@Value("${rest.bitfinex.url}") String url) {
        return build(url, Bitfinex.class);
    }

    @Bean
    public Bitstamp bitstamp(@Value("${rest.bitstamp.url}") String url) {
        return build(url, Bitstamp.class);
    }

}
