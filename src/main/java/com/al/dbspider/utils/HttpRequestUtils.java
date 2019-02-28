package com.al.dbspider.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * file:HttpRequestUtil
 * <p>
 * 文件简要说明
 *
 * @author 2018年01月03日  韩龚连  创建初始版本
 * @version 2018年01月03日  V1.0  简要版本说明
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@Slf4j
public class HttpRequestUtils {
    /**
     * @param url   要访问的url
     * @param param 请求参数  格式：key=123&v=456 (不传递参数时 param=null)
     * @return
     * @description Get 请求
     * @date 2018/1/3
     * @author hgl
     * @see
     * @since
     */
    public static String sendGet(String url, String param) {
        String urlStr = StringUtils.isEmpty(param) ? url : url + "?" + param;
        String result = "";
        BufferedReader in = null;
        try {
            URL netUrl = new URL(urlStr);
            // 打开和URL之间的连接
            URLConnection connection = netUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
//            Map<String, List<String>> map = connection.getHeaderFields();
//            // 遍历所有的响应头字段
//            for (String key : map.keySet()) {
//                System.out.println(key + "--->" + map.get(key));
//            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            log.error("发送 GET 请求出现异常：", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    log.error("关闭流异常：", e);
                }
            }
        }

        return result;
    }

    /**
     * @param url   要访问的url
     * @param param 请求参数  格式：key=123&v=456 (不传递参数时 param=null)
     * @return
     * @description Post 请求
     * @date 2018/1/3
     * @author hgl
     * @see
     * @since
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            //设置连接输出流为true,默认false (post 请求是以流的方式隐式的传递参数)
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            log.error("发送 POST 请求出现异常：", e);
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                log.error("关闭流异常：", ex);
            }
        }
        return result;
    }
}
