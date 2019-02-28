package com.al.dbspider.websocket;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/30 15:46
 */
public class OnlyKeyMessage<E> extends ArrayList<E> {
    private List<E> onlykeys = new ArrayList<>();

    public OnlyKeyMessage(List<E> onlyKeys) {
        this.onlykeys = onlyKeys;
    }

    public OnlyKeyMessage() {
    }

    public String getTopic() {
        return null;
    }

    public String getOnlykey() {
        return null;
    }

    public List<E> getOnlykeys() {
        return onlykeys;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(onlykeys);
    }
}
