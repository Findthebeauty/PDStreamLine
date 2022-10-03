package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.PROMOTION_TIMESTREAM_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.combinationHashMap;
import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.activities.transaction.Streamline;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.beans.TimestreamCombination;
import com.shepherdboy.pdstreamline.beanview.ProductLoader;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;
import com.shepherdboy.pdstreamline.services.MidnightTimestreamManagerService;
import com.shepherdboy.pdstreamline.view.ActivityInfoChangeWatcher;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;

import java.util.HashMap;

public class PromotionActivity extends BaseActivity {

    private HashMap<String, Timestream> oddments;
    private HashMap<String, TimestreamCombination> combinations;
    private DraggableLinearLayout dragLayout;

    private static ActivityInfoChangeWatcher watcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);
        MyApplication.initActionBar(getSupportActionBar());

        MyApplication.initDatabase(this);

        initActivity();
    }

    private void initActivity() {

        dragLayout = findViewById(R.id.parent);
        MyApplication.draggableLinearLayout = dragLayout;
        MyApplication.activityIndex = PROMOTION_TIMESTREAM_ACTIVITY;
        dragLayout.setLayoutChanged(true);
        oddments = new HashMap<>();
        combinations = new HashMap<>();

        watcher = new ActivityInfoChangeWatcher(PROMOTION_TIMESTREAM_ACTIVITY);

        if (SettingActivity.settingInstance.isAutoCombine()) {

            autoCombine(MidnightTimestreamManagerService.basket);

        }

        loadTimestreams();
    }

    private void loadTimestreams() {

        ProductLoader.initCellBody(MyApplication.PROMOTION_TIMESTREAM_ACTIVITY,
                dragLayout,combinations.size() + oddments.size(),0);
        ProductLoader.loadTimestreams(MyApplication.PROMOTION_TIMESTREAM_ACTIVITY,
                dragLayout,combinations,0);
        ProductLoader.loadTimestreams(MyApplication.PROMOTION_TIMESTREAM_ACTIVITY,
                dragLayout,oddments,combinations.size());
    }

    /**
     * 加载捆绑商品
     * @param v 目标ViewGroup
     * @param t 捆绑商品
     */
    public static void loadCombination(ViewGroup v, TimestreamCombination t) {


    }

    /**
     * 将单一商品加载到捆绑模式View中
     * @param v 目标ViewGroup
     * @param t 捆绑商品
     */
    public static void loadCombination(ViewGroup v, Timestream t) {


    }

    public void autoCombine(HashMap<String, Timestream> basket) {

        for (Timestream t : basket.values()) {

            if(t.getTimeStreamStateCode() == Timestream.CLOSE_TO_EXPIRE) {

                TimestreamCombination comb = combine(t);
                if(comb != null) {
                    combinations.put(t.getId(), comb);

                    if (combinationHashMap == null)
                        combinationHashMap = PDInfoWrapper.getTimestreamCombinations(sqLiteDatabase);
                    MyApplication.combinationHashMap.put(t.getId(), comb);
                }
            } else {

                Streamline.offShelvesTimestreams.add(t);
            }
        }

        for (TimestreamCombination comb : combinations.values()) {

            basket.remove(comb.getBuyTimestream().getId());
        }
    }

    //todo oddments的搭赠方法
    //todo 获取sibling列表

    /**
     * 将该批次商品自身进行买一赠一，如果有单数，将单出的一个放入oddments列表中由人工选择搭赠方式
     * @param t 需要做买一赠一的商品
     * @return TimestreamCombination捆绑好的商品
     */
    private TimestreamCombination combine(Timestream t) {

        Log.d("combine", t.toString());
        int inventory = Integer.parseInt(t.getProductInventory());

        t.setSiblingPromotionId(t.getId());
        t.setBuySpecs("1");
        t.setGiveawaySpecs("1");
        t.setDiscountRate("0.5");
        t.setInBasket(false);

        if (inventory % 2 == 1) {

            inventory -= 1;
            t.setProductInventory(String.valueOf(inventory));
            Timestream timestream = new Timestream(t);
            oddments.put(timestream.getId(), timestream);
            if (inventory < 2) return null;
        }

        return new TimestreamCombination(t);
    }


    public static void actionStart() {

        MyApplication.getContext().startActivity(new Intent(MyApplication.getContext(), PromotionActivity.class));
    }
}