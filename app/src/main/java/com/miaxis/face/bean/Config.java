package com.miaxis.face.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/18 0018.
 */
@Entity
public class Config implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private long id;
    private String ip;
    private int port;
    private String upTime;
    private float  passScore;
    private String banner;
    private int intervalTime;
    private String orgId;
    private String orgName;
    private boolean fingerFlag;
    private boolean netFlag;
    private boolean queryFlag;

    @Generated(hash = 1607863072)
    public Config(long id, String ip, int port, String upTime, float passScore,
            String banner, int intervalTime, String orgId, String orgName,
            boolean fingerFlag, boolean netFlag, boolean queryFlag) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.upTime = upTime;
        this.passScore = passScore;
        this.banner = banner;
        this.intervalTime = intervalTime;
        this.orgId = orgId;
        this.orgName = orgName;
        this.fingerFlag = fingerFlag;
        this.netFlag = netFlag;
        this.queryFlag = queryFlag;
    }

    @Generated(hash = 589037648)
    public Config() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUpTime() {
        return upTime;
    }

    public void setUpTime(String upTime) {
        this.upTime = upTime;
    }

    public float getPassScore() {
        return passScore;
    }

    public void setPassScore(float passScore) {
        this.passScore = passScore;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public int getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(int intervalTime) {
        this.intervalTime = intervalTime;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public boolean isFingerFlag() {
        return fingerFlag;
    }

    public void setFingerFlag(boolean fingerFlag) {
        this.fingerFlag = fingerFlag;
    }

    public boolean isNetFlag() {
        return netFlag;
    }

    public void setNetFlag(boolean netFlag) {
        this.netFlag = netFlag;
    }

    public boolean getFingerFlag() {
        return this.fingerFlag;
    }

    public boolean getNetFlag() {
        return this.netFlag;
    }

    public boolean isQueryFlag() {
        return queryFlag;
    }

    public void setQueryFlag(boolean queryFlag) {
        this.queryFlag = queryFlag;
    }

    public boolean getQueryFlag() {
        return this.queryFlag;
    }
}
