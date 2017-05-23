package com.miaxis.face.event;

import org.zz.faceapi.MXFaceInfo;

/**
 * Created by xu.nan on 2017/5/23.
 */

public class DrawRectEvent {

    private int faceNum;
    private MXFaceInfo[] faceInfos;

    public DrawRectEvent(int faceNum, MXFaceInfo[] faceInfos) {
        this.faceNum = faceNum;
        this.faceInfos = faceInfos;
    }

    public int getFaceNum() {
        return faceNum;
    }

    public void setFaceNum(int faceNum) {
        this.faceNum = faceNum;
    }

    public MXFaceInfo[] getFaceInfos() {
        return faceInfos;
    }

    public void setFaceInfos(MXFaceInfo[] faceInfos) {
        this.faceInfos = faceInfos;
    }
}
