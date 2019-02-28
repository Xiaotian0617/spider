package com.al.dbspider.monitor;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 错误消息管理,一个实例对应一个交易所或具体业务
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/30 16:39
 */
@Slf4j
public class ErrorMonitor {
    private final int size;
    private final String name;
    List<ErrorMessage> messageMap = new ArrayList<>();


    public ErrorMonitor(String name, int size) {
        this.size = size;
        this.name = name;
    }

    public void add(ErrorMessage errorMessage) {
        synchronized (this) {
            if (messageMap.size() == size) {
                log.error(name + "错误队列已满(" + size + ")");
                return;
            }
            messageMap.add(errorMessage);
        }
    }
}
