package com.al.dbspider;

import com.al.dbspider.utils.AiluPaasUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * file:topcoin
 * <p>
 * 文件简要说明
 *
 * @author 10:01  王楷
 * @version 10:01 V1.0
 * @par 版权信息：
 * 2018 Copyright 河南艾鹿网络科技有限公司 All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class DataProviceTest {

    @Resource
    private AiluPaasUtil ailuPaasUtil;

    @Test
    public void testSendMsg() {
        ailuPaasUtil.sendErrorMsgToPhone("", "服务器占用已达到95%，已濒临崩溃！");
    }

}
