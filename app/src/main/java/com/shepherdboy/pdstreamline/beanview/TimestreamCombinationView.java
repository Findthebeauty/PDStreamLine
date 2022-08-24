package com.shepherdboy.pdstreamline.beanview;

import static com.shepherdboy.pdstreamline.MyApplication.allProducts;
import static com.shepherdboy.pdstreamline.MyApplication.combinationHashMap;

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

import java.util.ArrayList;

public class TimestreamCombinationView extends LinearLayout {



    public TimestreamCombinationView(Context context, Timestream timestream) {
        super(context);

        this.setId(View.generateViewId());

        LayoutInflater inflater = LayoutInflater.from(context);
        ConstraintLayout combination = inflater.inflate(R.layout.comb_layout_1, null).findViewById(R.id.combination);

        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(2,2,2,2);
        combination.setBackground(getResources().getDrawable(R.drawable.timestream_border));

        this.addView(combination,lp);

        LinearLayout buyBackground = combination.findViewById(R.id.buy_background);
        LinearLayout giveawayBackground = combination.findViewById(R.id.give_away_background);

        TextView buyProductNameTv = combination.findViewById(R.id.buy_pd_name);
        TextView giveawayProductNameTv = combination.findViewById(R.id.give_away_pd_name);
        TextView buyDOPMeasure = combination.findViewById(R.id.buy_dop_measure);

        EditText buyDOPEt = combination.findViewById(R.id.buy_dop);
        EditText inventory = combination.findViewById(R.id.inventory);
        EditText inventoryMeasure = combination.findViewById(R.id.inventory_measure);

        TextView unitTv = combination.findViewById(R.id.unit);
        TextView giveawayDOPTv = combination.findViewById(R.id.give_away_dop);
        TextView giveawayFlagTv = combination.findViewById(R.id.give_away_flag);

        ArrayList<View> giveAwayViews = new ArrayList<>();
        giveAwayViews.add(giveawayBackground);
        giveAwayViews.add(giveawayProductNameTv);
        giveAwayViews.add(giveawayDOPTv);
        giveAwayViews.add(giveawayFlagTv);

        flushIds(combination);

        ConstraintSet set = new ConstraintSet();

        set.clone(combination);

        set.connect(buyBackground.getId(),ConstraintSet.LEFT, combination.getId(), ConstraintSet.LEFT);
        set.connect(buyBackground.getId(),ConstraintSet.RIGHT, buyDOPEt.getId(), ConstraintSet.RIGHT);
        set.connect(buyBackground.getId(),ConstraintSet.TOP, combination.getId(), ConstraintSet.TOP);
        set.connect(buyBackground.getId(),ConstraintSet.BOTTOM, buyProductNameTv.getId(), ConstraintSet.BOTTOM);

        set.connect(giveawayBackground.getId(),ConstraintSet.LEFT, combination.getId(), ConstraintSet.LEFT);
        set.connect(giveawayBackground.getId(),ConstraintSet.RIGHT, giveawayDOPTv.getId(), ConstraintSet.RIGHT);
        set.connect(giveawayBackground.getId(),ConstraintSet.TOP, giveawayProductNameTv.getId(), ConstraintSet.TOP);
        set.connect(giveawayBackground.getId(),ConstraintSet.BOTTOM, combination.getId(), ConstraintSet.BOTTOM);

        set.connect(buyProductNameTv.getId(),ConstraintSet.LEFT, combination.getId(), ConstraintSet.LEFT);
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

        String discountRate = timestream.getDiscountRate();

        TimestreamCombination timestreamCombination = null;

        switch (discountRate) {

            case "":

                for (View v : giveAwayViews) {

                    v.setVisibility(View.GONE);
                }

                buyProductNameTv.setText(timestream.getProductName());
                buyDOPEt.setText(DateUtil.typeMach(timestream.getProductDOP()).substring(0,10));
                inventory.setText(timestream.getProductInventory());
                unitTv.setText(allProducts.get(timestream.getProductCode()).getProductSpec());

                int color = MyApplication.getColorByTimestreamStateCode(context, timestream.getTimeStreamStateCode());

                buyBackground.setBackgroundColor(color);
                break;

            case "0.5":

                timestreamCombination = combinationHashMap.get(timestream.getId());

                break;

            case "0":

                timestreamCombination = combinationHashMap.get(timestream.getSiblingPromotionId());

            default:
                break;
        }


        if (timestreamCombination != null) {

            buyProductNameTv.setText(timestreamCombination.getBuyProductName());
            buyDOPEt.setText(DateUtil.typeMach(timestreamCombination.getBuyTimestream()
                    .getProductDOP()).substring(0,10));
            inventory.setText(timestreamCombination.getPackageCount());
            unitTv.setText("组");

            giveawayProductNameTv.setText(timestreamCombination.getGiveawayProductName());
            giveawayDOPTv.setText(DateUtil.typeMach(timestreamCombination.getGiveawayTimestream()
                    .getProductDOP()).substring(0,10));


            int color = MyApplication.getColorByTimestreamStateCode(context,
                    timestreamCombination.getBuyTimestream().getTimeStreamStateCode());
            buyBackground.setBackgroundColor(color);


            color = MyApplication.getColorByTimestreamStateCode(context,
                    timestreamCombination.getGiveawayTimestream().getTimeStreamStateCode());
            giveawayBackground.setBackgroundColor(color);

        }

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
