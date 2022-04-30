package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;
import static com.shepherdboy.pdstreamline.MyApplication.onShowTimeStreamsHashMap;
import static com.shepherdboy.pdstreamline.MyApplication.setTimeStreamViewOriginalBackgroundColor;
import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.shepherdboy.pdstreamline.sql.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.utils.DateUtil;
import com.shepherdboy.pdstreamline.utils.ScanEventReceiver;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;
import com.shepherdboy.pdstreamline.view.MyTextWatcher;

import java.util.LinkedList;



public class PossiblePromotionTimestreamActivity extends AppCompatActivity {

    private static final int PICK_OUT = 1;
    private static final int DELETE = 2;

    private static TextView[] textViews = new TextView[4]; //用于临时存放创建新timestream view时的textview的引用

    private static LinkedList<Timestream> timestreams;
    public static LinkedList<Timestream> newPromotionTimestreams = new LinkedList<>();

    public static void pickOutPossiblePromotionTimestream() {
        MyApplication.pickupChanges();
        ScanEventReceiver.executeChanges(MyApplication.thingsToSaveList);
        MyDatabaseHelper.PDInfoWrapper.getAndMoveTimestreamByDate(sqLiteDatabase,
                DateUtil.typeMach(MyApplication.today));
    }

    public static void onTimestreamViewReleased(View releasedChild, float horizontalDistance) {

        int viewState = getViewState(releasedChild, horizontalDistance);

        Timestream rmTs;

        switch (viewState) {

            case PICK_OUT:

//                todo 移除timestreamView，从possiblePromotionTimestream表中移除timestream，
//                 将timestream添加到newPromotionTimestreams中
                rmTs = MyApplication.removeTimestream((LinearLayout) releasedChild);
                newPromotionTimestreams.add(rmTs);

                break;

            case DELETE:

//              todo 移除timestreamView，从possiblePromotionTimestream表中移除timestream

                rmTs = MyApplication.removeTimestream((LinearLayout) releasedChild);

                break;

            case 0:

                draggableLinearLayout.putBack(releasedChild);
                break;
        }

        if (onShowTimeStreamsHashMap.size() == 0) {

            for(Timestream t : newPromotionTimestreams) {

                Log.d("I'm in!", t.toString());
            }

            Toast.makeText(draggableLinearLayout.getContext(), "新增临期商品已全部捡出!", Toast.LENGTH_LONG).show();
        }
    }

    /** 根据左右拖动的距离返回int值*/
    public static int getViewState(View draggedView, double horizontalDraggedDistance) {

        boolean isDragToPickOut = horizontalDraggedDistance > 0 && horizontalDraggedDistance >= draggedView.getHeight() * 1.6;
        boolean isDragToDelete = horizontalDraggedDistance < 0 && -horizontalDraggedDistance >= draggedView.getHeight() * 1.6;

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

                changedView.setBackgroundColor(Color.parseColor("#8BC34A"));
                break;

            case DELETE:

                changedView.setBackgroundColor(Color.parseColor("#FF0000"));
                break;

            default:

                setTimeStreamViewOriginalBackgroundColor((LinearLayout) changedView);


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_possible_promotion_timestream);

        draggableLinearLayout = findViewById(R.id.parent);

        MyApplication.initDatabase(this);

        timestreams = MyDatabaseHelper.PDInfoWrapper.getStaleTimestreams(sqLiteDatabase,
                MyDatabaseHelper.POSSIBLE_PROMOTION_TIMESTREAM);

        if (timestreams.size() == 0) {

            Toast.makeText(this,"好消息，今日无新增临期商品!", Toast.LENGTH_LONG).show();
            this.startActivity(new Intent(this, MainActivity.class));
            return;
        }

        initTimestreamsView(timestreams);

        loadTimestreams(timestreams);

    }

    /**将timestreams链表中的所有timestream加载到预先生成的timestreamView中*/
    private void loadTimestreams(LinkedList<Timestream> timestreams) {

        int childViewIndex = 1;
        LinearLayout tsView;

        for (Timestream timestream : timestreams) {

            tsView = (LinearLayout) draggableLinearLayout.getChildAt(childViewIndex);
            loadTimestream(timestream, tsView);

            MyApplication.onShowTimeStreamsHashMap.put(tsView.getId(), timestream);

            MyApplication.setTimeStreamViewOriginalBackgroundColor(timestream);

            MyTextWatcher.watch((EditText) (tsView.getChildAt(2)), timestream, MyApplication.TIMESTREAM_INVENTORY);
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
        codeTV.setText(timestream.getProductCode());
        crdTV.setText(timestream.getProductCoordinate());

        timestream.setBoundLayoutId(tsView.getId() + "");

    }

    /**根据timestream的数量预先生成timestreamView*/
    private static void initTimestreamsView(LinkedList<Timestream> timestreams) {

        MyApplication.init();
        MyTextWatcher.clearWatchers();
        DraggableLinearLayout.setLayoutChanged(true);

        int tsViewCount = draggableLinearLayout.getChildCount() - 1;

        while (tsViewCount < timestreams.size()) {

            addTimestreamView(draggableLinearLayout);
            tsViewCount++;
        }

    }

    /**添加单一timestreamView到父布局中*/
    private static void addTimestreamView(LinearLayout rootView) {

        Context context = rootView.getContext();
        LinearLayout childView = new LinearLayout(context);
        rootView.addView(childView, 1 + MyApplication.originalPositionHashMap.size());

        for (int i = 0; i < 4; i++) {

            textViews[i] = new TextView(context);
        }

        LinearLayout box = new LinearLayout(context);
        EditText et = new EditText(context);

        childView.addView(textViews[0]);
        childView.addView(textViews[1]);
        childView.addView(et);
        childView.addView(box);
        box.addView(textViews[2]);
        box.addView(textViews[3]);

        decorate(childView);
    }

    /**生成的timestreamView参数初始化*/
    private static void decorate(LinearLayout childView) {

        childView.setId(DateUtil.getIdByCurrentTime() + MyApplication.timeStreamIndex++);

        LinearLayout.LayoutParams lParams;
        TextView tv;
        EditText et;
        LinearLayout box;

        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        childView.setLayoutParams(lParams);
        childView.setBackground(childView.getResources().getDrawable(R.drawable.underline));
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
        lParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.47f);
        tv.setLayoutParams(lParams);
        tv.setGravity(Gravity.CENTER_VERTICAL);

        et = (EditText) childView.getChildAt(2);
        lParams = new LinearLayout.LayoutParams(0, 78, 0.15f);
        et.setLayoutParams(lParams);
        et.setGravity(Gravity.CENTER);
        et.setPadding(pd, 0, pd, 0);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);
        et.setSelectAllOnFocus(true);

        box = (LinearLayout) childView.getChildAt(3);
        lParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.15f);
        box.setLayoutParams(lParams);
        box.setOrientation(LinearLayout.VERTICAL);

        tv = textViews[2];
        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f);
        tv.setLayoutParams(lParams);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);

        tv = textViews[3];
        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f);
        tv.setLayoutParams(lParams);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);

    }

    public static void actionStart(Context context) {

        context.startActivity(new Intent(context, PossiblePromotionTimestreamActivity.class));
    }


}