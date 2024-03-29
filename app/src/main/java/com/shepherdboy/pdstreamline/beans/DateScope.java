package com.shepherdboy.pdstreamline.beans;

import java.util.UUID;

/**
 * 保质期范围，不同范围的保质期，临期时间，下架时间不一样
 */
public class DateScope {

    private String rangeValue;

    private String rangeUnit;

    private String promotionOffsetValue;

    private String promotionOffsetUnit;

    private String expireOffsetValue;

    private String expireOffsetUnit;

    private int scopeViewId;

    private final String scopeId;

    public DateScope(){

        scopeId = UUID.randomUUID().toString();
    }

    public DateScope(String[] info) {

        this();
        rangeValue = info[0];
        rangeUnit = info[1];
        promotionOffsetValue = info[2];
        promotionOffsetUnit = info[3];
        expireOffsetValue = info[4];
        expireOffsetUnit = info[5];
    }
    public DateScope(DateScope t) {

        this();
        rangeValue = t.getRangeValue();
        rangeUnit = t.getRangeUnit();
        promotionOffsetValue = t.getPromotionOffsetValue();
        promotionOffsetUnit = t.getPromotionOffsetUnit();
        expireOffsetValue = t.getExpireOffsetValue();
        expireOffsetUnit = t.getExpireOffsetUnit();
    }

    public String getScopeId() {
        return scopeId;
    }

    public int getScopeViewId() {
        return scopeViewId;
    }

    public void setScopeViewId(int scopeViewId) {
        this.scopeViewId = scopeViewId;
    }

    public String getRangeValue() {
        return rangeValue;
    }

    public void setRangeValue(String rangeValue) {
        this.rangeValue = rangeValue;
    }

    public String getRangeUnit() {
        return rangeUnit;
    }

    public void setRangeUnit(String rangeUnit) {
        this.rangeUnit = rangeUnit;
    }

    public String getPromotionOffsetValue() {
        return promotionOffsetValue;
    }

    public void setPromotionOffsetValue(String promotionOffsetValue) {
        this.promotionOffsetValue = promotionOffsetValue;
    }

    public String getPromotionOffsetUnit() {
        return promotionOffsetUnit;
    }

    public void setPromotionOffsetUnit(String promotionOffsetUnit) {
        this.promotionOffsetUnit = promotionOffsetUnit;
    }

    public String getExpireOffsetValue() {
        return expireOffsetValue;
    }

    public void setExpireOffsetValue(String expireOffsetValue) {
        this.expireOffsetValue = expireOffsetValue;
    }

    public String getExpireOffsetUnit() {
        return expireOffsetUnit;
    }

    public void setExpireOffsetUnit(String expireOffsetUnit) {
        this.expireOffsetUnit = expireOffsetUnit;
    }

    public String getRange() {

        return rangeValue + rangeUnit;
    }
}
