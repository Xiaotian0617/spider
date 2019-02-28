package com.al.dbspider.dao.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/18 14:31
 */
@Data
public class DepthLevel {
    private BigDecimal price;
    private Integer count;//order 量
    private BigDecimal amount;//成交量(系统内成交量为volume,amount为成交额)
}
