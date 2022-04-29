package com.shepherdboy.pdstreamline.beans;

import androidx.annotation.Nullable;

import com.shepherdboy.pdstreamline.utils.DateUtil;

import java.util.Date;
import java.util.UUID;

public class Timestream {

    private String id = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private boolean updated = false; // 数据是否为最新，否则需要更新到数据库

    public String getBoundLayoutId() {
        return boundLayoutId;
    }

    public void setBoundLayoutId(String boundLayoutId) {
        this.boundLayoutId = boundLayoutId;
    }

    private String boundLayoutId = null; // 是否已绑定到布局中，默认为false

    private String productCode;

    private String productName;

    private Date productDOP;

    private Date productPromotionDate;

    private Date productExpireDate;

    private String productCoordinate;

    private String productInventory;

    private boolean promoting = false;

    // todo bug 正常日期有几率显示颜色为过期灰
    public int getTimeStreamStateCode() {

        if (productExpireDate == null) {

            return 0;
        }

        if (productExpireDate.before(DateUtil.getNow()) || productExpireDate.equals(DateUtil.getNow())) {

            return -1;
        }

        if (productPromotionDate.before(DateUtil.getNow()) || productPromotionDate.equals(DateUtil.getNow())) {

            return 1;

        }

        return 0;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }


    public boolean isPromoting() {
        return promoting;
    }

    public void setPromoting(boolean promoting) {
        this.promoting = promoting;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductCode() {
        return productCode;
    }

    public Date getProductDOP() {
        return productDOP;
    }

    public void setProductDOP(Date productDOP) {
        this.productDOP = productDOP;
    }

    public Date getProductPromotionDate() {
        return productPromotionDate;
    }

    public void setProductPromotionDate(Date productPromotionDate) {
        this.productPromotionDate = productPromotionDate;
    }

    public Date getProductExpireDate() {
        return productExpireDate;
    }

    public void setProductExpireDate(Date productExpireDate) {
        this.productExpireDate = productExpireDate;
    }

    public String getProductCoordinate() {
        return productCoordinate;
    }

    public void setProductCoordinate(String productCoordinate) {
        this.productCoordinate = productCoordinate;
    }

    public String getProductInventory() {
        return productInventory;
    }

    public void setProductInventory(String productInventory) {
        this.productInventory = productInventory;
    }

    public Timestream() {
        setId(UUID.randomUUID().toString());
    }

    ;

    public Timestream(String productCode) {
        this();
        setProductCode(productCode);
    }

    ;

    public Timestream(String productCode, String productEXP, String productEXPTimeUnit, Date productDOP,
                      @Nullable String productCoordinate, @Nullable String productInventory) {
        this(productCode);
        setProductDOP(productDOP);
        setProductPromotionDate(DateUtil.calculatePromotionDate(productDOP, Integer.parseInt(productEXP),
                productEXPTimeUnit));
        setProductExpireDate(DateUtil.calculateProductExpireDate(productDOP, Integer.parseInt(productEXP),
                productEXPTimeUnit));
        setProductCoordinate(productCoordinate);
        setProductInventory(productInventory);
    }

    @Override
    public String toString() {
        return "Timestream{" +
                "id='" + id + '\'' +
                ", updated=" + updated +
                ", boundLayoutId='" + boundLayoutId + '\'' +
                ", productCode='" + productCode + '\'' +
                ", productDOP=" + productDOP +
                ", productPromotionDate=" + productPromotionDate +
                ", productExpireDate=" + productExpireDate +
                ", productCoordinate='" + productCoordinate + '\'' +
                ", productInventory='" + productInventory + '\'' +
                ", promoting=" + promoting +
                '}';
    }
}
