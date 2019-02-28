package com.al.dbspider.monitor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/30 16:33
 */
public abstract class ErrorMessage implements Message {
    protected AtomicLong id = new AtomicLong(0);
    //错误来源
    protected String name;
    protected Throwable t;

    public ErrorMessage(String name, Throwable t) {
        this.name = name;
        this.t = t;
    }

    @Override
    public Long getId() {
        return id.getAndIncrement();
    }

    @Override
    public String getContent() {
        return null;
    }

}
