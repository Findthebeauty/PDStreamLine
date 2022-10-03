package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.PD_INFO_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.currentProduct;
import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.beanview.ProductLoader;
import com.shepherdboy.pdstreamline.beanview.TimestreamCombinationView;
import com.shepherdboy.pdstreamline.binder.ProductObserver;
import com.shepherdboy.pdstreamline.binder.ProductSubject;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;
import com.shepherdboy.pdstreamline.utils.ScanEventReceiver;
import com.shepherdboy.pdstreamline.view.ActivityInfoChangeWatcher;
import com.shepherdboy.pdstreamline.view.ClosableScrollView;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PDInfoActivity extends BaseActivity {

    private static final int ADD_TIMESTREAM_LAYOUT = 1;
    private static final int REMOVE_TIMESTREAM_LAYOUT = 2;

    private static final int SHOW_PRODUCT_BY_CODE = 10;
    private static final int SHOW_PRODUCT_BY_INSTANCE = 11;

    private static Handler showHandler;
    private static String productToShow;
    private static ActivityInfoChangeWatcher watcher;

    private static ArrayList<TextView> timestreamChildTextViewList = new ArrayList<>(3);
    private static ArrayList<EditText> timestreamChildEditTextList = new ArrayList<>(3);
    private static float[] layoutWeightArray = new float[]{2.7f, 4f, 1.2f, 1.6f, 1.2f, 1f};
    private static String[] textArray = new String[]{"生产日期:", "坐标:", "库存:"};

    private static LinearLayout topTimestreamView;
    private static TimestreamCombinationView nextTrigger;
    private static EditText topDOPEditText,productCodeEditText,productNameEditText,
            productEXPEditText,productSpecEditText;
    public static Button scanner,productEXPTimeUnitButton;

    private static Activity activity;
    private static TextView tail;

    private static DraggableLinearLayout dragLayout;
    private static ClosableScrollView scrollView;

    private static ProductObserver observer;
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

    public static void postShowProduct(String after) {

        postMessage(SHOW_PRODUCT_BY_CODE, after);
    }

    private static void postMessage(int what, Object obj) {

        Message msg = showHandler.obtainMessage();
        msg.what = what;
        msg.obj = obj;
        showHandler.sendMessage(msg);
    }

    public static void postLoadProduct(Product currentProduct) {
        postMessage(SHOW_PRODUCT_BY_INSTANCE, currentProduct);
    }

    @Override
    protected void onStart() {
//        MyApplication.init();
        super.onStart();

        if (showHandler == null) {

            showHandler = new Handler() {

                @Override
                public void handleMessage(@NonNull Message msg) {

                    switch (msg.what) {

                        case SHOW_PRODUCT_BY_CODE:

                            searchNext((String) msg.obj);
                            break;

                        case SHOW_PRODUCT_BY_INSTANCE:

                            loadProduct((Product) msg.obj);
                            break;

                        case ProductSubject.SYNC_PRODUCT:

                            Product product = (Product) msg.obj;
                            if (product.getProductCode().equals(currentProduct.getProductCode()))
                                loadProduct(product);
                            break;
                    }
                }
            };

            MyApplication.handlers.add(showHandler);
        }

        initActivity();

        if (productToShow != null) {

            searchNext(productToShow);
            productToShow = null;

        } else if (currentProduct != null) {

            loadProduct(currentProduct);
        }

        watcher.setShouldWatch(true, MyApplication.PRODUCT_CODE);
    }

    private void initActivity() {

//        ActivityInfoChangeWatcher.init();
        MyApplication.activityIndex = PD_INFO_ACTIVITY;
        if(observer == null) {
            observer = new ProductObserver(PD_INFO_ACTIVITY, showHandler);
            MyApplication.productSubject.attach(observer);
        }
        dragLayout = findViewById(R.id.parent);
        draggableLinearLayout = dragLayout;
        scrollView = findViewById(R.id.closableScrollView);
        MyApplication.closableScrollView = scrollView;

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
    public void loadProduct(Product product) {

        watcher.setShouldWatch(false);

        LinkedHashMap<String, Timestream> timeStreams = product.getTimeStreams();

        initTimestreamView(product);

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
        ProductLoader.refreshTailHeight(this, dragLayout, tail);

        if(timeStreams.size() > 0) DraggableLinearLayout.selectAll(topDOPEditText);

        watcher.setShouldWatch(true);
    }

    private void loadTimestreams(LinkedHashMap<String, Timestream> timeStreams) {

        ProductLoader.loadTimestreams(PD_INFO_ACTIVITY, dragLayout, timeStreams, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        ActivityInfoChangeWatcher.destroy(watcher);

        nextTrigger = null;
        tail = null;

        MyApplication.productSubject.detach(observer);
        observer = null;
        super.onDestroy();
    }

    private void initTimestreamView(Product product) {

        dragLayout.setLayoutChanged(true);

        topTimestreamView = null;
        topDOPEditText = null;

        if (nextTrigger != null)
            dragLayout.removeView(nextTrigger);

        nextTrigger = (TimestreamCombinationView) ProductLoader.prepareNext(PD_INFO_ACTIVITY,
                product, dragLayout);

        dragLayout.addView(nextTrigger, dragLayout.getChildCount() - 1);

        if (tail == null) {
            tail = ProductLoader.prepareTailView(this, dragLayout);
            dragLayout.addView(tail);
        }

        ProductLoader.initCellBody(PD_INFO_ACTIVITY,
                dragLayout,product.getTimeStreams().size(),0);

        if (product.getTimeStreams().size() > 0) {

            topTimestreamView = (LinearLayout) dragLayout.getChildAt(0);
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

                if (rmTs == null) {
                    dragLayout.putBack(releasedChild);
                    return;
                }
                PDInfoWrapper.deleteTimestream(MyApplication.sqLiteDatabase, rmTs.getId());

                currentProduct.getTimeStreams().remove(rmTs.getId());

                MyApplication.productSubject.notify(currentProduct.getProductCode());
                break;

            case ADD_TIMESTREAM_LAYOUT:
//todo bug 空timestreamView复制时时间不为0点
//                addTimestream();
//                setTimeStreamViewOriginalBackgroundColor((LinearLayout) releasedChild);
            default:
                dragLayout.putBack(releasedChild);
                MyApplication.setTimeStreamViewOriginalBackground((LinearLayout) releasedChild);

                break;
        }
    }

    public static DraggableLinearLayout getDragLayout() {
        return dragLayout;
    }

    @Override
    protected void onPause() {

        MyApplication.closableScrollView = null;
        //暂停时将改动的商品信息保存到数据库,全局
        new Thread() {

            @Override
            public void run() {

                MyApplication.serialize(MyApplication.currentProduct);
            }
        }.start();
        super.onPause();
    }

    public static ProductObserver getObserver() {
        return observer;
    }
}