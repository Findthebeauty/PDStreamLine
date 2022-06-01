package com.shepherdboy.pdstreamline.beans;

public class SingletonSettingBean {

    private boolean autoCommitFlag;

    private String  autoCommitDelay;

    private static SingletonSettingBean singletonSettingBean;

    private SingletonSettingBean() {

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
