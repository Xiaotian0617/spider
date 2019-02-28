package com.al.dbspider.temp;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Position {

    @JSONField(name = "coin")
    private String coin;
    @JSONField(name = "holdAmount")
    private BigDecimal holdAmount;
    @JSONField(name = "volume")
    private BigDecimal volume;
    @JSONField(name = "line")
    private Line line;

    @JSONField(name = "dateArry")
    private List<String> times;
    @JSONField(name = "list1")
    private List<BigDecimal> timeVal1;
    @JSONField(name = "list2")
    private List<BigDecimal> timeVal2;
    @JSONField(name = "list3")
    private List<BigDecimal> timeVal3;
    @JSONField(name = "list4")
    private List<BigDecimal> timeVal4;
    @JSONField(name = "list5")
    private List<BigDecimal> timeVal5;
    @JSONField(name = "list6")
    private List<BigDecimal> timeVal6;

    @Data
    public static class Line {
        @JSONField(name = "data1")
        private List<List<BigDecimal>> klineFor1hList;
        @JSONField(name = "data2")
        private List<List<BigDecimal>> klineFor4hList;
        @JSONField(name = "data3")
        private List<List<BigDecimal>> klineFor12hList;
    }

}
