package com.vivo.hdp.sleep;

/**
 * Created by 10957084 on 2017/11/1.
 */

public class SleepInfo {
    private Integer id;
    private Integer brightTime;
    private Integer blackTime;
    private String packageName;


    public Integer getBrightTime() {
        return brightTime;
    }

    public void setBrightTime(Integer brightTime) {
        this.brightTime = brightTime;
    }

    public Integer getBlackTime() {
        return blackTime;
    }

    public void setBlackTime(Integer blackTime) {
        this.blackTime = blackTime;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
