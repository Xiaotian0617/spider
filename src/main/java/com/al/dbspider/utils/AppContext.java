package com.al.dbspider.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @Version 1.0
 * @Since JDK1.8
 * @Author junxiaoyang
 * @Company 洛阳艾鹿网络有限公司
 * @Date 07/12/2017 22:30
 */
@Component
public class AppContext implements ApplicationContextAware {
    private static ApplicationContext APPLICATIONCONTEXT;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        APPLICATIONCONTEXT = applicationContext;
    }

    public static <T> T getBean(Class<T> clz) {
        return APPLICATIONCONTEXT.getBean(clz);
    }

    public static <T> T getBean(Class<T> clz, Object... objects) {
        return APPLICATIONCONTEXT.getBean(clz, objects);
    }
}
