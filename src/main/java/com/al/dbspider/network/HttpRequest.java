package com.al.dbspider.network;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Slf4j
public class HttpRequest {
    private String mRequestName;
    private int mRequestCode;
    private Call<ResponseBody> mCall;
    private MyCallback<ResponseBody> myCallback;


    private HttpRequest(Builder builder) {
        this.mRequestName = builder.requestName;
        this.mRequestCode = builder.requestCode;
        this.mCall = builder.call;
        this.myCallback = builder.callback;
    }

    Callback<ResponseBody> callback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            //响应成功
            if (response.isSuccessful()) {
                try {
                    String json = response.body().string();
                    if (mRequestCode != -1) {
                        myCallback.onSuccess(json, mRequestCode);
                    }
                    log.info("Success \n" + " code : " + mRequestCode + " \n" + "name" + mRequestName + "\n data : " + json);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    myCallback.onFail(e, mRequestCode, mRequestName);
                }

            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable throwable) {
            throwable.printStackTrace();
            myCallback.onFail(throwable, mRequestCode, mRequestName);
        }
    };

    /**
     * 普通请求
     */
    public void sendRequest() {
        mCall.clone().enqueue(callback);
    }

    public void sendSynchronousRequest() {
        try {
            Response<ResponseBody> execute = mCall.clone().execute();
            callback.onResponse(mCall, execute);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            callback.onFailure(mCall, e);
        }
    }


    public static class Builder {
        private String requestName = "http请求描述";// 请求描述
        private int requestCode = -1;
        private Call<ResponseBody> call;
        private MyCallback<ResponseBody> callback;


        public Builder() {
        }


        /**
         * post 传参方式
         *
         * @param
         * @return
         */

        /**
         * @param requestName 请求名字,在日志中显示
         * @return
         */
        public Builder setRequestName(String requestName) {
            this.requestName = requestName;
            return this;
        }


        /**
         * @param requestCode 请求id
         * @return
         */
        public Builder setRequestCode(int requestCode) {
            this.requestCode = requestCode;
            return this;
        }

        /**
         * 设置请求
         *
         * @param call
         */
        public Builder setCall(Call<ResponseBody> call) {
            this.call = call;
            return this;
        }

        /**
         * 设置回掉
         *
         * @param callback
         * @return
         */
        public Builder setCallback(MyCallback<ResponseBody> callback) {
            this.callback = callback;
            return this;
        }

        /**
         * @return 返回一个请求对象
         */
        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }

}
