package com.miaxis.face.event;

import com.miaxis.face.bean.Record;

import org.zz.faceapi.MXFaceInfo;

/**
 * Created by Administrator on 2017/5/22 0022.
 */

public class ResultEvent {

    public static final int FACE_SUCCESS            = 0;
    public static final int FACE_FAIL_HAS_FINGER    = 1;
    public static final int FINGER_SUCCESS          = 3;
    public static final int FAIL                    = 4;
    public static final int ID_PHOTO                = 5;

    private int result;
    private Record record;
    private MXFaceInfo faceInfo;

    public ResultEvent(int result, Record record) {
        this.result = result;
        this.record = record;
    }

    public ResultEvent(int result, Record record, MXFaceInfo faceInfo) {
        this.result = result;
        this.record = record;
        this.faceInfo = faceInfo;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public MXFaceInfo getFaceInfo() {
        return faceInfo;
    }

    public void setFaceInfo(MXFaceInfo faceInfo) {
        this.faceInfo = faceInfo;
    }
}
