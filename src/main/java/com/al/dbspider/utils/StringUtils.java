package com.al.dbspider.utils;

/**
 * file:StringUtil
 * <p>
 * 文件简要说明  字符串工具类
 *
 * @author 2018年01月03日
 * 韩龚连
 * 创建初始版本
 * @version 2018年01月03日  V1.0  简要版本说明
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
public class StringUtils {
    /**
     * @param s 需要判断的String
     * @return 为空 返回true  反之返回false
     * @description 判断字符串 是否为null或者去空后仍为空字符串
     * @date 2018/1/3
     * @author hgl
     * @see
     * @since
     */
    public static boolean isEmpty(String s) {
        return s == null || "".equals(s.trim());
    }


    public static String toUpperCaseFirstOne(String s) {
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        } else {
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }

    //测试 使用
    public static void main(String[] args) {
        System.out.println(toUpperCaseFirstOne("binance"));
    }
}
