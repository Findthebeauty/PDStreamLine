package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.PD_INFO_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.currentProduct;
import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;
import static com.shepherdboy.pdstreamline.MyApplication.onShowTimeStreamsHashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.beanview.ProductLoader;
import com.shepherdboy.pdstreamline.beanview.TimestreamCombinationView;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;
import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.utils.DateUtil;
import com.shepherdboy.pdstreamline.utils.ScanEventReceiver;
import com.shepherdboy.pdstreamline.view.ActivityInfoChangeWatcher;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PDInfoActivity extends AppCompatActivity {

    private static final int ADD_TIMESTREAM_LAYOUT = 1;
    private static final int REMOVE_TIMESTREAM_LAYOUT = 2;

    private static Handler showHandler;
    private static String productToShow;
    private static ActivityInfoChangeWatcher watcher;

    private static ArrayList<TextView> timestreamChildTextViewList = new ArrayList<>(3);
    private static ArrayList<EditText> timestreamChildEditTextList = new ArrayList<>(3);
    private static float[] layoutWeightArray = new float[]{2.7f, 4f, 1.2f, 1.6f, 1.2f, 1f};
    private static String[] textArray = new String[]{"生产日期:", "坐标:", "库存:"};

    private static LinearLayout topTimestreamView;
    private static EditText topDOPEditText,productCodeEditText,productNameEditText,
            productEXPEditText,productSpecEditText;
    public static Button scanner,productEXPTimeUnitButton;

    private static Activity activity;

    /**
     * 启动PDInfoActivity活动并加载传入条码对应的商品，如果传入空值，则尝试加载上次加载过的商品--currentProduct
     * @param code
     */
    public static void actionStart(String code) {

        Intent intent = new Intent(MyApplication.getContext(), PDInfoActivity.class);
        MyApplication.getContext().startActivity(intent);
        if(code == null) return;
        productToShow = code;
    }

    @Override
    protected void onStart() {
//        MyApplication.init();
        super.onStart();

        initActivity();

        if (showHandler == null) {

            showHandler = new Handler() {

                @Override
                public void handleMessage(@NonNull Message msg) {

                    searchNext((String) msg.obj);
                }
            };

            MyApplication.handlers.add(showHandler);
        }

        if (productToShow != null) {

            searchNext(productToShow);
            productToShow = null;

        } else if (currentProduct != null) {

            loadProduct(currentProduct);
        }
    }

    public static Handler getShowHandler() {

        return showHandler;
    }

    private void initActivity() {

//        ActivityInfoChangeWatcher.init();
        MyApplication.activityIndex = PD_INFO_ACTIVITY;
        draggableLinearLayout = findViewById(R.id.parent);
        productCodeEditText = findViewById(R.id.product_code);
        productNameEditText = findViewById(R.id.product_name);
        productEXPEditText = findViewById(R.id.product_exp);
        productEXPTimeUnitButton = findViewById(R.id.time_unit);
        productSpecEditText = findViewById(R.id.spec);

        scanner = findViewById(R.id.zxing_barcode_scanner);

        MyApplication.registerCameraScanner(this, (View) findViewById(R.id.parent).getParent());

        scanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanActivity.actionStart(PDInfoActivity.this);
            }
        });

        productCodeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_NEXT
                || (event != null) && KeyEvent.KEYCODE_ENTER == event.getKeyCode()
                && KeyEvent.ACTION_UP == event.getAction()) {

                    searchNext(v.getText().toString());
                }
                return true;
            }
        });

        watcher.watch(productCodeEditText, null, MyApplication.PRODUCT_CODE, true);
        activity = PDInfoActivity.this;
    }

    public static Activity getActivity() {
        return activity;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        return MyApplication.tryCatchVolumeDown(this, event.getKeyCode()) || super.dispatchKeyEvent(event);
    }

    private void searchNext(String code) {

        if (code == null)
        code = ((EditText)findViewById(R.id.product_code)).getText().toString();
        ScanEventReceiver.show(code);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdinfo);
        MyApplication.initActionBar(getSupportActionBar());

        watcher = new ActivityInfoChangeWatcher(PD_INFO_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == 0 || requestCode != IntentIntegrator.REQUEST_CODE) return;

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        String scanResult = result.getContents();

        ScanEventReceiver.show(scanResult.trim());

    }
    public static void loadProduct(Product product) {

        watcher.setShouldWatch(false);

        currentProduct = product;

        LinkedHashMap<String, Timestream> timeStreams = product.getTimeStreams();

        initTimestreamView(timeStreams);

        productCodeEditText.setText(product.getProductCode());
        productNameEditText.setText(product.getProductName());
        productEXPEditText.setText(product.getProductEXP());
        productEXPTimeUnitButton.setText(product.getProductEXPTimeUnit());
        productSpecEditText.setText(product.getProductSpec());

        watcher.watch(productCodeEditText, null, MyApplication.PRODUCT_CODE, true);
        watcher.watch(productNameEditText, null, MyApplication.PRODUCT_NAME, true);
        watcher.watch(productEXPEditText, null, MyApplication.PRODUCT_EXP, true);
        watcher.watch(productSpecEditText, null, MyApplication.PRODUCT_SPEC, true);
        watcher.watch(productEXPTimeUnitButton);

        loadTimestreams(timeStreams);

        if(timeStreams.size() > 0) DraggableLinearLayout.selectAll(topDOPEditText);

        watcher.setShouldWatch(true);
    }

    private static void loadTimestreams(LinkedHashMap<String, Timestream> timeStreams) {

//        LinearLayout timestreamView;
//        int timestreamViewIndex = 0;
//
//        for (Timestream timestream : timeStreams.values()) {
//
//            timestreamView = (LinearLayout) draggableLinearLayout.getChildAt(timestreamViewIndex);
//
//            loadTimestream(timestream, timestreamView.getId());
//
//            timestreamViewIndex++;
//        }

        ProductLoader.loadTimestreams(PD_INFO_ACTIVITY, draggableLinearLayout, timeStreams, 0);

//        prepareNext();

    }

    private static void prepareNext() {

        int tsViewId = ((LinearLayout) draggableLinearLayout.getChildAt(draggableLinearLayout.getChildCount() - 1)).getId();

        EditText e = (EditText) ((LinearLayout) draggableLinearLayout.findViewById(tsViewId)).getChildAt(1);

        e.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus && watcher.isShouldWatch()) {

                    e.setOnFocusChangeListener(null);
                    Timestream t = new Timestream();
                    currentProduct.getTimeStreams().put(t.getId(), t);
                    AIInputter.fillTheBlanks(currentProduct, t);
                    loadTimestream(t, tsViewId);
                    addTimestreamView(draggableLinearLayout, 0);
                    prepareNext();
                }
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public static void loadTimestream(Timestream timestream, int timestreamViewId) {

        LinearLayout tView = (LinearLayout) draggableLinearLayout.findViewById(timestreamViewId);

        String productDOP = DateUtil.typeMach(timestream.getProductDOP());

        EditText timestreamDOPEditText = (EditText) tView.getChildAt(1);
        EditText timestreamCoordinateEditText = (EditText) tView.getChildAt(3);
        EditText timestreamInventoryEditText = (EditText) tView.getChildAt(5);

        timestreamDOPEditText.setOnFocusChangeListener(null);

        if (productDOP.equals("")) {

            timestreamDOPEditText.setText(productDOP);

        } else {

            timestreamDOPEditText.setText(productDOP.substring(0, 10));
        }

        timestreamCoordinateEditText.setText(timestream.getProductCoordinate());

        timestreamInventoryEditText.setText(timestream.getProductInventory());

        timestream.setBoundLayoutId(String.valueOf(timestreamViewId));

        onShowTimeStreamsHashMap.put(tView.getId(), timestream);

        MyApplication.setTimeStreamViewOriginalBackground(timestream);

        watcher.watch(timestreamDOPEditText, timestream, MyApplication.TIMESTREAM_DOP, true);
        watcher.watch(timestreamCoordinateEditText, timestream, MyApplication.TIMESTREAM_COORDINATE, true);
        watcher.watch(timestreamInventoryEditText, timestream, MyApplication.TIMESTREAM_INVENTORY, true);


        DraggableLinearLayout.selectAll(timestreamDOPEditText);
    }

    @Override
    protected void onDestroy() {
        watcher.destroy();
        super.onDestroy();
    }

    private static void initTimestreamView(LinkedHashMap<String, Timestream> timestreams) {

        DraggableLinearLayout.setLayoutChanged(true);

        topTimestreamView = null;
        topDOPEditText = null;

        draggableLinearLayout.addView(ProductLoader.prepareNext(PD_INFO_ACTIVITY,
                currentProduct.getProductCode(), draggableLinearLayout));
        ProductLoader.initCellBody(draggableLinearLayout,timestreams,0,currentProduct.getProductCode());
//        // 根据根view的childCount计算timestreamView的数量
//        int timestreamViewCount = draggableLinearLayout.getChildCount() - 1;
//
//
//        while (timestreamViewCount > timestreams.size()) {
//
//            // 删除从上往下第一个timestream
//            draggableLinearLayout.removeView(draggableLinearLayout.getChildAt(0));
//
//            timestreamViewCount--;
//        }
//
//        while (timestreamViewCount < timestreams.size()) {
//
//            addTimestreamView(draggableLinearLayout, 0);
//
//            timestreamViewCount++;
//        }

        if (timestreams.size() > 0) {

            topTimestreamView = (LinearLayout) draggableLinearLayout.getChildAt(0);
            topDOPEditText = ((TimestreamCombinationView)topTimestreamView).getBuyDOPEt();

        }

    }

    public static int getViewState(View draggedView, double draggedRadius) {

        // 根据拖拽的距离判断是复制还是删除控件
        float viewHeight = draggedView.getHeight();

        boolean isDragToCopy = draggedRadius >= viewHeight * 0.5 && draggedRadius <= viewHeight * 1.6;
        boolean isDragToDelete = draggedRadius > viewHeight * 1.6;

        if (isDragToCopy) {

            return ADD_TIMESTREAM_LAYOUT;

        } else if (isDragToDelete) {

            return REMOVE_TIMESTREAM_LAYOUT;

        } else {

            return 0;

        }

    }

    public static void onTimestreamViewPositionChanged(View changedView, float horizontalDistance, float verticalDistance) {

        double dragRadius = Math.sqrt(horizontalDistance * horizontalDistance + verticalDistance * verticalDistance);

        int viewState = getViewState(changedView, dragRadius);

        switch (viewState) {

            case ADD_TIMESTREAM_LAYOUT:

                changedView.setBackgroundColor(Color.parseColor("#8BC34A"));
                break;

            case REMOVE_TIMESTREAM_LAYOUT:

                changedView.setBackgroundColor(Color.parseColor("#FF0000"));
                break;

            default:

                MyApplication.setTimeStreamViewOriginalBackground((LinearLayout) changedView);
        }
    }

    public static void onTimestreamViewReleased(View releasedChild, float horizontalDistance, float verticalDistance) {

        double dragRadius = Math.sqrt(horizontalDistance * horizontalDistance + verticalDistance * verticalDistance);

        int viewState = getViewState(releasedChild, dragRadius);

        switch (viewState) {


            case REMOVE_TIMESTREAM_LAYOUT:

                Timestream rmTs = MyApplication.unloadTimestream((LinearLayout) releasedChild);

                PDInfoWrapper.deleteTimestream(MyApplication.sqLiteDatabase, rmTs.getId());

                currentProduct.getTimeStreams().remove(rmTs.getId());

                break;

            case ADD_TIMESTREAM_LAYOUT:
//todo bug 空timestreamView复制时时间不为0点
//                addTimestream();
//                setTimeStreamViewOriginalBackgroundColor((LinearLayout) releasedChild);
            default:
                draggableLinearLayout.putBack(releasedChild);
                MyApplication.setTimeStreamViewOriginalBackground((LinearLayout) releasedChild);

                break;
        }
    }

    private static void addTimestream() {
        int viewId;
        Timestream nT = new Timestream();
        AIInputter.fillTheBlanks(currentProduct, nT);
        currentProduct.getTimeStreams().put(nT.getId(), nT);

        viewId = addTimestreamView(draggableLinearLayout, -1);
        loadTimestream(nT, viewId);
    }

    public static int addTimestreamView(LinearLayout rootView, int offset) {

        Context context = rootView.getContext();
        LinearLayout linearLayout = new LinearLayout(context);

        DraggableLinearLayout.setLayoutChanged(true);
        rootView.addView(linearLayout, rootView.getChildCount() + offset);

        for (int i = 0; i < 3; i++) {

            timestreamChildTextViewList.add(i, new TextView(context));
            timestreamChildEditTextList.add(i, new EditText(context));
        }

        for (int i = 0; i < 3; i++) {

            linearLayout.addView(timestreamChildTextViewList.get(i));
            linearLayout.addView(timestreamChildEditTextList.get(i));
        }


        decorate(linearLayout);

        return linearLayout.getId();
    }

    private static void decorate(LinearLayout linearLayout) {

        linearLayout.setId(View.generateViewId());

        LinearLayout.LayoutParams layoutParams;
        TextView textView;
        EditText editText;

        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setBackgroundColor(Color.parseColor("#4Dffffff"));
        linearLayout.setAlpha(0.8f);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 0; i < 3; i++) {

            textView = timestreamChildTextViewList.get(i);
            editText = timestreamChildEditTextList.get(i);

            draggableLinearLayout.setTextSiz(textView, 12);
            draggableLinearLayout.setTextSiz(editText, 12);

            layoutParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, layoutWeightArray[i * 2]);
            textView.setLayoutParams(layoutParams);


            layoutParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, layoutWeightArray[i * 2 + 1]);
            editText.setLayoutParams(layoutParams);

            textView.setText(textArray[i]);
            editText.setText(null);

            textView.setGravity(Gravity.CENTER);
            editText.setGravity(Gravity.CENTER);

            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    }

    @Override
    protected void onPause() {

        //暂停时将改动的商品信息保存到数据库,全局
        new Thread() {

            @Override
            public void run() {

                MyApplication.serialize();
            }
        }.start();
        super.onPause();
    }


}