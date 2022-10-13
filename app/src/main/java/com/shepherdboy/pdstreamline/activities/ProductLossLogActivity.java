package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.PRODUCT_LOSS_LOG_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.ProductLoss;
import com.shepherdboy.pdstreamline.beanview.ProductLoader;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;
import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.view.ActivityInfoChangeWatcher;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;

import java.util.LinkedHashMap;

public class ProductLossLogActivity extends BaseActivity {

    private static ActivityInfoChangeWatcher watcher;
    EditText startDateEt;
    EditText endDateEt;
    Button searchBt;
    ViewGroup container;

    int layoutIndex;
    private static final int LOSS_LIST = 0;
    private static final int LOSS_INFO = 1;

    public static final int START_DATE = 50;
    public static final int END_DATE = 51;

    private static String startDate;
    private static String endDate;

    private static LinkedHashMap<String, ProductLoss> lossMap;

    public static void afterInfoChanged(String after, EditText watchedEditText, int filedIndex) {

        after = apply(after, watchedEditText);

        switch (filedIndex) {

            case START_DATE:

                setStartDate(after + " 00:00:00");
                break;

            case END_DATE:

                setEndDate(after + " 00:00:00");
                break;

            default:
                break;

        }
    }

    private static String apply(String after, EditText watchedEditText) {

        after = AIInputter.translate(after);
        ActivityInfoChangeWatcher.getActivityWatcher(PRODUCT_LOSS_LOG_ACTIVITY).setShouldWatch(false);
        watchedEditText.setText(after);

        if(watchedEditText.hasFocus()) {

            DraggableLinearLayout.selectAll(watchedEditText);
        }

        ActivityInfoChangeWatcher.getActivityWatcher(MyApplication.activityIndex).setShouldWatch(true);
        return after;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_loss_log);

        container = findViewById(R.id.container);

        startDateEt = findViewById(R.id.start_date);
        endDateEt = findViewById(R.id.end_date);
        searchBt = findViewById(R.id.search);

    }

    public static void actionStart(Context context) {

        context.startActivity(new Intent(context, ProductLossLogActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        MyApplication.activityIndex = PRODUCT_LOSS_LOG_ACTIVITY;
        layoutIndex = LOSS_LIST;

        watcher = ActivityInfoChangeWatcher.getActivityWatcher(PRODUCT_LOSS_LOG_ACTIVITY);
        if(watcher == null) watcher = new ActivityInfoChangeWatcher(PRODUCT_LOSS_LOG_ACTIVITY);

        watcher.watch(startDateEt, null, START_DATE, true);
        watcher.watch(endDateEt, null, END_DATE, true);
        watcher.setShouldWatch(true);

        searchBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                lossMap = PDInfoWrapper.getProductLoss(sqLiteDatabase, startDate, endDate);

                loadProductLoss(lossMap);
            }
        });
    }

    private void loadProductLoss(LinkedHashMap<String, ProductLoss> losses) {

        ProductLoader.initCellBody(PRODUCT_LOSS_LOG_ACTIVITY, container, losses.size(), 0);
        ProductLoader.loadProductLosses(container, losses, 0);
    }

    public static void setStartDate(String startDate) {
        ProductLossLogActivity.startDate = startDate;
    }

    public static void setEndDate(String endDate) {
        ProductLossLogActivity.endDate = endDate;
    }

    public static LinkedHashMap<String, ProductLoss> getLossMap() {
        return lossMap;
    }
}