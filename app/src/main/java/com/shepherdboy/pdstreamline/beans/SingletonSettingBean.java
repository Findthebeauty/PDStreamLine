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

    private String nextSalesmanCheckDay;

    private String  autoCommitDelay;

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

        while (next.before(today)) {

            calendar.add(2, Calendar.DATE);
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
