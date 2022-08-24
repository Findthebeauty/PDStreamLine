package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY;
import static com.shepherdboy.pdstreamline.MyApplication.allProducts;
import static com.shepherdboy.pdstreamline.MyApplication.combinationHashMap;
import static com.shepherdboy.pdstreamline.MyApplication.draggableLinearLayout;
import static com.shepherdboy.pdstreamline.MyApplication.sqLiteDatabase;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Cell;
import com.shepherdboy.pdstreamline.beans.Row;
import com.shepherdboy.pdstreamline.beans.Shelf;
import com.shepherdboy.pdstreamline.beans.Timestream;
import com.shepherdboy.pdstreamline.beans.TimestreamCombination;
import com.shepherdboy.pdstreamline.beanview.TimestreamCombinationView;
import com.shepherdboy.pdstreamline.sql.PDInfoWrapper;
import com.shepherdboy.pdstreamline.sql.ShelfDAO;
import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.utils.DateUtil;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;
import com.shepherdboy.pdstreamline.view.MyInfoChangeWatcher;
import com.shepherdboy.pdstreamline.view.ShelfAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class TraversalTimestreamActivity extends AppCompatActivity {

    private Shelf tempShelf;//货架信息改变时临时保存改变后的信息，用于回滚操作
    private Shelf defaultShelf;
    private static ArrayList<Shelf> shelfList;
    private static ArrayList<String> classifyList;

    public static final int MODIFY_SHELF = 0;
    public static final int SHOW_SHELF = 1;

    public static final int LAYOUT_SHELF_LIST = 100;
    public static final int LAYOUT_SHOW_SHELF_PRODUCT = 101;
    public static final int LAYOUT_MODIFY_SHELF_INFO = 102;
    private static int layoutIndex;

    public static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void initActivity(Context context) {
        Log.d("Traversal","initActivity");

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

                    switch (msg.what) {

                        case MODIFY_SHELF:

                            setContentView(R.layout.shelf_info);
                            layoutIndex = LAYOUT_MODIFY_SHELF_INFO;
                            loadShelfInfo(context, shelf);
                            setClassifyList(context, shelf);
                            break;

                        case SHOW_SHELF:

                            showShelf(shelf);
                            break;

                        default:
                            break;
                    }

                }
            };
        }

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

//    private void loadShelfEntry(Context context, Shelf shelf) {
//
//        LayoutInflater inflater = LayoutInflater.from(context);
//
//        LinearLayout shelvesView = ((AppCompatActivity)context).findViewById(R.id.shelves);
//
//        LinearLayout shelfView = inflater.inflate(R.layout.shelf_item, null).findViewById(R.id.shelf_item);
//
//        TextView nameTvFlag = shelfView.findViewById(R.id.shelf_name_flag);
//        TextView nameTv = shelfView.findViewById(R.id.shelf_name);
//        TextView classifyTvFlag = shelfView.findViewById(R.id.classify_flag);
//        TextView classifyTv = shelfView.findViewById(R.id.classify);
//        TextView maxRowFlag = shelfView.findViewById(R.id.max_row_flag);
//        TextView maxRow = shelfView.findViewById(R.id.max_row);
//
//        nameTvFlag.setId(View.generateViewId());
//        nameTv.setId(View.generateViewId());
//        classifyTvFlag.setId(View.generateViewId());
//        classifyTv.setId(View.generateViewId());
//        maxRowFlag.setId(View.generateViewId());
//        maxRow.setId(View.generateViewId());
//        shelfView.setId(View.generateViewId());
//
//        nameTv.setText(shelf.getName());
//        classifyTv.setText(shelf.getClassify());
//        maxRow.setText(String.valueOf(shelf.getMaxRowCount()));
//
//        shelf.setShelfViewId(shelfView.getId());
//
//        shelfView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//
//                if ("默认".equals(shelf.getName())) {
//
//                    Toast.makeText(context, "默认货架禁止修改", Toast.LENGTH_SHORT).show();
//                    return true;
//                }
//
//                setContentView(R.layout.shelf_info);
//                MyInfoChangeWatcher.clearWatchers();
//
//                tempShelf = new Shelf(shelf);
//
//                loadShelfInfo(context, shelf);
//
//                return true;
//            }
//        });
//
//        shelfView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                loadProductOntoShelf(shelf);
//                showShelf(shelf);
//            }
//        });
//
//        shelvesView.addView(shelfView,0);
//    }

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
                        initActivity(context);
                    }
                });

                normalDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        setContentView(R.layout.activity_traversal_timestream);
                        initActivity(context);
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
                initActivity(context);
            }
        });

        DraggableLinearLayout.setFocus(nameEt);
    }

    private void cancelModify(Shelf shelf) {

        shelf.setName(tempShelf.getName());
        shelf.setClassify(tempShelf.getClassify());
        shelf.setMaxRowCount(tempShelf.getMaxRowCount());
        shelf.setInfoChanged(false);
        setContentView(R.layout.activity_traversal_timestream);
        initActivity(TraversalTimestreamActivity.this);
    }


    /**
     * 展示货架
     * @param shelf
     */
    private void showShelf(Shelf shelf) {

        //todo 展示货架

        setContentView(R.layout.shelf);

        layoutIndex = LAYOUT_SHOW_SHELF_PRODUCT;

        draggableLinearLayout = TraversalTimestreamActivity.this.findViewById(R.id.row);

        showRow(draggableLinearLayout, 1); //todo 货架行的遍历方式

        DraggableLinearLayout.setLayoutChanged(true);

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

        loadCellHead(cell, view);

        for (Timestream timestream : timestreams.values()) {

//            loadCellBody(view, timestream);

            draggableLinearLayout.addView(new TimestreamCombinationView(draggableLinearLayout.getContext(), timestream));
        }

    }

    private void loadCellHead(Cell cell, DraggableLinearLayout view) {

        LayoutInflater inflater = LayoutInflater.from(view.getContext());
        LinearLayout cellHead = inflater.inflate(R.layout.cell_head_layout, null).findViewById(R.id.cell_head);
        TextView headNameTv = (TextView) cellHead.getChildAt(0);
        TextView headCodeTv = (TextView) cellHead.getChildAt(1);

        cellHead.setId(View.generateViewId());
        headNameTv.setId(View.generateViewId());
        headCodeTv.setId(View.generateViewId());

        headNameTv.setText(PDInfoWrapper.getProductName(cell.getProductCode(),
                sqLiteDatabase));

        String productCode = cell.getProductCode();

        if (productCode.length() > 6)
            productCode = productCode.substring(cell.getProductCode().length() - 6);

        headCodeTv.setText(productCode);
        view.addView(cellHead);
    }

    private static void loadCellBody(DraggableLinearLayout view, Timestream timestream) {

        LayoutInflater inflater = LayoutInflater.from(view.getContext());

        LinearLayout combination = inflater.inflate(R.layout.comb_layout, null).findViewById(R.id.combination);


        LinearLayout buyBackground = combination.findViewById(R.id.buy_background);
        LinearLayout giveawayBackground = combination.findViewById(R.id.give_away_background);

        TextView buyProductNameTv = combination.findViewById(R.id.buy_pd_name);
        TextView giveawayProductNameTv = combination.findViewById(R.id.give_away_pd_name);
        TextView buyDOPMeasure = combination.findViewById(R.id.buy_dop_measure);

        EditText buyDOPEt = combination.findViewById(R.id.buy_dop);
        EditText inventory = combination.findViewById(R.id.inventory);
        EditText inventoryMeasure = combination.findViewById(R.id.inventory_measure);

        TextView unitTv = combination.findViewById(R.id.unit);
        TextView giveawayDOPTv = combination.findViewById(R.id.give_away_dop);
        TextView giveawayFlagTv = combination.findViewById(R.id.give_away_flag);

        ArrayList<View> giveAwayViews = new ArrayList<>();
        giveAwayViews.add(giveawayBackground);
        giveAwayViews.add(giveawayProductNameTv);
        giveAwayViews.add(giveawayDOPTv);
        giveAwayViews.add(giveawayFlagTv);

        flushIds(combination);

        ConstraintSet set = new ConstraintSet();

        set.clone((ConstraintLayout) combination.getChildAt(0));

        set.connect(buyBackground.getId(),ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
        set.connect(buyBackground.getId(),ConstraintSet.RIGHT, buyDOPEt.getId(), ConstraintSet.RIGHT);
        set.connect(buyBackground.getId(),ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(buyBackground.getId(),ConstraintSet.BOTTOM, buyProductNameTv.getId(), ConstraintSet.BOTTOM);

        set.connect(giveawayBackground.getId(),ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
        set.connect(giveawayBackground.getId(),ConstraintSet.RIGHT, giveawayDOPTv.getId(), ConstraintSet.RIGHT);
        set.connect(giveawayBackground.getId(),ConstraintSet.TOP, giveawayProductNameTv.getId(), ConstraintSet.TOP);
        set.connect(giveawayBackground.getId(),ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        set.connect(buyProductNameTv.getId(),ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
        set.connect(buyProductNameTv.getId(),ConstraintSet.RIGHT, buyDOPMeasure.getId(), ConstraintSet.LEFT);
        set.connect(buyProductNameTv.getId(),ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);

        set.connect(giveawayProductNameTv.getId(),ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
        set.connect(giveawayProductNameTv.getId(),ConstraintSet.RIGHT, buyDOPMeasure.getId(), ConstraintSet.LEFT);
        set.connect(giveawayProductNameTv.getId(),ConstraintSet.TOP, buyProductNameTv.getId(), ConstraintSet.BOTTOM);

        set.connect(buyDOPMeasure.getId(),ConstraintSet.RIGHT, inventoryMeasure.getId(), ConstraintSet.LEFT);
        set.connect(buyDOPMeasure.getId(),ConstraintSet.BOTTOM, buyProductNameTv.getId(), ConstraintSet.BOTTOM);
        set.connect(buyDOPMeasure.getId(),ConstraintSet.TOP, buyProductNameTv.getId(), ConstraintSet.TOP);

        set.connect(buyDOPEt.getId(),ConstraintSet.BOTTOM, buyProductNameTv.getId(), ConstraintSet.BOTTOM);
        set.connect(buyDOPEt.getId(),ConstraintSet.TOP, buyProductNameTv.getId(), ConstraintSet.TOP);
        set.connect(buyDOPEt.getId(),ConstraintSet.LEFT, buyDOPMeasure.getId(), ConstraintSet.LEFT);
        set.connect(buyDOPEt.getId(),ConstraintSet.RIGHT, buyDOPMeasure.getId(), ConstraintSet.RIGHT);

        set.connect(inventoryMeasure.getId(),ConstraintSet.RIGHT, unitTv.getId(), ConstraintSet.LEFT);
        set.connect(inventoryMeasure.getId(),ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(inventoryMeasure.getId(),ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        set.connect(inventory.getId(),ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.connect(inventory.getId(),ConstraintSet.LEFT, inventoryMeasure.getId(), ConstraintSet.LEFT);
        set.connect(inventory.getId(),ConstraintSet.RIGHT, inventoryMeasure.getId(), ConstraintSet.RIGHT);
        set.connect(inventory.getId(),ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);

        set.connect(unitTv.getId(),ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
        set.connect(unitTv.getId(),ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        set.connect(unitTv.getId(),ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        set.connect(giveawayDOPTv.getId(),ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        set.connect(giveawayDOPTv.getId(),ConstraintSet.LEFT, buyDOPMeasure.getId(), ConstraintSet.LEFT);
        set.connect(giveawayDOPTv.getId(),ConstraintSet.RIGHT, buyDOPMeasure.getId(), ConstraintSet.RIGHT);
        set.connect(giveawayDOPTv.getId(),ConstraintSet.TOP, buyProductNameTv.getId(), ConstraintSet.BOTTOM);

        set.connect(giveawayFlagTv.getId(),ConstraintSet.LEFT, buyDOPMeasure.getId(), ConstraintSet.LEFT);
        set.connect(giveawayFlagTv.getId(),ConstraintSet.RIGHT, buyDOPMeasure.getId(), ConstraintSet.RIGHT);
        set.connect(giveawayFlagTv.getId(),ConstraintSet.BOTTOM, giveawayDOPTv.getId(), ConstraintSet.BOTTOM);
        set.connect(giveawayFlagTv.getId(),ConstraintSet.TOP, giveawayDOPTv.getId(), ConstraintSet.TOP);

        set.applyTo((ConstraintLayout) combination.getChildAt(0));

        view.addView(combination);

        String discountRate = timestream.getDiscountRate();

        TimestreamCombination timestreamCombination = null;

        switch (discountRate) {

            case "":

                for (View v : giveAwayViews) {

                    v.setVisibility(View.GONE);
                }

                buyProductNameTv.setText(timestream.getProductName());
                buyDOPEt.setText(DateUtil.typeMach(timestream.getProductDOP()).substring(0,10));
                inventory.setText(timestream.getProductInventory());
                unitTv.setText(allProducts.get(timestream.getProductCode()).getProductSpec());

                int color = MyApplication.getColorByTimestreamStateCode(view.getContext(), timestream.getTimeStreamStateCode());

                buyBackground.setBackgroundColor(color);
                break;

            case "0.5":

                timestreamCombination = combinationHashMap.get(timestream.getId());

                break;

            case "0":

                timestreamCombination = combinationHashMap.get(timestream.getSiblingPromotionId());

            default:
                break;
        }


        if (timestreamCombination != null) {

            buyProductNameTv.setText(timestreamCombination.getBuyProductName());
            buyDOPEt.setText(DateUtil.typeMach(timestreamCombination.getBuyTimestream()
                    .getProductDOP()).substring(0,10));
            inventory.setText(timestreamCombination.getPackageCount());
            unitTv.setText("组");

            giveawayProductNameTv.setText(timestreamCombination.getGiveawayProductName());
            giveawayDOPTv.setText(DateUtil.typeMach(timestreamCombination.getGiveawayTimestream()
                    .getProductDOP()).substring(0,10));


            int color = MyApplication.getColorByTimestreamStateCode(view.getContext(),
                    timestreamCombination.getBuyTimestream().getTimeStreamStateCode());
            buyBackground.setBackgroundColor(color);


            color = MyApplication.getColorByTimestreamStateCode(view.getContext(),
                    timestreamCombination.getGiveawayTimestream().getTimeStreamStateCode());
            giveawayBackground.setBackgroundColor(color);

        }
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

            case LAYOUT_SHELF_LIST:

                super.onBackPressed();
                break;

            case LAYOUT_MODIFY_SHELF_INFO:

                cancelModify(ShelfAdapter.getCurrentShelf());
                break;

            case LAYOUT_SHOW_SHELF_PRODUCT:
                initActivity(TraversalTimestreamActivity.this);
                break;

            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initActivity(TraversalTimestreamActivity.this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finalizeActivity();
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