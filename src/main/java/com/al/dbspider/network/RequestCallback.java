package com.al.dbspider.network;

public interface RequestCallback<T> {


    public void onSuccess(String json, int requestCode);

    public void onFail(Throwable throwable, int requestCode, String name);


}
