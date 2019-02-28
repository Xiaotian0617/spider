package com.al.dbspider.network;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;

/**
 * Created by 郭青枫 on 2018/1/3 0003.
 */

@Slf4j
public abstract class MyCallback<T extends ResponseBody> implements RequestCallback<T> {


    @Override
    public abstract void onSuccess(String json, int requestCode);

    @Override
    public void onFail(Throwable throwable, int requestCode, String name) {
        //处理一次统一的网络失败
        log.error("Fail \n" + " code : " + requestCode + " \n" + "\n data : " + throwable.getMessage());
        if (requestCode == -1) {
            //HttpUtils.get().sendEmail(name, throwable);
        } else {
            //HttpUtils.get().sendEmail(name, throwable);
        }
    }
}