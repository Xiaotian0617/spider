package com.al.dbspider.base;

import java.util.Objects;

/**
 * 交易所名称
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 18/01/2018 08:55
 */
public enum ExchangeConstant {
    //Aex("aex"),
    Binance("binance"),
    Bibox("bibox"),
    Bigone("bigone"),
    Bitfinex("bitfinex"),
    Bitflyer("bitflyer"),
    Bithumb("bithumb"),
    Bittrex("bittrex"),
    Bitz("bitz"),
    Bitstamp("bitstamp"),
    Bxthailand("bxthailand"),
    Cex("cex"),
    Coin900("coin900"),
    Coincheck("coincheck"),
    Coinegg("coinegg"),
    //Coinone("coinone"),
    Exx("exx"),
    Gate("gate"),
    Gemini("gemini"),
    Hksy("hksy"),
    Huobi("huobipro"),
    //Kkex("kkex"),
    Korbit("korbit"),
    Kraken("kraken"),
    Kucoin("kucoin"),
    Liqui("liqui"),
    Linkbitc("linkbitc"),
    Okex("okex"),
    Poloniex("poloniex"),
    Qbtc("obtc"),
    //Quoine("quoine"),
    //Ucoin("ucoin"),
    Ucx("ucx"),
    Zb("zb"),
    Coinnest("coinnest"),
    //Chaoex("12lian"),
    Bcex("bcex"),
    //Hitbtc("hitbtc"),
    //Btcbox("btcbox"),
    Coinbase("coinbase"),
    Bitmex("bitmex"),
    Coinw("coinw"), Fcoin("fcoin"), UCoin("ucoin"), Quintar("quintar"), MyToken("mytoken"), AiCoin("aicoin"), CoinMarketCap("coinmarketcap"), Coinone("coinone");

    private final String value;

    ExchangeConstant(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static ExchangeConstant valueExist(String value) {
        ExchangeConstant[] exchangeConstants = values();
        for (ExchangeConstant exchangeConstant : exchangeConstants) {
            if (Objects.equals(exchangeConstant.value, value)) {
                return exchangeConstant;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println("KRW<CNY".contains("CNY"));
    }

}
