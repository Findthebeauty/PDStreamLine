package com.shepherdboy.pdstreamline.beans;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;

import java.util.LinkedList;

public class TimestreamCombination {

    private String buyProductName;

    private String giveawayProductName;

    private Timestream buyTimestream;

    private Timestream giveawayTimestream;

    private boolean selfPromotion;//同一种产品自身买一赠一

    private int packageCount;

    public TimestreamCombination() {
    }

    public TimestreamCombination(Timestream t) {

        buyProductName = PDInfoWrapper.getProductName(t.getProductCode(),
                MyApplication.sqLiteDatabase);
        giveawayProductName = buyProductName;
        buyTimestream = t;
        giveawayTimestream = t;
        selfPromotion = true;
        packageCount = Integer.parseInt(t.getProductInventory()) / 2;
    }
    public TimestreamCombination(Timestream buyTimestream, Timestream giveawayTimestream) {

        buyProductName = PDInfoWrapper.getProductName(buyTimestream.getProductCode(),
                MyApplication.sqLiteDatabase);
        giveawayProductName = PDInfoWrapper.getProductName(giveawayTimestream.getProductCode(),
                MyApplication.sqLiteDatabase);
        this.buyTimestream = buyTimestream;
        this.giveawayTimestream = giveawayTimestream;
        selfPromotion = false;
        packageCount = Integer.parseInt(buyTimestream.getProductInventory());
    }

    public int getPackageCount() {
        return packageCount;
    }

    public void setPackageCount(int packageCount) {
        this.packageCount = packageCount;
    }

    public boolean isSelfPromotion() {
        return selfPromotion;
    }

    public void setSelfPromotion(boolean selfPromotion) {
        this.selfPromotion = selfPromotion;
    }

    public String getBuyProductName() {
        return buyProductName;
    }

    public void setBuyProductName(String buyProductName) {
        this.buyProductName = buyProductName;
    }

    public String getGiveawayProductName() {
        return giveawayProductName;
    }

    public void setGiveawayProductName(String giveawayProductName) {
        this.giveawayProductName = giveawayProductName;
    }

    public Timestream getBuyTimestream() {
        return buyTimestream;
    }

    public void setBuyTimestream(Timestream buyTimestream) {
        this.buyTimestream = buyTimestream;
    }

    public Timestream getGiveawayTimestream() {
        return giveawayTimestream;
    }

    public void setGiveawayTimestream(Timestream giveawayTimestream) {
        this.giveawayTimestream = giveawayTimestream;
    }

    /**
     * 将捆绑商品分解
     * @return
     */
    public LinkedList<Timestream> unpack() {

        LinkedList<Timestream> list = new LinkedList<>();

        Timestream.refresh(buyTimestream);
        list.add(buyTimestream);

        if (!selfPromotion) {

            Timestream.refresh(giveawayTimestream);
            list.add(giveawayTimestream);
        }

        return list;
    }
}
