package com.shepherdboy.pdstreamline.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.customview.widget.ViewDragHelper;

import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.utils.DateUtil;
import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.sql.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Timestream;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class MyDragLayout extends LinearLayout {

    public static boolean synchronizeLock = false;

    public static boolean listening = false; // 记录当前布局是否处于监听状态

    private static HashMap<EditText, TextWatcher> editTextTextWatcherHashMap = new HashMap<>();

    public static final LinkedList thingsToSaveList = new LinkedList(); // 修改后需要保存的信息队列

//    ArrayList<Double> historyRate = new ArrayList<>();

    private static MyDragLayout myDragLayout;

    public static MyDragLayout getMyDragLayout() {
        return myDragLayout;
    }

    public static void setMyDragLayout(MyDragLayout myDragLayout) {
        MyDragLayout.myDragLayout = myDragLayout;
    }

    private static int timeStreamCount = 1; // 当前界面需要生成的timeStream LinearLayout数量

    private boolean toDeal = false; // boolean值记录控件是否可拖拽

    private ViewDragHelper viewDragHelper;

    private static LinkedHashMap<Integer, Timestream> myAutoBackViewsHashMap = new LinkedHashMap<>(); // hashMap存放时光流

    private HashMap<Integer, Point> originalPositionHashMap1 = new HashMap<>(); // hashMap存放每个时光流的初始坐标

    private LinkedList<Integer> freeIdLinkedList = new LinkedList<>(); // linkedList存放空余的时光流id号

    private static int topTimeStreamId = R.id.time_stream_0;

    private static EditText topTimeStreamDOPEditText = null;

    private int originalBackgroundColor = 0; // 拖拽对象的初始背景色

    private int currentViewId = 0; // 当前操作控件的id

    float horizontalDistance = 0; // 控件的水平移动距离

    float verticalDistance = 0; // 控件的垂直移动距离

    boolean verticalDraggable;

    boolean horizontalDraggable;

    static EditText productCodeEditText;
    static EditText productNameEditText;
    static EditText productEXPEditText;
    static Button productEXPTimeUnitButton;

    static LinkedHashMap<String, Timestream> timeStreamHashMap;

    public MyDragLayout(Context context, AttributeSet attributeSet) {

        super(context, attributeSet);

//        init(context, attributeSet);

        // 初始化空余时光流id表
        for (int i = 1; i <= 9; i++) {
            freeIdLinkedList.add(getResourceId(i, R.id.class));
        }

        viewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {

            @Override
            public boolean tryCaptureView(@NonNull View child, int pointerId) {
                return true;
            }

            @Override
            public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {

                if (horizontalDraggable) {

                    return left;
                }
                return child.getLeft();
            }

            @Override
            public int clampViewPositionVertical(@NonNull View child, int top, int dy) {

                if (verticalDraggable) {

                    return top;

                }
                return child.getTop();
            }


            @Override
            public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {

                int viewState = getViewState(releasedChild);

//                recordDragDistance(releasedChild);

                // 释放控件同步为点击时的控件
                releasedChild = findViewById(currentViewId);

                if (viewState == 0) {

                    comeback(currentViewId);

                } else if (viewState == 1) {

                    comeback(currentViewId);

                    int linearLayoutId = 0;
                    try {

                        linearLayoutId = addTimeStreamView(getContext());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Date DOP = AIInputter.getPossibleDOP(MyApplication.currentProduct.getProductEXP(),
                            MyApplication.currentProduct.getProductEXPTimeUnit());

                    Timestream timeStream = new Timestream(
                            MyApplication.currentProduct.getProductCode(),
                            MyApplication.currentProduct.getProductEXP(),
                            MyApplication.currentProduct.getProductEXPTimeUnit(),
                            DOP, null, null);

                    timeStream.setBoundLayoutId(String.valueOf(linearLayoutId));

                    LinearLayout timeSteamLinearLayout = findViewById(linearLayoutId);

                    addInfoChangeWatcher(timeSteamLinearLayout, timeStream);
                    timeStreamHashMap.put(timeStream.getId(), timeStream);

                    EditText DOPEditText = (EditText)(timeSteamLinearLayout.getChildAt(1));
                    DOPEditText.setText(DateUtil.typeMach(DOP).substring(0, 10));
                    setFocus(DOPEditText);

                } else {

                    deleteTimeStreamView(releasedChild);

                }
                invalidate();

                //用完记录后归零
                horizontalDistance = 0;
                verticalDistance = 0;

            }

            public void comeback(int id) {

                int x = originalPositionHashMap1.get(id).x;
                int y = originalPositionHashMap1.get(id).y;

                viewDragHelper.settleCapturedViewAt(x, y);

            }

            @Override
            public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {

                super.onViewPositionChanged(changedView, left, top, dx, dy);


                // 将操控的view与点击时view同步
                changedView = findViewById(currentViewId);


                Point originalPoint = originalPositionHashMap1.get(currentViewId);

                if (null != originalPoint) {

                    horizontalDistance = changedView.getX() - originalPoint.x;
                    verticalDistance = changedView.getY() - originalPoint.y;
                }


                int viewState = getViewState(changedView);

                if (viewState == 1) {

                    changedView.setBackgroundColor(Color.parseColor("#8BC34A"));

                } else if (viewState == 2) {

                    changedView.setBackgroundColor(Color.parseColor("#FF0000"));

                } else {

                    // 还原背景色
                    changedView.setBackgroundColor(originalBackgroundColor);

                }
            }



//            void recordDragDistance (View draggedView) {
//
//                double dragRadius = Math.sqrt(horizontalDistance * horizontalDistance +
//                        verticalDistance * verticalDistance);
//
//                // 根据拖拽的距离判断是复制还是删除控件
//                float viewHeight = draggedView.getHeight();
//
//
//                double dragRate = dragRadius / viewHeight;
//
//                historyRate.add(dragRate);
//                double averageRate;
//                double sumRate = 0;
//                for (double rate : historyRate) {
//
//                    sumRate += rate;
//
//                }
//                averageRate = sumRate / historyRate.size();
//
//                Toast.makeText(getContext(), dragRate + " average " + averageRate, Toast.LENGTH_SHORT).show();
//
//
//            }

            public int getViewState(View draggedView) {

                double dragRadius = Math.sqrt(horizontalDistance * horizontalDistance +
                        verticalDistance * verticalDistance);


                // 根据拖拽的距离判断是复制还是删除控件
                float viewHeight = draggedView.getHeight();

                boolean isDragToCopy = dragRadius >= viewHeight * 0.5 && dragRadius <= viewHeight * 1.6;
                boolean isDragToDelete = dragRadius > viewHeight * 1.6;

               if (isDragToCopy) {

                    return 1;

                } else if (isDragToDelete) {

                    return 2;

                } else {

                    return 0;

                }
            }


            @Override
            public int getViewHorizontalDragRange(@NonNull View child) {

                return getMeasuredWidth() - child.getMeasuredWidth();

            }

            @Override
            public int getViewVerticalDragRange(@NonNull View child) {

                return getMeasuredHeight() - child.getMeasuredHeight();

            }




            // 拖拽结束回到原始位置
        });
    }

//    private void init(Context context, AttributeSet attributeSet) {
//
//        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.MyDragLayout);
//
//        horizontalDraggable = typedArray.getBoolean(R.styleable.MyDragLayout_horizontalDraggable, true);
//        verticalDraggable = typedArray.getBoolean(R.styleable.MyDragLayout_verticalDraggable, true);
//        toDeal = typedArray.getBoolean(R.styleable.MyDragLayout_toDeal, false);
//
//    }


    public static boolean isUnLock() {
        return synchronizeLock;
    }

    public static void setSynchronizeLock(boolean synchronizeLock) {
        MyDragLayout.synchronizeLock = synchronizeLock;
    }

    /**
     * 针对保质期单位的监控器，点击可以切换并设置保质期单位
     *
     * @param productEXPTimeUnitButton
     * @param product
     */
    public static void addInfoChangeWatcher(Button productEXPTimeUnitButton, Product product) {
        //TODO 修改exp与expTimeUnit后需要更新临期与到期时间
        productEXPTimeUnitButton.setOnClickListener(new View.OnClickListener() {
            String timeUnit;

            @Override
            public void onClick(View view) {
                timeUnit = MyDragLayout.productEXPTimeUnitButton.getText().toString();
                switch (timeUnit) {

                    case "天":

                        product.setProductEXPTimeUnit("年");
                        MyDragLayout.productEXPTimeUnitButton.setText("年");

                        break;

                    case "年":

                        product.setProductEXPTimeUnit("月");
                        MyDragLayout.productEXPTimeUnitButton.setText("月");

                        break;

                    case "月":

                        product.setProductEXPTimeUnit("天");
                        MyDragLayout.productEXPTimeUnitButton.setText("天");

                        break;
                    default:
                }

                synchronize(product, timeStreamHashMap);

                product.setUpdated(false);
            }
        });

    }


    /**
     * 针对单个EditText的监控器
     *
     * @param editText
     * @param timeStream
     * @param fieldName  editText对应的filed
     */
    public static void addInfoChangeWatcher(EditText editText, @Nullable Timestream timeStream,
                                            @Nullable Product product, String fieldName) {

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

//                todo 改变文本后延迟指定的时间后自动提交改变后的值，实现更简洁的录入方式

                if (!listening) {
                    return;
                }

                String after = s.toString();

                switch (fieldName) {

                    case "productName":

                        if (!after.equals(product.getProductName())) {

                            product.setProductName(after);
                            product.setUpdated(false);

                        }

                        break;

                    case "productEXP":

                        if (!after.equals(product.getProductEXP())) {

                            if (!AIInputter.validate(after, timeStream, MyApplication.PRODUCT_EXP)) {


                                break;

                            }

                            product.setProductEXP(after);
                            product.setUpdated(false);
                            synchronize(product, timeStreamHashMap);
                            setSynchronizeLock(true);

                        }

                        break;

                    case "productDOP":

                        if (!((after + " 00:00:00").equals(DateUtil.typeMach(timeStream.getProductDOP())))) {

                            if (!AIInputter.validate(after, timeStream, MyApplication.TIMESTREAM_DOP)) {

                                if (after.length() != 4) {

                                    break;

                                }

                                try {

                                    after = AIInputter.translate(product, timeStream.getProductDOP(), after);

                                } catch (Exception e) {

                                    e.printStackTrace();

                                }

                            }

                            MyDatabaseHelper.PDInfoWrapper.deleteProductDOP(
                                    MyApplication.sqLiteDatabase,timeStream.getId());

                            if (after.equals("")) {

                                timeStream.setProductDOP(null);
                                timeStream.setProductPromotionDate(null);
                                timeStream.setProductExpireDate(null);

                            } else {

                                try {

                                    timeStream.setProductDOP(DateUtil.typeMach(after));

                                } catch (Exception e) {

                                    e.printStackTrace();
                                }

                                myDragLayout.loadTimeStream(timeStream, Integer.parseInt(timeStream.getBoundLayoutId()));

                                synchronize(product, timeStream);
                                setSynchronizeLock(true);


                            }
                        }

                        break;

                    case "productCoordinate":

                        if (after != null && !after.equals(timeStream.getProductCoordinate())) {

                            timeStream.setProductCoordinate(after);
                            timeStream.setUpdated(false);

                        }

                        break;
                    case "productInventory":

                        if (after != null && !after.equals(timeStream.getProductInventory())) {

                            timeStream.setProductInventory(after);
                            timeStream.setUpdated(false);

                        }

                        break;

                    default:

                }

            }
        };

        editText.addTextChangedListener(textWatcher);
        editTextTextWatcherHashMap.put(editText, textWatcher);

    }

    private static void synchronize(@Nullable Product product, HashMap timeStreams) {

        Timestream timeStream;
        for (Object object : timeStreams.values()) {

            timeStream = (Timestream) object;

            synchronize(product, timeStream);

        }

    }


    private static void synchronize(@Nullable Product product, Timestream timeStream) {

        timeStream.setProductPromotionDate(DateUtil.calculatePromotionDate(
                timeStream.getProductDOP(),
                Integer.parseInt(product.getProductEXP()),
                product.getProductEXPTimeUnit()
        ));

        timeStream.setProductExpireDate(DateUtil.calculateProductExpireDate(
                timeStream.getProductDOP(),
                Integer.parseInt(product.getProductEXP()),
                product.getProductEXPTimeUnit()

        ));

        setTimeStreamLinearLayoutColor(timeStream.getTimeStreamStateCode(),
                (LinearLayout) myDragLayout.findViewById(Integer.parseInt(timeStream.getBoundLayoutId())));

        timeStream.setUpdated(false);
    }

    // 删除TimeStream view
    public void deleteTimeStreamView(View view) {

        originalPositionHashMap1.remove(currentViewId);

        // 默认保留一个时光流
        if (myAutoBackViewsHashMap.size() == 1) {

            try {

                Timestream timeStream = new Timestream(MyApplication.currentProduct.getProductCode());
                int timeStreamLayoutId = addTimeStreamView(getContext());
                LinearLayout timeStreamLayout = findViewById(timeStreamLayoutId);

                addInfoChangeWatcher(timeStreamLayout, timeStream);

            } catch (Exception e) {

                e.printStackTrace();

            }

        }

        for (Timestream timeStream : timeStreamHashMap.values()) {

            String timeStreamId = timeStream.getId();
            String boundLayoutId = timeStream.getBoundLayoutId();
            if (boundLayoutId != null && boundLayoutId.equals(String.valueOf(currentViewId))) {

                timeStreamHashMap.remove(timeStreamId);

                MyDatabaseHelper.PDInfoWrapper.deleteProductDOP(
                        MyApplication.sqLiteDatabase, timeStreamId);

                break;
            }

        }

        removeView(view);
        myAutoBackViewsHashMap.remove(currentViewId);
        freeIdLinkedList.add(currentViewId);

    }

    // 添加TimeStream View
    public int addTimeStreamView(Context context) throws Exception {

        if (freeIdLinkedList.size() == 0) {
            Toast.makeText(getContext(), "最多10个生产日期,再加要收钱了哈！",
                    Toast.LENGTH_SHORT).show();
            throw new Exception("加不了了");
        }

        // id为parent的是根视图，时光流添加到该视图里
        MyDragLayout myDragLayout = findViewById(R.id.parent);

        LinearLayout linearLayout = new LinearLayout(context);
        decorate(0, null, linearLayout);
        linearLayout.setId(freeIdLinkedList.remove());
        if (freeIdLinkedList.size() == 1) {

            Toast.makeText(getContext(), "还有最后一哆嗦！", Toast.LENGTH_SHORT).show();

        } else if (freeIdLinkedList.size() == 0) {

            Toast.makeText(getContext(), "么得了", Toast.LENGTH_SHORT).show();

        }

        TextView productDateTextView = new TextView(context);
        decorate(2.7f, "生产日期:", productDateTextView);


        EditText productDOPEditText = new EditText(context);
        decorate(4f, null, productDOPEditText);

        TextView coordinateTextView = new TextView(context);
        decorate(1.2f, "坐标:", coordinateTextView);


        EditText coordinateEditText = new EditText(context);
        decorate(1.6f, null, coordinateEditText);

        TextView inventoryTextView = new TextView(context);
        decorate(1.2f, "库存:", inventoryTextView);

        EditText inventoryEditText = new EditText(context);
        decorate(1f, null, inventoryEditText);

        myDragLayout.addView(linearLayout, 3 + myAutoBackViewsHashMap.size());
        linearLayout.addView(productDateTextView);
        linearLayout.addView(productDOPEditText);
        linearLayout.addView(coordinateTextView);
        linearLayout.addView(coordinateEditText);
        linearLayout.addView(inventoryTextView);
        linearLayout.addView(inventoryEditText);

        myAutoBackViewsHashMap.put(linearLayout.getId(), null);

        return linearLayout.getId();

    }

    public static void setFocus(EditText editText) {

        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        editText.requestFocusFromTouch();
        editText.selectAll();

    }

    /**
     * 通过id名字符串获取id值
     *
     * @param index   id序号
     * @param idClass R.id.class
     * @return id值
     */
    private int getResourceId(int index, Class<R.id> idClass) {

        try {

            Field idField = idClass.getDeclaredField("time_stream_" + index);

            return idField.getInt(idField);

        } catch (Exception e) {

            e.printStackTrace();

            return -2;
        }

    }

    // 获取当前指针下最前面的控件id
    public boolean getCurrentViewId(MotionEvent event) {

        try {

            View toCapture = viewDragHelper.findTopChildUnder((int) event.getX(), (int) event.getY());

            if (null != toCapture && toCapture instanceof LinearLayout) {

                LinearLayout currentTimeStream = (LinearLayout) toCapture;

                currentViewId = currentTimeStream.getId();

                originalBackgroundColor = ((ColorDrawable) currentTimeStream.getBackground()).getColor();

                return true;

            }
            currentViewId = 0;

        } catch (Exception e) {

            Log.e("mmp", "么得颜色");

        }

        return false;

    }

    public boolean isToDeal(int id) {

        return toDeal || id == R.id.time_stream_0 ||
                id == R.id.time_stream_1 ||
                id == R.id.time_stream_2 ||
                id == R.id.time_stream_3 ||
                id == R.id.time_stream_4 ||
                id == R.id.time_stream_5 ||
                id == R.id.time_stream_6 ||
                id == R.id.time_stream_7 ||
                id == R.id.time_stream_8 ||
                id == R.id.time_stream_9;

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        reloadHashMap();

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {

            getCurrentViewId(event);

            toDeal = isToDeal(currentViewId);

        }

        if (toDeal) {


            return viewDragHelper.shouldInterceptTouchEvent(event);

        }

        return super.onInterceptTouchEvent(event);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {

            getCurrentViewId(event);

            toDeal = isToDeal(currentViewId);

        }

        if (toDeal) {

            viewDragHelper.processTouchEvent(event);

            return true;

        }

        return super.onTouchEvent(event);

    }

    // 设置view的字体
    public void setTextSiz(View view, int dpSze) {


        if (view instanceof EditText) {

            ((EditText) view).setTextSize(TypedValue.COMPLEX_UNIT_DIP, dpSze);

        }

        if (view instanceof TextView) {

            ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_DIP, dpSze);

        }

        if (view instanceof Button) {

            ((Button) view).setTextSize(TypedValue.COMPLEX_UNIT_DIP, dpSze);

        }


    }

    // 设置timeStream的属性
    public void decorate(float layoutWeight, @Nullable String text, View view) {


        if (view instanceof LinearLayout) {

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    68
            );
            view.setLayoutParams(layoutParams);
            view.setBackgroundColor(Color.parseColor("#4Dffffff"));
            view.setAlpha(0.8f);
            ((LinearLayout) view).setOrientation(LinearLayout.HORIZONTAL);

        }
        // TODO 重新装饰
        if (view instanceof TextView) {

            setTextSiz(view, 12);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, layoutWeight
            );

            view.setLayoutParams(layoutParams);
            ((TextView) view).setText(text);
            ((TextView) view).setGravity(Gravity.CENTER);

        }

        if (view instanceof EditText) {


            setTextSiz(view, 12);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    0, LayoutParams.WRAP_CONTENT, layoutWeight
            );

            view.setLayoutParams(layoutParams);
            ((EditText) view).setGravity(Gravity.CENTER);
            ((EditText) view).setSelectAllOnFocus(true);
            ((EditText) view).setInputType(InputType.TYPE_CLASS_NUMBER);

        }

    }

//    public static int dip2px(Context context, float dpValue) {
//
//        final float scale = context.getResources().getDisplayMetrics().density;
//        return (int) (dpValue * scale + 0.5f);
//
//    }

    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    // 根据TimeStream对象的数量增减timeStream LinearLayout数量，并将内容清空
    public void initTimeStreams() {

        reloadHashMap();

        int timeStreamLinearLayoutCount = myAutoBackViewsHashMap.size();

        if (timeStreamLinearLayoutCount < timeStreamCount && timeStreamCount > 0) {

            do {

                try {
                    addTimeStreamView(getContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                timeStreamLinearLayoutCount++;

            } while (timeStreamLinearLayoutCount < timeStreamCount);

        } else if (timeStreamLinearLayoutCount > timeStreamCount && timeStreamCount > 0) {

            do {

                MyDragLayout parent = findViewById(R.id.parent);

                LinearLayout linearLayout = (LinearLayout) parent.getChildAt(3);
                currentViewId = linearLayout.getId();
                deleteTimeStreamView(linearLayout);
                timeStreamLinearLayoutCount--;

            } while (timeStreamLinearLayoutCount > timeStreamCount);

        }

        for (int i : myAutoBackViewsHashMap.keySet()) {

            unloadTimeStream(findViewById(i));
            myAutoBackViewsHashMap.put(i, null);

        }

        reloadHashMap();

    }

    // 清空单一timeStream
    public void unloadTimeStream(ViewGroup viewGroup) {

        int childCount = viewGroup.getChildCount();
        View childView;

        for (int i = 0; i < childCount; i++) {

            childView = viewGroup.getChildAt(i);

            if (childView instanceof EditText) {

                ((EditText) childView).setText("");

            }

        }

    }

    // 将timeStream信息绑定到timeStream LinearLayout中
    public void loadTimeStream(Timestream timeStream, int timeStreamLinearLayoutId) {

        LinearLayout timeStreamLinearLayout = findViewById(timeStreamLinearLayoutId);

        String productDOP = DateUtil.typeMach(timeStream.getProductDOP());

        if (productDOP.equals("")) {

            ((EditText) (timeStreamLinearLayout.getChildAt(1)))
                    .setText(productDOP);

        } else {

            ((EditText) (timeStreamLinearLayout.getChildAt(1)))
                    .setText(productDOP.substring(0, 10));

        }

        ((EditText) (timeStreamLinearLayout.getChildAt(3)))
                .setText(timeStream.getProductCoordinate());

        ((EditText) (timeStreamLinearLayout.getChildAt(5)))
                .setText(String.valueOf(timeStream.getProductInventory()));

        myAutoBackViewsHashMap.put(timeStreamLinearLayout.getId(), timeStream);

        timeStream.setBoundLayoutId(String.valueOf(timeStreamLinearLayoutId));

        setTimeStreamLinearLayoutColor(timeStream.getTimeStreamStateCode(), timeStreamLinearLayout);


    }

    /**
     * 根据timeStream状态设置linearLayout颜色，针对单个timestream
     */

    public static void setTimeStreamLinearLayoutColor(int timeStreamStateCode, LinearLayout timeStreamLinearLayout) {

        switch (timeStreamStateCode) {

            case 1:

                timeStreamLinearLayout.setBackgroundColor(Color.YELLOW);
                break;

            case -1:

                timeStreamLinearLayout.setBackgroundColor(Color.GRAY);
                break;

            default:
                timeStreamLinearLayout.setBackgroundColor(0);
                break;

        }

    }

    // 更新layout中的时光流
    public void reloadHashMap() {

        originalPositionHashMap1.clear();
        myAutoBackViewsHashMap.clear();
        LinearLayout temp;

        Point originalPosition;

        // 时光流控件是从第4个childView开始倒数第2个childView结束，初始为5个childView
        int order = 0;
        for (int timeStreamIndex = 0; timeStreamIndex < getChildCount() - 4; timeStreamIndex++) {

            temp = (LinearLayout) getChildAt(timeStreamIndex + 3);

            if (order == 0) {

                topTimeStreamId = temp.getId();

                topTimeStreamDOPEditText = (EditText) temp.getChildAt(1);

                order++;

            }

            originalPosition = new Point(temp.getLeft(), temp.getTop());

            originalPositionHashMap1.put(temp.getId(), originalPosition);

            if (!myAutoBackViewsHashMap.containsKey(temp.getId())) {

                myAutoBackViewsHashMap.put(temp.getId(), null);

            }
        }

//        originalBackgroundColor = ((ColorDrawable) myDragLayout.getChildAt(3).getBackground()).getColor();
    }

    public static void addInfoChangeWatcher(LinearLayout timeStreamLinearLayout, Timestream timeStream) {

        addInfoChangeWatcher((EditText) timeStreamLinearLayout.getChildAt(1),
                timeStream, MyApplication.currentProduct, "productDOP");
        addInfoChangeWatcher((EditText) timeStreamLinearLayout.getChildAt(3),
                timeStream, MyApplication.currentProduct, "productCoordinate");
        addInfoChangeWatcher((EditText) timeStreamLinearLayout.getChildAt(5),
                timeStream, MyApplication.currentProduct, "productInventory");

    }

    // 为所有包含商品信息的控件添加信息更改监控
    public static void addInfoChangeWatcherForAll(Product product) {

        for (Map.Entry<EditText, TextWatcher> entry : editTextTextWatcherHashMap.entrySet()) {

            entry.getKey().removeTextChangedListener(entry.getValue());

        }

        editTextTextWatcherHashMap.clear();

        addInfoChangeWatcher(productNameEditText, null, product, "productName");
        addInfoChangeWatcher(productEXPEditText, null, product, "productEXP");
        addInfoChangeWatcher(productEXPTimeUnitButton, product);

        LinearLayout timeStreamLinearLayout;
        Timestream timeStream;
        for (Map.Entry entry : myAutoBackViewsHashMap.entrySet()) {

            timeStreamLinearLayout = (LinearLayout) myDragLayout.findViewById((int) entry.getKey());
            timeStream = (Timestream) entry.getValue();

            addInfoChangeWatcher(timeStreamLinearLayout, timeStream);

        }


    }

    // 将改变的信息保存到thingsToSaveStake里面
    public static void pickupChanges() {

        if (MyApplication.currentProduct != null) {

            if (!MyApplication.currentProduct.isUpdated()) {

                thingsToSaveList.add(MyApplication.currentProduct);

            }

            if (!MyApplication.currentProduct.getTimeStreams().isEmpty()) {

                for (Timestream timeStream : timeStreamHashMap.values()) {

                    if (!timeStream.isUpdated()) {

                        thingsToSaveList.add(timeStream);

                    }

                }

            }

        }


    }

    public static void checkEssentialInfo(Product product) {

        for (String fieldName : AIInputter.essentialProductInfoList) {

            switch (fieldName) {

                case "productName":

                    if ("新商品，请输入商品名".equals(product.getProductName())) {

                        AIInputter.blanksToFillQueue.put("productName", productNameEditText);

                    }
                    break;

                case "productEXP":

                    if ("1".equals(product.getProductEXP())) {

                        AIInputter.blanksToFillQueue.put("productEXP", productEXPEditText);

                    }
                    break;


            }

        }

        for (String filedName : AIInputter.essentialTimestreamInfoList) {

            switch (filedName) {

                case "productDOP":

                    LinearLayout timeStreamLinearLayout = myDragLayout.findViewById(topTimeStreamId);
                    EditText DOPEditText = (EditText) timeStreamLinearLayout.getChildAt(1);
                    Timestream timeStream = myAutoBackViewsHashMap.get(topTimeStreamId);

                    if (timeStream.getTimeStreamStateCode() != 0) {

                        AIInputter.blanksToFillQueue.put("productDOP", DOPEditText);

                    }

            }

        }


    }


    // 展示
    public static void showProductInf(Product product) {
        listening = false;

        timeStreamHashMap = product.getTimeStreams();
        timeStreamCount = timeStreamHashMap.size();
        myDragLayout.initTimeStreams();

        productCodeEditText = myDragLayout.findViewById(R.id.product_code);
        productNameEditText = myDragLayout.findViewById(R.id.product_name);
        productEXPEditText = myDragLayout.findViewById(R.id.product_exp);
        productEXPTimeUnitButton = myDragLayout.findViewById(R.id.time_unit);

        productCodeEditText.setText(product.getProductCode());
        productNameEditText.setText(product.getProductName());
        productEXPEditText.setText(String.valueOf(product.getProductEXP()));
        productEXPTimeUnitButton.setText(product.getProductEXPTimeUnit());

        //展示时光流 0条， 1条， 多条
        Timestream tempTimestream;
        if (timeStreamHashMap.size() == 1) {

            for (Timestream timeStream : timeStreamHashMap.values()) {

                myDragLayout.loadTimeStream(timeStream, topTimeStreamId);

            }

        } else if (timeStreamHashMap.size() > 1) {

            int timeStreamIndex = 0;
            for (int timeStreamLinearLayoutId : myAutoBackViewsHashMap.keySet()) {

                for (Timestream timeStream : timeStreamHashMap.values()) {

                    if (timeStream.getBoundLayoutId() == null) {

                        myDragLayout.loadTimeStream(timeStream,
                                timeStreamLinearLayoutId);

                        break;

                    }

                }

                timeStreamIndex++;
            }

        }

        addInfoChangeWatcherForAll(product);
//        checkEssentialInfo(product);
        listening = true;

        setFocus(topTimeStreamDOPEditText);

        //todo 阻塞式
//        AIInputter.fillTheEssentialInfo(product);

    }

}
