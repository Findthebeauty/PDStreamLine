package com.shepherdboy.pdstreamline.beanview;

import static com.shepherdboy.pdstreamline.MyApplication.allProducts;
import static com.shepherdboy.pdstreamline.MyApplication.combinationHashMap;
import static com.shepherdboy.pdstreamline.MyApplication.onShowCombsHashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.beans.TimestreamCombination;
import com.shepherdboy.pdstreamline.utils.DateUtil;
import com.shepherdboy.pdstreamline.view.MyInfoChangeWatcher;

import java.util.ArrayList;

public class TimestreamCombinationView extends LinearLayout implements BeanView{

    private LinearLayout buyBackground;
    private LinearLayout giveawayBackground;
    private TextView buyProductNameTv;
    private TextView giveawayProductNameTv;
    private EditText buyDOPEt;
    private EditText inventory;
    private TextView unitTv;
    private TextView giveawayDOPTv;
    private TimestreamCombination timestreamCombination = null;
    private ArrayList<View> giveAwayViews;

    private String productCode;

    public TimestreamCombinationView(Context context, Timestream timestream) {
        this(context);

        bindData(timestream);
    }

    public TimestreamCombinationView(Context context) {
        super(context);

        initView(context);
    }

    private void initView(Context context) {
        this.setId(View.generateViewId());

        LayoutInflater inflater = LayoutInflater.from(context);
        ConstraintLayout combination = inflater.inflate(R.layout.comb_layout, null).findViewById(R.id.combination);

        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setPadding(1,0,1,0);
        this.setBackground(getResources().getDrawable(R.drawable.timestream_border));
        this.addView(combination,lp);

        buyBackground = combination.findViewById(R.id.buy_background);
        giveawayBackground = combination.findViewById(R.id.give_away_background);

        buyProductNameTv = combination.findViewById(R.id.buy_pd_name);
        giveawayProductNameTv = combination.findViewById(R.id.give_away_pd_name);
        TextView buyDOPMeasure = combination.findViewById(R.id.buy_dop_measure);

        buyDOPEt = combination.findViewById(R.id.buy_dop);
        inventory = combination.findViewById(R.id.inventory);
        EditText inventoryMeasure = combination.findViewById(R.id.inventory_measure);

        unitTv = combination.findViewById(R.id.unit);
        giveawayDOPTv = combination.findViewById(R.id.give_away_dop);
        TextView giveawayFlagTv = combination.findViewById(R.id.give_away_flag);

        giveAwayViews = new ArrayList<>();
        giveAwayViews.add(giveawayBackground);
        giveAwayViews.add(giveawayProductNameTv);
        giveAwayViews.add(giveawayDOPTv);
        giveAwayViews.add(giveawayFlagTv);

        for (View v : giveAwayViews) {

            v.setVisibility(View.GONE);
        }

        flushIds(combination);

        ConstraintSet set = new ConstraintSet();

        set.clone(combination);

        set.connect(buyBackground.getId(),ConstraintSet.LEFT, combination.getId(), ConstraintSet.LEFT);
        set.connect(buyBackground.getId(),ConstraintSet.RIGHT, combination.getId(), ConstraintSet.RIGHT);
        set.connect(buyBackground.getId(),ConstraintSet.TOP, combination.getId(), ConstraintSet.TOP);
        set.connect(buyBackground.getId(),ConstraintSet.BOTTOM, buyProductNameTv.getId(), ConstraintSet.BOTTOM);

        set.connect(giveawayBackground.getId(),ConstraintSet.LEFT, combination.getId(), ConstraintSet.LEFT);
        set.connect(giveawayBackground.getId(),ConstraintSet.RIGHT, combination.getId(), ConstraintSet.RIGHT);
        set.connect(giveawayBackground.getId(),ConstraintSet.TOP, giveawayProductNameTv.getId(), ConstraintSet.TOP);
        set.connect(giveawayBackground.getId(),ConstraintSet.BOTTOM, combination.getId(), ConstraintSet.BOTTOM);

        set.connect(buyProductNameTv.getId(), ConstraintSet.LEFT, combination.getId(), ConstraintSet.LEFT);
        set.connect(buyProductNameTv.getId(),ConstraintSet.RIGHT, buyDOPMeasure.getId(), ConstraintSet.LEFT);
        set.connect(buyProductNameTv.getId(),ConstraintSet.TOP, combination.getId(), ConstraintSet.TOP);

        set.connect(giveawayProductNameTv.getId(),ConstraintSet.LEFT, combination.getId(), ConstraintSet.LEFT);
        set.connect(giveawayProductNameTv.getId(),ConstraintSet.RIGHT, buyDOPMeasure.getId(), ConstraintSet.LEFT);
        set.connect(giveawayProductNameTv.getId(),ConstraintSet.TOP, buyProductNameTv.getId(), ConstraintSet.BOTTOM);

        set.connect(buyDOPMeasure.getId(),ConstraintSet.RIGHT, inventoryMeasure.getId(), ConstraintSet.LEFT);
        set.connect(buyDOPMeasure.getId(),ConstraintSet.BOTTOM, buyProductNameTv.getId(), ConstraintSet.BOTTOM);
        set.connect(buyDOPMeasure.getId(),ConstraintSet.TOP, buyProductNameTv.getId(), ConstraintSet.TOP);

        set.connect(buyDOPEt.getId(),ConstraintSet.BOTTOM, buyProductNameTv.getId(), ConstraintSet.BOTTOM);
        set.connect(buyDOPEt.getId(),ConstraintSet.TOP, buyProductNameTv.getId(), ConstraintSet.TOP);
        set.connect(buyDOPEt.getId(),ConstraintSet.LEFT, buyDOPMeasure.getId(), ConstraintSet.LEFT);
        set.connect(buyDOPEt.getId(),ConstraintSet.RIGHT, buyDOPMeasure.getId(), ConstraintSet.RIGHT);

        set.connect(inventoryMeasure.getId(),ConstraintSet.RIGHT, unitTv.getId(), ConstraintSet.LEFT);
        set.connect(inventoryMeasure.getId(),ConstraintSet.TOP, combination.getId(), ConstraintSet.TOP);
        set.connect(inventoryMeasure.getId(),ConstraintSet.BOTTOM, combination.getId(), ConstraintSet.BOTTOM);

        set.connect(inventory.getId(),ConstraintSet.BOTTOM, combination.getId(), ConstraintSet.BOTTOM);
        set.connect(inventory.getId(),ConstraintSet.LEFT, inventoryMeasure.getId(), ConstraintSet.LEFT);
        set.connect(inventory.getId(),ConstraintSet.RIGHT, inventoryMeasure.getId(), ConstraintSet.RIGHT);
        set.connect(inventory.getId(),ConstraintSet.TOP, combination.getId(), ConstraintSet.TOP);

        set.connect(unitTv.getId(),ConstraintSet.RIGHT, combination.getId(), ConstraintSet.RIGHT);
        set.connect(unitTv.getId(),ConstraintSet.TOP, combination.getId(), ConstraintSet.TOP);
        set.connect(unitTv.getId(),ConstraintSet.BOTTOM, combination.getId(), ConstraintSet.BOTTOM);

        set.connect(giveawayDOPTv.getId(),ConstraintSet.BOTTOM, combination.getId(), ConstraintSet.BOTTOM);
        set.connect(giveawayDOPTv.getId(),ConstraintSet.LEFT, buyDOPMeasure.getId(), ConstraintSet.LEFT);
        set.connect(giveawayDOPTv.getId(),ConstraintSet.RIGHT, buyDOPMeasure.getId(), ConstraintSet.RIGHT);
        set.connect(giveawayDOPTv.getId(),ConstraintSet.TOP, buyProductNameTv.getId(), ConstraintSet.BOTTOM);

        set.connect(giveawayFlagTv.getId(),ConstraintSet.LEFT, buyDOPMeasure.getId(), ConstraintSet.LEFT);
        set.connect(giveawayFlagTv.getId(),ConstraintSet.RIGHT, buyDOPMeasure.getId(), ConstraintSet.RIGHT);
        set.connect(giveawayFlagTv.getId(),ConstraintSet.BOTTOM, giveawayDOPTv.getId(), ConstraintSet.BOTTOM);
        set.connect(giveawayFlagTv.getId(),ConstraintSet.TOP, giveawayDOPTv.getId(), ConstraintSet.TOP);

        set.applyTo(combination);

    }

    public void bindData(Timestream timestream) {

        this.productCode = timestream.getProductCode();

        String discountRate = timestream.getDiscountRate();

        MyApplication.onShowTimeStreamsHashMap.put(this.getId(), timestream);
        timestream.setBoundLayoutId(String.valueOf(this.getId()));

        switch (discountRate) {

            case "":

                buyProductNameTv.setText(timestream.getProductName());
                buyDOPEt.setText(DateUtil.typeMach(timestream.getProductDOP()).substring(0,10));
                inventory.setText(timestream.getProductInventory());
                unitTv.setText(allProducts.get(timestream.getProductCode()).getProductSpec());

                int color = MyApplication.getColorByTimestreamStateCode(timestream.getTimeStreamStateCode());

                buyBackground.setBackgroundColor(color);

                break;

            case "0.5":

                for (View v : giveAwayViews) {

                    v.setVisibility(View.VISIBLE);
                }

                timestreamCombination = combinationHashMap.get(timestream.getId());

                break;

            case "0":

                for (View v : giveAwayViews) {

                    v.setVisibility(View.VISIBLE);
                }

                timestreamCombination = combinationHashMap.get(timestream.getSiblingPromotionId());


                MyApplication.onShowTimeStreamsHashMap.put(this.getId(), timestreamCombination.getBuyTimestream());
                timestreamCombination.getBuyTimestream().setBoundLayoutId(String.valueOf(this.getId()));
                break;

            default:
                break;
        }

        if (timestreamCombination != null) {

            onShowCombsHashMap.put(this.getId(), timestreamCombination);

            buyProductNameTv.setText(timestreamCombination.getBuyProductName());
            buyDOPEt.setText(DateUtil.typeMach(timestreamCombination.getBuyTimestream()
                    .getProductDOP()).substring(0,10));
            inventory.setText(timestreamCombination.getPackageCount());
            unitTv.setText("组");

            giveawayProductNameTv.setText(timestreamCombination.getGiveawayProductName());
            giveawayDOPTv.setText(DateUtil.typeMach(timestreamCombination.getGiveawayTimestream()
                    .getProductDOP()).substring(0,10));


            int color = MyApplication.getColorByTimestreamStateCode(timestreamCombination.getBuyTimestream().getTimeStreamStateCode());
            buyBackground.setBackgroundColor(color);


            color = MyApplication.getColorByTimestreamStateCode(timestreamCombination.getGiveawayTimestream().getTimeStreamStateCode());
            giveawayBackground.setBackgroundColor(color);

            MyInfoChangeWatcher.watch(buyDOPEt,timestreamCombination.getBuyTimestream(),MyApplication.TIMESTREAM_DOP,true);
            MyInfoChangeWatcher.watch(inventory,timestreamCombination.getBuyTimestream(),MyApplication.TIMESTREAM_INVENTORY,true);

        } else {

            MyInfoChangeWatcher.watch(buyDOPEt,timestream,MyApplication.TIMESTREAM_DOP,true);
            MyInfoChangeWatcher.watch(inventory,timestream,MyApplication.TIMESTREAM_INVENTORY,true);
        }

    }

    public EditText getBuyDOPEt() {
        return buyDOPEt;
    }

    public TextView getBuyProductNameTv() {
        return buyProductNameTv;
    }

    public EditText getInventory() {
        return inventory;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    /**
     * 重设模板中view的id为不重复的随机值，避免id冲突
     * @param view
     */
    private static void flushIds(View view) {

        view.setId(View.generateViewId());

        if (view instanceof ViewGroup) {

            ViewGroup viewGroup = (ViewGroup)view;

            for (int i = 0; i < viewGroup.getChildCount(); i++) {

                flushIds(viewGroup.getChildAt(i));
            }
        }
    }
}
