package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.PROMOTION_TIMESTREAM_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.combinationHashMap;
import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.activities.transaction.Streamline;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.beans.TimestreamCombination;
import com.shepherdboy.pdstreamline.beanview.ProductLoader;
import com.shepherdboy.pdstreamline.beanview.TimestreamCombinationView;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;
import com.shepherdboy.pdstreamline.services.MidnightTimestreamManagerService;
import com.shepherdboy.pdstreamline.view.ActivityInfoChangeWatcher;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;

import java.util.HashMap;

public class PromotionActivity extends BaseActivity {

    private static HashMap<String, Timestream> oddments;
    private static HashMap<String, TimestreamCombination> combinations;
    private static HashMap<String, Timestream> oldTimestreams;
    private DraggableLinearLayout dragLayout;

    private View temp;

    private static ActivityInfoChangeWatcher watcher;

    private static Handler handler;

    public static void onViewClick(View v) {

        if(!(v instanceof TimestreamCombinationView)) return;

        postCombine(v);
    }

    private static void postCombine(View v) {

        Message message = handler.obtainMessage();
        message.obj = v;
        handler.sendMessage(message);
    }


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
        oldTimestreams = new HashMap<>();

        watcher = new ActivityInfoChangeWatcher(PROMOTION_TIMESTREAM_ACTIVITY);
        initHandler();

        if (SettingActivity.settingInstance.isAutoCombine()) {

            autoCombine(MidnightTimestreamManagerService.basket);

            loadTimestreams(true);

        } else {

            filterProduct();
            loadTimestreams(false);
        }

    }

    private void initHandler() {

        if(handler != null) handler.removeCallbacksAndMessages(null);

        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {

                TimestreamCombinationView comb = (TimestreamCombinationView) msg.obj;
                initCombine(comb);
            }
        };

        MyApplication.handlers.add(handler);
    }

    private void initCombine(TimestreamCombinationView combView) {
        setContentView(R.layout.promotion_combine);

        Timestream timestream;

        String timestreamId = combView.getTimestreamId();

        if (combinations.containsKey(timestreamId)) {

            timestream = oldTimestreams.get(timestreamId);

        } else {

            timestream = oddments.get(timestreamId);
        }

        TimestreamCombinationView comb = new TimestreamCombinationView(PROMOTION_TIMESTREAM_ACTIVITY, this, timestream);

        LinearLayout parent = findViewById(R.id.parent);

        parent.addView(comb, 1);
    }


    private void filterProduct() {

        for(Timestream t : MidnightTimestreamManagerService.basket.values()) {

            if (t.getTimeStreamStateCode() == Timestream.CLOSE_TO_EXPIRE)
                oddments.put(t.getId(), t);
        }
    }

    private void loadTimestreams(boolean combined) {

        ProductLoader.initCellBody(MyApplication.PROMOTION_TIMESTREAM_ACTIVITY,
                dragLayout,combinations.size() + oddments.size(),0);

        if(combined) ProductLoader.loadTimestreams(MyApplication.PROMOTION_TIMESTREAM_ACTIVITY,
                dragLayout, combinations, 0);

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
                    combinations.put(comb.getBuyTimestream().getId(), comb);

                    if (combinationHashMap == null)
                        combinationHashMap = PDInfoWrapper.getTimestreamCombinations(sqLiteDatabase);
                    MyApplication.combinationHashMap.put(comb.getBuyTimestream().getId(), comb);
                }
            } else {

                Streamline.offShelvesTimestreams.add(t);
            }
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

        Timestream evenTimestream = new Timestream(t);
        oldTimestreams.put(evenTimestream.getId(), t);

        int inventory = Integer.parseInt(t.getProductInventory());

        evenTimestream.setSiblingPromotionId(t.getId());
        evenTimestream.setBuySpecs("1");
        evenTimestream.setGiveawaySpecs("1");
        evenTimestream.setDiscountRate("0.5");
        evenTimestream.setInBasket(false);

        if (inventory % 2 == 1) {

            inventory -= 1;
            evenTimestream.setProductInventory(String.valueOf(inventory));
            Timestream timestream = new Timestream(t);
            timestream.setProductInventory("1");
            oddments.put(timestream.getId(), timestream);
            if (inventory < 2) return null;
        }

        return new TimestreamCombination(evenTimestream);
    }


    public static void actionStart() {

        MyApplication.getContext().startActivity(new Intent(MyApplication.getContext(), PromotionActivity.class));
    }
}