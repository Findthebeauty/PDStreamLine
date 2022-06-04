package com.shepherdboy.pdstreamline.beans;

import com.alibaba.fastjson.annotation.JSONField;

public class SingletonSettingBean {

    @JSONField(serialize = false)
    private boolean updated;

    private boolean autoCommitFlag;

    private String  autoCommitDelay;

    private static SingletonSettingBean singletonSettingBean;

    private SingletonSettingBean() {

        updated = true;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
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
