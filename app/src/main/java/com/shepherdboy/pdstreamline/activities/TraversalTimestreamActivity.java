package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY_MODIFY_SHELF;
import static com.shepherdboy.pdstreamline.MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY_SHOW_SHELF;
import static com.shepherdboy.pdstreamline.MyApplication.allProducts;
import static com.shepherdboy.pdstreamline.MyApplication.combinationHashMap;
import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;
import static com.shepherdboy.pdstreamline.MyApplication.setTimeStreamViewOriginalBackgroundColor;
import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;
import static com.shepherdboy.pdstreamline.services.MidnightTimestreamManagerService.basket;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.shepherdboy.pdstreamline.beanview.CellHeadView;
import com.shepherdboy.pdstreamline.beanview.TimestreamCombinationView;
import com.shepherdboy.pdstreamline.sql.PDInfoWrapper;
import com.shepherdboy.pdstreamline.sql.ShelfDAO;
import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;
import com.shepherdboy.pdstreamline.view.MyInfoChangeWatcher;
import com.shepherdboy.pdstreamline.view.ShelfAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class TraversalTimestreamActivity extends AppCompatActivity {

    private static int dragThreshold = 105;
    private static final int DRAG_LEFT = 200;
    private static final int DRAG_RIGHT = 201;

    private Shelf tempShelf;//货架信息改变时临时保存改变后的信息，用于回滚操作
    private Shelf defaultShelf;
    private static Shelf currentShelf;
    private static ArrayList<Shelf> shelfList;
    private static ArrayList<String> classifyList;

    public static final int MSG_MODIFY_SHELF = 0;
    public static final int MSG_SHOW_SHELF = 1;

    public static final int LAYOUT_SHELF_LIST = 100;
    public static final int LAYOUT_SHOW_SHELF_PRODUCT = 101;
    public static final int LAYOUT_MODIFY_SHELF_INFO = 102;
    private static int layoutIndex;

    public static Handler handler;

    public static void onViewPositionChanged(View changedView, float horizontalDistance, float verticalDistance) {

        int viewState = getViewState(changedView, horizontalDistance);

        Log.d("positionChanged", "in");

        switch (viewState) {

            case DRAG_LEFT:

                changedView.setBackground(new ColorDrawable(MyApplication.DRAG_RANGE_SECOND_LEVEL));
                break;

            case DRAG_RIGHT:

                changedView.setBackground(new ColorDrawable(MyApplication.DRAG_RANGE_FIRST_LEVEL));
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
                PDInfoWrapper.deleteTimestream(sqLiteDatabase, ts.getId());
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

        if (handler == null) {

            handler = new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {

                    Shelf shelf = (Shelf) msg.obj;
                    currentShelf = shelf;
                    switch (msg.what) {

                        case MSG_MODIFY_SHELF:

                            modifyShelf(shelf);
                            break;

                        case MSG_SHOW_SHELF:

                            showShelf(shelf);
                            break;

                        default:
                            break;
                    }

                }
            };
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
    }

    private void showRow(DraggableLinearLayout draggableLinearLayout, int rowNumber) {


        Row row = ShelfDAO.getRow(ShelfAdapter.getCurrentShelf(), rowNumber);

        combinationHashMap = PDInfoWrapper.getTimestreamCombinations(sqLiteDatabase);

        for (Cell cell : row.getCells()) {

            showCell(cell, draggableLinearLayout);

        }


    }

    private void showCell(Cell cell, DraggableLinearLayout view) {

        LinkedHashMap<String, Timestream> timestreams = cell.getTimestreams();

        view.addView(new CellHeadView(view.getContext(), cell));

        for (Timestream timestream : timestreams.values()) {

            view.addView(new TimestreamCombinationView(view.getContext(), timestream));
        }

        prepareNext(cell, view);
    }

    private void prepareNext(Cell cell, DraggableLinearLayout view) {

        TimestreamCombinationView nextTrigger =
                new TimestreamCombinationView(view.getContext());
        view.addView(nextTrigger);

        nextTrigger.setProductCode(cell.getProductCode());

        TextView nameTv = nextTrigger.getBuyProductNameTv();
        EditText inventoryEt = nextTrigger.getInventory();
        nameTv.setText(allProducts.get(cell.getProductCode()).getProductName());
        inventoryEt.setFocusable(false);
        inventoryEt.setVisibility(View.INVISIBLE);


        EditText e = nextTrigger.getBuyDOPEt();

        e.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus && MyInfoChangeWatcher.isShouldWatch()) {

                    String code = cell.getProductCode();
                    Product p = allProducts.get(code);
                    Timestream t = new Timestream();
                    AIInputter.fillTheBlanks(p, t);

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


    public static void actionStart() {

        Context c = MyApplication.getContext();

        Intent i = new Intent(c, TraversalTimestreamActivity.class);

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
    }

    @Override
    protected void onStop() {
        super.onStop();
        finalizeActivity();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void finalizeActivity() {

        shelfList = null;
        classifyList = null;

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

}