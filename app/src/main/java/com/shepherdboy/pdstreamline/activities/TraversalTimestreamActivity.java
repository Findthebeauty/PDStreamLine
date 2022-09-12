package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY_MODIFY_SHELF;
import static com.shepherdboy.pdstreamline.MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF;
import static com.shepherdboy.pdstreamline.MyApplication.activityIndex;
import static com.shepherdboy.pdstreamline.MyApplication.combinationHashMap;
import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;
import static com.shepherdboy.pdstreamline.MyApplication.setTimeStreamViewOriginalBackgroundColor;
import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;
import static com.shepherdboy.pdstreamline.services.MidnightTimestreamManagerService.basket;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.activities.transaction.Streamline;
import com.shepherdboy.pdstreamline.beans.Cell;
import com.shepherdboy.pdstreamline.beans.Product;
import com.shepherdboy.pdstreamline.beans.Row;
import com.shepherdboy.pdstreamline.beans.Shelf;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.beans.TimestreamCombination;
import com.shepherdboy.pdstreamline.beanview.BeanView;
import com.shepherdboy.pdstreamline.beanview.CellHeadView;
import com.shepherdboy.pdstreamline.beanview.TimestreamCombinationView;
import com.shepherdboy.pdstreamline.dao.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;
import com.shepherdboy.pdstreamline.dao.ShelfDAO;
import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.view.ClosableScrollView;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;
import com.shepherdboy.pdstreamline.view.MyInfoChangeWatcher;
import com.shepherdboy.pdstreamline.view.ShelfAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TraversalTimestreamActivity extends AppCompatActivity {

    private static int dragThreshold = 105;
    private static final int DRAG_LEFT = 200;
    private static final int DRAG_RIGHT = 201;

    private Shelf tempShelf;//货架信息改变时临时保存改变后的信息，用于回滚操作
    private Shelf defaultShelf;
    private static Shelf currentShelf;
    private static Row currentRow;
    private static ArrayList<Shelf> shelfList;
    private static ArrayList<String> classifyList;

    public static final int MSG_MODIFY_SHELF = 0;
    public static final int MSG_SHOW_SHELF = 1;
    public static final int MSG_REFRESH_TAIL_HEIGHT = 2;
    public static final int MSG_SYNC_PRODUCT = 3;
    private static final int MSG_LOAD_VIEWS = 4;
    private static final int MSG_LOCATE_PRODUCT = 5;

    public static final int LAYOUT_SHELF_LIST = 100;
    public static final int LAYOUT_SHOW_SHELF_PRODUCT = 101;
    public static final int LAYOUT_MODIFY_SHELF_INFO = 102;
    private static int layoutIndex;

    public static Handler handler;

    public static ConcurrentLinkedQueue<View> newViews = new ConcurrentLinkedQueue<>();
    public static ConcurrentHashMap<String, View> headViews = new ConcurrentHashMap<>();
    private static final boolean[] continueProcess = new boolean[]{true};
    private static boolean loadFinished = false;
    private static boolean prepareFinished = false;
    private static TextView tail;

    public static void onViewPositionChanged(View changedView, float horizontalDistance, float verticalDistance) {

        continueProcess[0] = false;

        int viewState = getViewState(changedView, horizontalDistance);

        switch (viewState) {

            case DRAG_LEFT:

                changedView.setBackground(MyApplication.drawableSecondLevel);
                break;

            case DRAG_RIGHT:

                changedView.setBackground(MyApplication.drawableFirstLevel);
                break;

            default:

                setTimeStreamViewOriginalBackgroundColor((LinearLayout) changedView);
                break;
        }
    }

    private static int getViewState(View changedView, float horizontalDistance) {

        if (horizontalDistance < 0 && Math.abs(horizontalDistance) > dragThreshold)
            return DRAG_LEFT;

        if (horizontalDistance > 0 && Math.abs(horizontalDistance) > dragThreshold)
            return DRAG_RIGHT;

        return 0;
    }

    public static void onViewReleased(View releasedChild, float horizontalDistance, float verticalDistance) {

        int stateCode = getViewState(releasedChild, horizontalDistance);

        switch (stateCode) {

            case DRAG_LEFT:

                Timestream ts = MyApplication.unloadTimestream((LinearLayout) releasedChild);
                if (ts == null) {

                    draggableLinearLayout.putBack(releasedChild);
                    return;
                }

                MyApplication.deleteTimestream(ts.getId());
                ts.setInBasket(false);
                basket.remove(ts);
                break;

            case DRAG_RIGHT:

                ts = MyApplication.unloadTimestream((LinearLayout) releasedChild);


                if (ts == null) {

                    draggableLinearLayout.putBack(releasedChild);
                    return;
                }
                PDInfoWrapper.deleteTimestream(sqLiteDatabase, ts.getId());

                if (ts.getSiblingPromotionId() != null) {

                    TimestreamCombination comb = combinationHashMap.get(ts.getId());
                    List<Timestream> unpackedTimestreams = comb.unpack();

                    for (Timestream t : unpackedTimestreams) {

                        basket.add(t);
                        t.setInBasket(true);

                    }

                    Streamline.reposition(unpackedTimestreams);

                } else {

                    basket.add(ts);
                    ts.setInBasket(true);

                    Streamline.position(ts);
                }
                break;

            default:
                draggableLinearLayout.putBack(releasedChild);
                break;
        }

        if (!loadFinished) {

            Message msg = handler.obtainMessage();
            msg.what = MSG_LOAD_VIEWS;
            handler.sendMessage(msg);
        }
    }

    public static void recordTopProduct() {
        int x = draggableLinearLayout.getChildAt(0).getWidth() / 2;
        int offsetY = draggableLinearLayout.getChildAt(0).getHeight() / 2;
        ScrollView scr = (ScrollView) (draggableLinearLayout.getParent());
        View beanView = draggableLinearLayout.viewDragHelper.findTopChildUnder(x,
                 scr.getScrollY() + offsetY);

        if (beanView instanceof LinearLayout) {
            MyApplication.intentProductCode = ((BeanView)beanView).getProductCode();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (activityIndex == TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF)
        recordTopProduct();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initShowShelfList(Context context) {

        setContentView(R.layout.activity_traversal_timestream);
        MyApplication.initActionBar(getSupportActionBar());

        layoutIndex = LAYOUT_SHELF_LIST;

        if (defaultShelf == null) {
            defaultShelf = new Shelf();
            defaultShelf.setName("默认");
        }

        shelfList = ShelfDAO.getShelves();
        shelfList.add(defaultShelf);

        MyApplication.activityIndex = TRAVERSAL_TIMESTREAM_ACTIVITY;

        Button addShelfBt = findViewById(R.id.add_shelf);

//        setShelfList(context, shelfList);
        loadShelfEntry();
        addShelfBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setContentView(R.layout.shelf_info);

                layoutIndex = LAYOUT_MODIFY_SHELF_INFO;
                MyApplication.activityIndex = MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY_MODIFY_SHELF;

                Shelf shelf = new Shelf();

                loadShelfInfo(context, shelf);
                setClassifyList(context, shelf);
            }
        });


    }

    /**
     * 同步商品信息
     * @param code
     */
    private void syncProduct(String code) {

        List<BeanView> beanViews = MyApplication.productBeanViewsMap.get(code);

        for (BeanView beanView : beanViews) {

            if (beanView instanceof CellHeadView) {

                beanView.bindData(code);
            }

            if (beanView instanceof TimestreamCombinationView) {

                beanView.bindData(((TimestreamCombinationView) beanView).getTimestreamId());
            }
        }

    }

    private void modifyShelf(Shelf shelf) {
        setContentView(R.layout.shelf_info);
        initModifyShelf();
        loadShelfInfo(this, shelf);
        setClassifyList(this, shelf);
    }

    private void initModifyShelf() {
        MyApplication.activityIndex = TRAVERSAL_TIMESTREAM_ACTIVITY_MODIFY_SHELF;
        layoutIndex = LAYOUT_MODIFY_SHELF_INFO;
    }

    public static ArrayList<Shelf> getShelfList() {
        return shelfList;
    }

    private void loadShelfEntry() {

        RecyclerView recyclerView = findViewById(R.id.shelves);

        StaggeredGridLayoutManager layoutManager = new
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);

        ShelfAdapter shelfAdapter = new ShelfAdapter(shelfList);

        recyclerView.setAdapter(shelfAdapter);

    }

    private void clearRecyclerView() {

        RecyclerView recyclerView = findViewById(R.id.shelves);
        recyclerView.setAdapter(null);
        recyclerView.getLayoutManager().removeAllViews();
        recyclerView.setLayoutManager(null);

    }

    /**
     * 编辑货架信息
     * @param context
     * @param shelf
     */
    private void loadShelfInfo(Context context, Shelf shelf) {

        tempShelf = new Shelf(shelf);

        EditText nameEt = findViewById(R.id.name_et);
        Spinner classifySp = findViewById(R.id.classify_sp);
        EditText maxRow = findViewById(R.id.max_row_et);
        Button cancelBt = findViewById(R.id.cancel_bt);
        Button deleteBt = findViewById(R.id.delete_bt);
        Button saveBt = findViewById(R.id.save_bt);
        Button addClassifyBt = findViewById(R.id.add_classify_bt);

        nameEt.setText(shelf.getName());
        maxRow.setText(String.valueOf(shelf.getMaxRowCount()));

        addClassifyBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MyInfoChangeWatcher.commitAll(); //中止自动提交，主动提交变更的信息

                final EditText editText = new EditText(context);
                AlertDialog.Builder inputClassify = new AlertDialog.Builder(context);
                inputClassify.setTitle("请输入类别名").setView(editText);
                inputClassify.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String classify = editText.getText().toString();

                        boolean validated = AIInputter.validate(classify, shelf, MyApplication.SHELF_CLASSIFY);

                        if (validated) {

                            shelf.setClassify(editText.getText().toString());
                            shelf.setInfoChanged(true);
                            classifyList.add(classify);
                            setClassifyList(context, shelf, classifyList);
                        }

                    }
                }).show();
            }
        });

        setClassifyList(context, shelf);

        MyInfoChangeWatcher.watch(nameEt, shelf, MyApplication.SHELF_NAME);
        MyInfoChangeWatcher.watch(maxRow, shelf, MyApplication.SHELF_MAX_ROW);

        classifySp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                shelf.setClassify(classifyList.get(position));
                shelf.setInfoChanged(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cancelModify(shelf);

            }
        });

        deleteBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);
                normalDialog.setTitle("删除货架");

                int recordCount = ShelfDAO.selectCount(shelf.getId());

                String message = recordCount > 0 ? "此货架关联：" + recordCount + "条商品信息,确定删除吗？" :
                        "不可逆操作,确认删除吗？";

                normalDialog.setMessage(message);
                normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ShelfDAO.deleteShelf(shelf.getId());
                        setContentView(R.layout.activity_traversal_timestream);
                        exitModifyShelf();
                    }
                });

                normalDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        setContentView(R.layout.activity_traversal_timestream);
                        exitModifyShelf();

                    }
                });

                normalDialog.show();
            }
        });

        saveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MyInfoChangeWatcher.commitAll(); //中止自动提交，主动提交变更的信息

                if (shelf.isInfoChanged()) ShelfDAO.update(shelf);
                setContentView(R.layout.activity_traversal_timestream);
                exitModifyShelf();
            }
        });

        DraggableLinearLayout.selectAll(nameEt);
    }

    private void exitModifyShelf() {

        layoutIndex = LAYOUT_SHELF_LIST;
        initShowShelfList(TraversalTimestreamActivity.this);
    }

    private void initTransaction() {

        MyApplication.initActionBar(getSupportActionBar());

        loadFinished = false;
        prepareFinished = true;

        if (layoutIndex == LAYOUT_SHOW_SHELF_PRODUCT) {

            showShelf(currentShelf);
            return;
        }

        if (layoutIndex == LAYOUT_MODIFY_SHELF_INFO) {

            modifyShelf(currentShelf);
            return;
        }

        layoutIndex = LAYOUT_SHELF_LIST;
        initShowShelfList(TraversalTimestreamActivity.this);
    }

    private void cancelModify(Shelf shelf) {

        shelf.setName(tempShelf.getName());
        shelf.setClassify(tempShelf.getClassify());
        shelf.setMaxRowCount(tempShelf.getMaxRowCount());
        shelf.setInfoChanged(false);
        initShowShelfList(TraversalTimestreamActivity.this);
    }


    /**
     * 展示货架
     * @param shelf
     */
    private void showShelf(Shelf shelf) {

        //todo 展示货架
        currentShelf = shelf;
        setContentView(R.layout.shelf);

        initShowShelf();

        MyInfoChangeWatcher.setShouldWatch(false);
        showRow(draggableLinearLayout, 1); //todo 货架行的遍历方式

        DraggableLinearLayout.setLayoutChanged(true);
        MyInfoChangeWatcher.setShouldWatch(true);

    }

    private void initShowShelf() {
        layoutIndex = LAYOUT_SHOW_SHELF_PRODUCT;
        MyApplication.activityIndex = TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF;

        draggableLinearLayout = TraversalTimestreamActivity.this.findViewById(R.id.row);
        newViews.clear();
        headViews.clear();
    }

    private void showRow(DraggableLinearLayout draggableLinearLayout, int rowNumber) {


        Row row = ShelfDAO.getRow(ShelfAdapter.getCurrentShelf(), rowNumber);

        currentRow = row;
        if (combinationHashMap == null)
        combinationHashMap = PDInfoWrapper.getTimestreamCombinations(sqLiteDatabase);

        if(MyApplication.activityIndex == TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF)
        prepareTailView(draggableLinearLayout);

        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareViews(draggableLinearLayout, currentRow);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (MyApplication.intentProductCode != null) {

                    while ((!headViews.containsKey(MyApplication.intentProductCode)) || handler == null) {

                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    Message msg = handler.obtainMessage();
                    msg.obj = headViews.get(MyApplication.intentProductCode);
                    msg.what = MSG_LOCATE_PRODUCT;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void loadViews(DraggableLinearLayout draggableLinearLayout) {

        if (draggableLinearLayout == null) return;
        while (!newViews.isEmpty()) {

            View view = newViews.poll();
            draggableLinearLayout.addView(view, draggableLinearLayout.getChildCount() - 1);

            if (view instanceof CellHeadView) {

                headViews.put(((CellHeadView) view).getProductCode(), view);
            }
        }

        loadFinished = true;

        if (prepareFinished)
        refreshTailHeight();

        DraggableLinearLayout.setLayoutChanged(true);
    }

    private static void prepareViews(DraggableLinearLayout draggableLinearLayout, Row row) {

        continueProcess[0] = true;
        prepareFinished = false;
        synchronized (continueProcess) {
            while (row.getCells().size() > 0) {

                if(MyApplication.activityIndex != TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF) return;
                loadFinished = false;
                Cell cell = row.getCells().poll();
                MyApplication.productBeanViewsMap.put(cell.getProductCode(), new LinkedList<>());
                prepareCellView(cell, draggableLinearLayout);

                if (!continueProcess[0]) {
                    continue;
                }
                if(handler == null) return;
                Message msg = handler.obtainMessage();
                msg.what = MSG_LOAD_VIEWS;
                handler.sendMessage(msg);
            }


            prepareFinished = true;
        }
    }

    public static long log(long start, String s) {
        Log.d("showRow", s + (System.currentTimeMillis() - start) + "毫秒" + "");
        start = System.currentTimeMillis();
        return start;
    }

    private static void prepareCellView(Cell cell, DraggableLinearLayout view) {

        LinkedHashMap<String, Timestream> timestreams = cell.getTimestreams();

        CellHeadView cellHead = new CellHeadView(view.getContext(), cell);
        newViews.add(cellHead);
        for (Timestream timestream : timestreams.values()) {

            MyApplication.timeStreams.put(timestream.getId(),timestream);
            TimestreamCombinationView combView = new TimestreamCombinationView(view.getContext(), timestream);
            newViews.add(combView);
        }

        prepareNext(cell, view);
    }

    private void prepareTailView(DraggableLinearLayout draggableLinearLayout) {

        Rect outRect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                outRect.height());

        tail = new TextView(draggableLinearLayout.getContext());
        tail.setId(View.generateViewId());
        tail.setLayoutParams(lp);
        draggableLinearLayout.addView(tail,draggableLinearLayout.getChildCount() - 1);
    }

    private void refreshTailHeight() {

        DisplayMetrics dm = new DisplayMetrics();

        this.getWindowManager().getDefaultDisplay().getMetrics(dm);//this指当前activity

        int lastViewHeight = (draggableLinearLayout.getChildAt(0)).getHeight();

        int height = dm.heightPixels - ClosableScrollView.getOriginalY() - lastViewHeight * 2;
        tail.setHeight(height);
    }

    private static void prepareNext(Cell cell, DraggableLinearLayout view) {


        TimestreamCombinationView nextTrigger =
                new TimestreamCombinationView(view.getContext());

        nextTrigger.setProductCode(cell.getProductCode());

        TextView nameTv = nextTrigger.getBuyProductNameTv();
        EditText inventoryEt = nextTrigger.getInventory();
        nameTv.setText(MyApplication.getAllProducts().get(cell.getProductCode()).getProductName());
        inventoryEt.setFocusable(false);
        inventoryEt.setVisibility(View.INVISIBLE);

        EditText e = nextTrigger.getBuyDOPEt();
        e.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus && MyInfoChangeWatcher.isShouldWatch()) {

                    //将edittext所在view滚动到上方
                    locateView(nextTrigger, ClosableScrollView.SCROLL_FROM_TOUCH);

                    String code = cell.getProductCode();
                    Product p = MyApplication.getAllProducts().get(code);
                    Timestream t = new Timestream();
                    MyApplication.timeStreams.put(t.getId(), t);
                    AIInputter.fillTheBlanks(p, t);

                    PDInfoWrapper.updateInfo(sqLiteDatabase, t, MyDatabaseHelper.NEW_TIMESTREAM);
                    TimestreamCombinationView next =
                            new TimestreamCombinationView(view.getContext(), t);
                    MyInfoChangeWatcher.setShouldWatch(false);
                    view.addView(next,
                            view.indexOfChild(nextTrigger));
                    MyInfoChangeWatcher.setShouldWatch(true);
                    MyApplication.clearOriginalInfo();
                    MyApplication.recordDraggableView();
                    DraggableLinearLayout.setFocus(next.getBuyDOPEt());
                    DraggableLinearLayout.setLayoutChanged(true);

                }

            }
        });

//        view.addView(nextTrigger);
        newViews.add(nextTrigger);
    }

    private static void locateView(View view, int intent) {

        Message msg = Message.obtain();
        msg.what = intent;
        msg.obj = view;

        ClosableScrollView.getScrollHandler().sendMessage(msg);
    }


    /**
     * 重设模板中view的id为随机值，避免id冲突
     * @param view
     */
    private static void flushIds(View view) {

        view.setId(View.generateViewId());

        if (view instanceof ViewGroup ) {

            ViewGroup viewGroup = (ViewGroup)view;

            for (int i = 0; i < viewGroup.getChildCount(); i++) {

                flushIds(viewGroup.getChildAt(i));
            }
        }
    }

    private void setClassifyList(Context context, Shelf shelf) {

        classifyList = ShelfDAO.getClassify();

        setClassifyList(context, shelf, classifyList);
    }
    private void setClassifyList(Context context, Shelf shelf, ArrayList<String> classifyList) {

        Spinner spinner = findViewById(R.id.classify_sp);

        if (!classifyList.contains(shelf.getClassify())) classifyList.add(shelf.getClassify());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.item_selected,
                classifyList);

        adapter.setDropDownViewResource(R.layout.item_dropdown);

        spinner.setAdapter(adapter);

        spinner.setSelection(classifyList.indexOf(shelf.getClassify()));

    }

    public static ArrayList<String> getClassifyList() {
        return classifyList;
    }

    public static void loadProductOntoShelf(Shelf shelf) {

        if ("默认".equals(shelf.getName())) {

            //todo 加载所有商品
        } else {


        }

    }


    public static void actionStart(String code) {

        Context c = MyApplication.getContext();

        Intent i = new Intent(c, TraversalTimestreamActivity.class);

        MyApplication.intentProductCode = code;
        c.startActivity(i);
    }

    @Override
    public void onBackPressed() {

        switch (layoutIndex) {

            case LAYOUT_MODIFY_SHELF_INFO:

                cancelModify(ShelfAdapter.getCurrentShelf());
                break;

            case LAYOUT_SHOW_SHELF_PRODUCT:

                initShowShelfList(TraversalTimestreamActivity.this);
                break;

            default:
                super.onBackPressed();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        MyApplication.init();
        MyInfoChangeWatcher.init();
        initTransaction();
        initHandler();
    }

    public static void setContinueProcess(boolean continueProcess) {
        TraversalTimestreamActivity.continueProcess[0] = continueProcess;
    }

    private void initHandler() {

        if (handler == null) {

            handler = new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {

//                    if(msg.obj != null)

                        switch (msg.what) {

                            case MSG_MODIFY_SHELF:

                                currentShelf = (Shelf) msg.obj;
                                modifyShelf(currentShelf);
                                break;

                            case MSG_SHOW_SHELF:

                                currentShelf = (Shelf) msg.obj;
                                showShelf(currentShelf);
                                break;

                            case MSG_LOAD_VIEWS:

                                loadViews(draggableLinearLayout);
                                break;

                            case MSG_LOCATE_PRODUCT:

                                View view = (View) msg.obj;
                                locateView(view, ClosableScrollView.SCROLL_FROM_RELOCATE);
                                break;

                            case MSG_REFRESH_TAIL_HEIGHT:

                                refreshTailHeight();
                                break;

                            case MSG_SYNC_PRODUCT:

                                String code = (String) msg.obj;
                                syncProduct(code);
                                break;

                            default:
                                break;
                        }

                }
            };

            MyApplication.handlers.add(handler);
        }
    }

    @Override
    protected void onPause() {
        setContinueProcess(false);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finalizeActivity();
    }

    private void finalizeActivity() {

        shelfList = null;
        classifyList = null;
        continueProcess[0] = false;
        newViews.clear();
        headViews.clear();
        tail = null;
        MyInfoChangeWatcher.clearWatchers();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

}