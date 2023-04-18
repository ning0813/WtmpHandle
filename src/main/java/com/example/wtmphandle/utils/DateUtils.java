package com.example.wtmphandle.utils;

import org.apache.logging.log4j.util.Strings;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @ClassName： DateUtils
 * @description:
 * @author: ning.yang
 * @create: 2023/4/13 11:52
 */
public class DateUtils {

    public static String conversionTime(String timeStamp) {
        //yyyy-MM-dd HH:mm:ss 转换的时间格式  可以自定义
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //转换
        String time = sdf.format(new Date(Long.parseLong(timeStamp) * 1000L));

        return time;
    }




    public static String betweenTime(String endDate, String startDate) throws Exception {

        long nd =  1000* 24 * 60 * 60;
        long nh =  1000*60 * 60;
        long nm =  1000*60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        DateFormat fmt =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        long diff = fmt.parse(endDate).getTime() - fmt.parse(startDate).getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        String resultHour = null;
        if (hour < 12) {
            resultHour = "0" + hour;
        } else {
            resultHour = String.valueOf(hour);
        }
        String resultMin = null;
        if (min<10) {
            resultMin = "0" + min;
        } else {
            resultMin = String.valueOf(min);
        }
        return resultHour + ":" + resultMin;
    }
    public static String DateToMMMDD(String dateTime){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = dateFormat.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat fmt =new SimpleDateFormat("MMM d HH:mm", Locale.ENGLISH);
        String format = fmt.format(date);
        return  fmt.format(date);
    }
    public static String DateToHHMM(String dateTime){
        if (Strings.isEmpty(dateTime)) {
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = dateFormat.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat fmt =new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        String format = fmt.format(date);
        return  fmt.format(date);
    }


}
