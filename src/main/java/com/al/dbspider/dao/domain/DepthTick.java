package com.al.dbspider.dao.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/18 14:48
 */
@Data
public class DepthTick {
    private List<DepthLevel> asks = new ArrayList<>();
    private List<DepthLevel> bids = new ArrayList<>();

    public void addAskDepthLevel(DepthLevel depthLevel) {
        asks.add(depthLevel);
    }

    public void addBidDepthLevel(DepthLevel depthLevel) {
        bids.add(depthLevel);
    }
}
