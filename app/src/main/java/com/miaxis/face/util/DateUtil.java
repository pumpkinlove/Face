package com.miaxis.face.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Administrator on 2017/3/26 0026.
 */

public class DateUtil {
    public static String toHourMinString(Date date) {
        SimpleDateFormat myFmt = new SimpleDateFormat("HH:mm:ss");
        return myFmt.format(date);
    }

    public static String toMonthDay(Date date) {
        SimpleDateFormat myFmt = new SimpleDateFormat("yyyy-MM-dd");
        return myFmt.format(date);
    }

    public static String toAll(Date date) {
        SimpleDateFormat myFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return myFmt.format(date);
    }

    public static String toAllms(Date date) {
        SimpleDateFormat myFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        return myFmt.format(date);
    }

    public static Date fromMonthDay(String date) throws ParseException {
        SimpleDateFormat myFmt = new SimpleDateFormat("yyyy-MM-dd");
        return myFmt.parse(date);
    }

    public static Date addOneDay(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);  //把日期往后增加一天.整数往后推,负数往前移动
        return calendar.getTime();      //这个时间就是日期往后推一天的结果
    }
}
