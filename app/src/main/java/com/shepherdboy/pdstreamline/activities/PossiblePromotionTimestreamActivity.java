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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.beanview.TimestreamCombinationView;
import com.shepherdboy.pdstreamline.binder.ProductObserver;
import com.shepherdboy.pdstreamline.dao.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;
import com.shepherdboy.pdstreamline.view.ActivityInfoChangeWatcher;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;


public class PossiblePromotionTimestreamActivity extends BaseActivity {

    private static final int PICK_OUT = 1;
    private static final int DELETE = 2;

    private LinkedList<Timestream> possiblePromotionTimestreams;

    private ActivityInfoChangeWatcher watcher;

    private static Handler handler;

    private static ProductObserver observer;

    private static DraggableLinearLayout dragLayout;

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
//        MyApplication.saveChanges();
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

    public static ProductObserver getObserver() {
        return observer;
    }

    public static DraggableLinearLayout getDragLayout() {
        return dragLayout;
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

        if (handler != null) handler.removeCallbacksAndMessages(null);
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                loadTimestreams();
            }
        };
        MyApplication.handlers.add(handler);
    }

    @Override
    protected void onDestroy() {
        dragLayout = null;
        super.onDestroy();

        MyApplication.productSubject.detach(observer);
        observer = null;
    }

    private void initActivity() {

        MyApplication.activityIndex = POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY;

        if (dragLayout == null)
        dragLayout = findViewById(R.id.parent);

        draggableLinearLayout = dragLayout;

        observer = new ProductObserver(POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY, handler);
        MyApplication.productSubject.attach(observer);

        loadTimestreams();
    }

    private void loadTimestreams() {

        possiblePromotionTimestreams = PDInfoWrapper.getStaleTimestreams(sqLiteDatabase,
                MyDatabaseHelper.TIMESTREAM_TO_CHECK);

        LinkedList<Timestream> uncheckedTimestreams = filterTimestream(possiblePromotionTimestreams);

        if (uncheckedTimestreams.size() == 0) {

            Toast.makeText(this,"所有临期商品已捡出!", Toast.LENGTH_LONG).show();
            PromotionActivity.actionStart(null);
            return;
        }

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

            combView = (TimestreamCombinationView) dragLayout.getChildAt(childViewIndex);
            combView.bindData(POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY, timestream);

            childViewIndex++;
        }
    }

    /**根据timestream的数量预先生成timestreamView
     * @param timestreamsCount timestreams数量
     *
     * */
    private static void initTimestreamsView(int timestreamsCount) {

//        MyApplication.init();
        dragLayout.setLayoutChanged(true);

        int tsViewCount = dragLayout.getChildCount() - 1;

        while (tsViewCount < timestreamsCount) {

            addTimestreamView(dragLayout);
            tsViewCount++;
        }
    }

    /**添加单一timestreamView到父布局中*/
    private static void addTimestreamView(LinearLayout rootView) {

        rootView.addView(new TimestreamCombinationView(POSSIBLE_PROMOTION_TIMESTREAM_ACTIVITY, rootView.getContext()));

    }


    public static void actionStart() {

        pickOutPossibleStaleTimestream();
        MyApplication.getContext().startActivity(new Intent(MyApplication.getContext(), PossiblePromotionTimestreamActivity.class));
    }


}