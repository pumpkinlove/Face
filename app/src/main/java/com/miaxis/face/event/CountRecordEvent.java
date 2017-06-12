package com.miaxis.face.event;

/**
 * Created by xu.nan on 2017/6/8.
 */

public class CountRecordEvent {

    private long notUpCount;
    private long count;

    public CountRecordEvent() {
    }

    public CountRecordEvent(long notUpCount, long count) {
        this.notUpCount = notUpCount;
        this.count = count;
    }

    public long getNotUpCount() {
        return notUpCount;
    }

    public void setNotUpCount(long notUpCount) {
        this.notUpCount = notUpCount;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
