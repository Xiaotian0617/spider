package com.al.dbspider.utils;

import com.alibaba.fastjson.JSONObject;
import io.github.biezhi.ome.OhMyEmail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

import static io.github.biezhi.ome.OhMyEmail.SMTP_QQ;

@Slf4j
public class HttpUtils {

    private static HttpUtils instance = new HttpUtils();

    public static HttpUtils get() {
        return instance;
    }

    HttpClient client;
    final static String MD5KEY = "qwer321";

//    private Properties prop;

    private RequestConfig config;

    static final String ua = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36";
    static final String ua2 = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Mobile Safari/537.36";

    private HttpUtils() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(100);
        client = HttpClientBuilder.create().setConnectionManager(cm).setUserAgent(ua).build();
        config = RequestConfig.custom().setConnectionRequestTimeout(10000).setConnectTimeout(10000).setSocketTimeout(10000).build();
        initEmail();
    }

    /**
     * post json data to api
     *
     * @param url
     * @param json
     * @return
     */
    public String post(String url, String json) {
        HttpResponse resp = null;
        if (url == null || url.length() == 0) {
            log.warn("has no post api url setted! ");
            return "error - no api url";
        }
        String md5 = DigestUtils.md5Hex(json + MD5KEY);
        try {
            //StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            String[] urls = url.split(",");
            for (String u : urls) {
                u = u.trim() + "?md5=" + md5;
                HttpUriRequest post = RequestBuilder.post().setUri(u).addParameter("jsonObject", json).setConfig(config).build();
                resp = client.execute(post);
                String result = EntityUtils.toString(resp.getEntity());
                log.trace("response:{}", result);
            }
        } catch (Exception e) {
            log.error("post error" + e.getMessage(), e);
        } finally {
            if (resp != null) {
                // ensure the connection is released back to pool
                EntityUtils.consumeQuietly(resp.getEntity());
            }
        }
        return "";
    }

    /**
     * post json data to api
     *
     * @param url
     * @param map
     * @return
     */
    public String post(String url, Map<String, Object> map) {
        HttpResponse resp = null;
        if (url == null || url.length() == 0) {
            log.warn("has no post api url setted! ");
            return "error - no api url";
        }
        String str = JSONObject.toJSONString(map);
        log.trace("finalJson:" + str);
//        String md5 = DigestUtils.md5Hex(str+MD5KEY);
        try {
            StringEntity entity = new StringEntity(str, ContentType.APPLICATION_JSON);
            String[] urls = url.split(",");
            for (String u : urls) {
                HttpUriRequest post = RequestBuilder.post().setUri(u).setEntity(entity).setConfig(config).build();
                resp = client.execute(post);
                String result = EntityUtils.toString(resp.getEntity());
                log.trace("response:{}", result);
            }
        } catch (Exception e) {
            log.error("post error", e);
        } finally {
            if (resp != null) {
                // ensure the connection is released back to pool
                EntityUtils.consumeQuietly(resp.getEntity());
            }
        }
        return "";
    }

    /**
     * post json data to api
     *
     * @param url
     * @param json
     * @return
     */
    public String post(String url, String tcid, String json) {
        HttpResponse resp = null;
        if (url == null || url.length() == 0) {
            log.warn("has no post api url setted! ");
            return "error - no api url";
        }
        try {
            String[] urls = url.split(",");
            for (String u : urls) {
                HttpUriRequest post = RequestBuilder.post().setUri(u).addParameter("tcid", tcid)
                        .addParameter("data", json).setConfig(config).build();
                resp = client.execute(post);
                String result = EntityUtils.toString(resp.getEntity());
                log.trace("response:{}", result);
            }
        } catch (Exception e) {
            log.error("post error", e);
        } finally {
            if (resp != null) {
                // ensure the connection is released back to pool
                EntityUtils.consumeQuietly(resp.getEntity());
            }
        }
        log.trace("发送成功，发送内容：tcid:" + tcid + ",json:" + json, "返回内容：");
        return "";
    }

    public String get(String url) {
        HttpUriRequest request = RequestBuilder.get(url).setConfig(config).build();
        HttpResponse resp = null;
        try {
            resp = client.execute(request);
            return EntityUtils.toString(resp.getEntity());
        } catch (IOException e) {
            log.error("post error", e);
        } finally {
            if (resp != null) {
                // ensure the connection is released back to pool
                EntityUtils.consumeQuietly(resp.getEntity());
            }
        }
        return "";
    }

//    public String getApiUrl(String key) {
//        return prop.getProperty("api.save." + key);
//    }

//    public String getSocketApiUrl() {
//        return prop.getProperty("api.socket");
//    }

    private void initEmail() {
        OhMyEmail.config(SMTP_QQ(false), "306701943@qq.com", "dirbvsehmpkybgdb");
    }

    public String getEmails() {
        String emails = null;//prop.getProperty("notify.email");
        return emails != null ? emails : "306701943@qq.com";
    }

    public void sendEmail(String throwableMsg, String message) {
        try {
            OhMyEmail.subject(throwableMsg + "error")
                    .from("dbspider")
                    .to(getEmails())
                    .text("" + message)
                    .send();
        } catch (Exception e) {
            log.error("send mail error", e);
        }
    }

    public void sendEmail(String throwableMsg, Throwable th) {
        sendEmail(throwableMsg, ExceptionUtils.getStackTrace(th));
    }
}
