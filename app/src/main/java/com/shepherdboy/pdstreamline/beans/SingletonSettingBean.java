package com.shepherdboy.pdstreamline.beans;

import com.alibaba.fastjson.annotation.JSONField;
import com.shepherdboy.pdstreamline.utils.DateUtil;

import java.util.Calendar;
import java.util.Date;

public class SingletonSettingBean {

    @JSONField(serialize = false)
    private boolean updated;

    private boolean autoCombine = true;

    private boolean autoCommitFlag;

    private Date nextSalesmanCheckDay;

    private String  autoCommitDelay;

    private static SingletonSettingBean singletonSettingBean;

    private SingletonSettingBean() {

        nextSalesmanCheckDay = DateUtil.switchDate(DateUtil.getStartPointToday(), Calendar.DATE, 1);
        updated = true;
    }

    public Date getNextSalesmanCheckDay() {

        Date today = DateUtil.getStartPointToday();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nextSalesmanCheckDay);

        while (nextSalesmanCheckDay.before(today)) {

            calendar.add(2, Calendar.DATE); //业务员隔天来一次
        }

        nextSalesmanCheckDay = calendar.getTime();

        return nextSalesmanCheckDay;
    }

    public void setNextSalesmanCheckDay(Date nextSalesmanCheckDay) {
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
