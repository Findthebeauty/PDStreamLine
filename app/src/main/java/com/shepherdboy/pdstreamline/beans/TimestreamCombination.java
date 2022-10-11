package com.shepherdboy.pdstreamline.beans;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;

import java.util.LinkedList;

public class TimestreamCombination {

    private String buyProductName;

    private String giveawayProductName;

    private Timestream buyTimestream;

    private Timestream giveawayTimestream;

    private int packageCount;

    public TimestreamCombination() {
    }

    public TimestreamCombination(Timestream t) {

        this(t, null);
    }
    public TimestreamCombination(Timestream buyTimestream, Timestream giveawayTimestream) {

        if(giveawayTimestream == null) {
            giveawayTimestream = new Timestream(buyTimestream);

            String inventory = String.valueOf(Integer.parseInt(buyTimestream.getProductInventory()) / 2);
            buyTimestream.setProductInventory(inventory);
            giveawayTimestream.setProductInventory(inventory);
        }

        buyProductName = PDInfoWrapper.getProductName(buyTimestream.getProductCode(),
                MyApplication.sqLiteDatabase);
        giveawayProductName = PDInfoWrapper.getProductName(giveawayTimestream.getProductCode(),
                MyApplication.sqLiteDatabase);
        this.buyTimestream = buyTimestream;
        this.giveawayTimestream = giveawayTimestream;
        packageCount = Integer.parseInt(buyTimestream.getProductInventory());

        buyTimestream.setBuySpecs("1");
        buyTimestream.setGiveawaySpecs("1");
        buyTimestream.setDiscountRate("1");
        buyTimestream.setSiblingPromotionId(giveawayTimestream.getId());
        giveawayTimestream.setBuySpecs("1");
        giveawayTimestream.setGiveawaySpecs("1");
        giveawayTimestream.setDiscountRate("0");
        giveawayTimestream.setSiblingPromotionId(buyTimestream.getId());

    }

    public int getPackageCount() {
        return packageCount;
    }

    public void setPackageCount(int packageCount) {
        this.packageCount = packageCount;
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

        Timestream.refresh(giveawayTimestream);
        list.add(giveawayTimestream);

        return list;
    }

    @Override
    public String toString() {
        return "TimestreamCombination{" +
                "buyProductName='" + buyProductName + '\'' +
                ", giveawayProductName='" + giveawayProductName + '\'' +
                ", buyTimestream=" + buyTimestream +
                ", giveawayTimestream=" + giveawayTimestream +
                ", packageCount=" + packageCount +
                '}';
    }
}
