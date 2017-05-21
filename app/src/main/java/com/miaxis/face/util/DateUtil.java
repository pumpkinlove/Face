package com.miaxis.face.util;

import java.text.SimpleDateFormat;
import java.util.Date;

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
}
