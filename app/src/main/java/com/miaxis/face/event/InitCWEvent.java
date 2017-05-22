package com.miaxis.face.event;

/**
 * Created by Administrator on 2017/5/21 0021.
 */

public class InitCWEvent {

    public static final int ERR_LICENCE         = -2009;
    public static final int ERR_FILE_COMPARE    = -101;
    public static final int INIT_SUCCESS        = 0;

    private int result;

    public InitCWEvent(int result) {
        this.result = result;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
