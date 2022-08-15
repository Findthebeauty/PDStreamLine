package com.shepherdboy.pdstreamline.activities;

import static com.shepherdboy.pdstreamline.MyApplication.TRAVERSAL_TIMESTREAM_ACTIVITY;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.shepherdboy.pdstreamline.MyApplication;
import com.shepherdboy.pdstreamline.R;
import com.shepherdboy.pdstreamline.beans.Shelf;
import com.shepherdboy.pdstreamline.sql.ShelfDAO;
import com.shepherdboy.pdstreamline.utils.AIInputter;
import com.shepherdboy.pdstreamline.utils.DateUtil;
import com.shepherdboy.pdstreamline.view.DraggableLinearLayout;
import com.shepherdboy.pdstreamline.view.MyInfoChangeWatcher;

import java.util.ArrayList;

public class TraversalTimestreamActivity extends AppCompatActivity {

    private Shelf tempShelf;//货架信息改变时临时保存改变后的信息，用于回滚操作
    private Shelf defaultShelf;
    private static ArrayList<Shelf> shelfList;
    private static ArrayList<String> classifyList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traversal_timestream);

        initActivity(this);
        
    }



    private void initActivity(Context context) {

        if (defaultShelf == null) {
            defaultShelf = new Shelf();
            defaultShelf.setName("默认");
        }

        shelfList = ShelfDAO.getShelves();

        MyApplication.activityIndex = TRAVERSAL_TIMESTREAM_ACTIVITY;
//
//        draggableLinearLayout = findViewById(R.id.parent);
//        DraggableLinearLayout.setLayoutChanged(true);

        Button addShelfBt = findViewById(R.id.add_shelf);
        
        setShelfList(context, shelfList);

        addShelfBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setContentView(R.layout.shelf_info);

                Shelf shelf = new Shelf();

                loadShelfInfo(context, shelf);
                setClassifyList(context, shelf);

            }
        });

    }

    private void setShelfList(Context context, ArrayList<Shelf> shelves) {

        loadShelfEntry(context, defaultShelf);

        for (Shelf shelf : shelves) {

            loadShelfEntry(context, shelf);

        }

    }

    public static ArrayList<Shelf> getShelfList() {
        return shelfList;
    }

    private void loadShelfEntry(Context context, Shelf shelf) {

        LayoutInflater inflater = LayoutInflater.from(context);

        LinearLayout shelvesView = ((AppCompatActivity)context).findViewById(R.id.shelves);

        LinearLayout shelfView = inflater.inflate(R.layout.shelf_item, null).findViewById(R.id.shelf_item);

        TextView nameTvFlag = shelfView.findViewById(R.id.shelf_name_flag);
        TextView nameTv = shelfView.findViewById(R.id.shelf_name);
        TextView classifyTvFlag = shelfView.findViewById(R.id.classify_flag);
        TextView classifyTv = shelfView.findViewById(R.id.classify);
        TextView maxRowFlag = shelfView.findViewById(R.id.max_row_flag);
        TextView maxRow = shelfView.findViewById(R.id.max_row);

        nameTvFlag.setId(DateUtil.getIdByCurrentTime() + MyApplication.idIncrement++);
        nameTv.setId(DateUtil.getIdByCurrentTime() + MyApplication.idIncrement++);
        classifyTvFlag.setId(DateUtil.getIdByCurrentTime() + MyApplication.idIncrement++);
        classifyTv.setId(DateUtil.getIdByCurrentTime() + MyApplication.idIncrement++);
        maxRowFlag.setId(DateUtil.getIdByCurrentTime() + MyApplication.idIncrement++);
        maxRow.setId(DateUtil.getIdByCurrentTime() + MyApplication.idIncrement++);
        shelfView.setId(DateUtil.getIdByCurrentTime() + MyApplication.idIncrement++);

        nameTv.setText(shelf.getName());
        classifyTv.setText(shelf.getClassify());
        maxRow.setText(String.valueOf(shelf.getMaxRowCount()));

        shelf.setShelfViewId(shelfView.getId());

        shelfView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if ("默认".equals(shelf.getName())) {

                    Toast.makeText(context, "默认货架禁止修改", Toast.LENGTH_SHORT).show();
                    return true;
                }

                setContentView(R.layout.shelf_info);
                MyInfoChangeWatcher.clearWatchers();

                tempShelf = new Shelf(shelf);

                loadShelfInfo(context, shelf);

                return true;
            }
        });

        shelfView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadProductOntoShelf(shelf);
                showShelf(shelf);
            }
        });

        shelvesView.addView(shelfView,0);
    }

    private void loadShelfInfo(Context context, Shelf shelf) {

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
                            shelf.setUpdated(true);
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
                shelf.setUpdated(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                shelf.setName(tempShelf.getName());
                shelf.setClassify(tempShelf.getClassify());
                shelf.setMaxRowCount(tempShelf.getMaxRowCount());
                shelf.setUpdated(false);
                setContentView(R.layout.activity_traversal_timestream);
                initActivity(context);
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

                if (shelf.isUpdated()) ShelfDAO.update(shelf);
                setContentView(R.layout.activity_traversal_timestream);
                initActivity(context);
            }
        });

        DraggableLinearLayout.setFocus(nameEt);
    }



    /**
     * 展示货架
     * @param shelf
     */
    private void showShelf(Shelf shelf) {

        //todo 展示货架

        LayoutInflater inflater = LayoutInflater.from(TraversalTimestreamActivity.this);

        LinearLayout parent = TraversalTimestreamActivity.this.findViewById(R.id.parent);

        LinearLayout cellHead = inflater.inflate(R.layout.cell_head_layout, null).findViewById(R.id.cell_head);
        LinearLayout combination = inflater.inflate(R.layout.comb_layout, null).findViewById(R.id.combination);

        parent.addView(cellHead);
        parent.addView(combination);

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
            return;
        }



    }


    public static void actionStart() {

        Context c = MyApplication.getContext();

        Intent i = new Intent(c, TraversalTimestreamActivity.class);

        c.startActivity(i);
    }

}