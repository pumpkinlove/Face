package com.miaxis.face.util;

import android.os.Environment;

import java.io.File;
import java.util.Date;

/**
 * Created by Administrator on 2017/5/21 0021.
 */

public class LogUtil {

    public static final String LOG_NAME = "faceId_cw_log.txt";

    public static void writeLog(String content) {
        File log = new File(FileUtil.FACE_MAIN_PATH, LOG_NAME);
        FileUtil.writeFile(log, DateUtil.toAllms(new Date()) + "   " + content + "\r\n", true);
    }

}
