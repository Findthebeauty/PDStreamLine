package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;
import static com.shepherdboy.pdstreamline.MyApplication.drawableFirstLevel;
import static com.shepherdboy.pdstreamline.MyApplication.drawableSecondLevel;
import static com.shepherdboy.pdstreamline.MyApplication.onShowTimeStreamsHashMap;
import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;
import static com.shepherdboy.pdstreamline.services.MidnightTimestreamManagerService.basket;
import static com.shepherdboy.pdstreamline.services.MidnightTimestreamManagerService.timestreamRestoreHandler;
import static com.shepherdboy.pdstreamline.services.MidnightTimestreamManagerService.timestreamRestoreTask;
import static com.shepherdboy.pdstreamline.services.MidnightTimestreamManagerService.timestreamRestoreTimer;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.beanview.TimestreamCombinationView;
import com.shepherdboy.pdstreamline.dao.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;
import com.shepherdboy.pdstreamline.utils.DateUtil;
import com.shepherdboy.pdstreamline.view.ActivityInfoChangeWatcher;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;


public class PossiblePromotionTimestreamActivity extends AppCompatActivity {

    private static final int PICK_OUT = 1;
    private static final int DELETE = 2;


    private static TextView[] textViews = new TextView[4]; //用于临时存放创建新timestream view时的textview的引用

    private static LinkedList<Timestream> possiblePromotionTimestreams;

    private static ActivityInfoChangeWatcher watcher;

    private void cancelTimerTask() {

        if (timestreamRestoreTimer != null) timestreamRestoreTimer.cancel();
    }
    private void startTimerTask(){

        if (timestreamRestoreTimer != null) {

            timestreamRestoreTimer.cancel();
        }

        timestreamRestoreTimer = new Timer();

        if (timestreamRestoreTask == null) {

            timestreamRestoreTask = new TimerTask() {
                @Override
                public void run() {
                    Message m = new Message();
                    m.what = 1;
                    timestreamRestoreHandler.sendMessage(m);
                }
            };
        }

        timestreamRestoreTimer.schedule(timestreamRestoreTask, 6 * 60 * 60 * 1000, 1);
        timestreamRestoreTask = null;
    }

    @Override
    protected void onStop() {

        startTimerTask();
        super.onStop();
    }

    @Override
    protected void onPostResume() {

        cancelTimerTask();
        super.onPostResume();
    }

    public static void pickOutPossibleStaleTimestream() {
        MyApplication.saveChanges();
        PDInfoWrapper.getAndMoveTimestreamByDate(sqLiteDatabase,
                SettingActivity.settingInstance.getNextSalesmanCheckDay());
    }

    public static void onTimestreamViewReleased(View releasedChild, float horizontalDistance) {

        int viewState = getViewState(releasedChild, horizontalDistance);

        Timestream rmTs;

        switch (viewState) {

            case PICK_OUT:

                rmTs = MyApplication.unloadTimestream((LinearLayout) releasedChild);
                basket.put(rmTs.getId(), rmTs);
                rmTs.setInBasket(true);
                PDInfoWrapper.updateInfo(MyApplication.sqLiteDatabase, rmTs,
                        MyDatabaseHelper.UPDATE_BASKET);
                break;

            case DELETE:

                rmTs = MyApplication.unloadTimestream((LinearLayout) releasedChild);
                PDInfoWrapper.deleteTimestream(MyApplication.sqLiteDatabase, rmTs.getId());
                break;

            case 0:

                draggableLinearLayout.putBack(releasedChild);
                break;
        }

        if (onShowTimeStreamsHashMap.size() == 0) {

            for(Timestream t : basket.values()) {

            }
            Toast.makeText(draggableLinearLayout.getContext(), "新增临期商品已全部捡出!", Toast.LENGTH_LONG).show();
        }
    }

    /** 根据左右拖动的距离返回int值*/
    public static int getViewState(View draggedView, double horizontalDraggedDistance) {

        boolean isDragToPickOut = horizontalDraggedDistance >= 160;
        boolean isDragToDelete = horizontalDraggedDistance < 0 && -horizontalDraggedDistance >= 160;

        if (isDragToPickOut) {

            return PICK_OUT;

        } else if (isDragToDelete) {

            return DELETE;

        } else {

            return 0;
        }
    }

    /**根据拖拽的距离改变timestream显示的颜色*/
    public static void onTimestreamViewPositionChanged(View changedView, float horizontalDistance) {

        int viewState = getViewState(changedView, horizontalDistance);

        switch (viewState) {

            case PICK_OUT:

                changedView.setBackground(drawableFirstLevel);

                break;

            case DELETE:

                changedView.setBackground(drawableSecondLevel);

                break;

            default:

                MyApplication.setTimeStreamViewOriginalBackground((LinearLayout) changedView);
                break;
        }
    }

    @Override
    protected void onStart() {

        initActivity();
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_possible_promotion_timestream);
        MyApplication.initActionBar(getSupportActionBar());

        MyApplication.init();
        MyApplication.initDatabase(this);

        watcher = ActivityInfoChangeWatcher.getActivityWatcher(POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY);
        if(watcher == null) watcher = new ActivityInfoChangeWatcher(POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY);
    }

    private void initActivity() {

        MyApplication.activityIndex = POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY;

        draggableLinearLayout = findViewById(R.id.parent);

        possiblePromotionTimestreams = PDInfoWrapper.getStaleTimestreams(sqLiteDatabase,
                MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM);

        LinkedList<Timestream> uncheckedTimestreams = filterTimestream(possiblePromotionTimestreams);

        if (uncheckedTimestreams.size() == 0) {

            Toast.makeText(this,"所有临期商品已捡出!", Toast.LENGTH_LONG).show();
//            this.startActivity(new Intent(this, MainActivity.class));
            PromotionActivity.actionStart();
            return;
        }

        Log.d("unchecked", uncheckedTimestreams.size() + ": " + uncheckedTimestreams);
        initTimestreamsView(uncheckedTimestreams.size());

        loadTimestreams(uncheckedTimestreams);

        cancelTimerTask();
    }

    /**
     * 将in_basket为true即已经检出到篮子里的timestream放入basket容器中，返回未放入篮子中的timestream数量
     * @param possiblePromotionTimestreams 所有可能临期的timestream，包括已经放入篮子中的和货架上的
     * @return 未放入篮子即还在货架上的timestream
     */
    private LinkedList<Timestream> filterTimestream(LinkedList<Timestream> possiblePromotionTimestreams) {

//        basket.clear();

        LinkedList<Timestream> temp = new LinkedList<>();
        for (Timestream t : possiblePromotionTimestreams) {

            if (t.isInBasket()) {

                basket.put(t.getId(), t);

            } else {

                temp.add(t);
            }
        }

        return temp;
    }



    /**将timestreams链表中的所有timestream加载到预先生成的timestreamView中*/
    private void loadTimestreams(LinkedList<Timestream> timestreams) {

        int childViewIndex = 1;
        TimestreamCombinationView combView;

        for (Timestream timestream : timestreams) {

            if (timestream.isInBasket()) continue;

            combView = (TimestreamCombinationView) draggableLinearLayout.getChildAt(childViewIndex);
            combView.bindData(POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY, timestream);
//            loadTimestream(timestream, combView);

//            MyApplication.onShowTimeStreamsHashMap.put(combView.getId(), timestream);
//
//            MyApplication.setTimeStreamViewOriginalBackground(timestream);
//
//            watcher.watch((EditText) (combView.getChildAt(2)), timestream, MyApplication.TIMESTREAM_INVENTORY, true);
            childViewIndex++;
        }
    }

    /**加载单一timestream到view中*/
    private void loadTimestream(Timestream timestream, LinearLayout tsView) {

        LinearLayout box;

        TextView dopTV = (TextView) tsView.getChildAt(0);
        TextView nameTV = (TextView) tsView.getChildAt(1);
        EditText inventoryET = (EditText) tsView.getChildAt(2);

        box = (LinearLayout) tsView.getChildAt(3);
        TextView codeTV = (TextView) box.getChildAt(0);
        TextView crdTV = (TextView) box.getChildAt(1);

        dopTV.setText(DateUtil.typeMach(timestream.getProductDOP()).substring(5,10));
        nameTV.setText(timestream.getProductName());
        inventoryET.setText(timestream.getProductInventory());
        codeTV.setText(timestream.getShortCode());
        crdTV.setText(timestream.getProductCoordinate());

        timestream.setBoundLayoutId(tsView.getId() + "");

    }

    /**根据timestream的数量预先生成timestreamView
     * @param timestreamsCount timestreams数量
     *
     * */
    private static void initTimestreamsView(int timestreamsCount) {

        MyApplication.init();
        DraggableLinearLayout.setLayoutChanged(true);

        int tsViewCount = draggableLinearLayout.getChildCount() - 1;

        while (tsViewCount < timestreamsCount) {

            addTimestreamView(draggableLinearLayout);
            tsViewCount++;
        }

    }

    /**添加单一timestreamView到父布局中*/
    private static void addTimestreamView(LinearLayout rootView) {

        rootView.addView(new TimestreamCombinationView(POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY, rootView.getContext()));
//        Context context = rootView.getContext();
//        LinearLayout childView = new LinearLayout(context);
//        rootView.addView(childView, 1 + MyApplication.originalPositionHashMap.size());
//
//        for (int i = 0; i < 4; i++) {
//
//            textViews[i] = new TextView(context);
//        }
//
//        LinearLayout box = new LinearLayout(context);
//        EditText et = new EditText(context);
//
//        childView.addView(textViews[0]);
//        childView.addView(textViews[1]);
//        childView.addView(et);
//        childView.addView(box);
//        box.addView(textViews[2]);
//        box.addView(textViews[3]);
//
//        decorate(childView);
    }

    /**生成的timestreamView参数初始化*/
    private static void decorate(LinearLayout childView) {

        childView.setId(View.generateViewId());

        LinearLayout.LayoutParams lParams;
        TextView tv;
        EditText et;
        LinearLayout box;

        lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        childView.setLayoutParams(lParams);
        childView.setBackground(new ColorDrawable());
        childView.setAlpha(0.8f);
        childView.setOrientation(LinearLayout.HORIZONTAL);
        childView.setGravity(Gravity.CENTER_VERTICAL);
        childView.setWeightSum(1f);


        tv = textViews[0];
        lParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.23f);
        tv.setLayoutParams(lParams);
        tv.setGravity(Gravity.CENTER);
        int pd = (int) DraggableLinearLayout.dpToFloat(5, childView.getContext());
        tv.setPadding(pd, 0, pd, 0);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);

        tv = textViews[1];
        lParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.47f);
        tv.setLayoutParams(lParams);
        tv.setGravity(Gravity.CENTER_VERTICAL);

        et = (EditText) childView.getChildAt(2);
        lParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.15f);
        et.setLayoutParams(lParams);
        et.setGravity(Gravity.CENTER);
        et.setPadding(pd, 0, pd, 0);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);
        et.setBackground(null);
        et.setSelectAllOnFocus(true);

        box = (LinearLayout) childView.getChildAt(3);
        lParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.15f);
        box.setLayoutParams(lParams);
        box.setOrientation(LinearLayout.VERTICAL);

        tv = textViews[2];
        lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                0, 1f);
        tv.setLayoutParams(lParams);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);

        tv = textViews[3];
        lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                0, 1f);
        tv.setLayoutParams(lParams);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);

    }

    public static void actionStart() {

        MyApplication.getContext().startActivity(new Intent(MyApplication.getContext(), PossiblePromotionTimestreamActivity.class));
    }


}