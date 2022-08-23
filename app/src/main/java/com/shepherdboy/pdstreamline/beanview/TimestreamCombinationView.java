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

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.beans.TimestreamCombination;
import com.shepherdboy.pdstreamline.utils.DateUtil;

public class TimestreamCombinationView extends LinearLayout {



    public TimestreamCombinationView(Context context, Timestream timestream) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);

        LinearLayout combination = inflater.inflate(R.layout.comb_layout_1, null).findViewById(R.id.combination);

        TextView buyProductNameTv = combination.findViewById(R.id.buy_pd_name);

        EditText buyDOPEt = combination.findViewById(R.id.buy_dop);
        EditText inventory = combination.findViewById(R.id.inventory);

        TextView unitTv = combination.findViewById(R.id.unit);

        flushIds(combination);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(layoutParams);

        this.addView(combination);

        String discountRate = timestream.getDiscountRate();

        TimestreamCombination timestreamCombination = null;

        switch (discountRate) {

            case "":


                buyProductNameTv.setText(timestream.getProductName());
                buyDOPEt.setText(DateUtil.typeMach(timestream.getProductDOP()).substring(0,10));
                inventory.setText(timestream.getProductInventory());
                unitTv.setText(allProducts.get(timestream.getProductCode()).getProductSpec());

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

        }

    }


    /**
     * 重设模板中view的id为随机值，避免id冲突
     * @param view
     */
    private static void flushIds(View view) {

        view.setId(DateUtil.getIdByCurrentTime() + MyApplication.idIncrement++);

        if (view instanceof ViewGroup) {

            ViewGroup viewGroup = (ViewGroup)view;

            for (int i = 0; i < viewGroup.getChildCount(); i++) {

                flushIds(viewGroup.getChildAt(i));
            }
        }
    }
}
