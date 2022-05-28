package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.currentProduct;
import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;
import static com.shepherdboy.pdstreamline.MyApplication.onShowTimeStreamsHashMap;
import static com.shepherdboy.pdstreamline.MyApplication.originalPositionHashMap;
import static com.shepherdboy.pdstreamline.MyApplication.setTimeStreamViewOriginalBackgroundColor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.sql.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.utils.DateUtil;
import com.shepherdboy.pdstreamline.utils.ScanEventReceiver;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;
import com.shepherdboy.pdstreamline.view.MyTextWatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PDInfoActivity extends AppCompatActivity {

    private static final int ADD_TIMESTREAM_LAYOUT = 1;
    private static final int REMOVE_TIMESTREAM_LAYOUT = 2;

    private static ArrayList<TextView> timestreamChildTextViewList = new ArrayList<>(3);
    private static ArrayList<EditText> timestreamChildEditTextList = new ArrayList<>(3);
    private static float[] layoutWeightArray = new float[]{2.7f, 4f, 1.2f, 1.6f, 1.2f, 1f};
    private static String[] textArray = new String[]{"生产日期:", "坐标:", "库存:"};

    private static LinearLayout topTimestreamView;
    private static EditText topDOPEditText;

    public static EditText productCodeEditText;
    public static EditText productNameEditText;
    public static EditText productEXPEditText;
    public static Button productEXPTimeUnitButton;

    public static void actionStart() {

        Intent intent = new Intent(MyApplication.getContext(), PDInfoActivity.class);
        MyApplication.getContext().startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdinfo);
        MyApplication.initActionBar(getSupportActionBar());

        draggableLinearLayout = findViewById(R.id.parent);
        productCodeEditText = findViewById(R.id.product_code);
        productNameEditText = findViewById(R.id.product_name);
        productEXPEditText = findViewById(R.id.product_exp);
        productEXPTimeUnitButton = findViewById(R.id.time_unit);

        //点击按钮，测试用
        Button addProduct = (Button) findViewById(R.id.add_product);
        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serialize();

                String code = ((EditText)findViewById(R.id.product_code)).getText().toString();
                ScanEventReceiver.show(code);

            }
        });

    }

    public static void loadProduct(Product product) {

        MyTextWatcher.setShouldWatch(false);

        initTimestreamView(product.getTimeStreams());

        productCodeEditText.setText(product.getProductCode());
        productNameEditText.setText(product.getProductName());
        productEXPEditText.setText(product.getProductEXP());
        productEXPTimeUnitButton.setText(product.getProductEXPTimeUnit());

        MyTextWatcher.watch(productCodeEditText, null, MyApplication.PRODUCT_CODE);
        MyTextWatcher.watch(productNameEditText, null, MyApplication.PRODUCT_NAME);
        MyTextWatcher.watch(productEXPEditText, null, MyApplication.PRODUCT_EXP);
        MyTextWatcher.watch(productEXPTimeUnitButton);

        loadTimestreams(product.getTimeStreams());

        DraggableLinearLayout.setFocus(topDOPEditText);

        MyTextWatcher.setShouldWatch(true);
    }

    private static void loadTimestreams(LinkedHashMap<String, Timestream> timeStreams) {

        LinearLayout timestreamView;
        int timestreamViewIndex = 3;

        for (Timestream timestream : timeStreams.values()) {

            timestreamView = (LinearLayout) draggableLinearLayout.getChildAt(timestreamViewIndex);

            loadTimestream(timestream, timestreamView.getId());

            timestreamViewIndex++;
        }

    }

    public static void loadTimestream(Timestream timestream, int timestreamViewId) {

        LinearLayout tView = (LinearLayout) draggableLinearLayout.findViewById(timestreamViewId);

        String productDOP = DateUtil.typeMach(timestream.getProductDOP());

        EditText timestreamDOPEditText = (EditText) tView.getChildAt(1);
        EditText timestreamCoordinateEditText = (EditText) tView.getChildAt(3);
        EditText timestreamInventoryEditText = (EditText) tView.getChildAt(5);

        if (productDOP.equals("")) {

            timestreamDOPEditText.setText(productDOP);

        } else {

            timestreamDOPEditText.setText(productDOP.substring(0, 10));
        }

        timestreamCoordinateEditText.setText(timestream.getProductCoordinate());

        timestreamInventoryEditText.setText(timestream.getProductInventory());

        timestream.setBoundLayoutId(String.valueOf(timestreamViewId));

        MyApplication.setTimeStreamViewOriginalBackgroundColor(timestream);

        MyTextWatcher.watch(timestreamDOPEditText, timestream, MyApplication.TIMESTREAM_DOP);
        MyTextWatcher.watch(timestreamCoordinateEditText, timestream, MyApplication.TIMESTREAM_COORDINATE);
        MyTextWatcher.watch(timestreamInventoryEditText, timestream, MyApplication.TIMESTREAM_INVENTORY);

        onShowTimeStreamsHashMap.put(tView.getId(), timestream);

        DraggableLinearLayout.setFocus(timestreamDOPEditText);
    }

    private static void initTimestreamView(LinkedHashMap<String, Timestream> timestreams) {

        MyApplication.init();
        MyTextWatcher.clearWatchers();
        DraggableLinearLayout.setLayoutChanged(true);

        // 根据根view的childCount计算timestreamView的数量
        int timestreamViewCount = draggableLinearLayout.getChildCount() - 4;

        while (timestreamViewCount > timestreams.size()) {

            // 删除从上往下第一个timestream
            draggableLinearLayout.removeView(draggableLinearLayout.getChildAt(3));

            timestreamViewCount--;

        }

        while (timestreamViewCount < timestreams.size()) {

            addTimestreamView(draggableLinearLayout);

            timestreamViewCount++;

        }

        topTimestreamView = (LinearLayout) draggableLinearLayout.getChildAt(3);
        topDOPEditText = (EditText) topTimestreamView.getChildAt(1);

//        MyApplication.init();
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

                setTimeStreamViewOriginalBackgroundColor((LinearLayout) changedView);
        }
    }

    public static void onTimestreamViewReleased(View releasedChild, float horizontalDistance, float verticalDistance) {

        double dragRadius = Math.sqrt(horizontalDistance * horizontalDistance + verticalDistance * verticalDistance);

        int viewState = getViewState(releasedChild, dragRadius);

        int viewId;

        switch (viewState) {

            case ADD_TIMESTREAM_LAYOUT:
//todo bug 空timestreamView复制时时间不为0点
                Timestream nT = new Timestream();
                AIInputter.fillTheBlanks(currentProduct, nT);
                currentProduct.getTimeStreams().put(nT.getId(), nT);

                viewId = addTimestreamView(draggableLinearLayout);
                loadTimestream(nT, viewId);
                setTimeStreamViewOriginalBackgroundColor((LinearLayout) releasedChild);


                break;

            case REMOVE_TIMESTREAM_LAYOUT:

                Timestream rmTs = MyApplication.unloadTimestream((LinearLayout) releasedChild);

                MyDatabaseHelper.PDInfoWrapper.deleteTimestream(MyApplication.sqLiteDatabase, rmTs.getId());

                currentProduct.getTimeStreams().remove(rmTs.getId());

                if (originalPositionHashMap.size() < 1) {

                    viewId = addTimestreamView(draggableLinearLayout);
                    Timestream ts = new Timestream(currentProduct.getProductCode());
                    currentProduct.getTimeStreams().put(ts.getId(), ts);
                    ts.setUpdated(true);
                    loadTimestream(ts, viewId);
                }

                break;

            default:
                draggableLinearLayout.putBack(releasedChild);
                setTimeStreamViewOriginalBackgroundColor((LinearLayout) releasedChild);

                break;
        }
    }

    public static int addTimestreamView(LinearLayout rootView) {

        Context context = rootView.getContext();
        LinearLayout linearLayout = new LinearLayout(context);

        DraggableLinearLayout.setLayoutChanged(true);
        rootView.addView(linearLayout, 3 + MyApplication.originalPositionHashMap.size());

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

        linearLayout.setId(DateUtil.getIdByCurrentTime() + MyApplication.timeStreamIndex++);

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

            editText.setSelectAllOnFocus(true);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    }

    @Override
    protected void onPause() {

        //暂停时将改动的商品信息保存到数据库,全局
        serialize();
        super.onPause();
    }

    public static void serialize() {

        MyApplication.pickupChanges();
        MyApplication.saveChanges(MyApplication.thingsToSaveList);
    }
}