package com.al.dbspider.rest;

import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class BaseRest {

    protected final static ScheduledExecutorService SCHEDULER;

    static {
        SCHEDULER = Executors.newScheduledThreadPool(2);
    }

    protected static URL initUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static class PriceJson {
        public String jys;
        public String coin;
        public String unit;
        public String last;
    }

}
