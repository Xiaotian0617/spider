package com.al.dbspider.monitor;

/**
 * API或网络异常
 *
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 2018/7/30 16:36
 */
public class ApiErrorMessage extends ErrorMessage {
    private String url;

    public ApiErrorMessage(String name, String url, Throwable t) {
        super(name, t);
        this.url = url;
    }

    @Override
    public String getSummary() {
        return name + "的 API - " + url + "错误 : " + t.getMessage();
    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public String getContent() {
        return null;
    }
}
