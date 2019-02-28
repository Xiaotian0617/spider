package com.al.dbspider.network;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by 郭青枫 on 2018/1/4 0004.
 */

public class StartHttp {

    //    HttpRequest saveHttp;
    public String MONEY_URL;

    private static class startHttp {
        private static StartHttp startHttp = new StartHttp();
    }

    public static StartHttp getInstence() {
        return startHttp.startHttp;
    }

//    private StartHttp() {
//        MONEY_URL = HttpUtils.get().getApiUrl("money");
//    }

    protected ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2);

    public void start(Runnable runnable, int time) {
        SCHEDULER.scheduleAtFixedRate(runnable, 1, time, TimeUnit.SECONDS);
    }

}
