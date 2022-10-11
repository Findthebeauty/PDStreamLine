package com.shepherdboy.pdstreamline.beans;

import com.shepherdboy.pdstreamline.utils.DateUtil;

import java.util.Date;

/**
 * 损耗，记录损耗信息
 */

public class ProductLoss {

    private String id;
    private String siblingProductCode;
    private String siblingProductDOP;
    private String lossProductCode;
    private String lossProductDOP;
    private String lossInventory;
    private String lossType;
    private String processDate;
    private String processAccount;
    private String processPhotoId;

    public ProductLoss() {
        this.processDate = DateUtil.typeMach(new Date());
    }

    public ProductLoss(TimestreamCombination combination) {

        this(combination, "赠品", String.valueOf(combination.getPackageCount()));
    }

    public ProductLoss(TimestreamCombination combination, String lossType, String lossInventory) {
        this();
        Timestream buyTimestream = combination.getBuyTimestream();
        Timestream giveawayTimestream = combination.getGiveawayTimestream();

        setSiblingProductCode(buyTimestream.getProductCode());
        setSiblingProductDOP(DateUtil.typeMach(buyTimestream.getProductDOP()));
        setLossProductCode(giveawayTimestream.getProductCode());
        setLossProductDOP(DateUtil.typeMach(giveawayTimestream.getProductDOP()));
        setLossType(lossType);
        setLossInventory(lossInventory);
        setProcessAccount("管理员");
        setProcessPhotoId("todo");
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSiblingProductCode() {
        return siblingProductCode;
    }

    public void setSiblingProductCode(String siblingProductCode) {
        this.siblingProductCode = siblingProductCode;
    }

    public String getSiblingProductDOP() {
        return siblingProductDOP;
    }

    public void setSiblingProductDOP(String siblingProductDOP) {
        this.siblingProductDOP = siblingProductDOP;
    }

    public String getLossProductCode() {
        return lossProductCode;
    }

    public void setLossProductCode(String lossProductCode) {
        this.lossProductCode = lossProductCode;
    }

    public String getLossProductDOP() {
        return lossProductDOP;
    }

    public void setLossProductDOP(String lossProductDOP) {
        this.lossProductDOP = lossProductDOP;
    }

    public String getLossInventory() {
        return lossInventory;
    }

    public void setLossInventory(String lossInventory) {
        this.lossInventory = lossInventory;
    }

    public String getLossType() {
        return lossType;
    }

    public void setLossType(String lossType) {
        this.lossType = lossType;
    }

    public String getProcessDate() {
        return processDate;
    }

    public void setProcessDate(String processDate) {
        this.processDate = processDate;
    }

    public String getProcessAccount() {
        return processAccount;
    }

    public void setProcessAccount(String processAccount) {
        this.processAccount = processAccount;
    }

    public String getProcessPhotoId() {
        return processPhotoId;
    }

    public void setProcessPhotoId(String processPhotoId) {
        this.processPhotoId = processPhotoId;
    }
}
