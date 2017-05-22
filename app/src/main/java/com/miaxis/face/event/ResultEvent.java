package com.miaxis.face.event;

import org.zz.faceapi.MXFaceInfo;

/**
 * Created by Administrator on 2017/5/22 0022.
 */

public class ResultEvent {

    public static final int FACE_SUCCESS            = 0;
    public static final int FACE_FAIL_HAS_FINGER    = 1;
    public static final int FACE_FAIL_NO_FINGER     = 2;
    public static final int FINGER_SUCCESS          = 3;
    public static final int FINGER_FAIL             = 4;

    private int result;
    private MXFaceInfo faceInfo;
    private byte[] cameraData;

}
