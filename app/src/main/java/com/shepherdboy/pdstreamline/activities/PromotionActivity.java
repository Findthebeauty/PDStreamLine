package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.PROMOTION_TIMESTREAM_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.PROMOTION_TIMESTREAM_ACTIVITY_COMBINE;
import static com.shepherdboy.pdstreamline.MyApplication.combinationHashMap;
import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.activities.transaction.Streamline;
import com.shepherdboy.pdstreamline.beans.ProductLoss;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.beans.TimestreamCombination;
import com.shepherdboy.pdstreamline.beanview.ProductLoader;
import com.shepherdboy.pdstreamline.beanview.TimestreamCombinationView;
import com.shepherdboy.pdstreamline.dao.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;
import com.shepherdboy.pdstreamline.services.MidnightTimestreamManagerService;
import com.shepherdboy.pdstreamline.utils.DateUtil;
import com.shepherdboy.pdstreamline.view.ActivityInfoChangeWatcher;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class PromotionActivity extends BaseActivity {

    private static LinkedHashMap<String, Timestream> oddments;
    private static LinkedHashMap<String, TimestreamCombination> combinations;
    private static HashMap<String, Timestream> oldTimestreams;
    private DraggableLinearLayout dragLayout;

    private int layoutIndex;
    private static final int TIMESTREAMS_TO_COMBINE = 0;
    private static final int COMBINE = 1;

    private View temp;

    private static Timestream buyTimestream;
    private static Timestream giveawayTimestream;
    TimestreamCombinationView currentComb;

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

    private void combine(Timestream buyTimestream, Timestream giveawayTimestream) {

        if (buyTimestream == null || giveawayTimestream == null) return;
        int buyInventory = Integer.parseInt(buyTimestream.getProductInventory());
        int giveawayInventory = Integer.parseInt(giveawayTimestream.getProductInventory());

        int stateCode = giveawayTimestream.getTimeStreamStateCode();

        switch (stateCode) {

            case Timestream.CLOSE_TO_EXPIRE:

                TimestreamCombination comb;
                if (giveawayInventory > buyInventory) {

                    Timestream timestream = new Timestream(giveawayTimestream);
                    timestream.setProductInventory(String.valueOf(buyInventory));
                    giveawayTimestream.setProductInventory(String.valueOf(giveawayInventory - buyInventory));

                    giveawayTimestream.setUpdated(false);
                    PDInfoWrapper.deleteTimestream(sqLiteDatabase, giveawayTimestream.getId());
                    PDInfoWrapper.updateInfo(sqLiteDatabase, giveawayTimestream, MyDatabaseHelper.NEW_TIMESTREAM);

                    comb = new TimestreamCombination(buyTimestream, timestream);

                } else {

                    giveawayTimestream.setProductInventory(buyTimestream.getProductInventory());

                    comb = new TimestreamCombination(buyTimestream, giveawayTimestream);
                }

                combinationHashMap.put(buyTimestream.getId(), comb);

                break;

            case Timestream.FRESH:
            case Timestream.EXPIRED:

                Timestream timestream = new Timestream(giveawayTimestream);
                timestream.setProductInventory(String.valueOf(buyInventory));

                if (giveawayInventory > buyInventory) {

                    giveawayTimestream.setProductInventory(String.valueOf(giveawayInventory - buyInventory));

                    giveawayTimestream.setUpdated(false);
                    PDInfoWrapper.deleteTimestream(sqLiteDatabase, giveawayTimestream.getId());
                    PDInfoWrapper.updateInfo(sqLiteDatabase, giveawayTimestream, MyDatabaseHelper.NEW_TIMESTREAM);
                }

                comb = new TimestreamCombination(buyTimestream, timestream);
                combinationHashMap.put(buyTimestream.getId(), comb);
                break;
        }

        currentComb.bindData(PROMOTION_TIMESTREAM_ACTIVITY_COMBINE, buyTimestream);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.initActionBar(getSupportActionBar());

        MyApplication.initDatabase(this);

        initActivity();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (layoutIndex == COMBINE) {
            combine(buyTimestream, giveawayTimestream);
        }
    }

    private void initActivity() {

        oddments = new LinkedHashMap<>();
        combinations = new LinkedHashMap<>();
        oldTimestreams = new HashMap<>();

        watcher = new ActivityInfoChangeWatcher(PROMOTION_TIMESTREAM_ACTIVITY);
        initHandler();


        showBasket();
    }

    private void showBasket() {

        watcher = ActivityInfoChangeWatcher.getActivityWatcher(PROMOTION_TIMESTREAM_ACTIVITY);
        setContentView(R.layout.activity_promotion);
        dragLayout = findViewById(R.id.parent);
        Button submitBt = findViewById(R.id.submit);
        MyApplication.draggableLinearLayout = dragLayout;
        MyApplication.activityIndex = PROMOTION_TIMESTREAM_ACTIVITY;
        layoutIndex = TIMESTREAMS_TO_COMBINE;
        dragLayout.setLayoutChanged(true);

        if (SettingActivity.settingInstance.isAutoCombine()) {

            if(combinations.isEmpty() && oddments.isEmpty())
            autoCombine(MidnightTimestreamManagerService.basket);

            loadTimestreams(true);

        } else {

            if(combinations.isEmpty() && oddments.isEmpty())
            filterProduct();

            loadTimestreams(false);
        }

        submitBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!oddments.isEmpty())
                    Toast.makeText(PromotionActivity.this, "还有未处理商品", Toast.LENGTH_SHORT).show();

                submitProductLoss();
            }
        });
    }

    private void submitProductLoss() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                for (TimestreamCombination combination : combinations.values()) {

                    ProductLoss productLoss = new ProductLoss();

                    Timestream buyTimestream = combination.getBuyTimestream();
                    Timestream giveawayTimestream = combination.getGiveawayTimestream();

                    productLoss.setSiblingProductCode(buyTimestream.getProductCode());
                    productLoss.setSiblingProductDOP(DateUtil.typeMach(buyTimestream.getProductDOP()));
                    productLoss.setLossProductCode(giveawayTimestream.getProductCode());
                    productLoss.setLossProductDOP(DateUtil.typeMach(giveawayTimestream.getProductDOP()));
                    productLoss.setLossType("赠品");
                    productLoss.setLossInventory(String.valueOf(combination.getPackageCount()));
                    productLoss.setProcessAccount("管理员");
                    productLoss.setProcessPhotoId("todo");

                    PDInfoWrapper.updateInfo(sqLiteDatabase, productLoss);
                }

                combinations.clear();
                oddments.clear();
            }
        }).start();
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

    @Override
    public void onBackPressed() {
        switch (layoutIndex) {

            case COMBINE:

                showBasket();
                break;

            default:
                MainActivity.actionStart();
                break;
        }
    }

    private void initCombine(TimestreamCombinationView combView) {

        setContentView(R.layout.promotion_combine);
        assignMember();


        String timestreamId = combView.getTimestreamId();

        if (combinations.containsKey(timestreamId)) {

            buyTimestream = oldTimestreams.get(timestreamId);

        } else {

            buyTimestream = oddments.get(timestreamId);
        }

        currentComb = new TimestreamCombinationView(PROMOTION_TIMESTREAM_ACTIVITY_COMBINE, this, buyTimestream);

        LinearLayout parent = findViewById(R.id.parent);

        parent.addView(currentComb, 1);

        Button addGiveawayBt = findViewById(R.id.add);

        Button commitBt = findViewById(R.id.combine);

        addGiveawayBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PDInfoActivity.actionStart(null, PROMOTION_TIMESTREAM_ACTIVITY_COMBINE);
            }
        });

        commitBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(combView.getTimestreamCombination() == null) {

                    oddments.remove(timestreamId);

                } else {

                    combinations.remove(combView.getTimestreamCombination().getBuyTimestream().getId());
                }

                TimestreamCombination comb = currentComb.getTimestreamCombination();

                if(comb != null)
                    combinations.put(comb.getBuyTimestream().getId(), comb);

                showBasket();
            }
        });
    }

    private void assignMember() {
        MyApplication.activityIndex = PROMOTION_TIMESTREAM_ACTIVITY_COMBINE;
        layoutIndex = COMBINE;

        watcher = new ActivityInfoChangeWatcher(PROMOTION_TIMESTREAM_ACTIVITY_COMBINE);
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


    public static void actionStart(Timestream timestream) {

        giveawayTimestream = timestream;
        MyApplication.getContext().startActivity(new Intent(MyApplication.getContext(), PromotionActivity.class));
    }
}