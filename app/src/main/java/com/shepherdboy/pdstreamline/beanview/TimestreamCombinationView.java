package com.shepherdboy.pdstreamline.beanview;

import static com.shepherdboy.pdstreamline.MyApplication.PD_INFO_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.POSSIBLE_EXPIRED_TIMESTREAM_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.PRODUCT_LOSS_LOG_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.PROMOTION_TIMESTREAM_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.PROMOTION_TIMESTREAM_ACTIVITY_COMBINE;
import static com.shepherdboy.pdstreamline.MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF;
import static com.shepherdboy.pdstreamline.MyApplication.onShowCombsHashMap;
import static com.shepherdboy.pdstreamline.MyApplication.onShowTimeStreamsHashMap;

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
import com.shepherdboy.pdstreamline.activities.PDInfoActivity;
import com.shepherdboy.pdstreamline.activities.PossiblePromotionTimestreamActivity;
import com.shepherdboy.pdstreamline.activities.ProductLossInfoActivity;
import com.shepherdboy.pdstreamline.activities.TraversalTimestreamActivity;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.beans.ProductLoss;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.beans.TimestreamCombination;
import com.shepherdboy.pdstreamline.binder.ProductObserver;
import com.shepherdboy.pdstreamline.utils.DateUtil;
import com.shepherdboy.pdstreamline.view.ActivityInfoChangeWatcher;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class TimestreamCombinationView extends LinearLayout implements BeanView{

    private LinearLayout buyBackground;
    private LinearLayout giveawayBackground;
    private TextView buyProductNameTv;
    private TextView giveawayProductNameTv;
    private TextView buyDOPEt;
    private TextView inventory;
    private TextView unitTv;
    private TextView giveawayDOPTv;
    private TimestreamCombination timestreamCombination = null;
    private ArrayList<View> giveAwayViews;
    private String productCode;
    private String timestreamId;

    public TimestreamCombinationView(int activityIndex, Context context, Timestream timestream) {
        this(activityIndex, context);

        bindData(activityIndex, timestream);
    }

    public TimestreamCombinationView(int activityIndex, Context context) {
        super(context);

        initView(activityIndex, context);
    }

    private void initView(int activityIndex, Context context) {

        setId(View.generateViewId());
        setFocusable(true);
        setFocusableInTouchMode(true);

        LayoutInflater inflater = LayoutInflater.from(context);
        ConstraintLayout combination;
        switch (activityIndex) {

            case POSSIBLE_EXPIRED_TIMESTREAM_ACTIVITY:
            case POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY:
            case PROMOTION_TIMESTREAM_ACTIVITY:
            case PROMOTION_TIMESTREAM_ACTIVITY_COMBINE:
                combination = inflater.inflate(R.layout.comb_layout_uneditable, null).findViewById(R.id.combination);
                break;

            case PRODUCT_LOSS_LOG_ACTIVITY:
                combination = inflater.inflate(R.layout.comb_layout_product_loss, null).findViewById(R.id.combination);
                break;

            default:
                combination = inflater.inflate(R.layout.comb_layout, null).findViewById(R.id.combination);
                break;

        }

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
        EditText inventoryMeasure;
        inventoryMeasure = combination.findViewById(R.id.inventory_measure);

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

    public void bindData(ProductLoss loss) {

        Product product = MyApplication.getAllProducts().get(loss.getLossProductCode());


        buyDOPEt.setText(loss.getLossProductDOP().substring(0,10));
        buyProductNameTv.setText(product.getProductName());
        inventory.setText(loss.getLossInventory());

        try {

            Date DOP = DateUtil.typeMach(loss.getLossProductDOP());
            Timestream timestream = new Timestream(product, DOP, loss.getLossInventory());

            Date processDate = DateUtil.typeMach(loss.getProcessDate());

            buyBackground.setBackgroundColor(
                    MyApplication.getColorByTimestreamStateCode(
                            timestream.getTimestreamStateCode(processDate))
            );
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                ProductLossInfoActivity.actionStart(MyApplication.getCurrentActivityContext(),
                        loss.getId());
            }
        });
    }

    /**
     *
     * @param o timestream,timestreamCombination
     */
    public void bindData(int activityIndex, Object o) {

        setVisibility(VISIBLE);
        timestreamCombination = null;
        timestreamId = null;
        productCode = null;

        // 如果传入null，表示更新nextTrigger的信息，只需要更新商品名
        if (o == null) {

//            buyProductNameTv.setText(MyApplication.getAllProducts().get(productCode).getProductName());
            return;
        }

        ActivityInfoChangeWatcher watcher = ActivityInfoChangeWatcher.getActivityWatcher(activityIndex);

        watcher.removeWatcher(buyDOPEt);
        watcher.removeWatcher(inventory);
        watcher.removeWatcher(giveawayDOPTv);
//        watcher.watch(this);

        Timestream timestream;

        if (o instanceof Timestream) {

            timestream = (Timestream) o;

        } else {

            timestream = ((TimestreamCombination) o).getBuyTimestream();
        }

        this.productCode = timestream.getProductCode();
        this.timestreamId = timestream.getId();

        if(activityIndex == TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF)
        TraversalTimestreamActivity.combViews.put(timestreamId, this);

        String discountRate = timestream.getDiscountRate();

        onShowTimeStreamsHashMap.put(this.getId(), timestream);
        MyApplication.getAllTimestreams().put(timestreamId, timestream);

        switch (discountRate) {

            case "":

                for(View v : giveAwayViews) {
                    v.setVisibility(GONE);
                }

                buyProductNameTv.setText(MyApplication.getAllProducts().get(timestream.getProductCode()).getProductName());
                switch (activityIndex) {

                    case POSSIBLE_EXPIRED_TIMESTREAM_ACTIVITY:
                    case POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY:
                        buyDOPEt.setText(DateUtil.typeMach(timestream.getProductDOP()).substring(5,10));
                        break;

                    default:
                        buyDOPEt.setText(DateUtil.typeMach(timestream.getProductDOP()).substring(0,10));
                        break;
                }
                inventory.setText(timestream.getProductInventory());
                unitTv.setText(String.valueOf(MyApplication.getAllProducts().get(timestream.getProductCode()).getProductSpec()));

                if (timestream.isInBasket()) {

                    buyBackground.setBackground(getResources().getDrawable(R.drawable.item_selected));

                } else {

                    int color = MyApplication.getColorByTimestreamStateCode(timestream.getTimestreamStateCode());
                    buyBackground.setBackgroundColor(color);
                }

                onShowTimeStreamsHashMap.put(buyBackground.getId(), timestream); //一个combination有3个背景，整体的、商品和赠品的，3个背景都要记录

                mapping(activityIndex, timestream);
                break;

            case "1":

                for (View v : giveAwayViews) {

                    v.setVisibility(View.VISIBLE);
                }

                timestreamCombination = MyApplication.getCombinationHashMap().get(timestream.getId());
                break;

            case "0":

                timestreamCombination = MyApplication.getCombinationHashMap().get(timestream.getSiblingPromotionId());

                if(timestreamCombination.getBuyTimestream().getProductCode()
                        .equals(timestreamCombination.getGiveawayTimestream().getProductCode())) {

                    this.setVisibility(GONE);

                } else {

                    this.setVisibility(VISIBLE);
                }

                for (View v : giveAwayViews) {

                    v.setVisibility(View.VISIBLE);
                }
                break;

            default:
                break;
        }

        if (timestreamCombination != null) {

            onShowCombsHashMap.put(this.getId(), timestreamCombination);

            buyProductNameTv.setText(timestreamCombination.getBuyProductName());
            buyDOPEt.setText(DateUtil.typeMach(timestreamCombination.getBuyTimestream()
                    .getProductDOP()).substring(0,10));
            inventory.setText(String.valueOf(timestreamCombination.getPackageCount()));
            unitTv.setText("组");

            giveawayProductNameTv.setText(timestreamCombination.getGiveawayProductName());
            giveawayDOPTv.setText(DateUtil.typeMach(timestreamCombination.getGiveawayTimestream()
                    .getProductDOP()).substring(0,10));


            int color = MyApplication.getColorByTimestreamStateCode(timestreamCombination.getBuyTimestream().getTimestreamStateCode());
            buyBackground.setBackgroundColor(color);


            color = MyApplication.getColorByTimestreamStateCode(timestreamCombination.getGiveawayTimestream().getTimestreamStateCode());
            giveawayBackground.setBackgroundColor(color);

            onShowTimeStreamsHashMap.put(this.getId(), timestreamCombination.getBuyTimestream()); //一个combination有3个背景，整体的、商品和赠品的，3个背景都要记录
            onShowTimeStreamsHashMap.put(buyBackground.getId(), timestreamCombination.getBuyTimestream()); //一个combination有3个背景，整体的、商品和赠品的，3个背景都要记录
            onShowTimeStreamsHashMap.put(giveawayBackground.getId(), timestreamCombination.getGiveawayTimestream()); //一个combination有3个背景，整体的、商品和赠品的，3个背景都要记录

            switch (activityIndex) {

                case POSSIBLE_EXPIRED_TIMESTREAM_ACTIVITY:
                case POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY:
                case PROMOTION_TIMESTREAM_ACTIVITY:
                case PROMOTION_TIMESTREAM_ACTIVITY_COMBINE:
                    break;

                default:
                    watcher.watch((EditText) buyDOPEt,timestreamCombination.getBuyTimestream(),MyApplication.TIMESTREAM_DOP,true);
                    break;
            }

            watcher.watch((EditText) inventory,timestreamCombination.getBuyTimestream(),MyApplication.TIMESTREAM_INVENTORY,true);

        } else {

            switch (activityIndex) {

                case PRODUCT_LOSS_LOG_ACTIVITY:
                    break;

                case POSSIBLE_EXPIRED_TIMESTREAM_ACTIVITY:
                case POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY:
                case PROMOTION_TIMESTREAM_ACTIVITY:
                case PROMOTION_TIMESTREAM_ACTIVITY_COMBINE:
                    watcher.watch((EditText) inventory,timestream,MyApplication.TIMESTREAM_INVENTORY,true);
                    break;


                default:
                    watcher.watch((EditText) buyDOPEt,timestream,MyApplication.TIMESTREAM_DOP,true);
                    watcher.watch((EditText) inventory,timestream,MyApplication.TIMESTREAM_INVENTORY,true);
                    break;
            }


        }

    }

    private void mapping(int activityIndex, Timestream timestream) {

        ProductObserver observer = null;
        switch (activityIndex) {

            case POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY:
                observer = PossiblePromotionTimestreamActivity.getObserver();
                break;

            case PD_INFO_ACTIVITY:
                observer = PDInfoActivity.getObserver();
                break;

            case TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF:
                observer = TraversalTimestreamActivity.getObserver();
                break;

            default:
                break;
        }

        if(observer != null) observer.bind(timestream.getId(), this);
    }

    public EditText getBuyDOPEt() {
        return (EditText) buyDOPEt;
    }

    public TextView getBuyProductNameTv() {
        return buyProductNameTv;
    }

    public LinearLayout getBuyBackground() {
        return buyBackground;
    }

    public LinearLayout getGiveawayBackground() {
        return giveawayBackground;
    }

    public EditText getInventory() {
        return (EditText) inventory;
    }

    public String getTimestreamId() {
        return timestreamId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public TimestreamCombination getTimestreamCombination() {
        return timestreamCombination;
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
