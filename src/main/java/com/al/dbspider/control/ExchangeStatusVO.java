package com.al.dbspider.control;

import com.al.dbspider.base.ExchangeConstant;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/31 14:54
 */
@Data
public class ExchangeStatusVO {

    private Map<ExchangeConstant, ExchangeStatus> status = new HashMap<>();

    public void setStatus(Map<ExchangeConstant, ExchangeStatus> status) {
        this.status = status;
    }

    public Map<ExchangeConstant, ExchangeStatus> getStatus() {
        return status;
    }

    public void addStatus(ExchangeConstant exchangeConstant, ExchangeStatus exchangeStatus) {
        status.put(exchangeConstant, exchangeStatus);
    }
}
