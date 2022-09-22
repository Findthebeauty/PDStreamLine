package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.SHELF_CLASSIFY;
import static com.shepherdboy.pdstreamline.MyApplication.SHELF_MAX_ROW;
import static com.shepherdboy.pdstreamline.MyApplication.SHELF_NAME;
import static com.shepherdboy.pdstreamline.MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY_MODIFY_SHELF;
import static com.shepherdboy.pdstreamline.MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF;
import static com.shepherdboy.pdstreamline.MyApplication.activityIndex;
import static com.shepherdboy.pdstreamline.MyApplication.combinationHashMap;
import static com.shepherdboy.pdstreamline.MyApplication.currentProduct;
import static com.shepherdboy.pdstreamline.MyApplication.deleteTimestream;
import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;
import static com.shepherdboy.pdstreamline.MyApplication.drawableFirstLevel;
import static com.shepherdboy.pdstreamline.MyApplication.drawableSecondLevel;
import static com.shepherdboy.pdstreamline.MyApplication.getContext;
import static com.shepherdboy.pdstreamline.MyApplication.handlers;
import static com.shepherdboy.pdstreamline.MyApplication.init;
import static com.shepherdboy.pdstreamline.MyApplication.initActionBar;
import static com.shepherdboy.pdstreamline.MyApplication.intentProductCode;
import static com.shepherdboy.pdstreamline.MyApplication.onShowTimeStreamsHashMap;
import static com.shepherdboy.pdstreamline.MyApplication.setTimeStreamViewOriginalBackground;
import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;
import static com.shepherdboy.pdstreamline.MyApplication.timeStreams;
import static com.shepherdboy.pdstreamline.MyApplication.unloadTimestream;
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
import com.shepherdboy.pdstreamline.beanview.ProductLoader;
import com.shepherdboy.pdstreamline.beanview.TimestreamCombinationView;
import com.shepherdboy.pdstreamline.dao.MyDatabaseHelper;
import com.shepherdboy.pdstreamline.dao.PDInfoWrapper;
import com.shepherdboy.pdstreamline.dao.ShelfDAO;
import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.view.ActivityInfoChangeWatcher;
import com.shepherdboy.pdstreamline.view.ClosableScrollView;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;
import com.shepherdboy.pdstreamline.view.ShelfAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TraversalTimestreamActivity extends AppCompatActivity {

    private static int dragThreshold = 105;
    private static final int DRAG_LEFT = 200;
    private static final int DRAG_RIGHT = 201;

    private static ClosableScrollView container;
    private static DraggableLinearLayout shelfLayout;
    private static ActivityInfoChangeWatcher watcher;

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
    public static ConcurrentHashMap<String, CellHeadView> headViews = new ConcurrentHashMap<>();
    public static HashMap<String, TimestreamCombinationView> combViews = new HashMap<>();
    private static final boolean[] continueProcess = new boolean[]{true};
    private static boolean loadFinished = false;
    private static boolean prepareFinished = false;
    private static TextView tail;

    public static void onViewPositionChanged(View changedView, float horizontalDistance, float verticalDistance) {

        continueProcess[0] = false;

        int viewState = getViewState(changedView, horizontalDistance);

        switch (viewState) {

            case DRAG_LEFT:

                changedView.setBackground(drawableSecondLevel);
                break;

            case DRAG_RIGHT:

                changedView.setBackground(drawableFirstLevel);
                break;

            default:

                setTimeStreamViewOriginalBackground((LinearLayout) changedView);
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

                Timestream ts = unloadTimestream((LinearLayout) releasedChild);
                if (ts == null) {

                    draggableLinearLayout.putBack(releasedChild);
                    return;
                }

                deleteTimestream(ts.getId());
                ts.setInBasket(false);
                basket.remove(ts.getId());
                break;

            case DRAG_RIGHT:

//                ts = MyApplication.unloadTimestream((LinearLayout) releasedChild);
                ts = onShowTimeStreamsHashMap.get(releasedChild.getId());

                draggableLinearLayout.putBack(releasedChild);
                if (ts == null) return;
                PDInfoWrapper.deleteTimestream(sqLiteDatabase, ts.getId());

                if (ts.getSiblingPromotionId() != null) {

                    TimestreamCombination comb = combinationHashMap.get(ts.getId());
                    List<Timestream> unpackedTimestreams = comb.unpack();

                    for (Timestream t : unpackedTimestreams) {

                        if (t.getTimeStreamStateCode() == Timestream.FRESH) return;
                        basket.put(t.getId(), t);
                        t.setInBasket(true);
                    }

                    Streamline.reposition(unpackedTimestreams);

                } else {

                    if (ts.getTimeStreamStateCode() != Timestream.FRESH) {

                        basket.put(ts.getId(), ts);
                        ts.setInBasket(true);

                    }

                    Streamline.update(ts);
                }

                Product product = PDInfoWrapper.getProduct(ts.getProductCode(),
                        sqLiteDatabase, MyDatabaseHelper.ENTIRE_TIMESTREAM);

                postSyncProduct(product);
                break;

            default:
                draggableLinearLayout.putBack(releasedChild);
                break;
        }

        if (!loadFinished) {

            postLoadViews();
        }
    }

    public static void postSyncProduct(Product product) {
        if(product == null) return;
        if(layoutIndex != LAYOUT_SHOW_SHELF_PRODUCT) return;
        postMessage(MSG_SYNC_PRODUCT, product);
    }

    public static void postLoadViews() {

        postMessage(MSG_LOAD_VIEWS, null);
    }

    public static void recordTopProduct(MotionEvent event) {

        View beanView = null;

        if (event == null) {

            int x = draggableLinearLayout.getChildAt(0).getWidth() / 2;
            int offsetY = draggableLinearLayout.getChildAt(0).getHeight() / 2;
            ScrollView scr = (ScrollView) (draggableLinearLayout.getParent());
            beanView = draggableLinearLayout.viewDragHelper.findTopChildUnder(x,
                     scr.getScrollY() + offsetY);
        } else if (event.getActionMasked() == MotionEvent.ACTION_DOWN){

            beanView = draggableLinearLayout.viewDragHelper.findTopChildUnder((int) event.getX(), (int) (event.getY()));
        }

        if (beanView instanceof LinearLayout) {

            intentProductCode = ((BeanView)beanView).getProductCode();
            if(currentProduct == null || !intentProductCode.equals(currentProduct.getProductCode()))
            new Thread(new Runnable() {
                @Override
                public void run() {
                    currentProduct = PDInfoWrapper.getProduct(intentProductCode,
                            sqLiteDatabase, MyDatabaseHelper.ENTIRE_TIMESTREAM);
                }
            }).start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    private void initShowShelfList(Context context) {

        setContentView(R.layout.activity_traversal_timestream);
        initActionBar(getSupportActionBar());

        layoutIndex = LAYOUT_SHELF_LIST;

        if (defaultShelf == null) {
            defaultShelf = new Shelf();
            defaultShelf.setName("默认");
        }

        shelfList = ShelfDAO.getShelves();
        shelfList.add(defaultShelf);

        activityIndex = TRAVERSAL_TIMESTREAM_ACTIVITY;
        watcher = null;

        Button addShelfBt = findViewById(R.id.add_shelf);

//        setShelfList(context, shelfList);
        loadShelfEntry();
        addShelfBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setContentView(R.layout.shelf_info);

                layoutIndex = LAYOUT_MODIFY_SHELF_INFO;
                activityIndex = TRAVERSAL_TIMESTREAM_ACTIVITY_MODIFY_SHELF;
                watcher = ActivityInfoChangeWatcher.getActivityWatcher(TRAVERSAL_TIMESTREAM_ACTIVITY_MODIFY_SHELF);
                if(watcher == null) watcher = new ActivityInfoChangeWatcher(TRAVERSAL_TIMESTREAM_ACTIVITY_MODIFY_SHELF);

                Shelf shelf = new Shelf();

                loadShelfInfo(context, shelf);
                setClassifyList(context, shelf);
            }
        });


    }

    /**
     * 同步商品信息
     * @param product
     */
    private void syncProduct(Product product) {

        String code = product.getProductCode();

        CellHeadView headView = headViews.get(code);
        headView.bindData(TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF, code);

        int index = shelfLayout.indexOfChild(headView);

        ProductLoader.loadCellBody(TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF, shelfLayout, index + 1, product);
    }

    private void modifyShelf(Shelf shelf) {
        setContentView(R.layout.shelf_info);
        initModifyShelf();
        loadShelfInfo(this, shelf);
        setClassifyList(this, shelf);
    }

    private void initModifyShelf() {
        activityIndex = TRAVERSAL_TIMESTREAM_ACTIVITY_MODIFY_SHELF;
        layoutIndex = LAYOUT_MODIFY_SHELF_INFO;
        watcher = ActivityInfoChangeWatcher.getActivityWatcher(TRAVERSAL_TIMESTREAM_ACTIVITY_MODIFY_SHELF);
        if(watcher == null) watcher = new ActivityInfoChangeWatcher(TRAVERSAL_TIMESTREAM_ACTIVITY_MODIFY_SHELF);
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

                watcher.commitAll(); //中止自动提交，主动提交变更的信息

                final EditText editText = new EditText(context);
                AlertDialog.Builder inputClassify = new AlertDialog.Builder(context);
                inputClassify.setTitle("请输入类别名").setView(editText);
                inputClassify.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String classify = editText.getText().toString();

                        boolean validated = AIInputter.validate(classify, shelf, SHELF_CLASSIFY);

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

        watcher.watch(nameEt, shelf, SHELF_NAME);
        watcher.watch(maxRow, shelf, SHELF_MAX_ROW);

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

                watcher.commitAll(); //中止自动提交，主动提交变更的信息

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

        initActionBar(getSupportActionBar());

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

        if (layoutIndex == LAYOUT_SHOW_SHELF_PRODUCT) {

            activityIndex = TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF;
            watcher = ActivityInfoChangeWatcher.getActivityWatcher(TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF);
            draggableLinearLayout = shelfLayout;
            postLoadViews();
            postLocate();

        } else {

            setContentView(R.layout.shelf);
            initShowShelf();
            watcher.setShouldWatch(false);
            showRow(draggableLinearLayout, 1); //todo 货架行的遍历方式
        }
        DraggableLinearLayout.setLayoutChanged(true);
        watcher.setShouldWatch(true);


    }

    private void initShowShelf() {
        layoutIndex = LAYOUT_SHOW_SHELF_PRODUCT;
        activityIndex = TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF;
        watcher = new ActivityInfoChangeWatcher(activityIndex);
        shelfLayout = TraversalTimestreamActivity.this.findViewById(R.id.row);
        draggableLinearLayout = shelfLayout;
        container = (ClosableScrollView) shelfLayout.getParent();
        newViews.clear();
        headViews.clear();
        combViews.clear();
    }

    private void showRow(DraggableLinearLayout draggableLinearLayout, int rowNumber) {


        currentRow = ShelfDAO.getRow(ShelfAdapter.getCurrentShelf(), rowNumber);
        if (combinationHashMap == null)
        combinationHashMap = PDInfoWrapper.getTimestreamCombinations(sqLiteDatabase);

        if(activityIndex == TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF)
        prepareTailView(draggableLinearLayout);

        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareViews(draggableLinearLayout, currentRow);
            }
        }).start();

        postLocate();
    }

    private void postLocate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (intentProductCode != null) {

                    while ((!headViews.containsKey(intentProductCode)) || handler == null) {

                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    postMessage(MSG_LOCATE_PRODUCT, headViews.get(intentProductCode));
                }
            }
        }).start();
    }

    public static void postMessage(int what, Object obj) {
        if(handler == null) return;
        Message msg = handler.obtainMessage();
        msg.obj = obj;
        msg.what = what;
        handler.sendMessage(msg);
    }

    private void loadViews(DraggableLinearLayout draggableLinearLayout) {

        if (draggableLinearLayout == null) return;
        while (!newViews.isEmpty()) {

            View view = newViews.poll();
            draggableLinearLayout.addView(view, draggableLinearLayout.getChildCount() - 1);

            if (view instanceof CellHeadView) {

                headViews.put(((CellHeadView) view).getProductCode(), (CellHeadView) view);
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

                loadFinished = false;
                Cell cell = row.getCells().poll();
                prepareCellView(cell, draggableLinearLayout);

                if (!continueProcess[0]) {
                    continue;
                }

                postMessage(MSG_LOAD_VIEWS,null);
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

        CellHeadView cellHead = new CellHeadView(TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF, view.getContext(), cell.getProductCode());
        newViews.add(cellHead);
        for (Timestream timestream : timestreams.values()) {

            timeStreams.put(timestream.getId(),timestream);
            TimestreamCombinationView combView = new TimestreamCombinationView(TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF, view.getContext(), timestream);
            newViews.add(combView);
        }

        newViews.add(ProductLoader.prepareNext(TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF, cell.getProductCode(), view));
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

        Context c = getContext();

        Intent i = new Intent(c, TraversalTimestreamActivity.class);

        intentProductCode = code;
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

                                loadViews(shelfLayout);
                                break;

                            case MSG_LOCATE_PRODUCT:

                                ClosableScrollView.postLocate(
                                        ClosableScrollView.SCROLL_FROM_RELOCATE, msg.obj);
                                break;

                            case MSG_REFRESH_TAIL_HEIGHT:

                                refreshTailHeight();
                                break;

                            case MSG_SYNC_PRODUCT:

                                Product product = (Product) msg.obj;
                                syncProduct(product);
                                break;
                            default:
                                break;
                        }

                }
            };

            handlers.add(handler);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (layoutIndex == LAYOUT_SHOW_SHELF_PRODUCT) {
//            container.addView(shelfLayout);
//        }
    }

    @Override
    protected void onPause() {
        setContinueProcess(false);
//        if (layoutIndex == LAYOUT_SHOW_SHELF_PRODUCT) {
//            container.removeView(shelfLayout);
//        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

//        if(layoutIndex == LAYOUT_SHOW_SHELF_PRODUCT) {
//            container.removeView(shelfLayout);
//        }
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
        combViews.clear();
        tail = null;
        draggableLinearLayout = null;
        layoutIndex = LAYOUT_SHELF_LIST;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

}