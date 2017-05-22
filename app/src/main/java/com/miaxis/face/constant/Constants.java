package com.miaxis.face.constant;

/**
 * Created by Administrator on 2017/5/18 0018.
 */

public class Constants {

    public static final int PRE_WIDTH = 320;
    public static final int PRE_HEIGHT = 240;

    public static final int PIC_WIDTH = 640;
    public static final int PIC_HEIGHT = 480;

    public static final int CP_WIDTH = 1280;
    public static final int CP_HEIGHT = 960;
    public static final float zoomRate = CP_WIDTH / PRE_WIDTH;

    public static final int MAX_FACE_NUM       = 5;

    public static float PASS_SCORE             = 0.71f;        // 比对通过阈值

}
