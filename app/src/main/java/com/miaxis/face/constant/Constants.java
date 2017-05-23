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

    public static final int GPIO_INTERVAL = 100;

    public static final int GET_CARD_ID = 0;
    public static final int NO_CARD     = 134;

    public static final String[] FOLK = { "汉", "蒙古", "回", "藏", "维吾尔", "苗", "彝", "壮", "布依", "朝鲜",
            "满", "侗", "瑶", "白", "土家", "哈尼", "哈萨克", "傣", "黎", "傈僳", "佤", "畲",
            "高山", "拉祜", "水", "东乡", "纳西", "景颇", "柯尔克孜", "土", "达斡尔", "仫佬", "羌",
            "布朗", "撒拉", "毛南", "仡佬", "锡伯", "阿昌", "普米", "塔吉克", "怒", "乌孜别克",
            "俄罗斯", "鄂温克", "德昂", "保安", "裕固", "京", "塔塔尔", "独龙", "鄂伦春", "赫哲",
            "门巴", "珞巴", "基诺", "", "", "穿青人", "家人", "", "", "", "", "", "", "",
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
            "", "", "", "", "", "", "", "", "", "", "", "", "其他", "外国血统", "",
            "" };

    public static final int PHOTO_SIZE         = 38862;        // 解码后身份证图片长度
    public static final int mFingerDataSize    = 512;          // 指纹数据长度
    public static final int mFingerDataB64Size = 684;          // 指纹数据Base64编码后的长度

}
