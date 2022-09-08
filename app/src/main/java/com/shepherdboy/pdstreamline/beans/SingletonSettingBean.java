package com.shepherdboy.pdstreamline.beans;

import com.alibaba.fastjson.annotation.JSONField;
import com.shepherdboy.pdstreamline.utils.DateUtil;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class SingletonSettingBean {

    @JSONField(serialize = false)
    private boolean updated;

    private boolean autoCombine = true;

    private boolean autoCommitFlag;
    private boolean doubleClickFlag;
    private boolean longClickFlag;

    private String nextSalesmanCheckDay;

    private String autoCommitDelay;
    private String doubleClickDelay;
    private String longClickDelay;
    private String lastSyncTime;

    private static SingletonSettingBean singletonSettingBean;

    private SingletonSettingBean() {

        updated = true;
    }

    public String getNextSalesmanCheckDay() {

        Date today = DateUtil.getStartPointToday();

        Calendar calendar = Calendar.getInstance();

        Date next = null;
        try {
            next = DateUtil.typeMach(nextSalesmanCheckDay);
            calendar.setTime(next);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        assert next != null;
        while (next.before(today)) {

            calendar.add(Calendar.DATE, 2);
            next = calendar.getTime();
        }

        nextSalesmanCheckDay = DateUtil.typeMach(calendar.getTime());

        return nextSalesmanCheckDay;
    }

    public void setNextSalesmanCheckDay(String nextSalesmanCheckDay) {
        this.nextSalesmanCheckDay = nextSalesmanCheckDay;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public boolean isAutoCombine() {
        return autoCombine;
    }

    public void setAutoCombine(boolean autoCombine) {
        this.autoCombine = autoCombine;
    }

    public boolean isAutoCommitFlag() {
        return autoCommitFlag;
    }

    public void setAutoCommitFlag(boolean autoCommitFlag) {
        this.autoCommitFlag = autoCommitFlag;
    }

    public String getAutoCommitDelay() {
        return autoCommitDelay;
    }

    public void setAutoCommitDelay(String autoCommitDelay) {
        this.autoCommitDelay = autoCommitDelay;
    }

    public String getLongClickDelay() {
        return longClickDelay;
    }

    public void setLongClickDelay(String longClickDelay) {
        this.longClickDelay = longClickDelay;
    }

    public boolean isDoubleClickFlag() {
        return doubleClickFlag;
    }

    public void setDoubleClickFlag(boolean doubleClickFlag) {
        this.doubleClickFlag = doubleClickFlag;
    }

    public boolean isLongClickFlag() {
        return longClickFlag;
    }

    public void setLongClickFlag(boolean longClickFlag) {
        this.longClickFlag = longClickFlag;
    }

    public String getDoubleClickDelay() {
        return doubleClickDelay;
    }

    public void setDoubleClickDelay(String doubleClickDelay) {
        this.doubleClickDelay = doubleClickDelay;
    }

    public String getLastSyncTime() {
        if (lastSyncTime == null) return "1970-1-1 0:0:0";
        return lastSyncTime;
    }

    public void setLastSyncTime(String lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public static void setSingletonSettingBean(SingletonSettingBean singletonSettingBean) {
        SingletonSettingBean.singletonSettingBean = singletonSettingBean;
    }

    public static SingletonSettingBean getSingletonSettingBean() {

        if (singletonSettingBean == null) {

            singletonSettingBean = new SingletonSettingBean();
        }

        return singletonSettingBean;
    }

}
