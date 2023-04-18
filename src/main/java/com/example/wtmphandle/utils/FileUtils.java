package com.example.wtmphandle.utils;

import com.example.wtmphandle.config.WtmpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;

/**
 * @ClassName： FileUtils
 * @description: 文件处理
 * @author: ning.yang
 * @create: 2022/7/18 22:29
 */
public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    @Autowired
    WtmpConfig wtmpConfig;


    public static void writeLogfile(String msg, String filePath, String fileName) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            logger.info(file.getName() + "  does not exist...........");
            if (file.mkdir()) {
                logger.info(file.getName() + "  Created successfully");
            } else {
                logger.info(file.getName() + "  Creation failed");
            }
        }
        FileOutputStream fos = new FileOutputStream(filePath + "/" + fileName, true);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(msg);
        bw.newLine();
        bw.flush(); //将数据更新至文件
        bw.close();
        osw.close();
        fos.close();
    }


    public void writefile(String msg) throws IOException {

//        String outPath = "/home/arcana/log";
        File file = new File(wtmpConfig.getOutPath());

        if (!file.exists()) {
            logger.info(file.getName() + "  不存在...........");
            if (file.mkdir()) {
                logger.info(file.getName() + "  创建成功");
            } else {
                logger.info(file.getName() + "  创建失败");
            }
        }
        FileOutputStream fos = new FileOutputStream(file.getName() + File.separator + "errlog.log", true);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(msg);
        bw.newLine();
        bw.flush(); //将数据更新至文件
        bw.close();
        fos.close();
    }

}