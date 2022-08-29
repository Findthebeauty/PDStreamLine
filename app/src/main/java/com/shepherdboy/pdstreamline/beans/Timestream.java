package com.shepherdboy.pdstreamline.beans;

import androidx.annotation.Nullable;

import com.shepherdboy.pdstreamline.activities.SettingActivity;
import com.shepherdboy.pdstreamline.utils.DateUtil;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Timestream {

    public static final int FRESH = 0;

    public static final int CLOSE_TO_EXPIRE = 1;

    public static final int EXPIRED = -1;

    private String id = null;

    /**
     * 去掉捆绑信息
     * @param t 从捆绑商品中分解出来的Timestream
     */
    public static void refresh(Timestream t) {
        t.setPromoting(false);
        t.setDiscountRate("");
        t.setGiveawaySpecs(null);
        t.setBuySpecs(null);
        t.setUpdated(false);
        t.setSiblingPromotionId(null);
        t.setInBasket(true);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private boolean updated = false; // 数据是否为最新，否则需要更新到数据库

    private String boundLayoutId = null; // 是否已绑定到布局中，默认为false

    private String productCode;

    private String productName;

    private Date productDOP;

    private Date productPromotionDate;

    private Date productExpireDate;

    private String productCoordinate;

    private String productInventory;

    private String buySpecs;

    private String giveawaySpecs;

    private String discountRate = "";

    private String siblingPromotionId;

    private boolean inBasket;

    private boolean promoting = false;

    public boolean isInBasket() {
        return inBasket;
    }

    public void setInBasket(boolean inBasket) {
        this.inBasket = inBasket;
    }

    // todo bug 正常日期有几率显示颜色为过期灰
    public int getTimeStreamStateCode() {

        if (productExpireDate == null) return FRESH;

        Date target = DateUtil.getStartPointToday();

        Date next = null;
        try {
            next = DateUtil.typeMach(SettingActivity.settingInstance.getNextSalesmanCheckDay());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //如果业务员明天不来，则提前将明天到期的选出
        if (!next.after(DateUtil.getStartPointToday())) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(target);
            calendar.add(Calendar.DATE, 1);
            target = calendar.getTime();
        }

        if (productExpireDate.before(target) || productExpireDate.equals(target)) {

            return EXPIRED;
        }

        if (productPromotionDate.before(target) || productPromotionDate.equals(target)) {

            return CLOSE_TO_EXPIRE;

        }

        return FRESH;
    }

    public String getBoundLayoutId() {
        return boundLayoutId;
    }

    public void setBoundLayoutId(String boundLayoutId) {
        this.boundLayoutId = boundLayoutId;
    }

    public String getBuySpecs() {
        return buySpecs;
    }

    public void setBuySpecs(String buySpecs) {
        this.buySpecs = buySpecs;
    }

    public String getGiveawaySpecs() {
        return giveawaySpecs;
    }

    public void setGiveawaySpecs(String giveawaySpecs) {
        this.giveawaySpecs = giveawaySpecs;
    }

    public String getSiblingPromotionId() {
        return siblingPromotionId;
    }

    public void setSiblingPromotionId(String siblingPromotionId) {
        this.siblingPromotionId = siblingPromotionId;
    }

    public String getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(String discountRate) {
        this.discountRate = discountRate;
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

    public String getShortCode() {

        int length = productCode.length();

        if (length < 6) return productCode;

        return productCode.substring(length - 6);
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

    public Timestream(Timestream t) {

        this();
        setPromoting(false);
        setProductDOP(t.getProductDOP());
        setInBasket(t.isInBasket());
        setProductCoordinate(t.getProductCoordinate());
        setProductDOP(t.getProductDOP());
        setProductExpireDate(t.getProductExpireDate());
        setProductPromotionDate(t.getProductPromotionDate());
        setProductInventory("1");
    }

    public Timestream(String productCode) {
        this();
        setProductCode(productCode);
    }

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
