package com.shepherdboy.pdstreamline.beans;

import com.shepherdboy.pdstreamline.utils.AscDateComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Product {

    private String productCode;

    private String productName;

    private String productEXP;

    private String productEXPTimeUnit;

    private String productGroupNumber;

    private String productSpec;

    private String shelvesIndexes;

    private String defaultCoordinate;

    private LinkedHashMap<String, Timestream> timeStreams; // key: timestream的id

    private boolean updated = false;

    private String lastCheckDate;

    private String nextCheckDate;

    public String getProductSpec() {
        return productSpec;
    }

    public void setProductSpec(String productSpec) {
        this.productSpec = productSpec;
    }

    public String getLastCheckDate() {
        return lastCheckDate;
    }

    public void setLastCheckDate(String lastCheckDate) {
        this.lastCheckDate = lastCheckDate;
    }

    public String getNextCheckDate() {
        return nextCheckDate;
    }

    public void setNextCheckDate(String nextCheckDate) {
        this.nextCheckDate = nextCheckDate;
    }

    public String getProductGroupNumber() {
        return productGroupNumber;
    }

    public void setProductGroupNumber(String productGroupNumber) {
        this.productGroupNumber = productGroupNumber;
    }

    public String getShelvesIndexes() {
        return shelvesIndexes;
    }

    public void setShelvesIndexes(String shelvesIndexes) {
        this.shelvesIndexes = shelvesIndexes;
    }

    public String getDefaultCoordinate() {
        return defaultCoordinate;
    }

    public void setDefaultCoordinate(String defaultCoordinate) {
        this.defaultCoordinate = defaultCoordinate;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductEXP() {
        return productEXP;
    }

    public void setProductEXP(String productEXP) {
        this.productEXP = productEXP;
    }

    public String getProductEXPTimeUnit() {
        return productEXPTimeUnit;
    }

    public void setProductEXPTimeUnit(String productEXPTimeUnit) {
        this.productEXPTimeUnit = productEXPTimeUnit;
    }

    public LinkedHashMap<String, Timestream> getTimeStreams() {

        if (timeStreams == null) {

            timeStreams = new LinkedHashMap<>();

        }

        // 如果timestream数目大于1，则对timestream按照生产日期先后进行排序
        if (timeStreams.size() > 1) {

            Set<Map.Entry<String, Timestream>> entrySet = timeStreams.entrySet();
            List<Map.Entry<String, Timestream>> list = new ArrayList<>(entrySet);

            Collections.sort(list, AscDateComparator.getInstance());

            timeStreams = new LinkedHashMap<>();

            for (Map.Entry<String, Timestream> entry : list) {

                timeStreams.put(entry.getKey(), entry.getValue());
            }

        }

        return timeStreams;
    }

    public void setTimeStreams(LinkedHashMap<String, Timestream> timeStreams) {
        this.timeStreams = timeStreams;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productCode='" + productCode + '\'' +
                ", productName='" + productName + '\'' +
                ", productEXP='" + productEXP + '\'' +
                ", productEXPTimeUnit='" + productEXPTimeUnit + '\'' +
                ", productGroupNumber='" + productGroupNumber + '\'' +
                ", shelvesIndexes='" + shelvesIndexes + '\'' +
                ", defaultCoordinate='" + defaultCoordinate + '\'' +
                ", timeStreams=" + timeStreams +
                ", updated=" + updated +
                ", lastCheckDate='" + lastCheckDate + '\'' +
                ", nextCheckDate='" + nextCheckDate + '\'' +
                '}';
    }
}
