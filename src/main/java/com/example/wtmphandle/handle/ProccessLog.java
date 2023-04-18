package com.example.wtmphandle.handle;

import com.alibaba.fastjson.JSONObject;
import com.example.wtmphandle.bean.LoginInfo;
import com.example.wtmphandle.config.WtmpConfig;
import com.example.wtmphandle.utils.DateUtils;
import lombok.SneakyThrows;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @ClassName： ProccessLog
 * @description: 日志处理
 * @author: ning.yang
 * @create: 2023/4/11 16:27
 */
@Component
public class ProccessLog implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProccessLog.class);

    @Autowired
    WtmpConfig wtmpconfig;

    private static final Map<String, LoginInfo> cacheMap = new HashMap<>();
    private static final List<LoginInfo> queue = new ArrayList<>();

    public void loadFile() throws Exception {
        Thread t = new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                // 1.加载文件
                // 文件夹
                String inputPath = wtmpconfig.getWtmpPath();
                File file1 = new File(inputPath);
                StopWatch stopWatch = new StopWatch();
                if (file1.exists()) {
                    try {
                        File[] files = file1.listFiles();
                        for (File file : files) {//循环文件夹中的文件
                            stopWatch.start(file.getName());
                            if (file.isFile() && file.exists()) { //判断文件是否存在
                                logger.info("start handle " + file.getName() + " .......");
                                if (file.getName().contains("file")) {
                                    String[] names = file.getName().split(".file");
                                    String host = names[0];
                                    proccessData(inputPath + File.separator + file.getName(), host);
                                } else {
                                    logger.info(file.getName() + "\tThe file does not the requirements");
                                }
                            } else {
                                logger.info(file.getName() + " not file or not exists");
                            }
                            stopWatch.stop();
                            logger.info("process\t"+stopWatch.getLastTaskName()+ "\tcostTime :\t" + stopWatch.getLastTaskTimeMillis() +" ms");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    throw new Exception(file1.getPath() + " path not exists");
                }
            }

        });
        t.start();
    }

    // 文件内容
    private void proccessData(String fileName, String host) throws Exception {

        InputStreamReader in = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
        BufferedReader br = new BufferedReader(in, 50 * 1024 * 1024);
        Map<String, Object> tempMap = new HashMap<>();
        String messge = null;
        int count = 1;
        LoginInfo loginInfo = null;
        while (((messge = br.readLine()) != null)) {
            // 判断日志内容
//            System.out.println("line==>"+count +Arrays.asList(messge.split("\\s+")));
            List<String> tmpList = Arrays.asList(messge.split("\\s+"));
            String type = null;
            if (!tmpList.isEmpty()) {
                int size = tmpList.size();
                // 0 shutdown 2 重启 7 是登录， 8 登出
                // 登录用户 登录方式  ip 登入时间 登出时间  登录时长
                if (size == 13) {
//                    System.out.println("line=>"+count +"--size=>13--type=>"+tmpList.get(2)+"-->"+tmpList);
//                    System.out.printf("line=%d,size=13,type=%s,time=%s,sessionId=%s,-->%s\n",count,tmpList.get(2),tmpList.get(6),tmpList.get(3),tmpList);
                    type = tmpList.get(2);
                } else if (size == 14) {
//                    System.out.printf("line=%d,size=14,type=%s,time=%s,sessionId=%s,-->%s\n",count,tmpList.get(3),tmpList.get(7),tmpList.get(4),tmpList);
                    type = tmpList.get(3);
                } else if (size == 15) {
//                    System.out.printf("line=%d,size=15,type=%s,time=%s,sessionId=%s,-->%s\n",count,tmpList.get(3),tmpList.get(7),tmpList.get(4),tmpList);
                    type = tmpList.get(3);
                }
                createEntity(tmpList, size, type);
            }
            count++;
        }
        in.close();
        br.close();
        logger.info(fileName + "\t finshed .......");
        logger.info("file start write .......");
        // 登出问题数据放入队列
        for (LoginInfo info : cacheMap.values()) {
            info.setHuaan_host(host);
            entityToErrorString(info);
            queue.add(info);
        }
        cacheMap.clear();
        Collections.sort(queue, new Comparator<LoginInfo>() {
            @Override
            public int compare(LoginInfo o1, LoginInfo o2) {
                return o1.getLogin_time().compareTo(o2.getLogin_time());
            }
        });
        // 文件输出
        for (LoginInfo poll : queue) {
            if (Strings.isEmpty(poll.getHuaan_host())) {
                poll.setHuaan_host(host);
                entityToString(poll);
            }
            // 根据车牌号追加
            writefile(JSONObject.toJSONString(poll), host);
        }
        queue.clear();

    }

    private static void entityToString(LoginInfo loginInfo) {
        StringBuilder message = new StringBuilder();
        message.append(loginInfo.getUsername());
        message.append(" ");
        message.append(loginInfo.getLoginType());
        message.append(" ");
        if (Strings.isNotEmpty(loginInfo.getSrc_ip())) {
            message.append(loginInfo.getSrc_ip());
        }
        message.append(" ");
        message.append(DateUtils.DateToMMMDD(loginInfo.getLogin_time()));
        if (Strings.isNotEmpty(loginInfo.getLogout_time())) {
            message.append(" - ");
            message.append(DateUtils.DateToHHMM(loginInfo.getLogout_time()));
            message.append(" (");
            message.append(loginInfo.getCost_time());
            message.append(")");
        }
        loginInfo.setMessage(message.toString());
    }

    private static void entityToErrorString(LoginInfo loginInfo) {
        StringBuilder message = new StringBuilder();
        message.append(loginInfo.getUsername());
        message.append(" ");
        message.append(loginInfo.getLoginType());
        message.append(" ");
        if (Strings.isNotEmpty(loginInfo.getSrc_ip())) {
            message.append(loginInfo.getSrc_ip());
        }
        message.append(" ");
        message.append(DateUtils.DateToMMMDD(loginInfo.getLogin_time()));
        message.append(" - System is halted by system administrator.");
        loginInfo.setMessage(message.toString());
    }

    private static LoginInfo createEntity(List<String> list, int size, String type) throws Exception {
        LoginInfo loginInfo = null;
        if (size == 13) {
            if (type.equals("0")) {
                loginInfo = new LoginInfo();
                loginInfo.setUsername("shutdown");
                loginInfo.setLoginType(list.get(1));
                loginInfo.setLogin_time(DateUtils.conversionTime(list.get(6)));
            }
            if (!Objects.isNull(loginInfo)) {
                queue.add(loginInfo);
            }
        } else if (size == 14) {
            if (type.equals("2")) {
                loginInfo = new LoginInfo();
                loginInfo.setUsername("reboot");
                loginInfo.setLoginType("~");
                loginInfo.setLogin_time(DateUtils.conversionTime(list.get(7)));
                if (!Objects.isNull(loginInfo)) {
                    queue.add(loginInfo);
                }
            } else if (type.equals("7")) {
                loginInfo = new LoginInfo();
                loginInfo.setUsername(list.get(0));
                loginInfo.setLoginType(list.get(2));
                loginInfo.setLogin_time(DateUtils.conversionTime(list.get(7)));
                cacheMap.put(list.get(4), loginInfo);
            } else if (type.equals("8")) {
                String tmpId = list.get(4);
                if (cacheMap.containsKey(tmpId)) {
                    loginInfo = cacheMap.remove(tmpId);
                    loginInfo.setLogout_time(DateUtils.conversionTime(list.get(7)));
                    loginInfo.setCost_time(DateUtils.betweenTime(loginInfo.getLogout_time(), loginInfo.getLogin_time()));
//                    cacheMap.remove(tmpId);
                }
                if (!Objects.isNull(loginInfo)) {
                    queue.add(loginInfo);
                }
            }

        } else if (size == 15) {
            if (type.equals("7")) {
                loginInfo = new LoginInfo();
                loginInfo.setUsername(list.get(0));
                loginInfo.setLoginType(list.get(2));
                loginInfo.setSrc_ip(list.get(8));
                loginInfo.setLogin_time(DateUtils.conversionTime(list.get(7)));
                cacheMap.put(list.get(4), loginInfo);
            }
        }
        return loginInfo;
    }


    private void writefile(String msg, String fileName) throws IOException {
        String outPath = wtmpconfig.getOutPath();
        File file = new File(outPath);
        if (!file.exists()) {
            logger.info(file.getName() + "  不存在...........");
            if (file.mkdir()) {
                logger.info(file.getName() + "  创建成功");
            } else {
                logger.info(file.getName() + "  创建失败");
            }
        }
        FileOutputStream fos = new FileOutputStream(file.getPath() + File.separator + fileName + ".log", true);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(msg);
        bw.newLine();
        bw.flush(); //将数据更新至文件
        bw.close();
        fos.close();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (Strings.isNotEmpty(wtmpconfig.getWtmpPath()) && Strings.isNotEmpty(wtmpconfig.getOutPath())) {
            loadFile();
        } else {
            logger.info("inputPath or outPath not exists");
        }

    }
}

