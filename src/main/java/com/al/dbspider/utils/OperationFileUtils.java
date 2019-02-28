package com.al.dbspider.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static java.nio.file.Files.readAllBytes;

/**
 * CalcCenter
 * file:SaveFileUtils
 * <p>  文件操作类
 *
 * @author mr.wang
 * @version 2018年03月08日10:39:53 V1.0
 * @par 版权信息：
 * 2018 copyright 河南艾鹿网络科技有限公司 all rights reserved.
 */
@Slf4j
@Component
public class OperationFileUtils {

    public String readFile(String fileName) {
        ClassPathResource classPathResource = new ClassPathResource(fileName);
        try {
            InputStream inputStream = classPathResource.getInputStream();
            //把所有的数据读取到这个字节当中
            byte[] b = new byte[1000];
            //完整的读取一个文件
            inputStream.read(b);
            //read:返回的是读取的文件大小
            // 最大不超过b.length，返回实际读取的字节个数
            // System.out.println(Arrays.toString(b));//读取的是字节数组
            // 把字节数组转成字符串
            // System.out.println(new String(b));
            // 关闭流
            inputStream.close();
            return new String(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String readFile(Path path) {
        try {
            return new String(readAllBytes(path));
            //IO流处理 据说效率会高
        } catch (IOException e) {
            log.error("读取文件失败，错误内容为", e);
            return null;
        }
    }
}
