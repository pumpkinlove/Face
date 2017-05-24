package com.miaxis.face.util;

import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Base64;

import com.miaxis.face.constant.Constants;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;

/**
 * Created by xu.nan on 2017/5/23.
 */

public class MyUtil {
    public static String unicode2String(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length / 2; i++) {
            int a = bytes[2 * i + 1];
            if (a < 0) {
                a = a + 256;
            }
            int b = bytes[2 * i];
            if (b < 0) {
                b = b + 256;
            }
            int c = (a << 8) | b;
            sb.append((char) c);
        }
        return sb.toString();
    }

    public static String getYUVBase64(byte[] cameraData, int format) {
        YuvImage yuvImg = new YuvImage(cameraData, format, Constants.PRE_WIDTH, Constants.PRE_HEIGHT, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImg.compressToJpeg(new Rect(0, 0, Constants.PRE_WIDTH, Constants.PRE_HEIGHT), 100, out);
        byte[] bytes = out.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static String getSerialNumber(){
        String serial = null;
        try {
            Class<?> c =Class.forName("android.os.SystemProperties");
            Method get =c.getMethod("get", String.class);
            serial = (String)get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }
}
